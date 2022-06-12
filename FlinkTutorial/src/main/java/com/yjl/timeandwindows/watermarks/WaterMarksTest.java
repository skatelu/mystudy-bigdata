package com.yjl.timeandwindows.watermarks;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.Duration;

/**
 * 水位线的生成与应用
 */
public class WaterMarksTest {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100); // 设置warterMarks 周期性生成的时间，默认是 200ms

        env.fromElements(
                new Event("Mary", "./home", 1000L),
                new Event("Bob", "./cart", 2000L),
                new Event("Mary", "./home", 3000L),
                new Event("Bob", "./cart", 4000L),
                new Event("Mary", "./home", 5000L),
                new Event("Bob", "./cart", 6000L),
                new Event("Mary", "./home", 7000L),
                new Event("Bob", "./cart", 8000L),
                new Event("Mary", "./home", 9000L),
                new Event("Bob", "./cart", 10000L)
        )
                // 有序流的watermarks生成 assignTimestampsAndWatermarks 需要一个 WatermarkStrategy 的实现类，而WatermarkStrategy实现类里面，需要实现两个方法
                // .<Event>forMonotonousTimestamps() 返回的是 不带 TimestampAssigner 再调用 withTimestampAssigner 返回的就是带 TimestampAssigner 的 WatermarkStrategy
//                .assignTimestampsAndWatermarks(WatermarkStrategy.<Event>forMonotonousTimestamps().withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
//                    @Override
//                    public long extractTimestamp(Event element, long recordTimestamp) {
//                        return element.timestamp;
//                    }
//                }));

                // 需要传入的参数为 Duration maxOutOfOrderness 混乱程度，即最大乱序时间
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                .<Event>forBoundedOutOfOrderness(Duration.ofSeconds(2L))
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {

                                    @Override
                                    public long extractTimestamp(Event element, long recordTimestamp) {
                                        return element.timestamp;
                                    }
                                })
                );

        env.execute();
    }


}
