package model;

public class PairDouble {
	private double first, sec;

	public PairDouble(double i, double j) {
		this.first = i;
		this.sec = j;
	}

	public PairDouble() {
	}
	
	public double getFirst(){
		return this.first;
	}
	
	public double getSec(){
		return this.sec;
	}
}