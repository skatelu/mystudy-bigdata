package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.sql.Timestamp;
import java.time.Duration;

/**
 * flink 中灵活的 集合函数
 */
public class WindowAggregateTest {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置全局的并行度
        env.setParallelism(1);

        // 设置 waterMarks 周期性的生成时间
        env.getConfig().setAutoWatermarkInterval(100);

        // 添加 sink 源
        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                // 乱序流的watermark生成
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

        stream.keyBy(data -> data.user)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .aggregate(new AggregateFunction<Event, Tuple2<Long, Long>, String>() {

                    // 创建累加器 初始值
                    @Override
                    public Tuple2<Long, Long> createAccumulator() {
                        return Tuple2.of(0L, 0L);
                    }

                    // 每来一个数据 调用一个add 方法  返回状态类型
                    @Override
                    public Tuple2<Long, Long> add(Event value, Tuple2<Long, Long> accumulator) {

                        return Tuple2.of(accumulator.f0 + value.timestamp, accumulator.f1 + 1);
                    }

                    // 获取结果，窗口调用计算，触发一次计算，就调用一次
                    @Override
                    public String getResult(Tuple2<Long, Long> accumulator) {
                        Timestamp timestamp = new Timestamp(accumulator.f0 / accumulator.f1);

                        return timestamp.toString();
                    }

                    @Override
                    public Tuple2<Long, Long> merge(Tuple2<Long, Long> a, Tuple2<Long, Long> b) {

                        return Tuple2.of(a.f0 + b.f0, a.f1 + b.f1);
                    }
                })
                .print();

        env.execute();
    }

}
