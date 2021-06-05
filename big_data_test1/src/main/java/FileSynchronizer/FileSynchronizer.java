package FileSynchronizer;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;


/**
 * @author ytc
 * @date 2021/06/01
 */
public class FileSynchronizer {

    public static void main(String[] args) {
        ThreadPoolExecutor threadPool=
                new ThreadPoolExecutor(3, 10, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
        final String path="D:\\LocalRepository\\";
        S3Util.DownLoadFile(path);
        WatchService watchService= null;
        final CopyOnWriteArraySet<String> Monitor=new CopyOnWriteArraySet<>();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Monitor.clear();
            }
        }, 0,1000);
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(path);
            dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE);
            while (true){
                WatchKey key=watchService.take();
                List<WatchEvent<?>> eventList=key.pollEvents();
                for(final WatchEvent<?> event:eventList){
                    if(event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)){
                            Monitor.add(path+event.context());
                            System.out.println("创建了文件 "+event.context()+" ,开始同步上传");
                            FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
                                @Override
                                public Void call() {
                                    if (S3Util.UpLoadFile(path + event.context())) {
                                        System.out.println("文件 " + path + event.context() + " 上传完成");
                                    }
                                    else
                                        System.out.println("文件 " + path + event.context() + " 上传失败");
                                    return null;
                                }
                            });
                            threadPool.execute(task);
                    }else if(event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)){
                        System.out.println("删除了文件 "+event.context()+" ,开始同步删除");
                        FutureTask<Void> task=new FutureTask<>(new Callable<Void>() {
                            @Override
                            public Void call() {
                                if(S3Util.DeleteFile(path+event.context()))
                                    System.out.println("文件 " + path + event.context() + " 删除完成");
                                else
                                    System.out.println("文件 " + path + event.context() + " 删除失败");
                                return null;
                            }
                        });
                        threadPool.execute(task);
                    }else if(event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)){
                        if(!Monitor.contains(path+event.context())) {
                            System.out.println("修改了文件 " + event.context() + " ,开始同步更新");
                            FutureTask<Void> task = new FutureTask<>(new Callable<Void>() {
                                @Override
                                public Void call() {
                                    if(S3Util.UpLoadFile(path + event.context()))
                                        System.out.println("文件 " + path + event.context() + " 更新完成");
                                    else
                                        System.out.println("文件 " + path + event.context() + " 更新失败");
                                    return null;
                                }
                            });
                            threadPool.execute(task);
                        }
                    }
                }
                key.reset();
            }
        }catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally{
            threadPool.shutdown();
            threadPool=null;
            watchService=null;
            System.gc();
        }

    }

}
