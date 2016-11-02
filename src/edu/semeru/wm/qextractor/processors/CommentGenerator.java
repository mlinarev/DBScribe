package edu.semeru.wm.qextractor.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import edu.semeru.wm.qextractor.model.KeyVO;
import edu.semeru.wm.qextractor.model.MethodQueryVO;
import edu.semeru.wm.qextractor.model.QueryType;
import edu.semeru.wm.qextractor.model.QueryVO;
import edu.semeru.wm.qextractor.model.TableConstraintsVO;
import edu.semeru.wm.qextractor.model.TableVO;

public class CommentGenerator {

	ResourceBundle templates;
	HashSet<String> constraintsSentences;
	HashMap<String, TableConstraintsVO> constraints;
	HashMap<String, List<String>> foreignKeyTables;
	HashMap<String, List<String>> columns;
	
	
	public static String bootstrapJsLine = "<script src=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/js/bootstrap.min.js\"></script>";
    public static String bootstrapCssLine = "<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css\">";
    
	
	public CommentGenerator(){
		templates = ResourceBundle.getBundle("edu.semeru.wm.qextractor.processors.templates");
		
	}

	
	
	
	public void process( HashMap<String, List<String>> columns,
						HashMap<String, TableConstraintsVO> constraints,
						HashMap<String, List<String>> foreignKeyTables,
						HashMap<String, HashSet<String>> propagation,
						HashMap<String, MethodQueryVO> methodQueriesMap,
						String outputFile) throws IOException{
		StringBuilder comment = null;
		
		StringBuilder localInvocation = new StringBuilder();
		StringBuilder delegatedInvocation = new StringBuilder();
		StringBuilder mixedInvocation = new StringBuilder();
		StringBuilder methodsList = new StringBuilder();
		
		
		
		localInvocation.append("<a id=\"local\"></a><h3>Methods with local invocations: </h3><div class=\"panel panel-primary\" style=\"word-wrap: break-word;\">");
		delegatedInvocation.append("<a id=\"delegated\"></a><h3>Methods with only delegated invocations: </h3><div class=\"panel panel-success\" style=\"word-wrap: break-word;\">");
		mixedInvocation.append("<a id=\"mixed\"></a><h3>Methods with mixed invocations:  </h3><div class=\"panel panel-info\" style=\"word-wrap: break-word;\" >");
		
		
		HashSet<String> keys = new HashSet<String>();
		HashSet<String> delegatedSentences = null;
		ArrayList<String> delegatedSentencesList = null;
		
		for(String key: propagation.keySet()){
			keys.add( key);
		}
		for(String key: methodQueriesMap.keySet()){
			keys.add( key);
		}
		
		MethodQueryVO methodQueryVO = null;
		HashSet<String> calledMethodKeys = null;
		MethodQueryVO calledMethod = null;
		this.columns = columns;
		this.constraints = constraints;
		this.foreignKeyTables = foreignKeyTables;
		constraintsSentences = new HashSet<String>();
		
		ArrayList<String> constraintsSentencesList = null;
		
		String signature = null;
		String temp = null;
		String[] methodChain = null;
		String callId = null;
		String call4Display = null;
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
		writer.write("<html>");
		writer.write("<head>"); 
        writer.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        writer.write("<title>DbScribe report</title>"); 
        writer.write(bootstrapCssLine);
        writer.write("</head>"); 
        writer.write("<body><div class=\"container\">");
		
		
		int indexLocal = 0;
		int indexMixed = 0;
		int indexDelegated = 0;
		
		
		int queries = 0;
		int methodType = 0;
		
		ArrayList<String> sortedKeys = new ArrayList<String>(keys);
		Collections.sort(sortedKeys);
		String[] signatureData = null;
		String signature4Display = "";
		String signature4Menu = "";
		
		for(String method: sortedKeys){
			constraintsSentences = new HashSet<String>();
			delegatedSentences = new HashSet<String>();
			comment = new StringBuilder();
			
		
			methodQueryVO = methodQueriesMap.get(method);
			signatureData = method.split("\\|");
			signature4Display = signatureData[0]+"."+signatureData[1]+"(";
			signature4Menu = signatureData[0]+"."+signatureData[1]+"(";
			
			if(signatureData.length > 2){
				signature4Display+= signatureData[2].replace(",", ", ");
				signature4Menu+= signatureData[2];
				
			}
			
			signature4Display+=")";
			signature4Menu+=")";
			
			
			signature = method.replace("|", ".").replace(",", "-");
			
			comment.append("<a id=\""+signature+"\"></a><div class=\"panel-heading\"><b> &nbsp;"+signature4Display+"</b></div>");
			comment.append("<div class=\"panel-body\" ><p>");
			
			methodsList.append("<a href=\"#"+signature+"\">"+signature4Menu+"</a><br>");
			methodType = 0;
			if(methodQueryVO != null && methodQueryVO.getNonErrorQueriesNumber() > 0){
				//The method has local queries
				methodType = 1;
				comment.append(templates.getString("local_header"));
				comment.append(getSentenceFromSelectQueries(methodQueryVO.getSelectQueries()));
				comment.append(formatList(getSentencesFromUpdateQueries(methodQueryVO.getUpdateQueries()).toString()));
				comment.append(formatList(getSentencesFromDeleteQueries(methodQueryVO.getDeleteQueries()).toString()));
				comment.append(formatList(getSentencesFromInsertQueries(methodQueryVO.getInsertQueries()).toString()));
				comment.append(getSentenceFromDropQueries(methodQueryVO.getDropQueries()));
				comment.append(getSentenceFromAlterQueries(methodQueryVO.getAlterQueries()));
				comment.append(getSentenceFromCreateQueries(methodQueryVO.getCreateQueries()));
				comment.append(getSentenceFromTruncateQueries(methodQueryVO.getTruncateQueries()));
				
				queries += methodQueryVO.getSelectQueries().size();
				queries += methodQueryVO.getUpdateQueries().size();
				queries += methodQueryVO.getDeleteQueries().size();
				queries += methodQueryVO.getInsertQueries().size();
				queries += methodQueryVO.getDropQueries().size();
				queries += methodQueryVO.getAlterQueries().size();
				queries += methodQueryVO.getCreateQueries().size();
				queries += methodQueryVO.getTruncateQueries().size();
				
			}
			
			
			calledMethodKeys = propagation.get(method);
			if(calledMethodKeys != null && calledMethodKeys.size() > 0){
				//The method is a caller of other methods
				methodType += 2;
				comment.append(templates.getString("delegate_header"));
				for(String calledMethodKey: calledMethodKeys){
					
					
					temp = calledMethodKey;
					if(calledMethodKey.contains("#")){
						temp = temp.substring(temp.lastIndexOf("#")+1, temp.length());
					}
					calledMethod = methodQueriesMap.get(temp);
					if(calledMethod != null){
						methodChain = calledMethodKey.split("#");
						temp = "";
						
						if(methodChain.length > 1){
							temp = "the chain-call ";
						}
						for (String call : methodChain) {
							callId = call.replace("|", ".").replace(",", "-");
							call4Display = call.substring(0, call.lastIndexOf("|"));
							
							call4Display = call4Display.replace("|", ".");
							
							
							
							temp +="<a href=\"#"+callId+"\">"+call4Display+"</a>->";
						}
						if(temp.endsWith("->")){
							temp = temp.substring(0, temp.length()-2);
						}
						temp = temp.replace("->","&nbsp;<span class=\"glyphicon glyphicon-share-alt\"></span>&nbsp;");
    
						
						if(methodChain.length == 1){
							temp = "a call to the "+temp+" method";
						}
							
						calledMethodKey = temp;
						delegatedSentences.add(getSentenceFromDelegatedSelectQueries(calledMethod.getSelectQueries(),calledMethodKey));
						delegatedSentences.addAll(getSentencesFromDelegatedUpdateQueries(calledMethod.getUpdateQueries(),calledMethodKey));
						delegatedSentences.addAll(getSentencesFromDelegatedDeleteQueries(calledMethod.getDeleteQueries(),calledMethodKey));
						delegatedSentences.addAll(getSentencesFromDelegatedInsertQueries(calledMethod.getInsertQueries(),calledMethodKey));
						delegatedSentences.add(getSentenceFromDelegatedDropQueries(calledMethod.getDropQueries(),calledMethodKey));
						delegatedSentences.add(getSentenceFromDelegatedAlterQueries(calledMethod.getAlterQueries(),calledMethodKey));
						delegatedSentences.add(getSentenceFromDelegatedCreateQueries(calledMethod.getCreateQueries(),calledMethodKey));
						delegatedSentences.add(getSentenceFromDelegatedTruncateQueries(calledMethod.getTruncateQueries(),calledMethodKey));
						
						
						
					}
				}
				delegatedSentencesList = new ArrayList<String>(delegatedSentences);
				Collections.sort(delegatedSentencesList);
				for(String tempStr: delegatedSentencesList){
					if(!tempStr.isEmpty()){
						comment.append(tempStr);
					}
				}
				
			}
			
			
			if(!constraintsSentences.isEmpty()){
				comment.append(templates.getString("constr_header"));
				constraintsSentencesList = new ArrayList<String>(constraintsSentences);
				Collections.sort(constraintsSentencesList);
				for(String sentence : constraintsSentencesList){
					comment.append(sentence);
				}
				
			}
			
			comment.append("</p></div>");
			
			
			if(methodType == 1){
				localInvocation.append(comment);
				indexLocal++;
				
			}else if (methodType == 2){
				delegatedInvocation.append(comment);
				indexDelegated++;
				
			}else if (methodType == 3){
				mixedInvocation.append(comment);
				indexMixed++;
				
			}
			
			
			//writer.write("<div class=\"panel panel-primary\"><a id=\""+signature+"\"></a><div class=\"panel-heading\"><b>"+index+". &nbsp;"+signature+"</b></div>");
			//writer.write("<div class=\"panel-body\"><p>"+comment.toString()+"</p></div></div>");              
			
			
		}
		
		
		localInvocation.append("</div>");
		mixedInvocation.append("</div>");
		delegatedInvocation.append("</div>");
		
		
		writer.write("<div class=\"row\">");
		
		
		//Left 	Menu
		
		writer.write("<div class=\"col-xs-3\" style=\"position:fixed;float:left;left:0px\">");
		writer.write("<h2> DBScribe report</h2>");
		writer.write ("<div><p>Summary: "+indexLocal+"<a href=\"#local\"> methods with SQL local invocations</a>, "+indexMixed+" methods mixing <a href=\"#mixed\">local and delegated SQL invocations</a>, and " +
				+indexDelegated+ " methods with only <a href=\"#delegated\">delegated SQL invocations</a>. </p></div>");
		
		writer.write(" <div class=\"panel panel-default\" ><div class=\"panel-heading\">Methods</div>");
		writer.write("<div class=\"panel-body\" style=\"max-height:400px;overflow-x: scroll;overflow-y: scroll\">");
		writer.write(methodsList.toString());
		writer.write("</div>");
		writer.write("</div>");
		writer.write("</div>");
		//-- Left menu ends
		
	
		//Main content
		writer.write("<div class=\"col-xs-9\" style=\"float:right;right:0px\">");
			
		writer.newLine();
			
		writer.write(localInvocation.toString());
		writer.newLine();
		writer.flush();
		writer.write(mixedInvocation.toString());
		writer.newLine();
		writer.flush();
		writer.write(delegatedInvocation.toString());
		writer.newLine();
		writer.write("</div>");
		//-- Main content ends
		
		writer.write("</div>");
		
		writer.write("</div>");
		writer.flush();
		
		System.out.println("Queries "+queries);
		writer.write("</div></body></html>");
		writer.close();
	}
	
	

