package edu.semeru.wm.qextractor.model;

public enum QueryType {

	SELECT(1),
	INSERT(2),
	UPDATE(3),
	DELETE(4),
	DROP(5),
	TRUNCATE(6),
	CREATE(7),
	ALTER(8),
	
	ERROR(9);
	
	
	private final int id;
	
	QueryType(int id ) {
		this.id = id;
	}
	
	public int getId(){
		return this.id;
	}
	
}
