package model;

import edu.stanford.nlp.optimization.DiffFunction;

public class DiffFunctionPhi0 implements DiffFunction {
	private GeoGMNew gm;
	private double[] phi0;
	private double[] betads;
	private double[][] betanzs;
	private double normal=1;

	public DiffFunctionPhi0() {
		this.gm = GeoGMNew.getGM();
		this.betads = new double[Paras.K];
		this.betanzs = new double[Paras.K][gm.W];
	}

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return gm.W;
	}

	@Override
	public double valueAt(double[] arg0) {
		// TODO Auto-generated method stub
		this.phi0 = arg0;
		// intialize betads
		for (int z = 0; z < Paras.K; z++) {
			double betadz = 0;
			for (int ww = 0; ww < arg0.length; ww++) {
				double value = this.getBetaN(z, ww);
				// System.out.println("betaN: "+value);
				betadz += value;
				// System.out.println("betads "+z+": "+betadz);
			}
			this.betads[z] = betadz;
		}
		double sum = 0;
		for (int z = 0; z < Paras.K; z++) {
			double betad = this.betads[z];
			for (int w = 0; w < gm.W; w++) {
				double temp = this.phi0[z] + gm.phitopic[z][w];
				double sumtemp = gm.dzw[z][w] * (Math.log(betad) - temp);
				sum += sumtemp;
			}
		}
		// System.out.println("sum is:"+sum);
		return sum*normal;
	}

	@Override
	public double[] derivativeAt(double[] arg0) {
		this.phi0 = arg0;
		// int count=0;
		// for(double value:this.phi0){
		// if(count%100==0)
		// System.out.println();
		// System.out.print(value+"\t");
		// count++;
		// }
		// intialize betads
		for (int z = 0; z < Paras.K; z++) {
			double betadz = 0;
			for (int ww = 0; ww < arg0.length; ww++) {
				betadz += this.getBetaN(z, ww);
			}
			this.betads[z] = betadz;
		}
		double[] r = new double[arg0.length];
		
		double max=0;
		for (int w = 0; w < arg0.length; w++) {
			double betadz = 0;
			for (int z = 0; z < Paras.K; z++) {
				betadz += (gm.dz[z] * this.getBeta(z, w));
			}
			r[w] = (betadz - gm.dw[w]);
			if(Math.abs(r[w])>max){
				max=Math.abs(r[w]);
			}
		}
		if(normal==1){
			if(max<=50){
				normal=0.9;
			}
			//get normal
			else if(max>50&&max<100){
				normal=0.1;
			}
			else if(max>=100&&max<1000){
				normal=0.01;
			}
			else if(max>=1000&&max<1e4){
				normal=1e-3;
			}
			else if(max>=1e4 && max<1e5){
				normal=1e-4;
			}
			else{
				normal=1e-5;
			}
		}
		for(int w=0;w<arg0.length;w++){
			r[w]*=normal;
		}
		return r;
	}

	// private class BetaSetter implements Runnable{
	//
	// @Override
	// public void run() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// }
	// public void setBet
	public double getBeta(int z, int w) {
		double betan = this.getBetaN(z, w);
		return betan / this.betads[z];
	}

	public double getBetaN(int z, int w) {
		double exp = 0;
		// System.out.println(this.phi0[w]+"\t"+gm.phitopic[z][w]);
		exp = (this.phi0[w] + gm.phitopic[z][w]);
		double value = Math.exp(exp);
//		if (value == Double.POSITIVE_INFINITY) {
//			System.out.println(z + "\t" + w);
//			System.out.println(this.phi0[w] + "\t" + gm.phitopic[z][w]);
//		}
		return value;
	}
}
