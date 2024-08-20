import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final BlockingQueue<Long> requestQueue;
    private final int requestLimit;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestQueue = new LinkedBlockingQueue<>(requestLimit);
        this.requestLimit = requestLimit;
        setupRequestLimitThread(timeUnit.toSeconds(1));
    }

    private void setupRequestLimitThread(long timeUnitInSeconds) {
        Runnable requestLimitThread = () -> {
            while (true) {
                try {
                    requestQueue.take();
                    Thread.sleep(timeUnitInSeconds * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(requestLimitThread);
        thread.start();
    }

    public void createDocument(String document, String signature) {
        try {
            requestQueue.put(System.currentTimeMillis());
            HttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("https://ismp.crpt.ru/api/v3/lk/documents/create");
            httpPost.setHeader("Content-Type", "application/json");

            StringEntity requestEntity = new StringEntity(document, ContentType.APPLICATION_JSON);
            httpPost.setEntity(requestEntity);

            httpClient.execute(httpPost);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 10);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            String document = mapper.writeValueAsString(createDocumentObject());
            String signature = "exampleSignature";

            crptApi.createDocument(document, signature);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object createDocumentObject() {
        // Create and return document object
        return new Object();
    }
}

