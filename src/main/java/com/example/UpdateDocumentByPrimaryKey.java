package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by arun.khetarpal on 20/12/17.
 */
@AllArgsConstructor
@Slf4j
public class UpdateDocumentByPrimaryKey {
    private ArrayList<Person> personCache;
    private DocumentCollection documentCollection;

    public void doUpdate() {
        int idx = new Random().nextInt(personCache.size());
        Person person = personCache.get(idx);

        DocumentClient client = Program.createWriteClient();

        FeedOptions options = new FeedOptions();
        options.setPartitionKey(new PartitionKey(person.getGender()));

        FeedResponse<Document> queryDocResponse = client.queryDocuments(documentCollection.getSelfLink(),
                String.format("SELECT * FROM c WHERE c.id='%s'", person.getId(), person.getGender()),
                options);

        List<Document> returnedPersonDocument = queryDocResponse.getQueryIterable().toList();
        if(returnedPersonDocument.size() != 1) {
            log.error("Something must be wrong!");
        }

        Document returnedDoc = returnedPersonDocument.get(0);
        returnedDoc.set("CreditCard", "xx-xxx-xxxxxxxxx");

        RequestOptions requestOptions = new RequestOptions();

        AccessCondition condition = new AccessCondition();
        condition.setCondition(returnedDoc.getETag());
        condition.setType(AccessConditionType.IfMatch);

        requestOptions.setAccessCondition(condition);

        try {
            ResourceResponse<Document> returnedDocNew = client.replaceDocument(returnedDoc.getSelfLink(),
                    returnedDoc, requestOptions);

            log.info("Etag returned by new document {} is different from the one of old document {}",
                    returnedDocNew.getResource().getETag(),
                    returnedDoc.getETag());

        } catch(DocumentClientException dce) {
            log.error("Failed with condition {}", dce.getStatusCode());
        }

        /// TRY DOING ANOTHER UPDATE WITH OLDER DOCUMENT
        try {
            ResourceResponse<Document> returnedDocNew = client.replaceDocument(returnedDoc.getSelfLink(),
                    returnedDoc, requestOptions);

            // IF this statement gets executed, then something went wrong
            log.error("Document updated successfully?");
        } catch(DocumentClientException dce) { /* only this time we fail with 412 */
            log.info("We Failed  with condition {}", dce.getStatusCode());

        }
    }
}
