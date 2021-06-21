package Q4;

import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.io.jdbc.JDBCOutputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.types.Row;
import org.apache.flink.util.Collector;

import java.sql.Types;
import java.util.Properties;
import java.util.UUID;

/**
 * @author ytc
 * @ClassName demo
 * @Description Flink流式计算结果存入Mysql
 * @date 2021/06/21
 */
public class demo {
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
        DataStream<String> inputKafkaStream = env.addSource(kafkaConsumer);

        inputKafkaStream.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String text, Collector<String> collector) throws Exception {
                String[] strs=text.split("\n");
                for(String s:strs){
                    if(JSONObject.parseObject(s).keySet().size()==5)
                        collector.collect(s);
                }
            }
        }).map(new MapFunction<String, Row>() {
            @Override
            public Row map(String s) throws Exception {
                Row row=new Row(5);
                JSONObject jsonObject = JSONObject.parseObject(s);
                row.setField(0, jsonObject.get("buy_time"));
                row.setField(1, jsonObject.get("buy_address"));
                row.setField(2, jsonObject.get("origin"));
                row.setField(3, jsonObject.get("destination"));
                row.setField(4, jsonObject.get("username"));
                System.out.println(row.toString());
                return row;
            }
        }).writeUsingOutputFormat(JDBCOutputFormat.buildJDBCOutputFormat()
                .setDBUrl("jdbc:mysql://localhost:3306/flink?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8")
                .setDrivername("com.mysql.cj.jdbc.Driver")
                .setUsername("root")
                .setPassword("qq12345")
                .setSqlTypes(new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR})
                .setQuery("insert into ticket(buy_time,buy_address,origin,destination,username) values(?,?,?,?,?);")
                .finish()
        );
        try {
            env.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
