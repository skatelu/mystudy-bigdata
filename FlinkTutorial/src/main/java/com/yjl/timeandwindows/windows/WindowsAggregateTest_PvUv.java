package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.HashSet;

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

    /**
     * 自定义聚合函数
     */
    public static class AvgPv implements AggregateFunction<Event, Tuple2<Long, HashSet<String>>, Double> {


        @Override
        public Tuple2<Long, HashSet<String>> createAccumulator() {

            return Tuple2.of(0L, new HashSet<String>());
        }

        @Override
        public Tuple2<Long, HashSet<String>> add(Event value, Tuple2<Long, HashSet<String>> accumulator) {
            // 每来一条数据，pv个数加1，将user放入hashset中去重
            accumulator.f1.add(value.user);

            return Tuple2.of(accumulator.f0 + 1, accumulator.f1);
        }

        @Override
        public Double getResult(Tuple2<Long, HashSet<String>> accumulator) {
            // 窗口触发时，输出pv与uv的值

            return Double.valueOf(accumulator.f0 / accumulator.f1.size());
        }

        @Override
        public Tuple2<Long, HashSet<String>> merge(Tuple2<Long, HashSet<String>> a, Tuple2<Long, HashSet<String>> b) {
            return null;
        }
    }

}
