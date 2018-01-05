package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by arun.khetarpal on 20/12/17.
 */
@Slf4j
@AllArgsConstructor
public class DocumentReadBySecondayKey implements Runnable {
    private DocumentClient client;
    private DocumentCollection documentCollection;
    private Integer taskId;
    private Integer countOfDocs;
    private Statistics stats;
    private ArrayList<Person> personCache;

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
                Person person = personCache.get(idx);

                long tic = System.currentTimeMillis();

                FeedOptions options = new FeedOptions();
                options.setPartitionKey(new PartitionKey(person.getGender()));

                FeedResponse<Document> queryDocResponse = client.queryDocuments(documentCollection.getSelfLink(),
                        String.format("SELECT * FROM c WHERE c.favoriteNumber=%d", person.getFavoriteNumber()),
                        options);

                long tock = System.currentTimeMillis();
                stats.getElapsedTimeInMs().add((double) tock-tic);

                int cnt = 0;
                for(Person p : personCache) {
                    cnt += p.getFavoriteNumber() == person.getFavoriteNumber() &&
                            p.getGender().equals(person.getGender()) ? 1 : 0;
                }

                List<Document> docs = queryDocResponse.getQueryIterable().toList();
                if(docs.size() != cnt) {
                    log.error("Error size not matching docdb:{}, count:{} and favoriteNumber:{}",
                            docs.size(), cnt, person.getFavoriteNumber());
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
