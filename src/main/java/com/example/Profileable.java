package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public abstract class Profileable implements Runnable{

	private static final MetricRegistry metrics = new MetricRegistry();
	private static ConsoleReporter reporter;
	private static PrintStream printStream;
	private final String name;
	private Timer timer;
	
	public Profileable(String name){
		this.name = name;
	}

	
	private long st;
	
	public void start() {
		this.timer = metrics.timer(name);
		this.st = System.currentTimeMillis();
	}
	
	public void error(){
	    metrics.meter(name+"_failure").mark();
	}
	
	public void end(){
	    timer.update(System.currentTimeMillis()-st, TimeUnit.MILLISECONDS);
	}

	public synchronized static void  startReporter(String filePath) throws FileNotFoundException{
		printStream = new PrintStream(new File(filePath));
		reporter = ConsoleReporter.forRegistry(metrics)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.outputTo(printStream)
				.build();
		reporter.start(10, TimeUnit.SECONDS);
	}


	public synchronized static void stopReporter(){
		reporter.stop();
		printStream.close();
	}

}
