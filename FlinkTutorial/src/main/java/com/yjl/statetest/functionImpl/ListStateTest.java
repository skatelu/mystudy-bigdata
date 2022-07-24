package com.yjl.statetest.functionImpl;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

/**
 * 自定义列表状态，进行全外链接
 */
public class ListStateTest extends CoProcessFunction<Tuple3<String, String, Long>, Tuple3<String, String, Long>, String> {

    // 定义列表状态，用于保存两条流中已经到达的所有数据
    private ListState<Tuple3<String, String, Long>> stream1ListState;
    private ListState<Tuple3<String, String, Long>> stream2ListState;

    @Override
    public void open(Configuration parameters) throws Exception {
        stream1ListState = getRuntimeContext().getListState(new ListStateDescriptor<Tuple3<String, String, Long>>("stream1-list", Types.TUPLE(Types.STRING, Types.STRING, Types.LONG)));
        stream2ListState = getRuntimeContext().getListState(new ListStateDescriptor<Tuple3<String, String, Long>>("stream2-list", Types.TUPLE(Types.STRING, Types.STRING, Types.LONG)));
    }

    @Override
    public void processElement1(Tuple3<String, String, Long> left, Context ctx, Collector<String> out) throws Exception {

        // 获取另外一条流中所有的数据，配对输出
        Iterable<Tuple3<String, String, Long>> tuple3Iterable = stream2ListState.get();
        for (Tuple3<String, String, Long> stringStringLongTuple3 : tuple3Iterable) {
                out.collect("stream1 ----> " + left + "=>" + stringStringLongTuple3);
        }

        stream1ListState.add(left);
    }

    @Override
    public void processElement2(Tuple3<String, String, Long> right, Context ctx, Collector<String> out) throws Exception {

        Iterable<Tuple3<String, String, Long>> leftIterable = stream1ListState.get();

        for (Tuple3<String, String, Long> left : leftIterable) {
                out.collect("stream2 ----> " + right + "=>" + left);
        }

        stream2ListState.add(right);
    }
}
