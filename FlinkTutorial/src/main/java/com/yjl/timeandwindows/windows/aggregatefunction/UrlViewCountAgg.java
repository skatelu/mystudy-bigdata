package com.yjl.timeandwindows.windows.aggregatefunction;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.functions.AggregateFunction;

/**
 * 自定义窗口的增量聚合函数，统计当前分区内的所有的 url 数量
 */
public class UrlViewCountAgg implements AggregateFunction<Event, Long, Long> {


    @Override
    public Long createAccumulator() {
        return 0L;
    }

    @Override
    public Long add(Event event, Long aLong) {
        return aLong + 1;
    }

    @Override
    public Long getResult(Long aLong) {
        return aLong;
    }

    @Override
    public Long merge(Long aLong, Long acc1) {
        return null;
    }
}
