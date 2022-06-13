package com.yjl.sourceTest;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.kafka.common.serialization.StringSerializer;

public class SourceKafkaTest {


    public static void main(String[] args) throws Exception {

        // 获取执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 从Kafka中获取数据
        KafkaSource<String> kafkaSource = getKafkaSource();
        DataStreamSource<String> stream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka-Source", TypeInformation.of(String.class));

//        stream.print("Kafka");

        KafkaSink<String> kafkaSink = getKafkaSink();

        stream.map(n->{
            System.out.println(n);
            return n;
        }).sinkTo(kafkaSink);

        env.execute();
    }

    /**
     * 从kafka中获取相关数据
     * @return
     */
    private static KafkaSource<String> getKafkaSource(){
        KafkaSourceBuilder<String> builder = KafkaSource.builder();
        builder.setTopics("clicks");
        builder.setValueOnlyDeserializer(new SimpleStringSchema());
        builder.setStartingOffsets(OffsetsInitializer.earliest());
        builder.setProperty("bootstrap.servers", "docker10:38005,docker11:38005,docker12:38005");
        builder.setProperty("group.id", "consumer-group");
        builder.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        builder.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        builder.setProperty("auto.offset.reset", "latest");
        KafkaSource<String> kafkaSource = builder.build();

        return kafkaSource;
    }

    private static KafkaSink<String> getKafkaSink() {

        KafkaSink<String> sink = KafkaSink.<String>builder()
                .setBootstrapServers("docker10:38005,docker11:38005,docker12:38005")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopic("test")
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .setKafkaKeySerializer(StringSerializer.class)
                        .build()
                )
                .setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        return sink;
    }

}
