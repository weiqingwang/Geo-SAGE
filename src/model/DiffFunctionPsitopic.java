package model;

import edu.stanford.nlp.optimization.DiffFunction;

public class DiffFunctionPsitopic implements DiffFunction {
	private GeoGMNew gm;
	private int z;
	private double[] psitopic;
	private double gammadz;
	private double[] gammans;
	private double normal=1;

	public DiffFunctionPsitopic(int z) {
		this.gm=GeoGMNew.getGM();
		this.z = z;
		this.gammans=new double[gm.V];
	}

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return gm.V;
	}

	@Override
	public double valueAt(double[] arg0) {
		// TODO Auto-generated method stub
		this.psitopic = arg0;
		// intialize betads
		this.setGamma();
		double sum = 0;
		for (int v = 0; v < gm.V; v++) {
			sum += (gm.dzv[z][v] * (Math.log(this.gammadz/this.gammans[v]))*normal);
		}
		return sum;
	}

	@Override
	public double[] derivativeAt(double[] arg0) {
		this.psitopic = arg0;
		this.setGamma();
		double[] r = new double[arg0.length];
		double max=0;
		for (int v = 0; v < arg0.length; v++) {
			double dzv = gm.dzv[this.z][v];
			double gamma=this.gammans[v]/this.gammadz;
			r[v] = (gm.dz[z] * gamma - dzv);
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

	public void setGamma(){
		this.gammadz=0;
		for (int v = 0; v < gm.V; v++) {
			double gamman=(this.getGammaN(v));
			this.gammadz += gamman;
			this.gammans[v]=gamman;
		}
	}

	public double getGammaN(int v) {
		double gamman = 0;
		// infer alphan
		double exp = 0;
		exp += (gm.psi0[v] + this.psitopic[v]);
		gamman = Math.exp(exp);
		return gamman;
	}

}
