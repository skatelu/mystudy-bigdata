package com.yjl.chapter05;

import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.ArrayList;
import java.util.List;

public class SourceTest {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置全局并行度为 1
        environment.setParallelism(1);

        // 从不同的来源读取数据
        // 1、从文件中读取数据
        DataStreamSource<String> stream1 = environment.readTextFile("src/main/resources/input/clicks.txt");

        // 2、从集合中读取数据
        ArrayList<Integer> nums = new ArrayList<>();
        nums.add(1);
        nums.add(2);
        nums.add(3);
        nums.add(4);

        DataStreamSource<Integer> numSource = environment.fromCollection(nums);

        List<Event> events = new ArrayList<>();
        events.add(new Event("Mary", "./home", 1000L));
        events.add(new Event("Bob", "./cart", 2000L));
        DataStreamSource<Event> stream2 = environment.fromCollection(events);

        // 3、从元素读取数据
        DataStreamSource<Event> elements1 = environment.fromElements(
                new Event("Mary", "./home", 1000L),
                new Event("Bob", "./cart", 2000L)
        );

        // 4、从文本流中读取数据
        ParameterTool parameter = ParameterTool.fromPropertiesFile("src\\main\\resources\\flink.properties");
        DataStreamSource<String> lineDSS = environment.socketTextStream(
                parameter.get("flink.listener.host", "docker10"),
                parameter.getInt("flink.listener.port", 7777));



        stream1.print("1");
        numSource.print("num");
        stream2.print("2");
        elements1.print("element");
        lineDSS.print("socketTextStream");


        environment.execute();
    }

}
