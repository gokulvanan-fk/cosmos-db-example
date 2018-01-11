package com.example;

import com.microsoft.azure.documentdb.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Level;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
@Slf4j
public class Program {

    public static DocumentClient createWriteClient(@NonNull ConnectionMode connectionMode) {
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.setConnectionMode(connectionMode);
        policy.setPreferredLocations(Configuration.PREFERRED_WRITE_REGION);

        return createClient(Configuration.MASTER_KEY, policy);
    }

    public static DocumentClient createReadOnlyClient(@NonNull ConnectionMode connectionMode) {
        ConnectionPolicy policy = new ConnectionPolicy();
        policy.setConnectionMode(connectionMode);
        policy.setPreferredLocations(Configuration.PREFERRED_READ_REGION);

        return createClient(Configuration.READONLY_KEY, policy);
    }

    public static DocumentClient createClient(String key, ConnectionPolicy policy) {
        return new DocumentClient(Configuration.SERVICE_URI, key, policy, null);
    }

    public static Database createDatabaseIfNotExists(@NonNull DocumentClient documentClient) {
        FeedResponse<Database> databaseResponse =
                documentClient.queryDatabases(String.format("SELECT * FROM root as db WHERE db.id = '%s'",
                        Configuration.DATABASE_NAME), null);
        Iterator<Database> databases = databaseResponse.getQueryIterator();
        if(databases.hasNext()) {
            Database db = databases.next();
            log.info("Database with id '{}' already exists", db.getId());
            return db;
        }

        Database database = new Database();
        database.setId(Configuration.DATABASE_NAME);

        try {
            ResourceResponse<Database> databaseCreateResponse = documentClient.createDatabase(database,
                    null);
            log.info("[{}] Creating new database RU {} ", databaseCreateResponse.getActivityId(),
                    databaseCreateResponse.getRequestCharge());
            return databaseCreateResponse.getResource();
        } catch (DocumentClientException dc) {
            log.error("[{}] Failed to create database. Status {}", dc.getActivityId(), dc.getStatusCode());
            throw new RuntimeException("Database create failed");
        }
    }

    public static void deleteDatabase(@NonNull DocumentClient documentClient) {
        FeedResponse<Database> databaseResponse =
                documentClient.queryDatabases(String.format("SELECT * FROM root as db WHERE db.id = '%s'",
                        Configuration.DATABASE_NAME), null);
        Iterator<Database> databases = databaseResponse.getQueryIterator();
        Database database = null;
        boolean exists = false;

        if(databases.hasNext()) {
            exists = true;
            database = databases.next();
        }

        if(exists)
        {
            log.info("Database exists, cleaning it up");
            try {
                ResourceResponse<Database> databaseDeleteResponse =
                        documentClient.deleteDatabase(database.getSelfLink(), null);
            } catch(DocumentClientException dce) {
                log.error("[{}]Failed to delete database", dce.getActivityId(), dce);
                throw new RuntimeException("Database deletion failed");
            }
        }
    }

    public static DocumentCollection createPartitionedCollectionIfNotExists(@NonNull DocumentClient documentClient,
                                                                            @NonNull Database database) {
        FeedResponse<DocumentCollection> collectionsResponse =
                documentClient.queryCollections(database.getSelfLink(),
                        String.format("SELECT * FROM root as coll WHERE coll.id = '%s'",
                                Configuration.COLLECTION_NAME), null);

        Iterator<DocumentCollection> collections = collectionsResponse.getQueryIterator();
        if(collections.hasNext()) {
            DocumentCollection collection = collections.next();
            log.info("Collection with id '{}' already exists", collection.getId());
            return collection;
        }

        try {
            DocumentCollection collection = new DocumentCollection();
            collection.setId(Configuration.COLLECTION_NAME);

            PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
            partitionKeyDefinition.setPaths(Configuration.PARTITION_KEY_PATHS);
            collection.setPartitionKey(partitionKeyDefinition);

            if(Configuration.DISABLE_AUTOMATIC_INDEXING) {
                IndexingPolicy indexingPolicy = new IndexingPolicy();
                indexingPolicy.setIndexingMode(IndexingMode.Consistent);

                List<IncludedPath> includedPaths = new ArrayList<>();
                for(String path : Configuration.INCLUDE_INDEXING_PATHS) {
                    if(path.equals("")) continue;
                    IncludedPath includedPath = new IncludedPath();
                    includedPath.setPath(path);
                    includedPaths.add(includedPath);
                }
                if(!includedPaths.isEmpty()) {
                    indexingPolicy.setIncludedPaths(includedPaths);
                }

                List<ExcludedPath> excludedPaths = new ArrayList<>();
                for(String path : Configuration.EXCLUDE_INDEXING_PATHS) {
                    if(path.equals("")) continue;
                    ExcludedPath excludedPath = new ExcludedPath();
                    excludedPath.setPath(path);
                    excludedPaths.add(excludedPath);
                }
                if(!excludedPaths.isEmpty()) {
                    indexingPolicy.setExcludedPaths(excludedPaths);
                }

                collection.setIndexingPolicy(indexingPolicy);
            }

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.setOfferThroughput(Configuration.COLLECTION_THROUGHPUT);

            ResourceResponse<DocumentCollection> collectionCreateResponse =
                    documentClient.createCollection(database.getSelfLink(), collection, requestOptions);

            log.info("[{}] Creating new collection RU {} and collection size Quota {} ",
                    collectionCreateResponse.getActivityId(),
                    collectionCreateResponse.getRequestCharge(),
                    collectionCreateResponse.getCollectionSizeQuota());

            return collectionCreateResponse.getResource();
        } catch (DocumentClientException dc) {
            log.error("[{}] Failed to create collection. Status {} ", dc.getActivityId(), dc.getStatusCode(), dc);
            throw new RuntimeException("Collection create failed");
        }
    }

