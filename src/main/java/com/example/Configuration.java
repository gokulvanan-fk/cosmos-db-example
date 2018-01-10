package com.example;

import java.util.Arrays;
import java.util.List;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
public interface Configuration {
    
    String apiEndPoint ="https://arkhetarp04.documents.azure.com:443/;AccountKey=fQToLFud0g1s9S7TnKdceiRIMeFmTm2jLm5N41Nxc2LmbMMAHP212DLHZJY9L0U6zIT3MyqM4Q4dYzzi0dPgfg==";
    String key = "fQToLFud0g1s9S7TnKdceiRIMeFmTm2jLm5N41Nxc2LmbMMAHP212DLHZJY9L0U6zIT3MyqM4Q4dYzzi0dPgfg==";
    Boolean DO_CLEANUP = false;
    Boolean USE_DIRECT_HTTPS = false;
    String MASTER_KEY = key;
    String READONLY_KEY = key;
    String SERVICE_URI = apiEndPoint;
    List<String> PREFERRED_WRITE_REGION = Arrays.asList("East US");
    List<String> PREFERRED_READ_REGION = Arrays.asList("East US");
    String DATABASE_NAME = "testdb";
    String COLLECTION_NAME = "testcollection";
    Integer COLLECTION_THROUGHPUT = 1000000;
    List<String> PARTITION_KEY_PATHS = Arrays.asList("/id");
    Integer WRITE_TASKS_IN_PARALLEL = 100;
    Integer WRITE_INSERTS_PER_TASK =  100000;
    Integer READ_TASKS_IN_PARALLEL = 100;
    Integer READ_PER_TASK =  100000;
    Integer READ_INDEX_TASKS_IN_PARALLEL = 1;
    Integer READ_INDEX_PER_TASK =  10;
    Boolean DISABLE_AUTOMATIC_INDEXING = true;
    Boolean WRITE_USE_NEW_CLIENTS_IN_TASK = false;
    Boolean READ_USE_NEW_CLIENTS_IN_TASK = false;
    List<String> INCLUDE_INDEXING_PATHS = Arrays.asList("/gender/?", "/favoriteNumber/?");
    List<String> EXCLUDE_INDEXING_PATHS = Arrays.asList("/*");
}