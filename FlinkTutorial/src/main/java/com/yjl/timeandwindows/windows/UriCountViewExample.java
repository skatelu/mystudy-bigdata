package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlViewCountAgg;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlViewCountResult;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * 统计热点数据，url 访问量数据
 */
public class UriCountViewExample {

    public static void main(String[] args) throws Exception {
        // 创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 创建并行度
        env.setParallelism(1);
        // 设置waterMarker 水位线 生成周期 100ms
        env.getConfig().setAutoWatermarkInterval(100);
        // 生成数据源与水位线
        SingleOutputStreamOperator<Event> stream = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                    @Override
                                    public long extractTimestamp(Event event, long l) {
                                        return event.timestamp;
                                    }
                                })
                );
        // 打印Stream中的数据
//        stream.print("input");

        // 统计每个url的访问量
        stream.keyBy(data -> data.url)
                .window(TumblingEventTimeWindows.of(Time.seconds(1)))
                .aggregate(new UrlViewCountAgg(), new UrlViewCountResult())
                .print("result");

        env.execute();

    }

}
