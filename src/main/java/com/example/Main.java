package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.AllArgsConstructor;

import javax.naming.directory.SearchResult;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by arun.khetarpal on 31/12/17.
 */
public class Main {
    private static String SERVICE_URL = "https://flipkart.documents.azure.com:443";
    private static String DATABASE = "user";
    private static String COLLECTION = "uc2";
    private static String READ_ONLY_KEY = "Pnr2uzQqmXhJoJ8fU2ol8OsSpAPKWzFjcxmvOyMwRB06K9GbAxa0WQKHFkgZ3v6S3ztiXP2KvO1puG0HuSAsZw==";

    public static Database getDatabase(DocumentClient client) {
        FeedResponse<Database> databaseResponse =
                client.queryDatabases(String.format("SELECT * FROM root as db WHERE db.id = '%s'",
                        DATABASE), null);
        Iterator<Database> databases = databaseResponse.getQueryIterator();
        if(databases.hasNext()) {
            Database db = databases.next();
            return db;
        }
        return null;
    }

    public static DocumentCollection getCollection(DocumentClient client, Database database) {
        FeedResponse<DocumentCollection> collectionsResponse =
                client.queryCollections(database.getSelfLink(),
                        String.format("SELECT * FROM root as coll WHERE coll.id = '%s'",
                                COLLECTION), null);

        Iterator<DocumentCollection> collections = collectionsResponse.getQueryIterator();
        if(collections.hasNext()) {
            DocumentCollection collection = collections.next();
            return collection;
        }
        return null;
    }


    @AllArgsConstructor
    public class RunningParallel implements Runnable {
        DocumentClient documentClient;
        DocumentCollection documentCollection;
        boolean enableMaxParallelism;

        @Override
        public void run() {
            System.out.println("Enabled max parallelism " + enableMaxParallelism);
            for (int i = 0; i < 100; i++) {
                FeedOptions options = new FeedOptions();
                options.setEnableCrossPartitionQuery(true);
                options.setPageSize(100000);
                if(enableMaxParallelism)
                    options.setMaxDegreeOfParallelism(0);
                long tic = System.currentTimeMillis();

                FeedResponse<Document> queryDocResponse = documentClient.queryDocuments(documentCollection.getSelfLink(),
                        String.format(
                                "select * from c where c.accountId = \"preprod-cosmos-user-usersvc-cosmos-550312_test2_accId_1\""), options);
                long tock = System.currentTimeMillis();

                System.out.println(String.format(i + " " + Thread.currentThread().getName() + " Total documents %d", queryDocResponse.getQueryIterable().toList().size()));
                System.out.println(String.format(i + " " + Thread.currentThread().getName() + " Consumed RUs %f", queryDocResponse.getRequestCharge()));
                System.out.println(String.format(i + " " + Thread.currentThread().getName() + " Elapsed time to run %d ms", tock - tic));
            }
        }
    }


    public void func(DocumentClient client, DocumentCollection documentCollection, boolean enablePartion) throws Exception {
        ExecutorService writeExecutors = Executors.newFixedThreadPool(10,
                new ThreadFactory() {
                    int i=0;
                    public Thread newThread(Runnable r) {
                        Thread th = new Thread(r, "cosmos-reads-"+(i++));
                        th.setDaemon(true);
                        return th;
                    }
                });

        writeExecutors.submit(new RunningParallel(client, documentCollection, enablePartion));
        writeExecutors.awaitTermination(10, TimeUnit.MINUTES);
    }


    public static void main(String[] args) throws Exception {
        boolean enablePartition = false;
        if (args.length == 1) {
            enablePartition = true;
        }
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.setConnectionMode(ConnectionMode.Gateway);

        DocumentClient client =
                new DocumentClient(SERVICE_URL, READ_ONLY_KEY, policy, ConsistencyLevel.Session);
        Database database = getDatabase(client);
        DocumentCollection collection = getCollection(client, database);
        System.out.println("Collection " + collection.getSelfLink() + " " + " database " + database.getSelfLink());
        Main main = new Main();
        main.func(client, collection, enablePartition);
    }
}
