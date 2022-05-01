package com.yjl.wordcount;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;

/**
 * Flink 流处理的 有界数据处理-批处理；执行统计单词的任务
 */
public class BatchWordCount {

    public static void main(String[] args) throws Exception {
        // 1、创建流执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 2、读取文件
        DataStreamSource<String> lineDSS = env.readTextFile("src/main/resources/input/words.txt");
        // 3、转换数据格式，转换成二元组类型，由于Java中没有二元组类型，需要用Flink封装的类型
        SingleOutputStreamOperator<Tuple2<String, Long>> wordAndOneTuple = lineDSS.flatMap((String line, Collector<Tuple2<String, Long>> out) -> {
                    Arrays.stream(line.split(" ")).forEach(word -> {
                        out.collect(Tuple2.of(word, 1L));
                    });
                })
                .returns(Types.TUPLE(Types.STRING, Types.LONG));// 当lambda表达式使用Java泛型的时候，由于泛型擦除的存在，需要显示的声明类型信息
        // 4、按照 word 进行分组
//        KeySelector<Tuple2<String, Long>, String> tuple2StringKeySelector = new KeySelector<Tuple2<String, Long>, String>() {
//
//            @Override
//            public String getKey(Tuple2<String, Long> value) throws Exception {
//                return value.f0;
//            }
//        };
//        KeyedStream<Tuple2<String, Long>, String> wordAndOneKS = wordAndOneTuple.keyBy(tuple2StringKeySelector);
        KeyedStream<Tuple2<String, Long>, String> wordAndOneKS = wordAndOneTuple.keyBy(data -> data.f0);
        // 5、求和
        SingleOutputStreamOperator<Tuple2<String, Long>> result = wordAndOneKS.sum(1);
        // 6、打印
        result.print();
        // 7、执行
        env.execute();
    }

}
