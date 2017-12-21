package com.example;

import java.util.Arrays;
import java.util.List;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
public interface Configuration {
    Boolean DO_CLEANUP = false;
    String MASTER_KEY = "";
    String READONLY_KEY = "";
    String SERVICE_URI = "";
    List<String> PREFERRED_WRITE_REGION = Arrays.asList("East US");
    List<String> PREFERRED_READ_REGION = Arrays.asList("East US");
    String DATABASE_NAME = "testdb";
    String COLLECTION_NAME = "testcollection";
    Integer COLLECTION_THROUGHPUT = 1000000;
    List<String> PARTITION_KEY_PATHS = Arrays.asList("/gender");
    Integer WRITE_TASKS_IN_PARALLEL = 50;
    Integer WRITE_INSERTS_PER_TASK =  100000;
    Integer READ_TASKS_IN_PARALLEL = 40;
    Integer READ_PER_TASK =  10000;
    Boolean DISABLE_AUTOMATIC_INDEXING = true;
    Boolean WRITE_USE_NEW_CLIENTS_IN_TASK = false;
    Boolean READ_USE_NEW_CLIENTS_IN_TASK = false;
    List<String> INCLUDE_INDEXING_PATHS = Arrays.asList("/gender/?", "/favoriteNumber/?");
    List<String> EXCLUDE_INDEXING_PATHS = Arrays.asList("/*");
}