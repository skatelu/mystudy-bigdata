package com.yjl.timeandwindows.windows;

import com.yjl.chapter05.Event;
import com.yjl.chapter05.reducetest.ClickSource;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlCount;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlViewCountAgg;
import com.yjl.timeandwindows.windows.aggregatefunction.UrlViewCountResult;
import com.yjl.timeandwindows.windows.processwindowfunction.TopNProcessResult;
import org.apache.flink.api.common.eventtime.SerializableTimestampAssigner;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.SlidingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

/**
 * 统计一段时间内的 网站访问量，并进行排序
 *
 *  在窗口中可以用一个 HashMap 来保存每个 url 的访问次数，只要遍历窗口中的所有数据，
 * 自然就能得到所有 url 的热门度。最后把 HashMap 转成一个列表 ArrayList，然后进行排序、
 * 取出前两名输出就可以了
 *
 */
public class TopNExample {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置全局并行度
        env.setParallelism(1);
        // 设置自动生成水位线的周期 100ms
        env.getConfig().setAutoWatermarkInterval(100);

        // 添加数据源
        SingleOutputStreamOperator<Event> streamSource = env.addSource(new ClickSource())
                .assignTimestampsAndWatermarks(
                        // 设置水位线 延迟时间，这里设置成0 表示水位线不延迟
                        WatermarkStrategy
                                .<Event>forBoundedOutOfOrderness(Duration.ZERO)
                                .withTimestampAssigner(new SerializableTimestampAssigner<Event>() {
                                    @Override
                                    public long extractTimestamp(Event event, long l) {
                                        return event.timestamp;
                                    }
                                })// 设置取哪个时间作为 水位线时间
                );

        // 安装url进行分组，统计窗口内，每个url的访问数量
        SingleOutputStreamOperator<UrlCount> urlCountStream = streamSource.keyBy(data -> data.url)
                .window(SlidingEventTimeWindows.of(Time.seconds(10), Time.seconds(5)))
                .aggregate(new UrlViewCountAgg(), new UrlViewCountResult());

        urlCountStream.print("url count");

        // 2、对于 同一窗口统计出的访问量进行收集和排序
        urlCountStream.keyBy(data -> data.getWindowEnd())
                .process(new TopNProcessResult(2))
                .print();

        env.execute();
    }

}