	private String getSentenceFromDelegatedSelectQueries(HashSet<QueryVO> queries, String method){
		String sentence = "";
		String replacement = "";
		String tempMethod = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("delegate_queries");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			sentence = sentence.replace("#table", formatList(replacement));
			sentence = sentence.replace("#method", method);
		}
	
		return sentence;
	}
	
	private String getSentenceFromSelectQueries(HashSet<QueryVO> queries){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("local_queries");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			
			sentence = sentence.replace("#table", formatList(replacement));
		}
	
		return sentence;
	}
	
	
	private String getSentenceFromDelegatedCreateQueries(HashSet<QueryVO> queries, String method){
		String sentence = "";
		String replacement = "";
		
		
		if(queries.size() > 0){
			sentence = templates.getString("delegate_create");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			
			sentence = sentence.replace("#table", formatList(replacement));
			sentence = sentence.replace("#method", method);
			
		}
	
		return sentence;
	}
	
	private String getSentenceFromCreateQueries(HashSet<QueryVO> queries){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("local_create");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			
			sentence = sentence.replace("#table", formatList(replacement));
		}
	
		return sentence;
	}
	
	private String getSentenceFromDelegatedDropQueries(HashSet<QueryVO> queries, String method){
		String sentence = "";
		String replacement = "";
		
		
		if(queries.size() > 0){
			sentence = templates.getString("delegate_drop");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			sentence = sentence.replace("#table", formatList(replacement));
			sentence = sentence.replace("#method", method);
		}
	
		return sentence;
	}
	
	private String getSentenceFromDropQueries(HashSet<QueryVO> queries){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("local_drop");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			
			sentence = sentence.replace("#table", formatList(replacement));
		}
	
		return sentence;
	}
	
	
	private String getSentenceFromDelegatedAlterQueries(HashSet<QueryVO> queries, String method){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("delegate_alter");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			
			sentence = sentence.replace("#table", formatList(replacement));
			sentence = sentence.replace("#method", method);
		}
	
		return sentence;
	}
	
	private String getSentenceFromAlterQueries(HashSet<QueryVO> queries){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("local_alter");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			
			sentence = sentence.replace("#table", formatList(replacement));
		}
	
		return sentence;
	}
	
	private String getSentenceFromDelegatedTruncateQueries(HashSet<QueryVO> queries, String method){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("delegate_truncate");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			sentence = sentence.replace("#table", formatList(replacement));
			sentence = sentence.replace("#method", method);
		}
	
		return sentence;
	}
	
	private String getSentenceFromTruncateQueries(HashSet<QueryVO> queries){
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			sentence = templates.getString("local_truncate");
			for (QueryVO queryVO : queries) {
				replacement += queryVO.getTables().toString().toUpperCase();
			}
			sentence = sentence.replace("#table", formatList(replacement));
		}
	
		return sentence;
	}
	
	
	private HashSet<String> getSentencesFromDelegatedUpdateQueries(HashSet<QueryVO> queries, String method){
		String baseSentence;
		String sentence = "";
		HashSet<String> sentences = new HashSet<String>();
		String replacement = "";
		
		if(queries.size() > 0){
			baseSentence = templates.getString("delegate_update");
			for (QueryVO queryVO : queries) {
				sentence = new String();
				sentence = baseSentence;
				replacement = queryVO.getAttributes().toString();
				
				sentence = sentence.replace("#table", queryVO.getTables().get(0).toUpperCase());
				sentence = sentence.replace("#attr", formatList(replacement));
				
				sentence = sentence.replace("#method", method);
				sentences.add(sentence);
				constraintsSentences.addAll(getSentencesFromUpdateConstraint(queryVO.getTables().get(0), queryVO.getAttributes()));
			}
			
		}
	
		return sentences;
	}
	
	private HashSet<String> getSentencesFromUpdateQueries(HashSet<QueryVO> queries){
		String baseSentence;
		String sentence = "";
		HashSet<String> sentences = new HashSet<String>();
		String replacement = "";
		
		if(queries.size() > 0){
			baseSentence = templates.getString("local_update");
			for (QueryVO queryVO : queries) {
				sentence = new String();
				sentence = baseSentence;
				replacement = queryVO.getAttributes().toString();
				
				sentence = sentence.replace("#table", queryVO.getTables().get(0).toUpperCase());
				sentence = sentence.replace("#attr", formatList(replacement));
				sentences.add(sentence);
				constraintsSentences.addAll(getSentencesFromUpdateConstraint(queryVO.getTables().get(0), queryVO.getAttributes()));
			}
			
		}
	
		return sentences;
	}
	
	
	private HashSet<String> getSentencesFromDelegatedInsertQueries(HashSet<QueryVO> queries, String method){
		String baseSentence;
		String sentence = "";
		HashSet<String> sentences = new HashSet<String>();
		String replacement = "";
		List<String> tableCols = null;
		String table = null;
		TableVO tableVO = null;
		if(queries.size() > 0){
			baseSentence = templates.getString("delegate_insert");
			for (QueryVO queryVO : queries) {
				sentence = new String();
				if(queryVO.getAttributes().isEmpty()){
					replacement = "";
					try{
						table = queryVO.getTables().get(0);
						tableCols = columns.get(table);
						for (int i = 0; i < queryVO.getNumberColumnsToInsert(); i++) {
							replacement += tableCols.get(i)+", ";
						}
						
					}catch(Exception e){
						baseSentence = templates.getString("delegate_insert_no_columns");
						replacement =  queryVO.getNumberColumnsToInsert()+"";
					}
					
				}else{
					replacement = queryVO.getAttributes().toString();
					
					
				}
				sentence = baseSentence;
				
				sentence = sentence.replace("#table", queryVO.getTables().get(0).toUpperCase());
				sentence = sentence.replace("#attr", formatList(replacement));
				
				sentence = sentence.replace("#method", method);
				sentences.add(sentence);
				constraintsSentences.addAll(getSentencesFromInsertConstraint(queryVO.getTables().get(0), queryVO.getAttributes()));
			}
			
			
		}
	
		return sentences;
	}
	
	private HashSet<String> getSentencesFromInsertQueries(HashSet<QueryVO> queries){
		String baseSentence;
		String sentence = "";
		HashSet<String> sentences = new HashSet<String>();
		String replacement = "";
		
		if(queries.size() > 0){
			baseSentence = templates.getString("local_insert");
			for (QueryVO queryVO : queries) {
				sentence = new String();
				if(queryVO.getAttributes().isEmpty()){
					baseSentence = templates.getString("local_insert_no_columns");
					replacement =  queryVO.getNumberColumnsToInsert()+"";
				}else{
					replacement = queryVO.getAttributes().toString();
					
					
				}
				sentence = baseSentence;
				
				sentence = sentence.replace("#attr", formatList(replacement));
				sentence = sentence.replace("#table", formatList(queryVO.getTables().get(0).toUpperCase()));
				sentences.add(sentence);
				
				constraintsSentences.addAll(getSentencesFromInsertConstraint(queryVO.getTables().get(0), queryVO.getAttributes()));
			}
			
		}
	
		return sentences;
	}
	
	
	private HashSet<String> getSentencesFromDelegatedDeleteQueries(HashSet<QueryVO> queries, String method){
		String baseSentence;
		HashSet<String> sentences = new HashSet<String>();
		String sentence = "";
		String replacement = "";
		
		
		if(queries.size() > 0){
			baseSentence = templates.getString("delegate_delete");
			for (QueryVO queryVO : queries) {
				sentence = new String();
				sentence = baseSentence;
				replacement = queryVO.getTables().toString().toUpperCase();
				sentence = sentence.replace("#table", formatList(replacement));
				sentence = sentence.replace("#method", method);
				sentences.add(sentence);
				constraintsSentences.addAll(getSentencesFromDeleteConstraint(queryVO.getTables().get(0)));
			}
			
		}
	
		return sentences;
	}
	
	private HashSet<String> getSentencesFromDeleteQueries(HashSet<QueryVO> queries){
		String baseSentence;
		HashSet<String> sentences = new HashSet<String>();
		String sentence = "";
		String replacement = "";
		
		if(queries.size() > 0){
			baseSentence = templates.getString("local_delete");
			for (QueryVO queryVO : queries) {
				sentence = new String();
				sentence = baseSentence;
				replacement = queryVO.getTables().toString().toUpperCase();
				sentence = sentence.replace("#table", formatList(replacement));
				sentences.add(sentence);
				constraintsSentences.addAll(getSentencesFromDeleteConstraint(queryVO.getTables().get(0)));
				
			}
			
		}
	
		
		return sentences;
	}
	
	
	//-------- Constraints
	
	private HashSet<String> getSentencesFromDeleteConstraint(String table){
		String sentence = "";
		List<String> impact = foreignKeyTables.get(table);
		if(impact != null){
			sentence = templates.getString("constr_ref_delete");
			sentence = sentence.replace("#table", table.toUpperCase());
			sentence = sentence.replace("#list", formatList(impact.toString()));
			
		}
		HashSet<String> sentences = new HashSet<String>();
		if(!sentence.isEmpty()){
			sentences.add(sentence);
		}
		return sentences;
	}
	
	private HashSet<String> getSentencesFromUpdateConstraint(String table, List<String> attributes){
		String sentence = "";
		HashSet<String> sentences = new HashSet<String>();
		
		List<String> impact = foreignKeyTables.get(table);
		String attrReplacement = "";
		
		TableConstraintsVO tcvo =  constraints.get(table);
		if(tcvo != null){
			List<String> nonNullFields = tcvo.getNoNullFields();
			
			
			List<String> primaryKeys = tcvo.getPrimaryKeys();
			for(String attr: attributes){
				if(primaryKeys.contains(attr)){
					attrReplacement += attr+",";
				}
				
				if(nonNullFields != null && nonNullFields.contains(attr)){
					sentence = templates.getString("constr_non_null");
					sentence = sentence.replace("#table", table.toUpperCase());
					sentence = sentence.replace("#attr", attr);
					sentences.add(sentence);
					
				}
			}
			if(impact != null && !attrReplacement.isEmpty()){
				sentence = new String();
				sentence = templates.getString("constr_ref_update");
				sentence = sentence.replace("#table", table.toUpperCase());
				sentence = sentence.replace("#list", formatList(impact.toString()));
				sentence = sentence.replace("#attr", formatList(attrReplacement));
				
				
			}
			
			
			if(!sentence.isEmpty()){
				sentences.add(sentence);
			}
		}
		return sentences;
	}
	
	private HashSet<String> getSentencesFromInsertConstraint(String table, List<String> attributes){
		HashSet<String> sentences = new HashSet<String>();
		List<String> impact = foreignKeyTables.get(table);
		HashSet<String> attrReplacement = new HashSet<String>();
		String limitsReplacement = "";
		
		HashSet<String> foreignKeyReplacement = new HashSet<String>();
		String tempSentence = null;
		TableConstraintsVO tcvo =  constraints.get(table);
		if(tcvo != null){
			List<String> primaryKeys = tcvo.getPrimaryKeys();
			List<String> autonumFields = tcvo.getAutonumericFields();
			List<String> uniqueFields = tcvo.getUniqueFields();
			List<String> nonNullFields = tcvo.getNoNullFields();
			List<KeyVO> foreignKeys = tcvo.getForeignKeys();
			HashMap<Long, HashSet<String>> varCharConstraints = tcvo.getVarcharLengths();
			
			
			for(String attr: attributes){
				if(autonumFields != null && autonumFields.contains(attr)){
					tempSentence = new String();
					tempSentence = templates.getString("constr_autonum");
					tempSentence = tempSentence.replace("#table", table.toUpperCase());
					tempSentence = tempSentence.replace("#attr", attr);
					sentences.add(tempSentence);
					
				}
				
				if(uniqueFields != null && uniqueFields.contains(attr)){
					tempSentence = new String();
					tempSentence = templates.getString("constr_unique");
					tempSentence = tempSentence.replace("#table", table.toUpperCase());
					tempSentence = tempSentence.replace("#attr", attr);
					sentences.add(tempSentence);
					
				}
				
				if(nonNullFields != null && nonNullFields.contains(attr)){
					tempSentence = new String();
					tempSentence = templates.getString("constr_non_null");
					tempSentence = tempSentence.replace("#table", table.toUpperCase());
					tempSentence = tempSentence.replace("#attr", attr);
					sentences.add(tempSentence);
					
				}
				
				
				
			}
			
			
			if(foreignKeys != null && foreignKeys.size() > 0){
				for(KeyVO key: foreignKeys){
					attrReplacement.add(key.getColumnName());
					foreignKeyReplacement.add( "("+key.getColumnName()+"&nbsp;<span class=\"glyphicon glyphicon-chevron-right\"></span>&nbsp;"+key.getReferencedTable()+"."+key.getReferencedColumn()+")");
				}
				tempSentence = new String();
				tempSentence = templates.getString("constr_ref_integrity");
				tempSentence = tempSentence.replace("#table", table.toUpperCase());
				tempSentence = tempSentence.replace("#attr", formatList(attrReplacement.toString()));
				tempSentence = tempSentence.replace("#foreign-keys", formatList(foreignKeyReplacement.toString()));
				sentences.add(tempSentence);
				
			}
			
			if(varCharConstraints != null && varCharConstraints.size() > 0){
				Set<Long> limitKeys = varCharConstraints.keySet();
				for(Long limitKey: limitKeys){
					limitsReplacement += limitKey+" ("+varCharConstraints.get(limitKey)+"), ";
				}
				tempSentence = new String();
				tempSentence = templates.getString("constr_varchar");
				tempSentence = tempSentence.replace("#table", table.toUpperCase());
				tempSentence = tempSentence.replace("#limits", formatList(limitsReplacement));
				
				sentences.add(tempSentence);
			}
		}
		return sentences;
	}
	
	
	private String formatList(String str){
		String line = str;
		
		line = line.replace("][",", ").replace("[","").replace("]", "");
		line = line.trim();
		
		if(line.endsWith(",")){
				line = line.substring(0, line.length() -1);
		}
		 
		return line;
	}
    
}
