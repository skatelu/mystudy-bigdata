package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlCount;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlViewCountAgg;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlViewCountResult;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.OutputTag;

import java.time.Duration;

/**
 * 测试延迟数据 如何处理
 */
public class LateDataTest {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.getConfig().setAutoWatermarkInterval(100);

        SingleOutputStreamOperator<Event> strem = env.socketTextStream("docker10", 7777)
                .map(new MapFunction<String, Event>() {
                    @Override
                    public Event map(String s) throws Exception {

                        String[] fields = s.split(",");

                        return new Event(fields[0].trim(), fields[1].trim(), Long.valueOf(fields[2].trim()));
                    }
                })
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(2))
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                    @Override
                                    public long extractTimestamp(Event event, long l) {
                                        return event.timestamp;
                                    }
                                })
                );

        strem.print("input");

        // 定义一个输出标签
        OutputTag<Event> late = new OutputTag<Event>("late"){};

        SingleOutputStreamOperator<UrlCount> result = strem.keyBy(data -> data.url)
                .window(TumblingEventTimeWindows.of(Time.seconds(10)))
                .allowedLateness(Time.minutes(1)) // 允许窗口存在1分钟的延迟
                .sideOutputLateData(late) // 将超过一分钟后到的数据 写入侧输出流
                .aggregate(new UrlViewCountAgg(), new UrlViewCountResult());

        result.print("result");
        result.getSideOutput(late).print("late");

        env.execute();
    }

}
