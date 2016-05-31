package com.oracle.psr.oba.OdiBdaAnalysis;
/*
* Author KRMITRA. 
*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.AccessControlException;
import org.apache.hadoop.util.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.mortbay.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class OdiReducer extends Reducer<Text, Text, Text, Text>
{
		
	private static Logger reducerLogger = LoggerFactory.getLogger(OdiReducer.class);
	
	public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
	{
			
		//System.out.println( "KEY " + key.toString() + "And its values " + values.toString());
		/*
		 * Merges by adding NodeID# specific values to an Array list . 
		 */
		ArrayList<String> AllValues = new ArrayList<String>();
		
		int count = 0;
        while (values.iterator().hasNext()) {

        	AllValues.add(values.iterator().next().toString());
           
        }
       // System.out.println( "KEY " + key.toString() + "List of values " + AllValues);
        
        count = AllValues.size();
        
        context.write(key, new Text(AllValues + "|"));
        
	}
	
	   
  }


