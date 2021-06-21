package Q5;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.streaming.util.serialization.JSONKeyValueDeserializationSchema;

import java.util.*;

/**
 * @author ytc
 * @ClassName demo
 * @Description TODO
 * @date 2021/06/21
 */
public class demo {
    private static String user="汤欣欣";
    private static List<String> inputTopics;
    private static String bootstrapServers =
            "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";
    public static void main(String[] args) {
        inputTopics=new ArrayList<>();
        inputTopics.add("mn_buy_ticket_1"); //车票购买记录主题
        inputTopics.add("mn_hotel_stay_1");//酒店入住信息主题
        inputTopics.add("mn_monitoring_1");//监控系统数据主题
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", bootstrapServers);
        kafkaProperties.put("group.id", UUID.randomUUID().toString());
        kafkaProperties.put("auto.offset.reset", "earliest");
        kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        FlinkKafkaConsumer010<ObjectNode> kafkaConsumer = new FlinkKafkaConsumer010<ObjectNode>(inputTopics, new JSONKeyValueDeserializationSchema(true), kafkaProperties);
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        Thread thread=new Thread(() -> {
            DataStreamSource<ObjectNode> streamSource = env.addSource(kafkaConsumer);
            streamSource.filter(new FilterFunction<ObjectNode>() {
                @Override
                public boolean filter(ObjectNode jsonNodes) throws Exception {
                    return jsonNodes.get("value").get("username").asText("").equals(user);
                }
            }).map(new MapFunction<ObjectNode, Object>() {
                @Override
                public Object map(ObjectNode jsonNodes) throws Exception {
                    System.out.println(jsonNodes);
                    if(jsonNodes.get("metadata").get("topic").asText("").matches("mn_monitoring_1")){
                        return jsonNodes.get("value").get("found_time");
                    }else
                        return jsonNodes.get("value").get("buy_time");
                }
            });
            try {
                env.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        Scanner in=new Scanner(System.in);
        while(in.hasNextLine()){
            thread.interrupt();
            user=in.nextLine();
            thread=new Thread(()->{
                DataStreamSource<ObjectNode> streamSource = env.addSource(kafkaConsumer);
                streamSource.filter(new FilterFunction<ObjectNode>() {
                    @Override
                    public boolean filter(ObjectNode jsonNodes) throws Exception {
                        return jsonNodes.get("value").get("username").asText("").equals(user);
                    }
                }).map(new MapFunction<ObjectNode, Object>() {
                    @Override
                    public Object map(ObjectNode jsonNodes) throws Exception {
                        if(jsonNodes.get("metadata").get("topic").asText("").matches("mn_monitoring_1")){
                            return jsonNodes.get("value").get("found_time");
                        }else
                            return jsonNodes.get("value").get("buy_time");
                    }
                }).print();
                try {
                    env.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
        }

    }
}
