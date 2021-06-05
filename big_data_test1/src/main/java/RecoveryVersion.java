import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
/**
 * @author ytc
 * @date 2021/06/01
 */
public class RecoveryVersion {
    private final static String bucketName = "ytc";
    private final static String keyName = "hello.txt";
    private final static String retrievingPath = "c:\\Users\\严天成\\Desktop";
    private final static String accessKey = "12BD2990F33681DB1E4C";
    private final static String secretKey = "W0ExQ0UwQzcxMjVDQjVGNTk4Q0Y3Mjg3MTdEN0U4";
    private final static String versionId  = "E9Gat-.i3sWils9bDG0vYm4wANQ193d";
    private final static String serviceEndpoint = "http://10.16.0.1:81";
    private final static String signingRegion = "";
    public static void main(String[] args) {
        final BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey,secretKey);
        final ClientConfiguration ccfg = new ClientConfiguration().
                withUseExpectContinue(true);

        final EndpointConfiguration endpoint = new EndpointConfiguration(serviceEndpoint, signingRegion);

        final AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withClientConfiguration(ccfg)
                .withEndpointConfiguration(endpoint)
                .withPathStyleAccessEnabled(true)
                .build();

        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName, versionId);
        System.out.format("Retrieving %s from S3 bucket %s...\n", keyName, bucketName);

        S3ObjectInputStream s3is = null;
        FileOutputStream fos = null;
        final SimpleDateFormat dateFormate=new SimpleDateFormat("yyyy-mm-dd_HH-mm-ss");
        try {
            S3Object o = s3.getObject(getObjectRequest);
            s3is = o.getObjectContent();

            final String filePath = Paths.get(retrievingPath,
                    String.format("%s_%s",
                            dateFormate.format(o.getObjectMetadata().getLastModified()),
                            keyName)).toString();
            final File file = new File(filePath);
            fos = new FileOutputStream(file);
            byte[] read_buf = new byte[64 * 1024];
            int read_len = 0;
            while ((read_len = s3is.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }

            System.out.format("Save %s to %s\n", keyName, filePath);
        } catch (AmazonServiceException e) {
            System.err.println(e.toString());
            System.exit(1);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } finally {
            if (s3is != null) try { s3is.close(); } catch (IOException e) { }
            if (fos != null) try { fos.close(); } catch (IOException e) { }
        }

        System.out.println("Done!");
    }
}
