package model;

import java.util.HashSet;

/*
 * this class stores all the parameters in the model
 */
public class Paras {
	public static final int unratedItemSize=200;
	public static int K=100;
	public static int k=20;
	public static int H=7;
	public static String timeGran="season";
	public static int iter=250;
	public static double termate=1e-3;
	public static final int MAXTHREADSL = 64;
	public static final int MAXTHREADSM = 32; 
	public static HashSet<String> qcs;
	static{
		qcs=new HashSet<String>();
		qcs.add("NY");
		qcs.add("IL");
		qcs.add("PA");
		qcs.add("FL");
		qcs.add("MA");
		qcs.add("NJ");
		qcs.add("GA");
		qcs.add("MO");
		qcs.add("MI");
		qcs.add("WI");
		qcs.add("LA");
		qcs.add("NC");
		qcs.add("VA");
		qcs.add("OH");
		qcs.add("MD");
	}
}
