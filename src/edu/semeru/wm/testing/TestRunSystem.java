package edu.semeru.wm.testing;

import java.io.IOException;
import java.sql.SQLException;
import org.junit.Test;

import edu.semeru.wm.qextractor.processors.DBScribe;


public class TestRunSystem {

	private String splitter = System.getProperty("file.separator");
	
	//Change output folder before running any test cases
	private String outputFolder = "C:\\Users\\USBOLI\\Desktop\\DBScribeOutputs\\";
	
	private String host = "localhost";
	private String schema = "university";
	private String user = "root";
	private String passwd = "boyang";
	
	@Test
	public void TestUMASSystem(){
		String systemFolder = "Subjects" + splitter + "UMAS" + splitter + "src";
		String outputFile = outputFolder + splitter + "UMAS.html";
		try {
			DBScribe.runDBScribe(systemFolder, outputFile, host, schema, user, passwd);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("DONE");
	}


}
