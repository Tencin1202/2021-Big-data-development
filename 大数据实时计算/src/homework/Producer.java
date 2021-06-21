package homework;

import com.bingocloud.ClientConfiguration;
import com.bingocloud.Protocol;
import com.bingocloud.auth.BasicAWSCredentials;
import com.bingocloud.services.s3.AmazonS3Client;
import com.bingocloud.services.s3.model.S3Object;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.nlpcn.commons.lang.util.IOUtil;

import java.util.Properties;

/**
 * @author ytc
 * @ClassName KafkaProducer
 * @Description TODO
 * @date 2021/06/16
 */
public class Producer {
    private static final String accessKey = "12BD2990F33681DB1E4C";
    private static final String secretKey = "W0ExQ0UwQzcxMjVDQjVGNTk4Q0Y3Mjg3MTdEN0U4";
    private static final String endpoint = "http://scut.depts.bingosoft.net:29997";
    private static final String bucket = "ytc";
    //kafka参数
    private static final String topic = "mn_buy_ticket_1_ytc";
    private static final String bootstrapServers
            = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";

    public static void main(String[] args) {
        produce(getData());
    }

    public static String getData(){
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration ccfg = new ClientConfiguration();
        ccfg.setProtocol(Protocol.HTTP);
        AmazonS3Client S3 = new AmazonS3Client(credentials,ccfg);
        S3.setEndpoint(endpoint);
        S3Object s3Object = S3.getObject(bucket, "190806150787800.txt");
        return IOUtil.getContent(s3Object.getObjectContent(), "UTF-8");
    }

    public static void produce(String content){
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        String[] arr = content.split("\n");
        for (String s:arr) {
            if(!s.trim().isEmpty()) {
                ProducerRecord<String, String> record = new ProducerRecord<String,String>(topic,null,s);
                System.out.println("开始生产数据:"+s);
                producer.send(record);
            }
            producer.flush();
        }
        producer.close();

    }
}
