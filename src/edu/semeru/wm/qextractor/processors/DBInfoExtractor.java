package edu.semeru.wm.qextractor.processors;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.semeru.wm.qextractor.helper.ConnectionManager;
import edu.semeru.wm.qextractor.model.ColumnVO;
import edu.semeru.wm.qextractor.model.ConnectionVO;
import edu.semeru.wm.qextractor.model.KeyVO;
import edu.semeru.wm.qextractor.model.TableConstraintsVO;
import edu.semeru.wm.qextractor.model.TableVO;

public class DBInfoExtractor {
	
	private Connection connection;
	
	private HashMap<String, TableVO> tables;
	
	private HashMap<String, TableConstraintsVO> constraints;
	
	private HashMap<String, List<String>> foreignKeyTables;
	
	private HashMap<String, List<String>> columns;
	
	
	
	public HashMap<String, TableVO> getTables() {
		return tables;
	}

	
	public Connection getConnection() {
		return connection;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public List<String> getTablesNames(String schemaName) throws SQLException{
		List<String> names = new  ArrayList<String>();
		String query = "select table_name as object_name "
                + "from information_schema.tables "
                + "where table_schema = '" + schemaName + "' and table_type = 'BASE TABLE'";
		Statement stm = this.connection.createStatement();
        ResultSet rs = stm.executeQuery(query);
        while (rs.next()) {
	            names.add(rs.getString("object_name"));	            
        }
	        
        stm.close();
        return names; 
	}
	
	public ResultSet getColumnsResultSet(String schemaName, String tableName) throws SQLException{
		List<ColumnVO> columns = new ArrayList<ColumnVO>();
		Statement stm = this.connection.createStatement();
		String query = getColumnQuery(tableName, schemaName);
        ResultSet rs = stm.executeQuery(query);
        
        return rs; 
	}
	
	public ResultSet getConstraintsResultSet(String schemaName, String tableName, String colName) throws SQLException{
		List<ColumnVO> columns = new ArrayList<ColumnVO>();
		Statement stm = this.connection.createStatement();
		String query = getConstraintQuery(schemaName, tableName, colName);
        ResultSet rs = stm.executeQuery(query);
        
        return rs; 
	}

	public void processTables(String schemaName) throws SQLException  {

        TableVO table = null;
        ColumnVO column = null;

        tables = new HashMap<String, TableVO>();
        constraints = new HashMap<String, TableConstraintsVO>();
        foreignKeyTables = new HashMap<String, List<String>>();
        columns = new HashMap<String, List<String>>();
        
        int numberColumns = 0;
        ResultSet columnsResult = null;
        ResultSet constraintsResult = null;

       
        List<String> tablesNames = getTablesNames(schemaName);
       
        if (tablesNames != null && !tablesNames.isEmpty()) {
            String extra = null;
            String auxKeyType = null;
            String keyType = null;
            String isNullable = null;
            String constraintName = null;
            String totalReferenced = null;
            String auxTable = null;
            String auxCol = null;
            Object precision = null;
            Object numericScale = null;
            Object charMaxLength = null;
            TableConstraintsVO constraintsVO = null;
            
            
            for (String name : tablesNames) {
            	constraintsVO = new TableConstraintsVO();
                table = new TableVO();
                table.setName(name);
                constraintsVO.setTableName(name);
                
                columns.put(name, new ArrayList<String>());
                columnsResult = getColumnsResultSet(schemaName, name);
                
                
                while(columnsResult.next()){
                	numberColumns ++;
                    extra = "";
                    auxKeyType = "";
                    keyType = "";
                    isNullable = "";

                    column = new ColumnVO();
                   
                    column.setName((String) columnsResult.getString("column_name"));
                    column.setDefaultValue((String) columnsResult.getString("column_default"));
                    
                    isNullable = (String) columnsResult.getString("is_nullable");
                    column.setNullable(isNullable.equals("YES")?true:false);
                    precision =columnsResult.getObject("numeric_precision");
                    
                    if(!column.getNullable()){
                    	constraintsVO.addNoNullField(column.getName());
                    }
                    
                    if (precision != null) {
                        
                        if(precision instanceof Long){
                        	column.setNumericTypePrecision(String.valueOf((Long) precision));
                        }
                        
                        else if(precision instanceof Integer){
                            column.setNumericTypePrecision(String.valueOf((Integer) precision));
                        }
                        else if(precision instanceof BigInteger){
                            column.setNumericTypePrecision(String.valueOf((BigInteger) precision));
                        }
                    }
                    
                    numericScale = columnsResult.getObject("numeric_scale");
                    if (numericScale != null) {
                        if(numericScale instanceof Long){
                        	column.setNumericTypeScale(String.valueOf((Long) numericScale));
                        }
                        else if( numericScale instanceof Integer){
                            column.setNumericTypeScale(String.valueOf((Integer) numericScale));
                        }
                        else if( numericScale instanceof BigInteger ){
                            column.setNumericTypeScale(String.valueOf((BigInteger) numericScale));
                        }
                    }
                    
                    charMaxLength = columnsResult.getObject("character_maximum_length");
                    if (charMaxLength != null) {
                        if(charMaxLength instanceof Long){
                        column.setCharMaxLength(String.valueOf((Long) charMaxLength));
                        }
                        else if( charMaxLength instanceof Integer){
                            column.setCharMaxLength(String.valueOf((Integer) charMaxLength));
                        }
                        else if( charMaxLength instanceof BigInteger){
                            column.setCharMaxLength(String.valueOf((BigInteger) charMaxLength));
                        }
                        
                        constraintsVO.addVarcharConstraint(Long.parseLong(column.getCharMaxLength()), column.getName());
                        
                        
                    }
                    
                    column.setType((String) columnsResult.getString("column_type"));

                    extra = (String) columnsResult.getString("extra");

                    if (extra.equals("auto_increment")) {
                        column.setAutoNumeric(true);
                        constraintsVO.addAutonumericField(column.getName());
                    } else {
                        column.setAutoNumeric(false);
                    }

                    //Foreign keys
                    constraintsResult = getConstraintsResultSet(schemaName, name, column.getName());

                    if(constraintsResult != null) {
                        constraintName = "";
                        totalReferenced = "";
                        auxTable = "";
                        auxCol = "";

                        while(constraintsResult.next()) {
                            
                            keyType = columnsResult.getString("column_key");

                            
                            auxTable = constraintsResult.getString("referenced_table_name");
                            auxCol = constraintsResult.getString("referenced_column_name");

                            if (auxTable == null && auxCol == null) {
                                totalReferenced += name + "." + column.getName()+" ";
                                
                                keyType = "PK";
                                constraintsVO.addPrimayKey(column.getName());
                            } else {
                                if (!auxTable.equals(name)) {
                                    keyType = "FK";
                                    constraintsVO.addForeignKey(new KeyVO(auxTable, auxCol,column.getName()));
                                    
                                    if(!foreignKeyTables.containsKey(auxTable+"."+auxCol)){
                                    	foreignKeyTables.put(auxTable+"."+auxCol, new ArrayList<String>());
                                    }
                                    foreignKeyTables.get(auxTable+"."+auxCol).add(name);
                                }

                                totalReferenced += auxTable + "." + auxCol+" ";

                                
                            }

                            if (keyType.equals("UNI") || keyType.equals("PK")) {
                                keyType = "UNIQUE";
                                constraintsVO.adUniqueField(column.getName());
                            }

                            constraintName += constraintsResult.getString("constraint_name")+" ";
                            auxKeyType += keyType+" ";

                            column.setKeyType(auxKeyType);
                            column.setKeyName(constraintName);
                            column.setReferencedTable(totalReferenced);
                        }
                    } 

                    columns.get(name).add(column.getName());
                    constraintsResult.getStatement().close();
                    table.getColumns().put(column.getName(), column);
                }

                

                tables.put(name, table);
                
                constraints.put(name,constraintsVO);
                columnsResult.getStatement().close();
            }
        }
        
        System.out.println("-- Number of attributes in tables: "+numberColumns);
    }

    public HashMap<String, List<String>> getColumns() {
		return columns;
	}


	private String getConstraintQuery(final String schemaName, final String tableName, final String colName) {
        return "select key_column_usage.constraint_name,"
                + " key_column_usage.referenced_table_name,"
                + " key_column_usage.referenced_column_name"
                + " FROM information_schema.key_column_usage"
                + " where key_column_usage.table_schema= '" + schemaName + "' "
                + " and key_column_usage.table_name= '" + tableName + "' "
                + " and key_column_usage.column_name='" + colName + "';";
    }

    private String getColumnQuery(final String tableName, final String schema) {
        return "select  columns.table_schema,"
                + " columns.table_name,"
                + " columns.column_key,"
                + " columns.column_default,"
                + " columns.column_name,"
                + " columns.is_nullable,"
                + " columns.numeric_precision,"
                + " columns.numeric_scale,"
                + " columns.column_type,"
                + " columns.character_maximum_length,"
                + " columns.extra"
                + " from information_schema.columns"
                + " where table_schema = '" + schema + "'"
                + " and table_name = '" + tableName + "';";
    }

    
    
   
    public HashMap<String, TableConstraintsVO> getConstraints() {
		return constraints;
	}

	public HashMap<String, List<String>> getForeignKeyTables() {
		return foreignKeyTables;
	}
	
	

	

	public static void main(String args[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
    	DBInfoExtractor dbie = new DBInfoExtractor();
    	ConnectionVO vo = new ConnectionVO("localhost", "fusion", "fusion12345", "fusion");
    	dbie.setConnection(ConnectionManager.getConnection(vo));
    	dbie.processTables(vo.getSchemaName());
    	
    }

}
