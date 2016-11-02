package edu.semeru.wm.qextractor.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.semeru.wm.CallGraph.Analyzer;
import edu.semeru.wm.CallGraph.LeveledCallGraphComponent;
import edu.semeru.wm.CallGraph.Method;

import edu.semeru.wm.qextractor.helper.ConnectionManager;
import edu.semeru.wm.qextractor.model.ConnectionVO;
import edu.semeru.wm.qextractor.model.MethodQueryVO;
import edu.semeru.wm.qextractor.model.MethodVO;
import edu.semeru.wm.qextractor.model.TableConstraintsVO;

public class DBScribe {

	/**
	 * depth constraint of analysis
	 */
	public static final int LEVELTHRESHOLD = 15;
	
	
	public static void printDetectionList(String systemFolder, String outputFile) throws IOException{
		System.out.println("DBScribe is running");
		System.out.println("1. Running JDBCProcessor");
		JDBCProcessor processor = new JDBCProcessor();
		processor.processFolder(systemFolder);
		System.out.println("--- JDBC processing: DONE");
		//----------------------------------------------------------------------------
		//*Step 2: extract partial graph including only call-chains related to db operations
		// The key is the caller signature, and the value is a set of callees (signatures).
		HashMap<String, HashSet<String>> methodCalls = processor.getMethodCalls();
		//The key is a method signature,and the value is a VO object with all the information
		// of the queries/statements declared in that method.
		HashMap<String, MethodQueryVO> methodQueriesMap = processor.getMethodQueriesMap();
		
		
		HashSet<String> allSignatures = new HashSet<String>();
		allSignatures = processor.getMethodSignatures();
		Iterator<Entry<String, HashSet<String>>> it = methodCalls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, HashSet<String>> pair = (Map.Entry<String, HashSet<String>>)it.next();
			String caller = pair.getKey(); 
			allSignatures.add(caller);
			HashSet<String> hsCallees = pair.getValue();
			for(String cStr : hsCallees){
				allSignatures.add(cStr);
			}
		}

		System.out.println("2. Running Callgraph extractor");
		
