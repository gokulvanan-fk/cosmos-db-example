package com.example;

import com.google.gson.Gson;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by arun.khetarpal on 18/12/17.
 */

/**
 * Get by partition key, update a document and
 * get by seconday key
 */
@Slf4j
public class DocumentInsertWorker extends Profileable {
    private DocumentClient client;
    private DocumentCollection documentCollection;
    private Integer taskId;
    private Integer countOfDocs;
    private Statistics stats;
    private ObjectCache<Person> personObjectCache;

    private static Gson gson = new Gson();
    public DocumentInsertWorker(DocumentClient client,
            DocumentCollection collections,
            Integer taskId,
            Integer countOfDocs,
            Statistics stats,
            ObjectCache<Person> personObectCache) {
        super("DocumentInsertWorker");
        this.client = client;
        this.documentCollection = collections;
        this.taskId = taskId;
        this.countOfDocs = countOfDocs;
        this.stats = stats;
        this.personObjectCache = personObectCache;
    }

    public void run() {
        Asserts.check(client!=null && documentCollection!=null,
                "DocumentClient or DocumentCollection");

        DocumentClient client = this.client;

        if(Configuration.WRITE_USE_NEW_CLIENTS_IN_TASK) {
            client = Program.createWriteClient(ConnectionMode.DirectHttps);
        }

        double ruConsumed = 0;
        int percentage = -1;

        for (int i = 0; i < countOfDocs; i++) {
            Person person = PersonFactory.doCreate();
            String personJson = gson.toJson(person);

            personObjectCache.getCache().add(person);

            try {
                Document doc = new Document(personJson);

                long tic = System.currentTimeMillis();
                start();
                ResourceResponse<Document> createDocResponse =
                        client.createDocument(documentCollection.getSelfLink(), doc,
                                null, false);
                end();
                long tock = System.currentTimeMillis();
                stats.getElapsedTimeInMs().add((double) tock-tic);

                int currPercentage = (i * 100)/countOfDocs;
                if (currPercentage  % 10 == 0 && currPercentage > percentage) {
                    log.info("{}% completed", currPercentage);
                    percentage = currPercentage;
                }
                ruConsumed += createDocResponse.getRequestCharge();
            } catch (DocumentClientException dce) {
                error();
                log.error("[{}-{}]Failed to insert document ", dce.getActivityId(), taskId, dce);
                throw new RuntimeException("Failed to insert document ");
            }
        }

        stats.setConsumedRUs(ruConsumed);
    }
    
    
}
