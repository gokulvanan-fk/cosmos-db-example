package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
@AllArgsConstructor
public class DocumentReadByPrimaryKey implements Runnable {
    private DocumentClient client;
    private DocumentCollection documentCollection;
    private Integer taskId;
    private Integer countOfDocs;
    private Statistics stats;
    private ArrayList<Document> personCache;

    public void run() {
        Asserts.check(client!=null && documentCollection!=null,
                "DocumentClient or DocumentCollection");

        DocumentClient client = this.client;

        if(Configuration.READ_USE_NEW_CLIENTS_IN_TASK) {
            client = Program.createReadOnlyClient();
        }

        double ruConsumed = 0;
        int percentage = -1;

        for (int i = 0; i < countOfDocs; i++) {

            try {
                int idx = new Random().nextInt(personCache.size());
                Document person = personCache.get(idx);

                long tic = System.currentTimeMillis();

                RequestOptions options = new RequestOptions();
                options.setPartitionKey(new PartitionKey(person.get("gender")));
                ResourceResponse<Document> document = client.readDocument(person.getSelfLink(), options);

                long tock = System.currentTimeMillis();
                stats.getElapsedTimeInMs().add((double) tock-tic);

                if((double) tock-tic > Configuration.THREASHOLD_LATENCY) {
                    log.info("Slow query with activity ID: {}" , document.getActivityId());
                }

                int currPercentage = (i * 100)/countOfDocs;
                if (currPercentage  % 10 == 0 && currPercentage > percentage) {
                    log.info("{}% query completed", currPercentage);
                    percentage = currPercentage;
                }
                ruConsumed += document.getRequestCharge();
            } catch (Exception dce) {
                log.error("[{}]Failed to query document ", taskId, dce);
                throw new RuntimeException("Failed to query document ");
            }
        }

        stats.setConsumedRUs(ruConsumed);
    }
}