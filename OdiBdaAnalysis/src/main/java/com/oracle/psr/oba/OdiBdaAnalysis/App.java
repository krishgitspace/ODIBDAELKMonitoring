package com.oracle.psr.oba.OdiBdaAnalysis;
/*
* Author KRMITRA. 
*/
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.io.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
* Application Main . Writes ODI BDA PSR Runs Results Summary in JSON format .Using Parallel Map Reduce Batch Framework .
*/

public class App 
{
	private static Logger appLogger = LoggerFactory.getLogger(OdiMapper.class);
   public static void main( String[] args ) throws IOException, InterruptedException,Exception
   {
   	
   	
   	InitService.loadProperties();
   	
   	 System.out.println("hdfs service prop " + InitService.PROPERTIES.getProperty(InitService.PROPERTY_NODE_SERVER_TYPE));
   	String destHDFSPath = InitService.PROPERTIES.getProperty(InitService.PROPERTY_HDFS_IN_PATH);
		String destHDFSOutPath = InitService.PROPERTIES.getProperty(InitService.PROPERTY_HDFS_OUT_PATH);
		
//		System.out.println("classPath :" + System.getProperty("java.class.path"));
		
   	String hdfsURI = InitService.PROPERTIES.getProperty(InitService.PROPERTY_HDFS_URI);
   	System.out.println("HDFS URI and absolute in path: " + hdfsURI + destHDFSPath);
   	
   	  	
   	try{
   		
   	Configuration hdfs_conf = new Configuration();
		
		
	
   	InitService.hdfsInputPath = new Path(hdfsURI + destHDFSPath );
   	
   	InitService.hdfsOutputPath = new Path(hdfsURI + destHDFSOutPath );
   	
   	
   	hdfs_conf.set("fs.hdfs.impl",org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
   	hdfs_conf.set("fs.file.impl",org.apache.hadoop.fs.LocalFileSystem.class.getName());
   	
   	//hdfs_conf.set("mapred.child.java.opts", "-Xmx2048m");
   	//hdfs_conf.set("mapreduce.job.reduce.memory.mb", "8194m");
   	//hdfs_conf.set("mapreduce.reduce.java.opts", "8194m");
   	
       	
   	Job reportsPublisherjob = Job.getInstance(hdfs_conf, "DI JSON Out.");
   	    	
   	
   	System.out.println(InitService.hdfsInputPath);
   	
   	FileInputFormat.addInputPath(reportsPublisherjob, InitService.hdfsInputPath);
   	FileOutputFormat.setOutputPath(reportsPublisherjob, InitService.hdfsOutputPath);

   	reportsPublisherjob.setJarByClass(App.class);
   	reportsPublisherjob.setJobName(" JSON Summary Reports Publisher .");
   	
   	reportsPublisherjob.setMapperClass(OdiMapper.class);
   	reportsPublisherjob.setReducerClass(OdiReducer.class);
   	
   	reportsPublisherjob.setMapOutputKeyClass(Text.class);
 
   	reportsPublisherjob.setMapOutputValueClass(Text.class);
   	/*
   	 * Set your customized file output format .For us we will use JsonOutputFormat,which extends HDFS FileOutputFormat class.
   	 */
   	reportsPublisherjob.setOutputFormatClass(JsonOutputFormat.class);
   	
   	reportsPublisherjob.setOutputValueClass(Text.class);
   	reportsPublisherjob.setOutputValueClass(ArrayWritable.class);
   	
   	reportsPublisherjob.setNumReduceTasks(48);
   	
   	System.exit(reportsPublisherjob.waitForCompletion(true) ?  0 : 1);
   	   	
      }
   	catch (AccessControlException ace )
   	{
   		appLogger.error(ace.getMessage());
   	}
   
   }       
}
