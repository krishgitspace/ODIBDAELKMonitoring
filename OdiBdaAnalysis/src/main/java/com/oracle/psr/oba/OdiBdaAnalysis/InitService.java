package com.oracle.psr.oba.OdiBdaAnalysis;
/*
* Author KRMITRA. 
*/

import java.io.InputStream;

import java.io.IOException;
import java.util.Properties;

import org.apache.hadoop.fs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* HDFS initialize connection properties class.
*/

public abstract class InitService
{
	public static final Properties PROPERTIES = new Properties();

	private static final String PROPERTIES_FILE_NAME = "serverSide.properties";
	/* HDFS Properties **/
	public static final String PROPERTY_HDFS_URI = "hdfs.property.Uri";
	public static final String PROPERTY_HDFS_IN_PATH  = "hdfs.property.path.reportsInputLoc";
	public static final String PROPERTY_HDFS_OUT_PATH  = "hdfs.property.path.OutputData";
	/* NODE Properties **/
	public static final String PROPERTY_NODE_SERVER_TYPE  = "node.property.server.type";
	public static final String PROPERTY_NODE_PROC_TYPE  = "node.property.processor.type";
	public static final String PROPERTY_NODE_TOTAL_CORES  = "node.property.number_Of_CPU_cores";
	public static final String PROPERTY_NODE_MEM_SIZE  = "node.property.GB_Of_Memory";
	public static final String PROPERTY_OS_LINUX_VERSION  = "os.property.LINUX.version";
	public static final String PROPERTY_OS_KERNEL_VERSION = "os.property.KERNEL.version";
	/* APPS Properties **/	
	public static final String PROPERTY_APPS_JDK_VERSION = "apps.property.JDK.version";
	public static final String PROPERTY_APPS_HADOOP_VERSION = "apps.property.HADOOP.version";
	public static final String PROPERTY_APPS_BDA_VERSION = "bda.property.IMAGE.label";
	public static final String PROPERTY_APPS_BDA_TOPOLOGY = "bda.property.topology";
	public static final String PROPERTY_APPS_DB_VERSION = "apps.property.MySQL.version";
//	public static final String PROPERTY_APPS_ODI_LABEL = "apps.property.odi.label";
		
	public static Path hdfsInputPath =null;
	public static Path hdfsOutputPath =null;
		
	/*
	 * Declare a logger .
	 */
	
		private static Logger dfsLogger = LoggerFactory.getLogger(InitService.class);
		
/*
* Load hdfs properties.
*/
			 
	public static void loadProperties() throws IOException , Exception
	{
		String resourceName = PROPERTIES_FILE_NAME; // Load properties
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
	
		InputStream resourceStream =null;
		try
		{ 
			resourceStream = loader.getResourceAsStream(resourceName);
				    PROPERTIES.load(resourceStream);
				    
		}
		catch(IOException e)
		{
			dfsLogger.error("Error Exception while reading properties file : " , e);
		}finally
		{
			
			if(resourceStream!=null)
			{
				try{
					resourceStream.close();
				}
	
				catch (IOException ex)
				{
					dfsLogger.error("Error Exception while reading properties file : " , ex);	
				}
			}
		}
	
		
		
		for (String name : new String[]{PROPERTY_HDFS_URI,PROPERTY_HDFS_IN_PATH,PROPERTY_HDFS_OUT_PATH,PROPERTY_NODE_SERVER_TYPE,PROPERTY_NODE_PROC_TYPE,PROPERTY_NODE_TOTAL_CORES,
		PROPERTY_NODE_MEM_SIZE,PROPERTY_OS_LINUX_VERSION,PROPERTY_OS_KERNEL_VERSION,PROPERTY_APPS_JDK_VERSION,PROPERTY_APPS_HADOOP_VERSION,PROPERTY_APPS_BDA_VERSION,
		PROPERTY_APPS_BDA_TOPOLOGY,PROPERTY_APPS_DB_VERSION}) {
				String value =  PROPERTIES.getProperty(name);
				
				if (value !=null)
				{
					 PROPERTIES.setProperty(name,value);
					 dfsLogger.info("Print Properties name value pair : " + name + " , " + value);
					System.out.println("Print Properties name value pair : " + name + " , " + value);
				}
			}

	} /* End of Load Properties */
	}