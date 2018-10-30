package com.exlibris.dps.submissionvl;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


/**
 * Submission App for Visual Library
 * 
 * Main Class for starting and controlling the application
 * 
 * Repo: https://bitbucket.org/ethbib_bit/submissionvl/
 * 
 * @author Lars Haendler
 * 
 */
public class AppStarter {

	private static ServerSocket s;

	private static final int DEFAULT_PORT = 7777;
	private static final String APP_NAME = "submissionvl";
	private static final String VERSION_FILE = "version/version.txt";
	
	private static String VERSION = "unknown";
	private static String BUILD = "unknown";
	
	private static Logger logger = Logger.getLogger(AppStarter.class);
	
	
	/**
	 * main method to start the application
	 * 
	 * @param String[] holding all parameters passed to the application
	 */
	public static void main(String[] args) {
		
		//check number of arguments
		if(args.length < 2)
		{
			System.out.println("Two arguments are required.");
			System.exit(1);
		}
		else
		{
			File configFile = new File(System.getProperty("user.dir") + File.separator + args[0]);
			File log4jFile = new File(System.getProperty("user.dir") + File.separator + args[1]);

			//if a third value exists, use it as port
			//if not use the default port value
			int port = (args.length == 3) ? Integer.valueOf(args[2]) : DEFAULT_PORT;

			//check if config file really exists
			if(!configFile.exists())
			{
				System.out.println("config file '" + configFile.getAbsolutePath() + "' does not exist.");
				System.exit(1);
			}
			
			//check if log4j file really exists
			if(!log4jFile.exists())
			{
				System.out.println("log4j file '" + log4jFile.getAbsolutePath() + "' does not exist.");
				System.exit(1);
			}
			
			//extract version and build number
			getVersionAndBuild();
			
			//start actual initialsation
			init(args[0], args[1], port);
		}
	}
	
	
	/**
	 * Initializer for AppStarter
	 * 
	 * @param configRelativePath
	 * @param log4jRelativePath
	 * @param port
	 */
	public static void init(String configRelativePath, String log4jRelativePath, int port)
	{
		//get the correct log4j config file
		PropertyConfigurator.configure(log4jRelativePath);
		
		//version debug out
		logger.debug(APP_NAME + " v" + VERSION);
		logger.debug("config: " + configRelativePath + ", log4j: " + log4jRelativePath + ", port: " + port);
		
		//Start application
		logger.debug("Started -");

		
		//TODO: remove for productive use
		/*
		logger.debug("TestReader still active");
		TestReader test = new TestReader(configRelativePath);
		test.init();
		System.exit(0);
		*/
		
		//check that not two instance of the application run at once
		//create a pseudo server app
		try
		{
			s = new ServerSocket(port, 10, InetAddress.getLocalHost());
		}
		catch (UnknownHostException e)
		{
			logger.error("Application already running");
			logger.error(e.getMessage());
			System.exit(1);
		}
		catch (IOException e)
		{
			logger.error("Unexpected error: " + e.getMessage());
			System.exit(2);
		}

		//Start actual application
		final SubmissionSingleton subApp = SubmissionSingleton.getInstance(configRelativePath);
		subApp.init();
			
		
		//close pseudo server app 
		try
		{
			s.close();
		}
		catch (IOException e)
		{
			logger.error("Unexpected error: " + e.getMessage());
			System.exit(2);
		}
		
		logger.debug("Finished -");
	}
	
	
	/**
	 * Extract version and build number from version.txt
	 * 
	 */
	private static void getVersionAndBuild()
	{	
		//file content has to look like this:
		//  version=1.4
		//  build.date=2018-09-27
		try(Scanner fileReader = new Scanner(new File(VERSION_FILE))) {
			VERSION = fileReader.nextLine().split("=")[1];
			BUILD = fileReader.nextLine().split("=")[1];
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
