package com.example;

import com.microsoft.azure.documentdb.*;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;
import rx.Observable;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
@AllArgsConstructor
public class DocumentReadByPrimaryKeyAsync implements Runnable {
    private AsyncDocumentClient client;
    private DocumentCollection documentCollection;
    private Integer taskId;
    private Integer countOfDocs;
    private Statistics stats;
    private ArrayList<Person> personCache;

    public void run() {
        Asserts.check(client!=null && documentCollection!=null,
                "DocumentClient or DocumentCollection");

        AsyncDocumentClient client = this.client;

        if(Configuration.READ_USE_NEW_CLIENTS_IN_TASK) {
            client = Program.createReadOnlyClientAsync();
        }

        double ruConsumed = 0;
        int percentage = -1;

        for (int i = 0; i < countOfDocs; i++) {

            try {
                int idx = new Random().nextInt(personCache.size());
                Person person = personCache.get(idx);

                long tic = System.currentTimeMillis();

                FeedOptions options = new FeedOptions();
                options.setPartitionKey(new PartitionKey(person.getGender()));

                FeedResponsePage<Document> queryDocResponse = client.queryDocuments(documentCollection.getSelfLink(),
                        String.format("SELECT * FROM c WHERE c.id='%s'", person.getId(), person.getGender()),
                        options).toBlocking().first();

                long tock = System.currentTimeMillis();
                stats.getElapsedTimeInMs().add((double) tock-tic);

                if((double) tock-tic > Configuration.THREASHOLD_LATENCY) {
                    log.info("Slow query with activity ID: {}" , queryDocResponse.getActivityId());
                }

                int currPercentage = (i * 100)/countOfDocs;
                if (currPercentage  % 10 == 0 && currPercentage > percentage) {
                    log.info("{}% query completed", currPercentage);
                    percentage = currPercentage;
                }
                ruConsumed += queryDocResponse.getRequestCharge();
            } catch (Exception dce) {
                log.error("[{}]Failed to query document ", taskId, dce);
                throw new RuntimeException("Failed to query document ");
            }
        }

        stats.setConsumedRUs(ruConsumed);
    }
}