    public static ObjectCache<Person> runInsertBenchmark(DocumentClient client,Config config, boolean drillDownOnSummary) {
        log.info("Initializing Insertion benchmarks");

//
        Database database = createDatabaseIfNotExists(client);
        log.info("Initializing database with rid {}", database.getSelfLink());
        DocumentCollection documentCollection = createPartitionedCollectionIfNotExists(client, database);
        log.info("Initializing collection with rid {}", documentCollection.getSelfLink());

        ExecutorService writeExecutors = Executors.newFixedThreadPool(config.getWriteConcc(),
                new ThreadFactory() {
                    int i=0;
                    public Thread newThread(Runnable r) {
                        Thread th = new Thread(r, "cosmos-inserts-"+(i++));
                        th.setDaemon(true);
                        return th;
                    }
                });

        Statistics[] statistics = new Statistics[config.getWriteConcc()];
        for(int i = 0; i < statistics.length; i++)
            statistics[i] = new Statistics();

        ObjectCache<Person> personObjectCache = new ObjectCache<>();

        for (int i = 0; i < config.getWriteConcc(); i++) {
            writeExecutors.submit(new DocumentInsertWorker(client, documentCollection, i,
                    config.getWriteRequestSize(), statistics[i], personObjectCache));
        }

        writeExecutors.shutdown();

        try {
            writeExecutors.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception ex) {
            log.error("ExecutorService::awaitTermination ", ex);
        }

        Statistics summaryStats = new Statistics();
        summaryStats.setDELIMITER("\n");
        for (int i = 0; i < config.getWriteConcc(); i++) {
            summaryStats.setConsumedRUs(statistics[i].getConsumedRUs() + summaryStats.getConsumedRUs());
            summaryStats.getElapsedTimeInMs().addAll(statistics[i].getElapsedTimeInMs());
        }

        if(drillDownOnSummary) {
            for (int i = 0; i < config.getWriteConcc(); i++) {
                log.info("Task with id: {} Stats: {}", i, statistics[i]);
            }
        }

        log.info("Insert Summary stats: \n{}", summaryStats);

        return personObjectCache;
    }

    public static void runQueryByPrimaryKey(DocumentClient client, Config config, ArrayList<Person> personCache, boolean drillDownOnSummary) {
        log.info("Initializing QueryByPrimaryKey benchmarks");

        Database database = createDatabaseIfNotExists(client);
        log.info("Initializing database with rid {}", database.getSelfLink());
        DocumentCollection documentCollection = createPartitionedCollectionIfNotExists(client, database);
        log.info("Initializing collection with rid {}", documentCollection.getSelfLink());

        ExecutorService readExecutors = Executors.newFixedThreadPool(config.getReadConcc(),
                new ThreadFactory() {
                    int i=0;
                    public Thread newThread(Runnable r) {
                        Thread th = new Thread(r, "cosmos-read-pk-"+(i++));
                        th.setDaemon(true);
                        return th;
                    }
                });

        Statistics[] statistics = new Statistics[config.getReadConcc()];
        for(int i = 0; i < statistics.length; i++)
            statistics[i] = new Statistics();

        for (int i = 0; i < config.getReadConcc(); i++) {
            readExecutors.submit(new DocumentReadByPrimaryKey(client,documentCollection, i,
                    config.getReadRequestSize(), statistics[i], personCache));
        }

        readExecutors.shutdown();

        try {
            readExecutors.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception ex) {
            log.error("ExecutorService::awaitTermination ", ex);
        }

        Statistics summaryStats = new Statistics();
        summaryStats.setDELIMITER("\n");
        for (int i = 0; i < config.getReadConcc(); i++) {
            summaryStats.setConsumedRUs(statistics[i].getConsumedRUs() + summaryStats.getConsumedRUs());
            summaryStats.getElapsedTimeInMs().addAll(statistics[i].getElapsedTimeInMs());
        }

        if(drillDownOnSummary) {
            for (int i = 0; i < config.getReadConcc(); i++) {
                log.info("Task with id: {} Stats: {}", i, statistics[i]);
            }
        }

        log.info("Read by PK Summary stats: \n{}", summaryStats);
    }

