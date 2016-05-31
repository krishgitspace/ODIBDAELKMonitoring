package com.oracle.psr.oba.OdiBdaAnalysis;
/*
 * Author KRMITRA.
 */
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class JsonOutputFormat extends FileOutputFormat<Text, Text> 
{
	
	TaskAttemptContext job;
	//public static char sep = File.separatorChar;
	
	public static String valueString;
	public static String keyString;
	
	@Override
	/* * 
	 *Reducers uses this overridden context.write() method to dump in json files .
	 */
	public RecordWriter<Text, Text> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
	this.job = job;

	return new JsonWriter(job);

	}
	
	/*
	 * JsonWriter extends abstract class RecordWriter to write the json files.
	 */
	public class JsonWriter extends RecordWriter<Text, Text> {
		private final Log log = LogFactory.getLog(JsonWriter.class);
		TaskAttemptContext job;
		Path file;
		FileSystem fs;

		int i = 0;

		
		 final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		 
		 String DEFAULT_DAY_PREFIX = "yyyy-MM-dd ";
			
		  /*
		   * Add DI Session steps to ArrayList  .
		   */

	 	public final ArrayList<Map<String,String>> sessList = new ArrayList<Map<String,String>>();
				
	    /*
		 * Chop Stats node initialization .
		 */
	    
	    /*
		 * Map for storing Run Header values.
		 */
	    
	   	public Map<String , String> HeaderMap;

		/*
		 * Map for storing Odi session values.
		 */
		HashMap<String, String> OSHMap = null;

		/**
		 *  A TreeMap for sorting OSHMap by Task OrderId.
		 */
		TreeMap<Integer, HashMap<String, String>> OSHTreeMap = null;
		Map<String,Object> head = new LinkedHashMap<String,Object>();
		
		Map<String,Object> headDetails = new HashMap<String,Object>();
		
		/*
		 *  A TreeMap for sorting OSHMap by Task OrderId.
		 */
		ArrayList<Map<String,Object>> MetricSamples = new ArrayList<Map<String,Object>>();
		
	   	String[] TESTCASE_NAME_FIELDS = new String[] { "Run Id", "Run Header",
			"Knowledge Module", "Scenario Id", "Run Date", "ODI Label",
			"Session Name", "Test Description",
			"Server Type", "Processor", "Total Cores",
			"Memory Size", "Linux Version", "Kernel Version",
			"JDK Version", "Hadoop Version",
			"BDA Version", "BDA Nodes",
			"DB Version" };
		public boolean SEC1 =  false;
		public boolean SEC2 =  false;

		public boolean setDate = false;
		public boolean setMeta = false;
		public String foundSessDate = null;
		public String foundSessEntry = null;

		public String foundRunMeta =null;
		public String foundStatsEntry =null;
		

		
		JsonWriter(TaskAttemptContext job){
		this.job = job;
		}
		
		@Override
		public void close(TaskAttemptContext context) throws IOException {
		//doc.close();
		}
		//@Override write() method.
		public synchronized void write(Text key, Text value) throws IOException, InterruptedException{
		Configuration conf = job.getConfiguration();
		Path name = getDefaultWorkFile(job, null);
		String outfilepath = name.toString();
		String keyname = key.toString();
		
		Path file =new Path((outfilepath.substring(0,outfilepath.length()-16)));
		
		String fileName = "ODI_PSR_1213BP_Meta_" + keyname + ".json";

		String fileName1 = "ODI_PSR_1213BP_Sess_" + keyname + ".json";

		//String fileName2 = "ODI_PSR_1213BP_Sample_";
		
        Path fullPath=new Path(file,fileName);
   
        Path fullPath1=new Path(file,fileName1);
   
      //  Path fullPath2=new Path(file,fileName2);
        fs=file.getFileSystem(conf);
        			
         valueString=value.toString();
         keyString = key.toString();
				System.out.println("VALUE String :" +  valueString);
				System.out.println("VALUE String :" +  keyString);
				
							 try {
								writeToJson(valueString);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 try {
								publishJson(fullPath,fullPath1,file,keyString);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							 
							
						
		}
		/*
		 * Calls writerForJson for writing json .
		 */
		public void writeToJson(String valueString) throws IOException ,Exception{

			writerForJson(valueString, keyString);
		}
		/*
		 * Does the actual writing to json  .
		 */
		public void writerForJson(String line, String RunId)
				throws IOException, Exception {
		
			OSHTreeMap = new TreeMap<Integer, HashMap<String, String>>(); 
			InitService.loadProperties();
			 System.out.println("hdfs service prop " + InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_BDA_TOPOLOGY));
			 	 
			 
			ArrayList<String> lineSplits = getSplittedList(line);
			System.out.println("SEC1 " +SEC1 + " SEC2 " + SEC2 + " setDate " + setDate + " setMeta " + setMeta +  "List Size " + lineSplits.size());
			
			/* Chop Node Initialization */
			
	    
			String  NodeArr[] = InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_BDA_TOPOLOGY).split(",");
			
			int NodeLength = NodeArr.length;
			
			System.out.println("NodeLength " + NodeLength + "  .");
			/** Start of FOR (Out) **/	
			for (  int Nd_i = 0 ; Nd_i < NodeLength ; Nd_i++)
			{
					
					/**
					 * Map for storing all nodes native system metrics unsorted values.
					 */
										
				TreeMap<Long, Integer> CPUTreeMap = new TreeMap<Long, Integer>();
				TreeMap<Long, Integer> DiskKBRdTreeMap = new TreeMap<Long, Integer>();
				TreeMap<Long, Integer> DiskKBwrtTreeMap = new TreeMap<Long, Integer>();
				TreeMap<Long, Integer> NetKBInTreeMap = new TreeMap<Long, Integer>();
				TreeMap<Long, Integer> NetKBoutTreeMap = new TreeMap<Long, Integer>();
				String Nd = InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_BDA_TOPOLOGY).split(",")[Nd_i];
				System.out.println ("Node Name " + Nd) ;
			
								/** Start of FOR (In) **/	
					    	for (int i=0 ; i < lineSplits.size(); i++)
					    	{
					    		
					    		System.out.println("SEC1 " +SEC1 + " SEC2 " + SEC2 + "setDate" + setDate + "setMeta" + setMeta +  "val of i " + i);
					    		
					    		System.out.println("val of line " + lineSplits.get(i));
					    		
					    		if (setDate==true && lineSplits.get(i).contains("STAT") && SEC2==true)
					    		{
					    			foundStatsEntry = lineSplits.get(i);
					    			if(!foundStatsEntry.contains("-"))
					    			{
					    				
								
					    				//TreeMap<Long, Integer> FreeMemTreeMap = new TreeMap<Long, Integer>();
					    					
												
												if (foundStatsEntry.contains(Nd))
												{
																						
																String[] statsItems = foundStatsEntry.split(",");
																System.out.println(statsItems[3]+ "   another " + statsItems[2]);
																if(!statsItems[3].matches("T"))
																{
																	//put free memory calc here .
																	
																	CPUTreeMap.put(parseToDate(statsItems[2]),Integer.parseInt(statsItems[3]));
																	DiskKBRdTreeMap.put(parseToDate(statsItems[2]),Integer.parseInt(statsItems[13]));
																	DiskKBwrtTreeMap.put(parseToDate(statsItems[2]),Integer.parseInt(statsItems[15]));
																	if (statsItems[17].substring(statsItems[17].length()-1).matches("K"))
																	{
																		int val = Integer.parseInt(statsItems[17].substring(0,statsItems[17].length()-1))*1000;
																		
																	
																	NetKBInTreeMap.put(parseToDate(statsItems[2]),val);
																	}
																	else {NetKBInTreeMap.put(parseToDate(statsItems[2]),Integer.parseInt(statsItems[17]));}
																	if (statsItems[19].substring(statsItems[19].length()-1).matches("K"))
																	{
																		int val = Integer.parseInt(statsItems[19].substring(0,statsItems[19].length()-1))*1000;
																		
																	
																	NetKBoutTreeMap.put(parseToDate(statsItems[2]),val);
																	}
																	else{NetKBoutTreeMap.put(parseToDate(statsItems[2]),Integer.parseInt(statsItems[19]));}
																}
																			
												}
												
										}
					    		  }
					    		
					    		
					    		if (SEC1==false)
					    		{
						    	
						    		
						    		if (setMeta==true && lineSplits.get(i).contains("SESS") && setDate==false )
						    		{
						    			
						    			 foundSessDate = lineSplits.get(i).split(",")[9].split(" ")[0];
						    			 DEFAULT_DAY_PREFIX= foundSessDate + " ";
						    		   		if (SEC1==false  && HeaderMap == null)
						    	          	{
						    	          	  	initializeHeaderMap(foundRunMeta);
						    	          	  	setDocHeader();	
						    	        		
						    	        		printTestDetailsTable();
						    	        	
						    	        		SEC1=true;
						    	          	}
						    			  setDate=true;
						    			  i =0 ;
						    		}
						    		
						    		if (lineSplits.get(i).contains("META") && setMeta==false)
						    		{
						    			 foundRunMeta = lineSplits.get(i);
						      			setMeta=true;
						      		  i =0 ;
						    		}
					    		}
					    		if (SEC1==true &&  SEC2==false)
					    		{
					    			OSHMap = new HashMap<String, String>();
					    			
					    			if (i==(lineSplits.size()-1))
					    			{
					    				SEC2=true;
					    				i=0;
					    		createSessTable();
					
					    			}
						    		if (lineSplits.get(i).contains("SESS") && SEC2==false )
						    		{
						    			
						    			
						    			 foundSessEntry = lineSplits.get(i);
						    			 
						    			 String[] sessionItems = foundSessEntry.split(",");
						    				
						    				//PdfPTable table1 = createSessTable(sessionItems[2],sessionItems[3],sessionItems[4],
						    					//	sessionItems[6],sessionItems[7],sessionItems[9],sessionItems[10],sessionItems[11]);
						    			 OSHMap.put("Session Name", sessionItems[2]);
						    			 OSHMap.put("SB No", sessionItems[3]);
						    			 OSHMap.put("Scenario Task No", sessionItems[4]);
						    			 OSHMap.put("Task Name1", sessionItems[6]);
						    			 OSHMap.put("Task Name2", sessionItems[7]);
						    			 OSHMap.put("Task Begin", sessionItems[9]);
						    			 OSHMap.put("Task End", sessionItems[10]);
						    			 OSHMap.put("duration", sessionItems[11]);
						    	
						    			 
						    			 System.out.println("Sess Entry " + foundSessEntry);
						    			 //	printSessDetailsTable(foundSessEntry);
						    			 OSHTreeMap.put(Integer.parseInt(OSHMap.get("Scenario Task No")), OSHMap);
						    			 
						    		}
					
					    		}
					    		 
					    		 
					    	} /** End of FOR (IN) **/
	    	
					    	/** Call to PrintResource Metrics Graphs.
							 *  Calculated over each Nodes and prints the respective collection.
							 */
								
					    	MetricSamples.add(addSamples(Nd,CPUTreeMap,"CPU Graph"));
					    	MetricSamples.add(addSamples(Nd,DiskKBRdTreeMap,"Disk Reads in KB"));
					    	MetricSamples.add(addSamples(Nd,DiskKBwrtTreeMap,"Disk Writes in KB"));
					    	MetricSamples.add(addSamples(Nd,NetKBInTreeMap,"Net i/o in (KB)"));
					    	MetricSamples.add(addSamples(Nd,NetKBoutTreeMap,"Net i/o out (KB)"));
					   					    
							} /** End of FOR (Out) **/
					    	
			} /** End of writerForJson **/	
		
		
		/*
		 * Removes Extra Spaces .
		 */
	    public String[] RemoveExtraSpaces(String firstArray[]) {

	    	  
	    	    List<String> list = new ArrayList<String>();

	    	    for(String s : firstArray) {
	    	       if(s != " " && s.length() > 0) {
	    	          list.add(s);
	    	       }
	    	    }

	    	    firstArray = list.toArray(new String[list.size()]);
	    	    return firstArray;
	    	}
	    
		/* Start of HeaderMap */

		 public void initializeHeaderMap(String input) throws IOException, Exception
	    {
			 HeaderMap = new HashMap<String , String>();
			 String[] headerItems = input.split(",");
			 String[] cleanHeaderItems = RemoveExtraSpaces(headerItems);
				InitService.loadProperties();
			 System.out.println("hdfs service prop " + InitService.PROPERTIES.getProperty(InitService.PROPERTY_NODE_SERVER_TYPE));
			 for(int i=0 ; i < cleanHeaderItems.length ; i++){
			 System.out.println("Header Item : " + cleanHeaderItems[i]);
			 }
			 	HeaderMap.put("Run Id",cleanHeaderItems[0]);
				HeaderMap.put("Run Header",cleanHeaderItems[2].split("-")[1]);
				HeaderMap.put("Knowledge Module",cleanHeaderItems[1].split("-")[1]);
				HeaderMap.put("Scenario Id",cleanHeaderItems[2].split("-")[0]);
				HeaderMap.put("Run Date",foundSessDate);
			//	
				HeaderMap.put("ODI Label",cleanHeaderItems[3]);
				HeaderMap.put("Session Name",cleanHeaderItems[1].split("-")[0]);
				HeaderMap.put("Test Description",cleanHeaderItems[cleanHeaderItems.length -2]);
				HeaderMap.put("Server Type",InitService.PROPERTIES.getProperty(InitService.PROPERTY_NODE_SERVER_TYPE));
				HeaderMap.put("Processor",InitService.PROPERTIES.getProperty(InitService.PROPERTY_NODE_PROC_TYPE));
				HeaderMap.put("Total Cores",InitService.PROPERTIES.getProperty(InitService.PROPERTY_NODE_TOTAL_CORES));
				HeaderMap.put("Memory Size",InitService.PROPERTIES.getProperty(InitService.PROPERTY_NODE_MEM_SIZE));
				HeaderMap.put("Linux Version",InitService.PROPERTIES.getProperty(InitService.PROPERTY_OS_LINUX_VERSION));
				HeaderMap.put("Kernel Version",InitService.PROPERTIES.getProperty(InitService.PROPERTY_OS_KERNEL_VERSION));
				HeaderMap.put("JDK Version",InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_JDK_VERSION));
				HeaderMap.put("Hadoop Version",InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_HADOOP_VERSION));
				HeaderMap.put("BDA Version",InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_BDA_VERSION));
				HeaderMap.put("BDA Nodes",InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_BDA_TOPOLOGY));
				HeaderMap.put("DB Version",InitService.PROPERTIES.getProperty(InitService.PROPERTY_APPS_DB_VERSION));
							
	    	
	    	}
			/* 
			 * Returns Header Map values from matching keys .
			 * To insert in Testcase Details section or Run Header.
			 * */
			  public String getHeaderMap(String code)
		      {
		      
		      	if (HeaderMap.containsKey(code))
		      	{
		      		return HeaderMap.get(code);
		      	}
		      	return null;
		       } /* End of HeaderMap */

			  
				/*Start of print Test Details. And ODI session details table headers*/
				public void printTestDetailsTable() throws  MalformedURLException, IOException {
					
					for (int i = 0; i < TESTCASE_NAME_FIELDS.length; i ++) {

						String Name = TESTCASE_NAME_FIELDS[i];
						String ValPair = getHeaderMap(TESTCASE_NAME_FIELDS[i]);
						
						 createDocMap(Name,ValPair);
	
					}
				}
				/*
				 * For creating a doc map.
				 */		
			private void createDocMap(String name,
						String valPair) {
				headDetails.put(name, valPair);
					
				}

			public void setDocHeader() throws  IOException {
				Date da = new Date();
				SimpleDateFormat format_r1 = new SimpleDateFormat("MMMM dd,yyyy");

				String date= format_r1.format(da);
				Map<String,String> header = new HashMap<String,String>();
				header.put("Report Created",date);
				
				//String[] header = new String[] {"Report Created",date};
				
				head.put("ODI BDA PSR Run Report ", header);
										
			} /* End of setDocHeader */


		/*
		 * Used to Add metric items for (cpu,net,disk,.etc per node) . Return Sorted Map.
		 */
		public Map<String,Object>  addSamples(String graphName,TreeMap<Long, Integer> GenricTreeMap,String Title) throws IOException
		{

			Iterator i = valueGraphIterator(GenricTreeMap);
			String Name = graphName.concat("_"+Title);
			Map<String,Object> Samples = new LinkedHashMap<String,Object>();
			ArrayList<Map<String,Object>> dataSamples = new ArrayList<Map<String,Object>>();
			//Samples.put(graphName, Title);
			 while(i.hasNext()) {
			    	Map<String,Object> individualSamples = new LinkedHashMap<String,Object>();
			       	Entry<Long, Integer> e=(Entry<Long, Integer>) i.next();
					Long key = e.getKey();
					Integer Value = e.getValue();
						System.out.println("timeInUTC" + getUTCStringFormatDate(key) + "ValInString" + Value.toString());
						individualSamples.put("\"ts\":", "\""+getUTCStringFormatDate(key)+"\"");
						individualSamples.put("\"val\":", Value);
						individualSamples.put("\"name\":", "\""+Name+"\"");
						dataSamples.add(individualSamples);
					
			    }
			    
			    Samples.put(Name, dataSamples);
			    
				return Samples;
		 
		
		 } /* End of Add Samples */
		

		/*Start of print ODI Session Details. */
		/*
		 * Prints Session details ,sorted by task Order Id .
		*/
		public void createSessTable() throws InterruptedException
		{
			 //ArrayList<String> cell = new ArrayList<String>();
			
			if (OSHTreeMap != null) {

		         Iterator<Map.Entry<Integer, HashMap<String,String>>> i = valueIterator(OSHTreeMap);
					
				
					  while(i.hasNext()) {
					    	Entry<Integer, HashMap<String,String>> e=i.next();
						Integer key = e.getKey();
			System.out.println("I am Key : " + key  );
			Thread.sleep(100);
		                  HashMap<String, String> value=e.getValue();
		                
		                        sessList.add(value);
					  }
			}
			
		}
		
		/*
		 * To Sort Map entries .
		 */
		
		public Iterator valueIterator(TreeMap<Integer,HashMap<String,String>> map) {
		    Set<Map.Entry<Integer, HashMap<String,String>>> set = new TreeSet(new Comparator<Map.Entry<Integer, HashMap<String,String>>>() {
		        @Override
		        public int compare(Entry<Integer, HashMap<String,String>> o1, Entry<Integer, HashMap<String,String>> o2) {
		        	
						return  o1.getKey().compareTo(o2.getKey()) > 0 ? 1 : -1;
				
		        }
		    });
		    set.addAll((Collection<? extends Entry<Integer, HashMap<String, String>>>) map.entrySet());
		    return set.iterator();
		}
		
		/* 
	     * Returns Time Stamp format in UTC ISO 8601
	     *  */
	    
	    public String getUTCStringFormatDate(Long ts) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			//SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	 		//dateFormat.setTimeZone(TimeZone.getTimeZone("UTC")); //comment utc timezone
	 		Date date = new Date(ts);
			return dateFormat.format(date);
			
			}

		/*
		 * Map Iterator. 
		 */
		public Iterator valueGraphIterator(TreeMap<Long , Integer> map) {
	        Set set = new TreeSet(new Comparator<Map.Entry<Long, Integer>>() {
	            @Override
	            public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2) {
	         //  	DateFormat f = new SimpleDateFormat("HH:mm:ss"); 
	        	 
	      
		        	try {
		        		Long d1=o1.getKey();
		        		Long d2=o2.getKey();
		  
		        		if(d1==d2){
		        			return 1;
		        		}
		        		
						return  	d1.compareTo(d2) > 0 ? 1 : -1;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						
						return 0;
					}
	            }
	        });
	        set.addAll(map.entrySet());
	        return set.iterator();
	    }

		
		/*
		 * Returns a results set of individual splitted list out of the Whole List . Split by "\\|" .Return type array list .@Param String input .
		 */
		public ArrayList<String> getSplittedList(String strInput){
			
	    	ArrayList<String> result = new ArrayList<String>();
	    	
	    	int start = 1;
	    	
	    	for (int current =0 ; current < strInput.length() ; current++)
	    	{
	    		boolean atLastChar = (current == strInput.length() - 1);
			    if(atLastChar) result.add(strInput.substring(start));
			    
			    else if (strInput.charAt(current) == '|') {
			        result.add(strInput.substring(start, current));
			        start = current + 1;
			    }
	    	}
			return result;
		}
		/*
		 * Parse millisec ts to date .
		 */
		public long parseToDate(String dayPoint) throws ParseException {
	        return SDF.parse(DEFAULT_DAY_PREFIX + dayPoint).getTime();
	    }
	    
		
		public void publishJson(Path path,Path path1,Path file,String Keyname) throws Exception {
			
		     
			 Map<String,Object> mapList = new HashMap<String,Object>();
			 head.put("Report Details", headDetails);
			 mapList.put("PSR RUN Details" , head);
			
			 Map<String,Object> sessHeaders = new HashMap<String,Object>();
			
			 sessHeaders.put("Step Details", sessList);
			 

			 ObjectMapper mapper2 = new ObjectMapper();
				// ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
				 ObjectWriter writer2 = mapper2.defaultPrettyPrintingWriter();
				 writer2.writeValue(fs.create(path,true), mapList);
				 System.out.println("-Check Head_Meta JSON output.-");
			
				 String sessSamples = sessList.toString();
				 
					String tokens_sess = sessSamples.substring(2, (sessSamples.length()-2));
					List<String> tokenList_sess = Arrays.asList(tokens_sess.split("\\{"));
					 
					PrintWriter pw_sess = new PrintWriter(fs.create(path1,true));
					int list_sz=tokenList_sess.size();   
					 for (int j=1;j<list_sz;j++)
					    {  	    	
					    	if (j==(list_sz-1))
					    	{	pw_sess.println("{\""+ tokenList_sess.get(j).replaceAll("=", "\":\"").replaceAll(", ", "\", \"").replace("}\", \"", "\"},").replace("duration\":\"","duration\":").replace("\", \"Task Begin", ", \"Task Begin").replace("SB No\":\"", "SB No\":").replace("\", \"Scenario Task No\":\"", ", \"Scenario Task No\":") + "}" );}
						 	else
					    	pw_sess.println("{\""+ tokenList_sess.get(j).replaceAll("=", "\":\"").replaceAll(", ", "\", \"").replace("}\", \"", "\"},").replace("duration\":\"","duration\":").replace("\", \"Task Begin", ", \"Task Begin").replace("SB No\":\"", "SB No\":").replace("\", \"Scenario Task No\":\"", ", \"Scenario Task No\":").replace("\"},", "},") );
					    }
					    
					    pw_sess.close();
					
					    
				 System.out.println("-Check Session JSON output.-");
				 
			 for (int i= 0 ; i <MetricSamples.size() ; i++  )
				{
			
					String fileName_samples = Keyname + "_ODI_PSR_1213BP_Samples_" + i + ".json";

			        Path fullPath_samples=new Path(file,fileName_samples);
			        
		
					String items = MetricSamples.get(i).values().toString();
					String tokens = items.substring(2, (items.length()-2));
							 System.out.println("-Check JSON samples .-" + items );
					 System.out.println("-Check JSON samples tokens .-" + tokens );
					 List<String> tokenList = Arrays.asList(tokens.split("\\{"));
					 
					 PrintWriter pw = new PrintWriter(fs.create(fullPath_samples,true));
					    for (int j=1;j<tokenList.size();j++)
					    {  	    	
					    	pw.println("{"+ tokenList.get(j).replaceAll("=", ""));
					    }
					    pw.close();
					    
				 System.out.println("-Check Samples JSON output .-" + i + "\n " );
			 } 
				
			 
			}
		
	}	//JsonWriter Class end.
		

	
}