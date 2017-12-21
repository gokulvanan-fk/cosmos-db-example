package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by arun.khetarpal on 20/12/17.
 */
@AllArgsConstructor
@Slf4j
@Getter
public class ChangeFeedObserver implements Runnable {
    private DocumentClient client;
    private String partitionKeyRange;
    private DocumentCollection documentCollection;
    private ArrayList<Document> documents;

    @Override
    public void run() {
        ChangeFeedOptions changeFeedOptions = new ChangeFeedOptions();
        changeFeedOptions.setStartFromBeginning(true);


        FeedResponse<Document> changeFeedResponse = client.queryDocumentChangeFeed(documentCollection.getSelfLink(),
                changeFeedOptions);

        while(true)
        {
            Iterator<Document> changeFeedIterator = changeFeedResponse.getQueryIterator();

            while(changeFeedIterator.hasNext()) {
                documents.add(changeFeedIterator.next());
            }

            String requestContinuation = changeFeedResponse.getResponseContinuation();

            try {
                log.info("No more document in feed till now, already read {}. Sleeping for 10 sec", documents.size());
                Thread.sleep(10000);
            } catch(InterruptedException ie) {
                log.error("", ie);
            }
            changeFeedOptions = new ChangeFeedOptions();
            changeFeedOptions.setRequestContinuation(requestContinuation);

            changeFeedResponse = client.queryDocumentChangeFeed(documentCollection.getSelfLink(),
                    changeFeedOptions);
        }
    }
}