		//Boyang's part that returns the partial call graph
		LeveledCallGraphComponent lcgComponent = new LeveledCallGraphComponent();
		lcgComponent.analyze(methodCalls, methodQueriesMap, allSignatures);
		Analyzer analyzer = lcgComponent.getAz();
		System.out.println("--- Callgraph extraction: DONE");
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		writer.write("Class,Method,Depth,Comment, Source-code-method-hash");
		writer.newLine();
		Set<String> keys = methodQueriesMap.keySet();
		MethodQueryVO vo = new MethodQueryVO();
		ArrayList <ArrayList <Method>> callers = null;
		int depth = 0;
		String methodData[] = null;
		for(String key:keys){
			vo = methodQueriesMap.get(key);
			callers = analyzer.findCallerListByName(key);
			depth = 0;
			for(ArrayList <Method> path : callers){
				if(path.size() > depth){
					depth = path.size();
				}
			}
			methodData = key.split("\\|");
			writer.write(methodData[0]+","+methodData[1]+","+depth+","+vo.getComment()+","+vo.getMethodSnippetHash());
			writer.newLine();
		}
		writer.close();
		
		
	}
	
	
	public static void printPaths(String systemFolder, String outputFolder) throws IOException{
		System.out.println("DBScribe is running");
		System.out.println("1. Running JDBCProcessor");
		JDBCProcessor processor = new JDBCProcessor();
		processor.processFolder(systemFolder);
		System.out.println("--- JDBC processing: DONE");
		//----------------------------------------------------------------------------
		//*Step 2: extract partial graph including only call-chains related to db operations
		// The key is the caller signature, and the value is a set of callees (signatures).
		HashMap<String, HashSet<String>> methodCalls = processor.getMethodCalls();
		//The key is a method signature,and the value is a VO object with all the information
		// of the queries/statements declared in that method.
		HashMap<String, MethodQueryVO> methodQueriesMap = processor.getMethodQueriesMap();
		HashMap<String, MethodVO> allMethodsMap = processor.getAllMethods();
		
		
		HashSet<String> allSignatures = new HashSet<String>();
		allSignatures = processor.getMethodSignatures();
		Iterator<Entry<String, HashSet<String>>> it = methodCalls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, HashSet<String>> pair = (Map.Entry<String, HashSet<String>>)it.next();
			String caller = pair.getKey(); 
			allSignatures.add(caller);
			HashSet<String> hsCallees = pair.getValue();
			for(String cStr : hsCallees){
				allSignatures.add(cStr);
			}
		}

		System.out.println("2. Running Callgraph extractor");
		
		//Boyang's part that returns the partial call graph
		LeveledCallGraphComponent lcgComponent = new LeveledCallGraphComponent();
		lcgComponent.analyze(methodCalls, methodQueriesMap, allSignatures);
		Analyzer analyzer = lcgComponent.getAz();
		System.out.println("--- Callgraph extraction: DONE");
		
		
		BufferedWriter writer = null;
		Set<String> keys = methodQueriesMap.keySet();
		MethodQueryVO vo = new MethodQueryVO();
		ArrayList <ArrayList <Method>> callers = null;
		int depth = 0;
		String methodData[] = null;
		int pathIndex = 1;
		MethodVO callerVO = null;
		for(String key:keys){
			vo = methodQueriesMap.get(key);
			callers = analyzer.findCallerListByName(key);
			depth = 0;
			methodData = key.split("\\|");
			for(ArrayList <Method> path : callers){
				if(path.size() > depth){
					depth = path.size();
				}
				writer = new BufferedWriter(new FileWriter(outputFolder+File.separator+"path-"+pathIndex+".csv"));
				writer.write("Class,Method,Depth,Comment, Source-code-method-hash");
				writer.newLine();
				
				
				for(Method m: path){
					callerVO = allMethodsMap.get(m.getKey());
					writer.write(callerVO.getPackageName()+"."+callerVO.getClassName()+","+callerVO.getName()+","+--depth+","+callerVO.getComment()+","+callerVO.getMethodSnippetHash());
					writer.newLine();
					
				}
				writer.close();
			}
			
			
			pathIndex++;
		}
		
		
		
	}
	
	public static void runDBScribe(String systemFolder, String outputFile, String host, String schema, String user, String passwd) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
		//String outputFile = "/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/output/text.html";
		// *Step 1: Process source code to (i) extract couples (caller-->callee), 
		// (ii) locate methods using sql queries/statements, and (iii) parse the sql queries/statements
		System.out.println("DBScribe is running");
		System.out.println("1. Running JDBCProcessor");
		long startTime = System.currentTimeMillis();
		
		JDBCProcessor processor = new JDBCProcessor();
		processor.processFolder(systemFolder);
		// Change the path here to the folder of the system under analysis
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/CS597-PROJECT/src/");
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/magma-master");
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/systems/RiskIt/riskinsurance-master/src");
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/systems/Broker-master/src");
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/systems/UMAS-Project-master/CS597-PROJECT/src");
		
		
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/agilefant-master/webapp/src");
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/test3");
		
		System.out.println("--- JDBC processing: DONE");
		//----------------------------------------------------------------------------
		
		
		
		//*Step 2: extract partial graph including only call-chains related to db operations
		
		// The key is the caller signature, and the value is a set of callees (signatures).
		HashMap<String, HashSet<String>> methodCalls = processor.getMethodCalls();
		
		//The key is a method signature,and the value is a VO object with all the information
		// of the queries/statements declared in that method.
		HashMap<String, MethodQueryVO> methodQueriesMap = processor.getMethodQueriesMap();
		
		HashSet<String> allMethods = new HashSet<String>();
		System.out.println("*Number of methods calling at least one method: " + methodCalls.size());
		System.out.println("*Total queries: "+processor.getTotalQueries());
		System.out.println("*Error queries: "+processor.getErrorQueries());
		
		allMethods = processor.getMethodSignatures();
		Iterator<Entry<String, HashSet<String>>> it = methodCalls.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, HashSet<String>> pair = (Map.Entry<String, HashSet<String>>)it.next();
			String caller = pair.getKey(); 
			allMethods.add(caller);
			HashSet<String> hsCallees = pair.getValue();
			for(String cStr : hsCallees){
				allMethods.add(cStr);
			}
		}

		System.out.println("2. Running Callgraph extractor");
		
		//Boyang's part that returns the partial call graph
		LeveledCallGraphComponent lcgComponent = new LeveledCallGraphComponent();
		lcgComponent.analyze(methodCalls, methodQueriesMap, allMethods);
		Analyzer analyzer = lcgComponent.getAz();
		
		
		System.out.println("--- Callgraph extraction: DONE");
		
		
		//----------------------------------------------------------------------------
		// *Step 3: Extract schema constraints from db
				
		DBInfoExtractor dbie = new DBInfoExtractor();
		// Change here the db connection settings
    	//ConnectionVO vo = new ConnectionVO("localhost", "root", "12345", "riskit");
    	
		ConnectionVO vo = new ConnectionVO(host, user, passwd, schema);
    	dbie.setConnection(ConnectionManager.getConnection(vo));
    	System.out.println("3. Running Constraints extractor");
		
    	dbie.processTables(vo.getSchemaName());
    	
    	HashMap<String, TableConstraintsVO> constraints = dbie.getConstraints();    	
    	HashMap<String, List<String>> foreignKeyTables = dbie.getForeignKeyTables();
    	System.out.println("--- Constraints extraction: DONE");
		
   	
    	//----------------------------------------------------------------------------
    	// *Step 4: Propagate methods through partial call graph
    	QueriesPropagator qp = new QueriesPropagator();
    	HashMap<String, HashSet<String>> propagation= qp.getPropagatedMethods(methodQueriesMap, analyzer);
    			
    	//----------------------------------------------------------------------------
		// *Step 5: Generate comments using partial call graph, parsed queries, 
		// and constraints extracted from the db
		
		CommentGenerator comGenerator = new CommentGenerator();
		System.out.println("4. Running Comments generator");
		comGenerator.process(dbie.getColumns(), constraints, foreignKeyTables, propagation, processor.getMethodQueriesMap(), outputFile);
		System.out.println("--- Comments generation: DONE");
		long endTime = System.currentTimeMillis();
		System.out.println("* Total execution time(ms): "+(endTime-startTime));
		
	}
	
	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
		String systemFolder =null;
		
		String outputFile =null;
		String outputFolder =null;
		String host = null;
		String schema = null;
		String user = null;
		String passwd = null;
		
		if(args[0].equals("-r")){
			systemFolder = args[1];
			outputFile = args[2];
			host = args[3];
			schema = args[4];
			user = args[5];
			passwd = args[6];
			
			DBScribe.runDBScribe(systemFolder, outputFile, host, schema, user, passwd);
		}
		else if(args[0].equals("-p")){
			systemFolder = args[1];
			outputFile = args[2];
			
			DBScribe.printDetectionList(systemFolder, outputFile );
		}
		
		else if(args[0].equals("-cc")){
			systemFolder = args[1];
			outputFolder = args[2];
			DBScribe.printPaths(systemFolder, outputFolder);
		}
			

	}
	
	

}
