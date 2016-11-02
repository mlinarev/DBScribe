package edu.semeru.wm.qextractor.helper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import edu.semeru.wm.qextractor.model.ConnectionVO;

public class ConnectionManager {

	public static Connection getConnection(ConnectionVO conn) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
        Class.forName("com.mysql.jdbc.Driver").newInstance();
          return DriverManager.getConnection(
                         "jdbc:mysql://"+conn.getHost()+"/"+conn.getSchemaName(),
                         conn.getUser(),
                         conn.getPassword());
    }
}
