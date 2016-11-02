package edu.semeru.wm.qextractor.model;

public class KeyVO {

	
	private String referencedColumn;
	private String referencedTable;
	private String columnName;
	
	
	
	
	public KeyVO( String referencedTable, String referencedColumn,
			String columnName) {
		
		this.referencedColumn = referencedColumn;
		this.referencedTable = referencedTable;
		this.columnName = columnName;
	}
	
	public String getReferencedColumn() {
		return referencedColumn;
	}
	public void setReferencedColumn(String referencedColumn) {
		this.referencedColumn = referencedColumn;
	}
	public String getReferencedTable() {
		return referencedTable;
	}
	public void setReferencedTable(String referencedTable) {
		this.referencedTable = referencedTable;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
	

}
