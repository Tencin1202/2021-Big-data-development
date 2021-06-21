package Q2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ytc
 * @ClassName demo
 * @Description 统计游客到达数目前五的城市
 * @date 2021/06/21
 */
public class demo {
    private static volatile Map<String,Integer> KF=new ConcurrentHashMap<>();
    private static volatile  Map<Integer, HashSet<String>> FK=new ConcurrentHashMap<>();
    public static void main(String[] args) {
        String topic="mn_buy_ticket_1_ytc";
        String bootstrapServers="bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", bootstrapServers);
        kafkaProperties.put("group.id", UUID.randomUUID().toString());
        kafkaProperties.put("auto.offset.reset", "earliest");
        kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        FlinkKafkaConsumer010<String> kafkaConsumer = new FlinkKafkaConsumer010<>(topic, new SimpleStringSchema(), kafkaProperties);
        kafkaConsumer.setCommitOffsetsOnCheckpoints(true);
        DataStreamSource<String> inputKafkaStream = env.addSource(kafkaConsumer);

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Object [] freq= FK.keySet().toArray();
                Arrays.sort(freq);
                //System.out.println(Arrays.toString(freq));
                int max=5;
                HashSet<String> set=null;
                System.out.println("截至现在,游客到达前5的层城市有");
                for(int i=freq.length-1;i>=0;i--){
                    set= FK.get(freq[i]);
                    for(String s:set){
                        if(max<=0)
                            break;
                        System.out.println(s+":"+freq[i]);
                        max--;
                    }
                    if(max<=0)
                        break;
                }
            }
        },60000,60000);
        inputKafkaStream.map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                System.out.println(s);
                JSONObject jsonObject= JSON.parseObject(s);
                String destination=jsonObject.getString("destination");
                if(!KF.containsKey(destination)){
                    KF.put(destination, 1);
                    if(FK.containsKey(1))
                        FK.get(1).add(destination);
                    else{
                        HashSet<String> set=new HashSet<>();
                        set.add(destination);
                        FK.put(1,set);
                    }
                }else {
                    Integer freq = KF.get(destination);
                    KF.put(destination, freq+1);
                    FK.get(freq).remove(destination);
                    if(FK.get(freq).size()==0)
                        FK.remove(freq);
                    if(FK.containsKey(freq+1))
                        FK.get(freq+1).add(destination);
                    else{
                        HashSet<String> set=new HashSet<>();
                        set.add(destination);
                        FK.put(freq+1,set);
                    }
                }
                return s;
            }
        });
        try {
            env.execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
