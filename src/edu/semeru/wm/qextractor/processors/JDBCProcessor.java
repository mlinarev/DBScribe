package edu.semeru.wm.qextractor.processors;

import java.awt.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import edu.semeru.wm.qextractor.helper.ASTHelper;
import edu.semeru.wm.qextractor.helper.SQLParser;
import edu.semeru.wm.qextractor.model.MethodQueryVO;
import edu.semeru.wm.qextractor.model.MethodVO;
import edu.semeru.wm.qextractor.model.QueryType;
import edu.semeru.wm.qextractor.model.QueryVO;


public class JDBCProcessor {
	
	private Hashtable<String, String> variables;
	private HashSet<String> queries;
	private SQLParser parser = new SQLParser();
	private HashMap<String, HashSet<String>> methodCalls;
	private HashSet<String> methodSignatures;
	private int totalQueries;
	private int errorQueries;
	
	
	

	private HashMap<String, MethodQueryVO> methodQueriesMap;
	private HashMap<String, MethodVO> allMethods;
	
	
	public String processStringLiteral(String literal){
		String data[] = literal.split("\\+");
		StringBuffer newLiteral = new StringBuffer();
		for(String segment : data){
			if(variables.containsKey(segment.trim())){
				newLiteral.append(" ").append(variables.get(segment.trim())).append(" ");
			}else{
				newLiteral.append(segment);
			}
			
		}
		return newLiteral.toString();
	}
	
