package com.yjl.kafkatest;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class ConsumerDemo {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers","10.166.147.61:38005,10.166.147.62:38005,10.166.147.63:38005");
        props.put("group.id","dmp-2-sr-etctu");
        props.put("enable.auto.commit","false");
        props.put("auto.commit.intervals.ms","100");
        props.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer consumer = new KafkaConsumer(props);

        consumer.subscribe(Arrays.asList("ETCTU"));

        while (true){
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(300L));

            for(ConsumerRecord<String,String> record : records){
                System.out.println(record.offset()+"----"+"-----"+ record.value());
                Date date = new Date(record.timestamp());
                System.out.println(date);
            }

        }

    }
}