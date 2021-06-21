package homework;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.util.Collector;

import java.util.Properties;
import java.util.UUID;

/**
 * @author ytc
 * @ClassName KafkaConsumer
 * @Description TODO
 * @date 2021/06/16
 */
public class Consumer {
    private static final String accessKey = "12BD2990F33681DB1E4C";
    private static final String secretKey = "W0ExQ0UwQzcxMjVDQjVGNTk4Q0Y3Mjg3MTdEN0U4";
    private static final String endpoint = "http://scut.depts.bingosoft.net:29997";
    private static final String bucket = "ytc";
    private static final String keyPrefix = "upload/";
    private static final int period = 5000;
    //kafka参数
    private static final String inputTopic = "mn_buy_ticket_1_ytc";
    private static final String bootstrapServers
            = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";

    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServers);
        properties.put("group.id", UUID.randomUUID().toString());
        properties.put("auto.offset.reset", "earliest");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        FlinkKafkaConsumer010<String> kafkaConsumer = new FlinkKafkaConsumer010<String>(inputTopic,new SimpleStringSchema(),properties);
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        DataStreamSource<String> inputKafkaStream = env.addSource(kafkaConsumer);
        inputKafkaStream.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String text, Collector<String> collector) throws Exception {
                String[] strs=text.split("\n");
                for(String s:strs){
                    if(JSONObject.parseObject(s).keySet().size()==5)
                        collector.collect(s);
                }
            }
        }).writeUsingOutputFormat(new S3Writer(accessKey, secretKey, endpoint, bucket, keyPrefix, period));
        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
