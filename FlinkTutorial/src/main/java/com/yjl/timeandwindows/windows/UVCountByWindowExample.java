package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import com.yjl.timeandwindows.windows.processwindowfunction.UvCountByWindow;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * 电商系统，统计每小时的 UV 量
 */
public class UVCountByWindowExample {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置全局并行度
        env.setParallelism(1);
        // 设置周期性的生成 warterMarks 100 ms
        env.getConfig().setAutoWatermarkInterval(100);

        // 创建数据来源 source
        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                // 设置 warterMarks 延后生成时间
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy
                                // 设置 waterMarks 是否延后生成，会导致窗口是否延后关闭
                                .<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                // 设置 返回那个字段 作为 waterMarks 水位线
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>(){

                                    @Override
                                    public long extractTimestamp(Event event, long l) {
                                        return event.timestamp;
                                    }

                                })
                );


        // 将所有数据都发向 同一个窗口，按窗口统计 UV
        stream.keyBy(data -> true)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .process(new UvCountByWindow())
                .print();

        env.execute();
    }

}
