package edu.semeru.wm.qextractor.model;

public class MethodVO {

	private String name;
	private String packageName;
	private String className;
	private int nArgs;
	private String key;
	private String signature;
	
	private String methodSnippetHash;
	private String comment;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public int getnArgs() {
		return nArgs;
	}
	public void setnArgs(int nArgs) {
		this.nArgs = nArgs;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getMethodSnippetHash() {
		return methodSnippetHash;
	}
	public void setMethodSnippetHash(String methodSnippetHash) {
		this.methodSnippetHash = methodSnippetHash;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
 

}
