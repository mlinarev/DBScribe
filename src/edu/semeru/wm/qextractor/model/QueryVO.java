package edu.semeru.wm.qextractor.model;

import java.util.ArrayList;
import java.util.List;

public class QueryVO {

	private String query;
	private List<String> tables;
	private List<String> attributes;
	private int type;
	private int numberColumnsToInsert;
	
	public int getNumberColumnsToInsert() {
		return numberColumnsToInsert;
	}

	public void setNumberColumnsToInsert(int numberColumnsToInsert) {
		this.numberColumnsToInsert = numberColumnsToInsert;
	}

	public QueryVO() {
		this.tables = new ArrayList<String>();
		this.attributes = new ArrayList<String>();
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public List<String> getTables() {
		return tables;
	}
	
	public void addTable(String table){
		tables.add(table);
	}

	public void setTables(List<String> tables) {
		this.tables = tables;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}
	
	public void addAttribute(String attribute){
		attributes.add(attribute);
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
		
	}

	@Override
	public boolean equals(Object obj) {
		QueryVO vo = (QueryVO)obj;
		if(type != vo.getType()){
			return false;
		}
		else if(!query.equals(vo.getQuery())){
			return false;
		}
		
		else if(numberColumnsToInsert != vo.getNumberColumnsToInsert()){
			return false;
		}
		else if(tables.size() != vo.getTables().size()){
			return false;
		}
		else if(attributes.size() != vo.getAttributes().size()){
			return false;
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return type + numberColumnsToInsert+ tables.hashCode() + attributes.hashCode()+query.hashCode();
	}

	 
	
	
	

}
