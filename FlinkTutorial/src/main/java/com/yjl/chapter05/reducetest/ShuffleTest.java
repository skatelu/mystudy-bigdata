package com.yjl.chapter05.reducetest;

import com.yjl.chapter05.Event;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.LocalStreamEnvironment;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * 随机分配策略
 */
public class ShuffleTest {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置全局并行度
        env.setParallelism(1);
        // 创建数据源
        DataStreamSource<Event> envSource = env.addSource(new ClickSource());

        // 设置随机分发
//        envSource.shuffle().print().setParallelism(4);

        // 轮询下发数据
        envSource.rebalance().print().setParallelism(4);

        env.execute();
    }

}
