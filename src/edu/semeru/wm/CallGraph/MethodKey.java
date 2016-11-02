package edu.semeru.wm.CallGraph;

import java.util.HashMap;

/**
 * 
 * A method key using for map finding
 * <name, numofparas>
 * @author Boyang
 *
 */
public class MethodKey{
	// method name 
	public final String funcName;

	//num of parameters
	public final int NumPara;


	public MethodKey(String key1, int key2) {
		this.funcName = key1;
		this.NumPara = key2;
	}


	@Override   
	public boolean equals(Object o) {
		if(o instanceof MethodKey){
			MethodKey ref = (MethodKey) o;
			return this.funcName.equals(ref.funcName) && this.NumPara == ref.NumPara;
		}else{
			return false;
		}
	}

	@Override
	public String toString() {
		return "";
	}
	
	
	@Override
	public int hashCode()
	{
        int hashcode = 0;
        hashcode = NumPara*20;
        hashcode += funcName.hashCode();
        return hashcode;
	}
	
	
	public static void main(String [] args){
		System.out.println("key testing ");
		String s1 = "abc";
		String s2 = "abc";
		System.out.println(s1.compareTo(s2));

		MethodKey key1 = new MethodKey("abc" , 2);
		MethodKey key2 = new MethodKey("def" , 2);
		MethodKey key3 = new MethodKey("abc" , 2);
		MethodKey key4 = new MethodKey("def" , 1);
		System.out.println("key1 == key2 : " + (key1 == key2));
		System.out.println("key1 == key3 : " + (key1 == key3));
		System.out.println("key1 == key4 : " + (key1 == key4));
		System.out.println("key2 == key4 : " + (key2 == key4));

		HashMap <MethodKey , Integer> map = new HashMap <MethodKey , Integer>();
		map.put(key1, 1);

		map.put(key3, 5);
		System.out.println("key 1 : " + map.get( new MethodKey("abc" , 2)));

	}

}
