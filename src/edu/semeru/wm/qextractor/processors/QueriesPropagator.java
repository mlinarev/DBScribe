package edu.semeru.wm.qextractor.processors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.semeru.wm.CallGraph.Analyzer;

import edu.semeru.wm.CallGraph.Method;
import edu.semeru.wm.qextractor.model.MethodQueryVO;
public class QueriesPropagator {

	public HashMap<String, HashSet<String>> getPropagatedMethods(HashMap<String, MethodQueryVO> methodQueriesMap,
			Analyzer analyzer){
		HashMap<String, HashSet<String>> propagatedQueries = new HashMap<String, HashSet<String>>();
		ArrayList<ArrayList<Method>> callgraph = null;
		
		Set<String> methodKeys = methodQueriesMap.keySet();
		int index= 0;
		String previous = null;
		ArrayList<Method> path = null;
		Method currentMethod = null;
		Method previousMethod = null;
		
		String previousKey = null;
		String currentKey = null;
		HashSet<String> previousCalls = null;
		MethodQueryVO methodQueryVO = null;
		for(String key: methodKeys){
			
			
			
			if(!key.isEmpty()){
				
				callgraph = analyzer.findCallerListByName(key);
				
				
				int maxLevel = 1;
				for(int i = 0; i < callgraph.size(); i++){
					if(callgraph.get(i).size() > maxLevel){
						maxLevel = callgraph.get(i).size();
					}
				}
				
				
				for(int level = 1; level < maxLevel; level++){
					for(int i = 0; i < callgraph.size(); i++){
						path = callgraph.get(i);
						
						if(level < path.size() ){
							previousMethod = path.get(level - 1);
							currentMethod = path.get(level);
							
							previousKey = previousMethod.getClassName()+"|"+previousMethod.getMethodName()+"|"+previousMethod.getMethodArgs();
							
							currentKey = currentMethod.getClassName()+"|"+currentMethod.getMethodName()+"|"+currentMethod.getMethodArgs();
							
							if(currentKey.contains("ComputeAverageEducation") || previousKey.contains("ComputeAverageEducation")){
								int j = 0;
								j++;
							}
							
							if(!propagatedQueries.containsKey(currentKey)){
								propagatedQueries.put(currentKey, new HashSet<String>());
							}
							methodQueryVO = methodQueriesMap.get(previousKey);
							if(methodQueryVO != null && methodQueryVO.getNonErrorQueriesNumber() > 0){
								propagatedQueries.get(currentKey).add(previousKey);
							}
							if(level != 1 && !previousKey.equals(currentKey)){
								previousCalls = propagatedQueries.get(previousKey);
								if(previousCalls != null){
									Object[] temp = previousCalls.toArray();
									for (int j =0; j < temp.length;j++) {
										String call = (String)temp[j];
										propagatedQueries.get(currentKey).add(previousKey+"#"+call);
									}
								}
								
								
							}
								
							
						}
						
					}
				}
				
				
				
			}
			
		}
		
		return propagatedQueries;
	}

}
