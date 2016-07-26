package model;

public class Pair {
	private int first, sec;

	public Pair(int i, int j) {
		this.first = i;
		this.sec = j;
	}

	public Pair() {
	}
	
	public int getFirst(){
		return this.first;
	}
	
	public int getSec(){
		return this.sec;
	}
}