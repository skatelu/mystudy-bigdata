package com.yjl.timeandwindows.windows.aggregatefunction;

import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

/**
 * 包装窗口信息，输出URL的count 数量
 * 窗口函数中的 处理窗口函数 获取上下文信息后，用来输出结果信息
 */
public class UrlViewCountResult extends ProcessWindowFunction<Long,UrlCount,String, TimeWindow> {

    @Override
    public void process(String url, Context context, Iterable<Long> iterable, Collector<UrlCount> collector) throws Exception {
        long start = context.window().getStart();
        long end = context.window().getEnd();

        Long count = iterable.iterator().next();
        collector.collect(new UrlCount(url, count, start, end));
    }
}
