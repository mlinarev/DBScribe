package edu.semeru.wm.qextractor.model;

public class ConnectionVO {

	private String host;
    private String user;
    private String password;
    private String schemaName;
    
    
	public ConnectionVO(String host, String user, String password,
			String schemaName) {
		super();
		this.host = host;
		this.user = user;
		this.password = password;
		this.schemaName = schemaName;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
    
	
}
