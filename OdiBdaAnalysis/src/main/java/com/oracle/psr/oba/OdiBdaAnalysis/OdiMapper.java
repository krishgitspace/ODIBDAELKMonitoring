package com.oracle.psr.oba.OdiBdaAnalysis;
/*
* Author KRMITRA. 
*/


import java.io.IOException;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/*
 * The Mapper Class which emits RUNID# (Key) & Attribute(Values) Pairs for reducers.
 */
public class OdiMapper extends Mapper <LongWritable ,Text , Text , Text >
{
		 	
	private static Logger mapperLogger = LoggerFactory.getLogger(OdiMapper.class); 
	
	public void map(LongWritable Key ,Text value ,Context context) throws IOException, InterruptedException 
	{
		
	
		String[] lineItems = value.toString().split(",");
		 StringBuilder sb = new StringBuilder();
		String tableId = null;
		String RUNID = null;
		System.out.println(value.toString() + " line items length " + lineItems.length);
		
		if (lineItems.length==6)
		{
			tableId = ",META";
			 RUNID = lineItems[0].trim();
			
			   
		
		sb = sb.append(RUNID);
		for (int i=1; i < lineItems.length ; i++)
					sb.append(",").append(lineItems[i]);
		sb.append(tableId+"|");
		}
		
		if (lineItems.length==11)
		{
			tableId = ",SESS";
			 RUNID = lineItems[10].trim();
			
			   
		
		sb = sb.append(RUNID);
		for (int i=0; i < lineItems.length ; i++)
					sb.append(",").append(lineItems[i]);
		sb.append(tableId+"|");
		}
		else if (lineItems.length==21){
				 RUNID = lineItems[19].trim();
			
				 tableId = ",STAT";
		
		sb = sb.append(RUNID);
		for (int i=0; i < lineItems.length ; i++)
					sb.append(",").append(lineItems[i]);
		sb.append(tableId+"|");
			}		
			
		System.out.println("Run ID" + RUNID + "And SB " + sb.toString() );
		
		//mapperLogger.info(RUNID);
		
		//System.out.println(RUNID);
		 //RUNID = lineItems[0].trim();
		context.write(new Text(RUNID),new Text(sb.toString()));
		// context.write(new Text(RUNID),new Text(value.toString()));
		}
    }
