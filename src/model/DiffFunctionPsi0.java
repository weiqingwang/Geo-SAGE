package model;

import edu.stanford.nlp.optimization.DiffFunction;

public class DiffFunctionPsi0 implements DiffFunction {
	private GeoGMNew gm;
	private double[] psi0;
	private double[] gammads;
	private double normal=1;

	public DiffFunctionPsi0() {
		this.gm = GeoGMNew.getGM();
		this.gammads = new double[Paras.K];
	}

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return gm.V;
	}

	@Override
	public double valueAt(double[] arg0) {
		this.psi0 = arg0;
		// TODO Auto-generated method stub
		// intialize betads
		for (int z = 0; z < Paras.K; z++) {
			double gammadz = 0;
			for (int vv = 0; vv < arg0.length; vv++) {
				gammadz += this.getGammaN(vv, z);
			}
			this.gammads[z] = gammadz;
		}
		double sum = 0;
		for (int z = 0; z < Paras.K; z++) {
			double gammad = this.gammads[z];
			for (int v = 0; v < gm.V; v++) {
				double temp = gm.psi0[z] + gm.psitopic[z][v];
				double sumtemp=gm.dzv[z][v] * (Math.log(gammad) - temp)*this.normal;
				sum += sumtemp;
			}
		}
		return sum;
	}

	@Override
	public double[] derivativeAt(double[] arg0) {
		double max=0;
		this.psi0 = arg0;
		for (int z = 0; z < Paras.K; z++) {
			double gammad = 0;
			for (int vv = 0; vv < gm.V; vv++) {
				gammad += this.getGammaN(vv, z);
			}
			this.gammads[z] = gammad;
		}
		double[] r = new double[arg0.length];
		for (int v = 0; v < arg0.length; v++) {
			double dv = gm.dv[v];
			double gamma = 0;
			for (int z = 0; z < Paras.K; z++) {
				gamma += (gm.dz[z] * this.getGamma(z, v));
			}
			r[v] = (gamma - dv);
			if(Math.abs(r[v])>max){
				max=Math.abs(r[v]);
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
		for(int v=0;v<arg0.length;v++){
			r[v]*=normal;
		}
		return r;
	}

	public double getGamma(int z, int v) {
		// get the relative variables
		double gamman = this.getGammaN(v, z);
		return gamman / this.gammads[z];
	}

	public double getGammaN(int v, int z) {
		double gamman = 0;
		// infer alphan
		double exp = 0;
		exp += (this.psi0[v] + gm.psitopic[z][v]);
		gamman = Math.exp(exp);
		return gamman;
	}

}
