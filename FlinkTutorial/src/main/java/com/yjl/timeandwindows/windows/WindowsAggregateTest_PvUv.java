package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import com.yjl.timeandwindows.windows.aggregatefunction.AvgPv;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * 开窗统计pv和uv，两者相除得到 人均pv
 */
public class WindowsAggregateTest_PvUv {

    public static void main(String[] args) throws Exception {
        // 创建运行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 创建并行度
        env.setParallelism(1);
        // 设置watermark 周期性生成时间
        env.getConfig().setAutoWatermarkInterval(100);

        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                    @Override
                                    public long extractTimestamp(Event element, long recordTimestamp) {
                                        return element.timestamp;
                                    }
                                })
                );

        stream.print("data");

        // 统计 pv uv，所有数据放在一起统计
        stream.keyBy(data -> true)
                .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(2)))
                .aggregate(new AvgPv())
                .print();

        env.execute();

    }
}