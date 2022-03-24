package com.abdullahteke.hpna.main;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.abdullahteke.hpna.controller.LogManager;
import com.abdullahteke.hpna.controller.SimpleFileWriter;
import com.rendition.api.RenditionAPIException;
import com.rendition.api.Result;
import com.rendition.api.Session;

public class ConfigBackup {
	
	String user="";
	String pass="";
	String host="";
	String sharePath="";
	String backupPath="";
	String[] deviceGroups=null;
	
	Properties props=null;


	public ConfigBackup() {
		
		LogManager.getInstance().getLogger().info("============== Starting Config Backup To File System =================");
		props= new Properties();

		loadConfig();
	
		backupConfigFiles();
		
		LogManager.getInstance().getLogger().info("============== Finishing Config Backup To File System =================");
	}		
		
			
	private void backupConfigFiles() {
		setBackupPath();
		LogManager.getInstance().getLogger().info("Backup Directory Has Been Prepared");

		String st="";
		int deviceID=-1;
		String hostName="";
		LogManager.getInstance().getLogger().info("Starting Backup Of Config File");

		for (int i=0;i<deviceGroups.length;i++){
			st=deviceGroups[i].trim();
			
			Session session=new Session();
			
			try {
				session.open(user, pass,host);
				Result res=session.exec("list device -group "+st);
				ResultSet set=res.getResultSet();
				LogManager.getInstance().getLogger().info("Getting Device List In "+st+" Group");

				while (set.next()){
					deviceID=set.getInt("DeviceID");
					hostName= set.getString("HostName");
					
					if (deviceID>-1 && hostName.length() > 0){
						backupConfigFile(deviceID, hostName,st);
					}
					
					deviceID=-1;
					
				}
				
			} catch (RenditionAPIException e) {
				LogManager.getInstance().getLogger().error("Error Getting Device List In : "+st+" Group"+e.getMessage());
			} catch (SQLException e) {
				LogManager.getInstance().getLogger().error("Error Getting Device List In : "+st+" Group"+e.getMessage());
			}
			
			session.close();
			
			
		}
		
	}


	private void setBackupPath() {
		
		Date now= new Date();
		
		SimpleDateFormat dt = new SimpleDateFormat("MMM-yyyy"); 
	
		String dirName= dt.format(now);
		System.out.println(dirName);
		
		File f = new File("backup/"+dirName);
		
		if (!f.exists()) {
			f.mkdir();
		}
		
		SimpleDateFormat dt2 = new SimpleDateFormat("yyyy-MM-dd");
		String subDirName=dt2.format(now);
		
		File f2 = new File("backup/"+dirName+"/"+subDirName);
		
		if (!f2.exists()) {
			f2.mkdir();
		}
		
		for (int i=0;i<deviceGroups.length;i++){
			File tmpFile= new File("backup/"+dirName+"/"+subDirName+"/"+deviceGroups[i].trim());
			if (!tmpFile.exists()){
				tmpFile.mkdir();
			}
		}
		
		this.backupPath=sharePath+"/"+dirName+"/"+subDirName;
	
	}

	private void backupConfigFile(int deviceID,String hostName,String groupName) {
		SimpleFileWriter writer = SimpleFileWriter.openFileForWriting(backupPath+"/"+groupName+"/"+hostName+".cfg");
		
		Session session=new Session();
		
		try {
			session.open(user, pass,host);
		} catch (RenditionAPIException e) {
			LogManager.getInstance().getLogger().error("Error: For Accessing Config File Of "+hostName+" "+ e.getMessage());
		}
		
		Result result=null;
		String st="";
		try {
			result = session.exec("show device config -deviceid "+deviceID);
			st=result.getText();
			writer.println(st);
			LogManager.getInstance().getLogger().info("Configuration for "+hostName+" Backed Up Successfully.");
			
		} catch (RenditionAPIException e1) {
			LogManager.getInstance().getLogger().error("Error: For Accessing Config File Of "+hostName+" "+ e1.getMessage());

		}
	
	

		writer.close();
		session.close();
		
		
	}
	
	private void loadConfig() {
		
		FileInputStream in;
		
		try {
			in = new FileInputStream("config/config.properties");

			props.load(in);		

			this.user=props.getProperty("user");
			this.pass=props.getProperty("pass");
			this.host=props.getProperty("host");
			this.sharePath= props.getProperty("sharePath");
			this.deviceGroups=props.getProperty("deviceGroups").split(";");


			in.close();
			LogManager.getInstance().getLogger().info("Configuration File Loaded Successfully");

		} catch (FileNotFoundException e) {	
			LogManager.getInstance().getLogger().error("Configuration File Could Not Find"+e.getMessage());
			
		} catch (IOException e) {
			LogManager.getInstance().getLogger().error("Configuration File Could Not Read: "+e.getMessage());
		} 
	}

	public static void main(String[] args) {

		new ConfigBackup();

	}

}
