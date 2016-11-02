package edu.semeru.wm.qextractor.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MethodQueryVO {

	private String name;
	private String packageName;
	private String className;
	private int nArgs;
	private String key;
	private String signature;
	
	private String methodSnippetHash;
	
	private HashSet<QueryVO> selectQueries;
	private HashSet<QueryVO> insertQueries;
	private HashSet<QueryVO> updateQueries;
	private HashSet<QueryVO> deleteQueries;
	private HashSet<QueryVO> dropQueries;
	private HashSet<QueryVO> truncateQueries;
	private HashSet<QueryVO> createQueries;
	private HashSet<QueryVO> alterQueries;
	
	
	private HashSet<QueryVO> errorQueries;
	private int nonErrorQueries;
	
	private List<String> calledMethods;
	
	private String comment;
	
	public MethodQueryVO() {
		selectQueries = new HashSet<QueryVO>();
		insertQueries = new HashSet<QueryVO>();
		updateQueries = new HashSet<QueryVO>();
		deleteQueries = new HashSet<QueryVO>();
		dropQueries = new HashSet<QueryVO>();
		truncateQueries = new HashSet<QueryVO>();
		createQueries = new HashSet<QueryVO>();
		alterQueries = new HashSet<QueryVO>();
		
		errorQueries = new HashSet<QueryVO>();
		calledMethods = new ArrayList<String>();
		
		nonErrorQueries = 0;
		
	}
	
	public int getNonErrorQueriesNumber(){
		return nonErrorQueries;
	}
	public void addSelect(QueryVO vo){
		selectQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addInsert(QueryVO vo){
		if(!insertQueries.contains(vo)){
			insertQueries.add(vo);
			nonErrorQueries++;
		}
	}
	
	public void addUpdate(QueryVO vo){
		updateQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addDelete(QueryVO vo){
		deleteQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addDrop(QueryVO vo){
		dropQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addTruncate(QueryVO vo){
		truncateQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addCreate(QueryVO vo){
		createQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addAlter(QueryVO vo){
		alterQueries.add(vo);
		nonErrorQueries++;
	}
	
	public void addError(QueryVO vo){
		errorQueries.add(vo);
	}

	
	public void addQuery (QueryVO vo){
		int type = vo.getType();
		if(type == QueryType.SELECT.getId()){
			addSelect(vo);
		} else if(type == QueryType.INSERT.getId()){
			addInsert(vo);
		} else if (type == QueryType.UPDATE.getId()){
			addUpdate(vo);
		} else if (type == QueryType.DELETE.getId()){
			addDelete(vo);
		} else if (type == QueryType.DROP.getId()){
			addDrop(vo);
		} else if(type == QueryType.TRUNCATE.getId()){
			addTruncate(vo);
		} else if(type == QueryType.CREATE.getId()){
			addCreate(vo);
		} else if(type == QueryType.ALTER.getId()){
			addAlter(vo);
		} else if(type == QueryType.ERROR.getId()){
			addError(vo);
		}
	}
	
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getnArgs() {
		return nArgs;
	}

	public void setnArgs(int nArgs) {
		this.nArgs = nArgs;
	}

	public HashSet<QueryVO> getSelectQueries() {
		return selectQueries;
	}

	public HashSet<QueryVO> getInsertQueries() {
		return insertQueries;
	}

	public HashSet<QueryVO> getUpdateQueries() {
		return updateQueries;
	}

	public HashSet<QueryVO> getDeleteQueries() {
		return deleteQueries;
	}

	public HashSet<QueryVO> getDropQueries() {
		return dropQueries;
	}

	public HashSet<QueryVO> getTruncateQueries() {
		return truncateQueries;
	}

	public HashSet<QueryVO> getCreateQueries() {
		return createQueries;
	}

	public HashSet<QueryVO> getErrorQueries() {
		return errorQueries;
	}

	
	public HashSet<QueryVO> getAlterQueries() {
		return alterQueries;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public void addCalledMethod(String method){
		this.calledMethods.add(method);
	}

	public List<String> getCalledMethods() {
		return calledMethods;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getMethodSnippetHash() {
		return methodSnippetHash;
	}

	public void setMethodSnippetHash(String methodSnippetHash) {
		this.methodSnippetHash = methodSnippetHash;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
	
	
	public MethodVO getMethodVO(){
		MethodVO methodVO = new MethodVO();
		methodVO.setClassName(className);
		methodVO.setComment(comment);
		methodVO.setKey(key);
		methodVO.setMethodSnippetHash(methodSnippetHash);
		methodVO.setName(name);
		methodVO.setnArgs(nArgs);
		methodVO.setPackageName(packageName);
		methodVO.setSignature(signature);
		
		return methodVO;
	}
	
}