    public static void runQueryBySecondayKey(DocumentClient client, Config config, ArrayList<Person> personCache, boolean drillDownOnSummary) {
        log.info("Initializing QueryBySecondayKey benchmarks");

        Database database = createDatabaseIfNotExists(client);
        log.info("Initializing database with rid {}", database.getSelfLink());
        DocumentCollection documentCollection = createPartitionedCollectionIfNotExists(client, database);
        log.info("Initializing collection with rid {}", documentCollection.getSelfLink());

        boolean crossPartition = config.isCrossPartition();
        ExecutorService readExecutors = Executors.newFixedThreadPool(config.getReadIndexConcc(),
                new ThreadFactory() {
                    int i=0;
                    public Thread newThread(Runnable r) {
                        Thread th = new Thread(r, "cosmos-read-index-sk-"+(i++));
                        th.setDaemon(true);
                        return th;
                    }
                });

        Statistics[] statistics = new Statistics[config.getReadIndexConcc()];
        for(int i = 0; i < statistics.length; i++)
            statistics[i] = new Statistics();

        for (int i = 0; i < config.getReadIndexConcc(); i++) {
            readExecutors.submit(new DocumentReadBySecondayKey(client, documentCollection,
                    crossPartition, i,
                    config.getReadIndexRequestSize(), statistics[i], personCache));
        }

        readExecutors.shutdown();

        try {
            readExecutors.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception ex) {
            log.error("ExecutorService::awaitTermination ", ex);
        }

        Statistics summaryStats = new Statistics();
        summaryStats.setDELIMITER("\n");
        for (int i = 0; i < config.getReadIndexConcc(); i++) {
            summaryStats.setConsumedRUs(statistics[i].getConsumedRUs() + summaryStats.getConsumedRUs());
            summaryStats.getElapsedTimeInMs().addAll(statistics[i].getElapsedTimeInMs());
        }

        if(drillDownOnSummary) {
            for (int i = 0; i < config.getReadIndexConcc(); i++) {
                log.info("Task with id: {} Stats: {}", i, statistics[i]);
            }
        }

        log.info("Read by SK Summary stats: \n{}", summaryStats);
    }

    public static void runUpdate(DocumentClient client, ArrayList<Person> personCache)
    {
        log.info("Initializing QueryBySecondayKey benchmarks");

        Database database = createDatabaseIfNotExists(client);
        log.info("Initializing database with rid {}", database.getSelfLink());
        DocumentCollection documentCollection = createPartitionedCollectionIfNotExists(client, database);
        log.info("Initializing collection with rid {}", documentCollection.getSelfLink());


        new UpdateDocumentByPrimaryKey(personCache, documentCollection).doUpdate();

    }

    public static void runChangeFeedObserver(DocumentClient client, ExecutorService feedExecutors)
    {
        log.info("Initializing change feed observer");

        Database database = createDatabaseIfNotExists(client);
        log.info("Initializing database with rid {}", database.getSelfLink());
        DocumentCollection documentCollection = createPartitionedCollectionIfNotExists(client, database);
        log.info("Initializing collection with rid {}", documentCollection.getSelfLink());

        ArrayList<Document> docs = new ArrayList<>();
        feedExecutors.submit(new ChangeFeedObserver(client, "", documentCollection, docs));
    }

    public static void main(String[] args) throws Exception {
        // do a dirty setup for the logger
        org.apache.log4j.BasicConfigurator.configure();
        org.apache.log4j.LogManager.getRootLogger().setLevel(Level.INFO);

        /*
        ExecutorService feedExecutors = Executors.newCachedThreadPool(new ThreadFactory() {
            int i=0;
            public Thread newThread(Runnable r) {
                Thread th = new Thread(r, "cosmos-change-feed-"+(i++));
                th.setDaemon(true);
                return th;
            }
        });
        */
        
        Config config = new Config(args[0]);
        Profileable.startReporter(args[1]);
        DocumentClient client = createWriteClient(config.getPolicy());


        if(Configuration.DO_CLEANUP) //start with a clean slate
        {
            log.info("Doing a precheck for cleanup");
            deleteDatabase(client);
        }

        //runChangeFeedObserver(client, feedExecutors);

        ObjectCache<Person> insertedDocs = Program.runInsertBenchmark(client,config, false);
        ArrayList<Person> personArrayList = new ArrayList<>();
        for(Person p : insertedDocs.getCache())
            personArrayList.add(p);

        Program.runQueryByPrimaryKey(client,config, personArrayList, true);
        Program.runQueryBySecondayKey(client,config, personArrayList, true);
//        Program.runUpdate(client, personArrayList);

        /*try {
            feedExecutors.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception ex) {
            log.error("Stopped feed ", ex);
        }*/
        Profileable.stopReporter();
    }
}
