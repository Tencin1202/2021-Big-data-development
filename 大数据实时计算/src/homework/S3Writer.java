package homework;

import com.alibaba.fastjson.JSONObject;
import com.bingocloud.ClientConfiguration;
import com.bingocloud.Protocol;
import com.bingocloud.auth.BasicAWSCredentials;
import com.bingocloud.services.s3.AmazonS3Client;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.configuration.Configuration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author ytc
 * @ClassName S3Writer
 * @Description TODO
 * @date 2021/06/16
 */
public class S3Writer implements OutputFormat<String> {
    private Timer timer;
    private File file;
    private FileWriter fileWriter;
    private Long length=0L;
    private HashMap<String,LinkedList<String>> map;
    private AmazonS3Client amazonS3;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String bucket;
    private String keyPrefix;
    private int period;

    public S3Writer(String accessKey, String secretKey, String endpoint, String bucket, String keyPrefix, int period) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.keyPrefix = keyPrefix;
        this.period = period;
        this.map=new HashMap<>();
    }

    public void upload(){
        synchronized (this) {
            if(length>0) {
                if (fileWriter == null) {
                    file = new File( System.nanoTime()+".txt");
                    try {
                        fileWriter = new FileWriter(file, true);
                        Set<String> keys = map.keySet();
                        for (String key:keys) {
                            LinkedList<String> strings = map.get(key);
                            fileWriter.append(key).append("\n");
                            for (String s:strings) {
                                fileWriter.append(s).append("\n");
                            }
                            fileWriter.append("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    fileWriter.close();
                    String targetKey = keyPrefix + System.nanoTime();
                    amazonS3.putObject(bucket, targetKey, file);
                    System.out.println("开始上传文件"+file.getAbsoluteFile() +"到桶"+bucket+"的目录"+targetKey+"下");
                    fileWriter=null;
                    file=null;
                    map.clear();
                    length = 0L;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void configure(Configuration configuration) {
        timer=new Timer("S3Writer");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                upload();
            }
        },1000,period);
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        amazonS3 = new AmazonS3Client(credentials, clientConfig);
        amazonS3.setEndpoint(endpoint);

    }

    @Override
    public void open(int i, int i1) throws IOException {

    }

    @Override
    public void writeRecord(String s) throws IOException {
        synchronized (this){
            if (StringUtils.isNoneBlank(s)) {
                JSONObject jsonObject = JSONObject.parseObject(s);
                String username= (String) jsonObject.get("username");
                LinkedList<String> list = map.get(username);
                if(list==null){
                    list=new LinkedList<>();
                    map.put(username, list);
                }
                list.add(s);
                length+=s.length();
            }
        }
    }

    @Override
    public void close() throws IOException {
        fileWriter.flush();
        fileWriter.close();
        timer.cancel();
    }
}
