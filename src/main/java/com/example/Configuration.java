package com.example;

import java.util.Arrays;
import java.util.List;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
public interface Configuration {
    
    String apiEndPoint ="https://flipkart.documents.azure.com:443/;AccountKey=7KvlpnedY1uPqwTU4mEPiwx9IXDiXAJFb9Vp1Vi1VAZq68sxapQdyUWzelXIsrHldq0FS6hl4IYlnI1HRfnnWQ==";
    String key = "7KvlpnedY1uPqwTU4mEPiwx9IXDiXAJFb9Vp1Vi1VAZq68sxapQdyUWzelXIsrHldq0FS6hl4IYlnI1HRfnnWQ";
    Boolean DO_CLEANUP = false;
    String MASTER_KEY = key;
    String READONLY_KEY = key;
    String SERVICE_URI = apiEndPoint;
    List<String> PREFERRED_WRITE_REGION = Arrays.asList("East US");
    List<String> PREFERRED_READ_REGION = Arrays.asList("East US");
    String DATABASE_NAME = "testdb";
    String COLLECTION_NAME = "testcollection";
    Integer COLLECTION_THROUGHPUT = 1000000;
    List<String> PARTITION_KEY_PATHS = Arrays.asList("/gender");
    Integer WRITE_TASKS_IN_PARALLEL = 1;
    Integer WRITE_INSERTS_PER_TASK =  10;
    Integer READ_TASKS_IN_PARALLEL = 1;
    Integer READ_PER_TASK =  10;
    Integer READ_INDEX_TASKS_IN_PARALLEL = 1;
    Integer READ_INDEX_PER_TASK =  10;
    Boolean DISABLE_AUTOMATIC_INDEXING = true;
    Boolean WRITE_USE_NEW_CLIENTS_IN_TASK = false;
    Boolean READ_USE_NEW_CLIENTS_IN_TASK = false;
    List<String> INCLUDE_INDEXING_PATHS = Arrays.asList("/gender/?", "/favoriteNumber/?");
    List<String> EXCLUDE_INDEXING_PATHS = Arrays.asList("/*");
}