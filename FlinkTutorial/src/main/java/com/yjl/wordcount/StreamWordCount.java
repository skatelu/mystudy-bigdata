package com.yjl.wordcount;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * Flink 流式处理，并返回响应结果 监听Socket localhost  7777 接口 发送的消息，并进行实时的流处理
 * nc -lk 7777  在7777端口发送信息
 */
public class StreamWordCount {

    public static void main(String[] args) throws Exception {
        // 1、 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 2、 读取文本流
//        DataStreamSource<String> lineDSS = env.socketTextStream("docker10", 7777);
        // 2、从参数中提取主机名和端口号
        // flink 提供的从当前的 配日志文件中读取参数的工具
        ParameterTool parameter = ParameterTool.fromPropertiesFile("src\\main\\resources\\flink.properties");
        DataStreamSource<String> lineDSS = env.socketTextStream(
                parameter.get("flink.listener.host", "docker10"),
                parameter.getInt("flink.listener.port", 7777));


        // 3、转换数据格式
        SingleOutputStreamOperator<Tuple2<String, Long>> wordAndOne = lineDSS.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
            Arrays.stream(line.split(" ")).forEach(word -> {
                out.collect(Tuple2.of(word, 1L));
            });
        }).returns(Types.TUPLE(Types.STRING, Types.LONG));
        // 4、分组
        KeyedStream<Tuple2<String, Long>, String> wordAndOneKS = wordAndOne.keyBy(t -> t.f0);
        // 5、求和
        SingleOutputStreamOperator<Tuple2<String, Long>> result = wordAndOneKS.sum(1);
        // 6、打印
        result.print();
        // 7、执行
        env.execute();
    }

}
