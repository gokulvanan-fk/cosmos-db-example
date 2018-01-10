package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.util.ArrayList;
import java.util.Random;

@Slf4j
public class DocumentReadByPrimaryKey extends Profileable {
    private DocumentClient client;
    private DocumentCollection documentCollection;
    private Integer taskId;
    private Integer countOfDocs;
    private Statistics stats;
    private ArrayList<Person> personCache;

    
    public DocumentReadByPrimaryKey(DocumentClient client,
            DocumentCollection collections,
            Integer taskId,
            Integer countOfDocs,
            Statistics stats,
            ArrayList<Person> personObectCache) {
        super("DocumentReadByPrimaryKey");
        this.client = client;
        this.documentCollection = collections;
        this.taskId = taskId;
        this.countOfDocs = countOfDocs;
        this.stats = stats;
        this.personCache = personObectCache;
    }

    public void run() {
        Asserts.check(client!=null && documentCollection!=null,
                "DocumentClient or DocumentCollection");

        DocumentClient client = this.client;

        if(Configuration.READ_USE_NEW_CLIENTS_IN_TASK) {
            client = Program.createReadOnlyClient(ConnectionMode.DirectHttps);
        }

        double ruConsumed = 0;
        int percentage = -1;

        for (int i = 0; i < countOfDocs; i++) {

            try {
                int idx = new Random().nextInt(personCache.size());
                Person person = personCache.get(idx);

                long tic = System.currentTimeMillis();

                FeedOptions options = new FeedOptions();
                options.setPartitionKey(new PartitionKey(person.getId()));
                start();
                FeedResponse<Document> queryDocResponse = client.queryDocuments(documentCollection.getSelfLink(),
                        String.format("SELECT * FROM c WHERE c.id='%s'", person.getId()),
                        options);
                end();
                long tock = System.currentTimeMillis();
                stats.getElapsedTimeInMs().add((double) tock-tic);

                int currPercentage = (i * 100)/countOfDocs;
                if (currPercentage  % 10 == 0 && currPercentage > percentage) {
                    log.info("{}% query completed", currPercentage);
                    percentage = currPercentage;
                }
                ruConsumed += queryDocResponse.getRequestCharge();
            } catch (Exception dce) {
                error();
                log.error("[{}]Failed to query document ", taskId, dce);
                throw new RuntimeException("Failed to query document ");
            }
        }

        stats.setConsumedRUs(ruConsumed);
    }
}