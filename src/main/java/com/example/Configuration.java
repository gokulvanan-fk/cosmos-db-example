package com.example;

import java.util.Arrays;
import java.util.List;

/**
 * Created by arun.khetarpal on 18/12/17.
 */
public interface Configuration {
    Boolean DO_CLEANUP = true;
    String MASTER_KEY = // "Ubw4wmjUCKdXtoRT8Vd1koKek3hXwHm3luKvg4BBXvzzkpjYwBVVevNwfdDt3kKgiDDX6kzbqpcm47YPRyBchg==";
            "E0wSdcHSY9QQVMMy0pBQC7udPd6yYexNARXGt2erH9JRTS6uh1FEjKLiYBAQtZTmMFB1yzMMwVjZzCi2pN4www==";
    String READONLY_KEY = //"QFImCcosiQSe7GVereub6gdDFBXUYGKdtnBjfPICo771YYIghDZD2IhEMvcaM0iyLOkaJ418Bpx1gCetrxCs9w==";
            "9ketm7FSZ32seaD9DFgpPdpvXBTJXWeU9kZlnHtu49fmTB4i0O8Qbsc6jKq0y3qAJEgFfoOaTfcVk9Ewzpa8YQ==";
    String SERVICE_URI = // "https://akhetarp02.documents.azure.com:443/";
            "https://arkhetar03.documents.azure.com:443/";
    List<String> PREFERRED_WRITE_REGION = Arrays.asList("East US");
    List<String> PREFERRED_READ_REGION = Arrays.asList("East US");
    String DATABASE_NAME = "testdb";
    String COLLECTION_NAME = "testcollection";
    Integer COLLECTION_THROUGHPUT = 1000000;
    List<String> PARTITION_KEY_PATHS = Arrays.asList("/gender");
    Integer WRITE_TASKS_IN_PARALLEL = 50;
    Integer WRITE_INSERTS_PER_TASK =  10000;
    Integer READ_TASKS_IN_PARALLEL = 50;
    Integer READ_PER_TASK =  10000;
    Boolean DISABLE_AUTOMATIC_INDEXING = true;
    Boolean WRITE_USE_NEW_CLIENTS_IN_TASK = true;
    Boolean READ_USE_NEW_CLIENTS_IN_TASK = false;
    List<String> INCLUDE_INDEXING_PATHS = Arrays.asList("/gender/?", "/favoriteNumber/?");
    List<String> EXCLUDE_INDEXING_PATHS = Arrays.asList("/*");
    Boolean USE_GATEWAY_MODE = false;
    Integer THREASHOLD_LATENCY = 100;
}