package model;

import java.util.ArrayList;

/*
 * each activity is indexed by the index of user i and the index of j, D_ij
 */
public class UserProfile {
	private int[] spatialItems;
	private String[] locations;
	private ArrayList<Integer>[] contents;
	// private ArrayList<ArrayList<Integer>> contents;
	private int[] ss;// s indicates the first index of 1
//	private int[] times;
	private int[] topics;
	private int asize;// indicate the actual size
	private double[] alphad;
	private double[] alphan;// with the u,t,s,l,z
	private double[][] alphans;// with the u,t,s,l,but with all other topics
	// private double[][] betad;
	// private double[][]

	public UserProfile(int size) {
		this.spatialItems = new int[size];
		this.locations = new String[size];
		this.contents = new ArrayList[size];
		for (int i = 0; i < size; i++) {
			contents[i] = new ArrayList<Integer>();
		}
		this.ss = new int[size];
//		this.times = new int[size];
		this.topics = new int[size];
		this.asize = 0;
		this.alphad = new double[size];
		this.alphan = new double[size];
		this.alphans = new double[size][Paras.K];
	}

	public void addOneRecord(int spatialItem, String location, int s, int time,
			ArrayList<Integer> content) {
		this.spatialItems[this.asize] = spatialItem;
		this.locations[this.asize] = location;
		this.contents[this.asize] = content;
		this.ss[this.asize] = s;
//		this.times[this.asize] = time;
		this.asize++;
	}

	public int getSize() {
		return this.asize;
	}

	public void setZ(int i, int topic) {
		this.topics[i] = topic;
	}

	public int getZ(int i) {
		return this.topics[i];
	}

	public String getL(int i) {
		return this.locations[i];
	}

	// public ArrayList<Integer> getW(int i){
	// return this.contents.get(i);
	// }
	public int getV(int i) {
		return this.spatialItems[i];
	}

	public int getS(int i) {
		return this.ss[i];
	}

//	public int getT(int i) {
//		return this.times[i];
//	}

	public double getAlphad(int i) {
		return this.alphad[i];
	}

	public void setAlphad(int i, double alphad) {
		this.alphad[i] = alphad;
	}

	public double getAlphan(int i) {
		return this.alphan[i];
	}

	public void setAlphan(int i, double logAlpha) {
		this.alphan[i] = logAlpha;
	}

	public double[] getAlphans(int i) {
		return this.alphans[i];
	}

	public void setAlphans(int i, double[] alphans) {
		this.alphans[i] = alphans;
	}

	public void setItem(int i, int item) {
		this.spatialItems[i] = item;
	}

	// public void setContents(int i, HashSet<Integer> contents){
	// this.contents[i]=contents;
	// }
	public ArrayList<Integer> getContents(int i) {
		return this.contents[i];
	}
}