	public void processFolder(String folderPath){
		methodCalls = new HashMap<String, HashSet<String>>();
		methodSignatures = new HashSet<String>();
		methodQueriesMap = new HashMap<String, MethodQueryVO>();
		allMethods = new HashMap<String, MethodVO>();
		
		totalQueries = 0;
		errorQueries = 0;
		
		Collection<File> files = FileUtils.listFiles(new File(folderPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
		for (File file : files) {
			if(file.getName().endsWith(".java")){
				processFile(file.getAbsolutePath(), folderPath);
			}
		}
		
	}
	
	private void processFile(String filePath, String projectPath){
		net.sf.jsqlparser.statement.Statement statement = null;
		char queryType='E';
		int queryTypeId;
		BufferedReader reader = null;
		StringBuffer source = new StringBuffer();
		List<String> lines = new ArrayList<String>();
		String unitName = filePath.substring(filePath.lastIndexOf(File.separator)+1).replace(".java", "");
		Block block = null;
		List<Statement> statements = null;
	
		String pckName = null;
		String className = null;
		String commentStr = null;
		int methodDeclarationArgs = 0;
		String methodArgsType = null;
		String strStatement = null;
		HashSet<String> tables = null;
		List<Column> columns = null;
		MethodQueryVO methodQueryVO = null;
		QueryVO queryVO = null;
		List tempArray = null;
		String methodSignature = null;
		String sourceCode = null;
		
		
		
		
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while((line = reader.readLine()) != null){
				lines.add(line);
				source.append(line).append("\n");
			}
			reader.close();
			
			CompilationUnit cu = ASTHelper.getASTAndBindings(source.toString(), projectPath, unitName);
			
			IProblem[] problems = cu.getProblems();

				/*for (IProblem problem : problems) {
				System.err.println(problem.toString());
			}*/
			
			if(cu.getPackage() != null){
				pckName = cu.getPackage().getName().toString();
			}
			else{
				pckName = "";
			}
			List<MethodDeclaration> methods = ASTHelper.getMethodDeclarationsFromCU(cu);
			
			methodCalls.putAll(ASTHelper.getMethodCallsFromCU(cu));
			for (MethodDeclaration methodDeclaration : methods) {
				methodArgsType = "";
				methodQueryVO = new MethodQueryVO();
				
				sourceCode = methodDeclaration.toString().replace("\n", " ").replaceAll("\\s+", " ");
				
				Javadoc comment = methodDeclaration.getJavadoc();
			    commentStr = "";
				if(comment != null){
					commentStr = comment.toString().replace("/**", "").replace("*/", "").replace("*", " ").replace("\n", " ").replace(","," ").replaceAll("\\s+"," ").trim();
				}
				
				methodDeclarationArgs = 0;
				tempArray = methodDeclaration.parameters();
				if(tempArray != null){
					methodDeclarationArgs = tempArray.size();
					for(Object arg: tempArray){
						
						try{
							methodArgsType += ((SingleVariableDeclaration)arg).getType().resolveBinding().getName()+",";
						}catch(Exception ex){
							methodArgsType += "*,";
						}
					}
					if(!methodArgsType.isEmpty()){
						methodArgsType = methodArgsType.substring(0,methodArgsType.length()-1);
					}
					
				}
				if(methodDeclaration.getParent() instanceof TypeDeclaration){
					className = ((TypeDeclaration)methodDeclaration.getParent()).getName().toString();
				}else if(methodDeclaration.getParent() instanceof EnumDeclaration){
					className = ((EnumDeclaration)methodDeclaration.getParent()).getName().toString();
				}
				
				methodSignature = pckName+"."+className+"|"+methodDeclaration.getName().getFullyQualifiedName()+"("+methodArgsType+")";
				
				
				methodQueryVO.setSignature(methodSignature);
				methodQueryVO.setKey(pckName+"."+className+"|"+methodDeclaration.getName().getFullyQualifiedName()+"|"+methodArgsType);
				methodQueryVO.setName(methodDeclaration.getName().getFullyQualifiedName());
				methodQueryVO.setnArgs(methodDeclarationArgs);
				methodQueryVO.setComment(commentStr);
				methodQueryVO.setMethodSnippetHash(org.apache.commons.codec.digest.DigestUtils.md5Hex(sourceCode));
				methodQueryVO.setClassName(className);
				methodQueryVO.setPackageName(pckName);
				
				
				methodSignatures.add(methodQueryVO.getKey());
				allMethods.put(methodQueryVO.getKey(), methodQueryVO.getMethodVO());
				
					variables = new Hashtable<String, String>();
				queries = new HashSet<String>();
				
				block = methodDeclaration.getBody();
				if(block != null){
					statements = block.statements();
					
					//Filter statements
					for (Statement stmt : statements) {
						 getStatements(stmt);
					}
					
					for(String query: queries){
						columns = new ArrayList<Column>();
						queryVO = new QueryVO();
						query = query.replace("()", "");
						queryVO.setQuery(query);
						
						try{
							statement = parser.getStatement(query);
							statement.accept(parser);
							tables = parser.getTables();
							for(String table:tables){
								queryVO.getTables().add(table);
							}
							
							
							if(statement instanceof Select){
								queryType = 'S';
								queryTypeId = QueryType.SELECT.getId();
								
								 
							}
							else if(statement instanceof Update){
								queryType = 'U';
								columns =((Update)statement).getColumns();
								queryTypeId = QueryType.UPDATE.getId();
								
							}
							else if(statement instanceof Insert){
								queryType = 'I';
								columns = ((Insert)statement).getColumns();
								queryTypeId = QueryType.INSERT.getId();
								queryVO.setNumberColumnsToInsert( ((ExpressionList)((Insert) statement).getItemsList()).getExpressions().size());
								
							}
							else if(statement instanceof CreateTable){
								queryType = 'C';
								queryTypeId = QueryType.CREATE.getId();
								
							}
							else if(statement instanceof Drop){
								queryType = 'P';
								queryTypeId = QueryType.DROP.getId();
								queryVO.getTables().add(((Drop) statement).getType()+" "+ ((Drop)statement).getName());
								
							}else if(statement instanceof Delete){
								queryType = 'D';
								queryTypeId = QueryType.DELETE.getId();
								
								
								
							}else if(statement instanceof Truncate){
								queryType = 'T';
								queryTypeId = QueryType.TRUNCATE.getId();
								
							}else{
								queryType = 'A';
								queryTypeId = QueryType.ALTER.getId();
								
							}
							
						}catch(Exception ex){
							queryType = 'E';
							queryTypeId = QueryType.ERROR.getId();
							errorQueries++;
							
						}
						
						queryVO.setType(queryTypeId);
						
						
						if(columns == null){
							columns = new ArrayList<Column>();
						}
						
						for(Column column: columns){
							queryVO.addAttribute(column.getColumnName());
						}
						methodQueryVO.addQuery(queryVO);
						totalQueries++;
						
						
						
						if(tables == null){
							tables = new HashSet<String>();
						}
						
						//This is the output on console. Just for debugging purposes
						/*System.out.println(pckName+"|"+className+"|"+
						methodDeclaration.getName().getFullyQualifiedName()+"|"+
						methodDeclarationArgs+"|"+
						queryType+"|"+
						tables.toString()+"|"+
						query+"|"+
						commentStr+"|"+columns.toString());
						parser.clearTablesSet();*/
						
						
						
					}
					
					
				}
				if(methodQueryVO.getNonErrorQueriesNumber() > 0){
					methodQueriesMap.put(methodQueryVO.getKey(), methodQueryVO);
				}
			}
			
			
		} catch (FileNotFoundException e) {
			Logger.getLogger(JDBCProcessor.class.getName()).severe(
					" File not found " + filePath);


		} catch (IOException e) {
			Logger.getLogger(JDBCProcessor.class.getName()).severe(
					" Error reading/writing file" + filePath);
		}

		
	}
	
	

	private  List<Statement> getStatements(Statement stmt){
		List<Statement> statements = new ArrayList<Statement>();
		List<Statement> recursiveStmts = new ArrayList<Statement>();
		ITypeBinding bind = null;
		Type type = null;
		String typeStr =null;
		String methodName = null;
		String varName = null;
		Expression exprTemp = null;
		Expression exprTemp2 = null;
		Expression conditionExpr = null;
		List fragments = null;
		List arguments = null;
		String string2Add = null;
		
		if(stmt == null){
			return statements;
		}
		
		if(stmt instanceof EnhancedForStatement){
			statements.addAll(getStatements( ((EnhancedForStatement) stmt).getBody()));	
		}else if (stmt instanceof SwitchStatement){
			recursiveStmts =((SwitchStatement) stmt).statements();
			for(Statement stm2: recursiveStmts){
				statements.addAll(getStatements(stm2));
			}
			
		}
		else if(stmt instanceof ForStatement){
			statements.addAll(getStatements( ((ForStatement) stmt).getBody()));	
		}else if(stmt instanceof DoStatement){
			statements.addAll(getStatements( ((DoStatement) stmt).getBody()));	
		}else if(stmt instanceof IfStatement){
			conditionExpr = ((IfStatement) stmt). getExpression();
			if(conditionExpr instanceof InfixExpression){
				
				Object leftOperand = ((InfixExpression)conditionExpr).getLeftOperand() ;
				Object rightOperand = ((InfixExpression)conditionExpr).getRightOperand() ;
				if(leftOperand instanceof MethodInvocation){
					processMethodInvocation(stmt, statements, (Expression)leftOperand);
					
				}
				if(rightOperand instanceof MethodInvocation){
					processMethodInvocation(stmt, statements, (Expression)rightOperand);
					
				}
			}
			statements.addAll(getStatements( ((IfStatement) stmt).getThenStatement()));
			statements.addAll(getStatements( ((IfStatement) stmt).getElseStatement()));
		}else if(stmt instanceof TryStatement){
			statements.addAll(getStatements( ((TryStatement) stmt).getBody()));	
		}else if(stmt instanceof WhileStatement){
			statements.addAll(getStatements( ((WhileStatement) stmt).getBody()));	
		}
		
		
		else if(stmt instanceof Block){
			recursiveStmts = ((Block)stmt).statements();
			for(Statement blockStmt: recursiveStmts){
				statements.addAll(getStatements( blockStmt));
			}
		
		}else if( stmt instanceof VariableDeclarationStatement){
			
			type = ((VariableDeclarationStatement)stmt).getType();
		    typeStr = type.toString();
		    fragments = ((VariableDeclarationStatement)stmt).fragments();
			exprTemp = ((VariableDeclarationFragment)fragments.get(0)).getInitializer();
		
			varName = ((VariableDeclarationFragment)fragments.get(0)).getName().toString();
			
			if(exprTemp instanceof MethodInvocation){
				processMethodInvocation(stmt, statements,  exprTemp);
			}
			
			
			else if(typeStr.equals("String")){
				
				if(exprTemp instanceof StringLiteral || exprTemp instanceof InfixExpression){
					statements.add(stmt);
					
					variables.put(varName, processStringLiteral(exprTemp.toString()));
				}else if(exprTemp instanceof ClassInstanceCreation){
					arguments = ((ClassInstanceCreation)exprTemp).arguments();
					if(arguments.size() == 0){
						variables.put(varName, "");
					}
					else{
						variables.put(varName, ((Expression)arguments.get(0)).toString());
					}
					statements.add(stmt);
				}
				
			}else if(typeStr.equals("StringBuffer") && exprTemp instanceof ClassInstanceCreation){
				arguments = ((ClassInstanceCreation)exprTemp).arguments();
				if(arguments.size() == 0){
					variables.put(varName, "");
				}
				else{
					variables.put(varName, ((Expression)arguments.get(0)).toString().trim());
				}
				
				statements.add(stmt);
			}
			
		}else if( stmt instanceof ExpressionStatement){
			exprTemp = ((ExpressionStatement)stmt).getExpression();
			
			
			
			if(exprTemp instanceof MethodInvocation){
				exprTemp2 = ((MethodInvocation)exprTemp).getExpression();
				
				if(exprTemp2 != null && exprTemp2.resolveTypeBinding()!=null){
					bind = exprTemp2.resolveTypeBinding();
					
					typeStr = bind.getName();
					methodName = ((MethodInvocation)exprTemp).getName().toString();
					
					
					if((typeStr.equals("Statement") || bind.getName().equals("LiferayStatement") ) && (methodName.equals("addBatch") || methodName.equals("execute") || methodName.equals("executeQuery") || methodName.equals("executeUpdate"))){
						extractQuery((MethodInvocation)exprTemp);
						statements.add(stmt);
					}
					else if((typeStr.equals("Connection") || typeStr.equals("LiferayConnection") ) && methodName.equals("prepareStatement")){
						extractQuery((MethodInvocation)exprTemp);
						statements.add(stmt);
					}else if(typeStr.equals("StringBuffer") && methodName.equals("append")){
						String tempQuery = new String();
						do{
							arguments = ((MethodInvocation)exprTemp).arguments();
							if(variables.containsKey(	((Expression)arguments.get(0)).toString().trim() )){
								tempQuery = variables.get(((Expression)arguments.get(0)).toString().trim()) + " "+tempQuery;
							}
							else{
								tempQuery = ((Expression)arguments.get(0)).toString().trim()+" "+tempQuery;
							}
							if(exprTemp2 instanceof SimpleName){
								break;
							}
							try{
								exprTemp = ((MethodInvocation)exprTemp2);
							}catch(Exception ex){
								break;
							}
							exprTemp2 = ((MethodInvocation)exprTemp).getExpression();
							
						}while(exprTemp instanceof MethodInvocation);
						varName = exprTemp2.toString();
						
						variables.put(varName, variables.get(varName)+ tempQuery.toString());
						statements.add(stmt);
					}
					//Spring-framework
					else if((methodName.startsWith("execute") || methodName.startsWith("query")  || methodName.startsWith("update") ) && typeStr.equals("JdbcTemplate") ){
						extractQuery((MethodInvocation)exprTemp);
						statements.add(stmt);
					}
					//liminal SQLutilities
					else if((methodName.startsWith("addSql") || methodName.startsWith("query")  ) && (typeStr.equals("SQLUtilities") || typeStr.equals("Transaction"))){
						extractQuery((MethodInvocation)exprTemp);
						statements.add(stmt);
					}
				}
				
				
			}else if(exprTemp instanceof Assignment){
				exprTemp2 = ((Assignment)exprTemp).getLeftHandSide();
				
				if(exprTemp2 instanceof FieldAccess){
					bind = ((FieldAccess)exprTemp2).getName().resolveTypeBinding();
					varName = ((FieldAccess)exprTemp2).getName().toString();
				}
				else if (exprTemp2 instanceof ParenthesizedExpression){
					varName = ((ParenthesizedExpression)exprTemp2).getExpression().toString();
					bind  = ((ParenthesizedExpression)exprTemp2).getExpression().resolveTypeBinding();
				}
				else if (!(exprTemp2 instanceof ArrayAccess) && !(exprTemp2 instanceof SuperFieldAccess)){
					//Discarding the case of array accesses or superfield accesses
					bind = ((Name)((Assignment)exprTemp).getLeftHandSide()).resolveTypeBinding();
					
					varName = ((Name)((Assignment)exprTemp).getLeftHandSide()).toString();
				}
				
				
				if(bind != null){
					exprTemp2 = ((Assignment)exprTemp).getRightHandSide();
					typeStr = bind.getName();
					
					if(typeStr.equals("String")){
						string2Add = "";
						if (exprTemp2 instanceof InfixExpression || exprTemp2 instanceof StringLiteral){
							string2Add = processStringLiteral(exprTemp2.toString());
							 
							 
						}
						else if(exprTemp2 instanceof ClassInstanceCreation){
							arguments = ((ClassInstanceCreation)exprTemp2).arguments();
							if(arguments.size() > 0){
								string2Add = ((Expression)arguments.get(0)).toString();
							}
						}
						
						if(!((Assignment)exprTemp).getOperator().equals(Assignment.Operator.PLUS_ASSIGN) && !string2Add.isEmpty()){
							variables.put(varName, string2Add);
						}
						else if(variables.get(varName) != null){
							 variables.put(varName, variables.get(varName)+ string2Add);
						}
						
						
						
						
						
						statements.add(stmt);
					}
					else if(typeStr.equals("StringBuffer")){
						string2Add = "";
						if(exprTemp2 instanceof MethodInvocation){
							arguments = ((MethodInvocation)exprTemp2).arguments();
							
							
							methodName = ((MethodInvocation)exprTemp2).getName().toString();
							if(methodName.equals("append")){
								string2Add = ((Expression)arguments.get(0)).toString();
							}
							
						}else if(exprTemp2 instanceof ClassInstanceCreation){
							arguments = ((ClassInstanceCreation)exprTemp2).arguments();
							if(arguments.size() > 0){
								string2Add = ((Expression)arguments.get(0)).toString();
							}
						}
						
						if(variables.get(varName) != null){
							 variables.put(varName, variables.get(varName)+ string2Add);
						}else{
							variables.put(varName, string2Add);
						}
						
						statements.add(stmt);
					}else if(exprTemp2 instanceof MethodInvocation){
						try{
							methodName = ((MethodInvocation)exprTemp2).getName().toString();
							bind = ((MethodInvocation)exprTemp2).getExpression().resolveTypeBinding();
						}catch(Exception e){
							bind = null;
						}
						if(bind != null){
							typeStr = bind.getName();
							if((typeStr.equals("Statement") || bind.getName().equals("LiferayStatement") ) && methodName.startsWith("execute")){
								extractQuery((MethodInvocation)exprTemp2);
								statements.add(stmt);
							}
							else if((typeStr.equals("Connection")|| typeStr.equals("LiferayConnection") ) && methodName.equals("prepareStatement")){
								extractQuery((MethodInvocation)exprTemp2);
								statements.add(stmt);
							}
							//Spring-framework
							else if((methodName.startsWith("execute") || methodName.startsWith("query")  || methodName.startsWith("update") ) && typeStr.equals("JdbcTemplate") ){
								extractQuery((MethodInvocation)exprTemp2);
								statements.add(stmt);
							}
							//liminal SQLutilities
							else if((methodName.startsWith("addSql") || methodName.startsWith("query")  ) && (typeStr.equals("SQLUtilities") || typeStr.equals("Transaction"))){
								extractQuery((MethodInvocation)exprTemp2);
								statements.add(stmt);
							}
						}
						
						
					}
					
				}
			}	
			
		}
		
		
		return statements;
	}

	private void processMethodInvocation(Statement stmt,
			List<Statement> statements, Expression exprTemp) {
		ITypeBinding bind;
		String methodName;
		methodName =  ((MethodInvocation)exprTemp).getName().toString();
		try{
			bind = ((MethodInvocation)exprTemp).getExpression().resolveTypeBinding();
			
		}catch(Exception ex){
			bind = null;
		}
		if(bind !=null){	
			if( (methodName.equals("addBatch") || methodName.equals("execute") || methodName.equals("executeQuery") || methodName.equals("executeUpdate") )&& (bind.getName().equals("Statement") || bind.getName().equals("LiferayStatement") )){
				extractQuery((MethodInvocation)exprTemp);
				statements.add(stmt);
			}
			else if(methodName.startsWith("prepareStatement") && (bind.getName().equals("Connection")|| bind.getName().equals("LiferayConnection") )){
				extractQuery((MethodInvocation)exprTemp);
				statements.add(stmt);
			}
			//Spring-framework
			else if((methodName.startsWith("execute") || methodName.startsWith("query")  || methodName.startsWith("update") ) && bind.getName().equals("JdbcTemplate") ){
				extractQuery((MethodInvocation)exprTemp);
				statements.add(stmt);
			}
			//liminal SQLutilities
			else if((methodName.startsWith("addSql") || methodName.startsWith("query")  ) && (bind.getName().equals("SQLUtilities") || bind.getName().equals("Transaction"))){
				extractQuery((MethodInvocation)exprTemp);
				statements.add(stmt);
			}
		}
	}

	private void extractQuery(MethodInvocation expression) {
		
		List arguments = expression.arguments();
		if(arguments.size()>0){
			Expression exprTemp = (Expression)arguments.get(0);
			
			String query = null;
			if(exprTemp instanceof StringLiteral || exprTemp instanceof InfixExpression){
				query = exprTemp.toString();
				
			}
			else if(exprTemp instanceof Name){
				query = variables.get( ((Name)exprTemp).toString());
			}
			else if(exprTemp instanceof MethodInvocation && exprTemp.toString().endsWith("toString()")){
				query = variables.get( ((MethodInvocation)exprTemp).getExpression().toString());
			}
			if(query != null){
				queries.add(query.replace(" + ","").replace("+ ","").replace(" +","").replace("\"", "").replace("\\","").replace("+","").replace("`", "").replace("%s","undefined"));
			}
		}
	}
	
	
	
	public HashMap<String, MethodQueryVO> getMethodQueriesMap() {
		return methodQueriesMap;
	}

	public HashMap<String, MethodVO> getAllMethods() {
		return allMethods;
	}
	
	public HashSet<String> getMethodSignatures() {
		return methodSignatures;
	}

	public HashMap<String, HashSet<String>> getMethodCalls() {
		return methodCalls;
	}
	
	

	public int getTotalQueries() {
		return totalQueries;
	}

	public void setTotalQueries(int totalQueries) {
		this.totalQueries = totalQueries;
	}

	public int getErrorQueries() {
		return errorQueries;
	}

	public void setErrorQueries(int errorQueries) {
		this.errorQueries = errorQueries;
	}

	public static void main(String[] args){
		JDBCProcessor processor = new JDBCProcessor();
		//processor.processFile("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/BugReportDAO.java");
		//processor.processFile("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/ActionDAO.java");
		//processor.processFile("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/CS597-PROJECT/src/com/umas/code/People.java");
		processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/CS597-PROJECT/src/");
		//processor.processFolder("/Users/mariolinares/Documents/academy/SEMERU/Code-tools/Q-Extractor/examples4test/test/");
		//processor.processFolder("/Users/mariolinares/Downloads/jena-master/");
		
		
		
	}

}
