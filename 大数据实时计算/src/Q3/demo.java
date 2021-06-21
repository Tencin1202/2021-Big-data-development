package Q3;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.*;
import java.util.Properties;

/**
 * @author ytc
 * @ClassName demo
 * @Description 读取mysql进入kafka
 * @date 2021/06/21
 */
public class demo {

    private static final String topic = "mn_buy_ticket_1_ytc";
    private static final String bootstrapServers
            = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037";
    private static final String url="jdbc:hive2://bigdata115.depts.bingosoft.net:22115/user14_db";
    public static void main(String[] args) {
        Connection connection=null;
        Properties properties=new Properties();
        properties.setProperty("driverClassName", "org.apache.hive.jdbc.HiveDriver");
        try {
            connection= DriverManager.getConnection(url,properties);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        PreparedStatement preparedStatement =null;
        ResultSet resultSet=null;
        JSONObject jsonObject=null;
        String key=null;
        String s=null;
        try {
            preparedStatement=connection.prepareStatement("select * from user");
            resultSet = preparedStatement.executeQuery();
            int col=resultSet.getMetaData().getColumnCount();
            while (resultSet.next()){
                jsonObject=new JSONObject();
                for(int i=1;i<=col;i++){
                    key=resultSet.getMetaData().getColumnLabel(i);
                    jsonObject.put(key, resultSet.getString(key));
                }
                s=jsonObject.toJSONString();
                System.out.println("开始生产数据:"+s);
                producer.send(new ProducerRecord<String,String>(topic,null,s));
                producer.flush();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }finally {
            try {
                producer.close();
                if (resultSet != null) {
                    resultSet.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
