package com.yjl.timeandwindows.windows.aggregatefunction;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple2;

import java.util.*;

/**
 * 使用自定义的 聚合函数，对url进行统计，将结果封装成排好序的 List 进行输出
 * 使用 hashmap，统计所有的Url地址
 */
public class UrlHashMapCountAgg implements AggregateFunction<String, HashMap<String, Long>, List<Tuple2<String, Long>>> {

    @Override
    public HashMap<String, Long> createAccumulator() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Long> add(String s, HashMap<String, Long> accumulator) {

        if (accumulator.containsKey(s)) {
            Long count = accumulator.get(s);
            accumulator.put(s, count++);
        } else {
            accumulator.put(s, 1L);
        }

        return accumulator;
    }

    /**
     * 将 中间状态 hashMap 转换成 List，然后用 List的Sort 函数进行排序，将排好序的 List 进行输出
     * @param accumulator
     * @return
     */
    @Override
    public List<Tuple2<String, Long>> getResult(HashMap<String, Long> accumulator) {
        List<Tuple2<String, Long>> result = new ArrayList<>();

        for (Map.Entry<String, Long> resultEntry : accumulator.entrySet()) {
            result.add(Tuple2.of(resultEntry.getKey(), resultEntry.getValue()));
        }

        result.sort(new Comparator<Tuple2<String, Long>>() {
            @Override
            public int compare(Tuple2<String, Long> o1, Tuple2<String, Long> o2) {
                return o2.f1.intValue() - o1.f1.intValue();
            }
        });

        return result;
    }

    @Override
    public HashMap<String, Long> merge(HashMap<String, Long> stringLongHashMap, HashMap<String, Long> acc1) {
        return null;
    }
}
