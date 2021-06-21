package Q1;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author ytc
 * @ClassName demo
 * @Description 统计最近一分钟内b出现的次数
 * @date 2021/06/21
 */
public class demo {
    private static int num;
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStreamSource<String> text = env.socketTextStream("localhost", 9999);
        Timer timer= new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("前一分钟内b出现了"+num+"次");
                num=0;
            }
        }, 60000,60000);
        SingleOutputStreamOperator<String> stream = text.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                String[] strings = s.toLowerCase().split("\\W+");
                for (String str:strings) {
                    boolean flag=true;
                    for(int i=0;i<str.length();i++){
                        if(str.charAt(i)=='b'){
                            num++;
                            if(flag){
                                collector.collect(str);
                                flag=false;
                            }
                        }
                    }
                }
            }
        }).map(new MapFunction<String, String>() {
            @Override
            public String map(String s) throws Exception {
                return "发现目标:" + s;
            }
        });
        stream.print();
        try {
            env.execute("Window Stream WordCount");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
