package com.abdullahteke.hpna.controller;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



public class LogManager {
	
	private static LogManager instance;
	private Logger logger;	
	
	public static void setInstance(LogManager instance) {
		LogManager.instance = instance;
	}

	public static LogManager getInstance() {
		
		if (instance==null){
			instance=new LogManager();
		}
		return instance;
	}
	

	public LogManager() {
		
		logger= Logger.getLogger("Logger");
		PropertyConfigurator.configure("config/log.properties");
		
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	
}
