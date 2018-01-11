package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import com.microsoft.azure.documentdb.ConnectionMode;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import org.apache.commons.lang3.StringUtils;

public class Config implements Configuration{

    final Properties props;
    public Config(String path) throws IOException{
        this.props = new Properties();
        try(FileReader reader = new FileReader(path)){
            try(BufferedReader br = new BufferedReader(reader)){
                props.load(br);
            }
        }
    }
    
    public int getWriteConcc(){
        String val = props.getProperty("PUT_CONCC");
        return (val == null) ? WRITE_TASKS_IN_PARALLEL : Integer.parseInt(val);
    }
    
    public int getWriteRequestSize(){
        String val = props.getProperty("PUT_SIZE");
        return (val == null) ? WRITE_INSERTS_PER_TASK : Integer.parseInt(val);
    }
    
    public int getReadConcc(){
        String val = props.getProperty("GET_CONCC");
        return (val == null) ? READ_TASKS_IN_PARALLEL : Integer.parseInt(val);
    }
    
    
    public int getReadRequestSize(){
        String val = props.getProperty("GET_SIZE");
        return (val == null) ? READ_PER_TASK : Integer.parseInt(val);
    }
    
    public int getReadIndexConcc(){
        String val = props.getProperty("GET_INDEX_CONCC");
        return (val == null) ? READ_INDEX_TASKS_IN_PARALLEL : Integer.parseInt(val);
    }
    
    public int getReadIndexRequestSize(){
        String val = props.getProperty("GET_INDEX_SIZE");
        return (val == null) ? READ_INDEX_PER_TASK : Integer.parseInt(val);
    }

    public boolean isCrossPartition() {
        String val = props.getProperty("CROSS_PARTITION");
        return val != null && "true".equals(StringUtils.trim(val));
    }

    public ConnectionMode getPolicy() {
        String val = props.getProperty("USE_DIRECT_HTTPS");
        return val != null && "true".equals(StringUtils.trim(val)) ?
                ConnectionMode.DirectHttps : ConnectionMode.Gateway;
    }
}
