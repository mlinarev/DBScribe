package edu.semeru.wm.qextractor.helper;

import java.io.StringReader;
import java.util.HashSet;
import java.util.Iterator;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.Union;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class SQLParser  implements SelectVisitor, FromItemVisitor, StatementVisitor{

	private HashSet<String> tables;
	private CCJSqlParserManager pm;
	
	
	public SQLParser(){
		clearTablesSet();
		pm = new CCJSqlParserManager();
	}
	
	
	public Statement getStatement(String query) throws JSQLParserException{
		clearTablesSet();
		return pm.parse(new StringReader(query));
	}
	
	public HashSet<String> getTables() {
		return tables;
	}



	public void setTables(HashSet<String> tables) {
		this.tables = tables;
	}



	public void clearTablesSet(){
		tables = new HashSet<String>();
	}
	
	/**
	 * @param args
	 * @throws JSQLParserException 
	 */
	public static void main(String[] args) throws JSQLParserException {
		CCJSqlParserManager pm = new CCJSqlParserManager();
		String sql = "SELECT * FROM MY_TABLE1, MY_TABLE2, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 "+
		" WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)" ;
		
		sql="Delete from waitlistDelete from emailedwaitlist";
		Statement statement = pm.parse(new StringReader(sql));

		SQLParser tablesNamesFinder = new SQLParser();
		statement.accept(tablesNamesFinder);
		
		System.out.println(tablesNamesFinder.getTables().toString());
		

	}



	@Override
	public void visit(Table arg0) {
		tables.add(arg0.getWholeTableName());
		
	}



	@Override
	public void visit(SubSelect arg0) {
		arg0.getSelectBody().accept(this);
		
	}



	@Override
	public void visit(SubJoin arg0) {
		arg0.getLeft().accept(this);
		arg0.getJoin().getRightItem().accept(this);
		
	}



	@Override
	public void visit(PlainSelect arg0) {
		arg0.getFromItem().accept(this);
		
		if (arg0.getJoins() != null) {
			for (Iterator joinsIt = arg0.getJoins().iterator(); joinsIt.hasNext();) {
				Join join = (Join) joinsIt.next();
				join.getRightItem().accept(this);
			}
		}
		
		//if (arg0.getWhere() != null)
		//	arg0.getWhere().accept(this);

		
	}



	@Override
	public void visit(Union arg0) {
		for (Iterator iter = arg0.getPlainSelects().iterator(); iter.hasNext();) {
			PlainSelect plainSelect = (PlainSelect) iter.next();
			visit(plainSelect);
		}
		
	}



	@Override
	public void visit(Select arg0) {
		arg0.getSelectBody().accept(this);
		
	}



	@Override
	public void visit(Delete arg0) {
		tables.add(arg0.getTable().getWholeTableName());
		
	}



	@Override
	public void visit(Update arg0) {
		tables.add(arg0.getTable().getWholeTableName());
		
	}



	@Override
	public void visit(Insert arg0) {
		tables.add(arg0.getTable().getWholeTableName());
		
	}



	@Override
	public void visit(Replace arg0) {
	}



	@Override
	public void visit(Drop arg0) {
		
	}



	@Override
	public void visit(Truncate arg0) {
		tables.add(arg0.getTable().getWholeTableName());
		
	}



	@Override
	public void visit(CreateTable arg0) {
		tables.add(arg0.getTable().getWholeTableName());
		
	}

	



}
