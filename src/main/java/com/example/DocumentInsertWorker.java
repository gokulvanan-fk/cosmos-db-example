package com.example;

import com.google.gson.Gson;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import com.microsoft.azure.documentdb.*;
import jdk.nashorn.internal.runtime.RecompilableScriptFunctionData;
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
@AllArgsConstructor
public class DocumentInsertWorker implements Runnable {
    private DocumentClient client;
    private DocumentCollection documentCollection;
    private Integer taskId;
    private Integer countOfDocs;
    private Statistics stats;
    private ObjectCache<Document> personObjectCache;

    private static Gson gson = new Gson();

    public void run() {
        Asserts.check(client!=null && documentCollection!=null,
                "DocumentClient or DocumentCollection");

        DocumentClient client = this.client;

        if(Configuration.WRITE_USE_NEW_CLIENTS_IN_TASK) {
            client = Program.createWriteClient();
        }

        double ruConsumed = 0;
        int percentage = -1;

        for (int i = 0; i < countOfDocs; i++) {
            Person person = PersonFactory.doCreate();
            String personJson = gson.toJson(person);

            try {
                Document doc = new Document(personJson);

                long tic = System.currentTimeMillis();
                ResourceResponse<Document> createDocResponse =
                        client.createDocument(documentCollection.getSelfLink(), doc,
                                null, false);

                long tock = System.currentTimeMillis();
                stats.getElapsedTimeInMs().add((double) tock-tic);

                personObjectCache.getCache().add(createDocResponse.getResource());

                int currPercentage = (i * 100)/countOfDocs;
                if (currPercentage  % 10 == 0 && currPercentage > percentage) {
                    log.info("{}% completed", currPercentage);
                    percentage = currPercentage;
                }
                ruConsumed += createDocResponse.getRequestCharge();
            } catch (DocumentClientException dce) {
                log.error("[{}-{}]Failed to insert document ", dce.getActivityId(), taskId, dce);
                throw new RuntimeException("Failed to insert document ");
            }
        }

        stats.setConsumedRUs(ruConsumed);
    }
}
