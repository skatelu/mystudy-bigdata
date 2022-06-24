package com.yjl.timeandwindows.windows.aggregatefunction;

import com.yjl.chapter05.Event;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.text.DecimalFormat;
import java.util.HashSet;

/**
 * 自定义窗口聚合函数 aggregateFunction，计算 窗口 pv与uv的比值
 */
public class AvgPv implements AggregateFunction<Event, Tuple2<Float, HashSet<String>>, String> {


    /**
     * 初始化状态总值
     * @return
     */
    @Override
    public Tuple2<Float, HashSet<String>> createAccumulator() {
        return Tuple2.of(0.00f, new HashSet<String>());
    }

    /**
     * 每来一个值，就会调用一次add方法，计算过程也是放在该函数中的
     * @param event
     * @param accumulator
     * @return
     */
    @Override
    public Tuple2<Float, HashSet<String>> add(Event event, Tuple2<Float, HashSet<String>> accumulator) {

        accumulator.f1.add(event.user);

        return Tuple2.of(accumulator.f0 + 1, accumulator.f1);
    }

    /**
     * 当窗口关闭时，会调用此方法，输出结果
     * @param accumulator 这个是flink中的状态，调用该方法时，会传入 flink的状态进行计算
     * @return
     */
    @Override
    public String getResult(Tuple2<Float, HashSet<String>> accumulator) {

        DecimalFormat df = new DecimalFormat("0.00");

        // 窗口触发时，输出 pv / uv 的值
        return df.format((accumulator.f0 / accumulator.f1.size()));
    }

    /**
     * 调用 Session 窗口时，需要将状态进行合并，在此处进行，场景很少，可以不实现
     * @param stringHashSetTuple2
     * @param acc1
     * @return
     */
    @Override
    public Tuple2<Float, HashSet<String>> merge(Tuple2<Float, HashSet<String>> stringHashSetTuple2, Tuple2<Float, HashSet<String>> acc1) {
        return null;
    }
}
