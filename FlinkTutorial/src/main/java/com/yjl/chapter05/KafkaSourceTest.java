package com.yjl.chapter05;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class KafkaSourceTest {

    public static void main(String[] args) {
        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从Kafka中获取数据
        KafkaSourceBuilder<String> builder = KafkaSource.builder();
        builder.setTopics("clicks");
        builder.setValueOnlyDeserializer(new SimpleStringSchema());
        builder.setStartingOffsets(OffsetsInitializer.earliest());
        builder.setProperty("bootstrap.servers", "hadoop102:9092");
        builder.setProperty("group.id", "consumer-group");
        builder.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        builder.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        builder.setProperty("auto.offset.reset", "latest");
        KafkaSource<String> kafkaSource = builder.build();

        DataStreamSource<String> stringDataStreamSource = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka-Source", TypeInformation.of(String.class));

    }

}
