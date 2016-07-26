package model;

import edu.stanford.nlp.optimization.DiffFunction;

public class DiffFunctionThetauser implements DiffFunction {
	private GeoGMNew gm;
	private double[] thetauser;
	private int u;
	private UserProfile up;
	private double normal=1;

	public DiffFunctionThetauser(int u) {
		this.u = u;
		this.gm = GeoGMNew.getGM();
		this.up = gm.user_items.get(u);
	}

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return Paras.K;
	}

	@Override
	public double valueAt(double[] arg0) {
		this.thetauser=arg0;
//		int count=0;
//		for(double value:this.thetauser){
//			if(count%100==0)
//				System.out.println();
//			System.out.print(value+"\t");
//			count++;
//		}
		this.setAlpha();
		double fl = 0;
		for (int v = 0; v < up.getSize(); v++) {
			double alphad = up.getAlphad(v);
			double alphan = up.getAlphan(v);
			double alpha = Math.log(alphad / alphan);
			fl += alpha;
		}
		return fl*normal;
	}

	@Override
	public double[] derivativeAt(double[] arg0) {
		this.thetauser=arg0;
		//System.out.println("This is the output from DiffFunctionThetauser.derivativeAt");
		this.setAlpha();
		double[] r = new double[arg0.length];
		double max=0;
		for (int i = 0; i < arg0.length; i++) {
			double duz = gm.duz[this.u][i];
			double alphasum = 0;
			for (int v = 0; v < up.getSize(); v++) {
				double alpha = 0;
				double alphan = up.getAlphans(v)[i];
				double alphad = up.getAlphad(v);
				alpha = alphan / alphad;
				alphasum += alpha;
			}
			r[i] = alphasum - duz;
			if(Math.abs(r[i])>max){
				max=Math.abs(r[i]);
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
		for(int z=0;z<arg0.length;z++){
			r[z]*=normal;
		}
		// System.out.println("");
		return r;
	}

	public void setAlpha() {
		// TODO Auto-generated method stub
		// for the begin u
		int size=up.getSize();
		for (int v=0;v<size;v++) {
			int s = up.getS(v);
			int z = up.getZ(v);
			String l = up.getL(v);
			double alphad = 0;
			double alphan = 0;
			double[] alphans = new double[Paras.K];
			for (int zz = 0; zz < Paras.K; zz++) {
				double alphant = this.getAlphaN(s, l, zz);
				alphans[zz] = alphant;
				if (zz == z) {
					alphan = alphant;
				}
				alphad += alphant;
			}
			up.setAlphan(v, alphan);
			up.setAlphad(v, alphad);
			up.setAlphans(v, alphans);
			// GeoGM.getGM().user_items.get(u).setAlphad(v, alphad);
		}
	}

	public double getAlphaN(int s, String l, int z) {
		double alphan = 0;
		// infer alphan
		double exp = 0;
		exp += (gm.theta0[z] + thetauser[z]);
		for (int h = 1; h <= Paras.H; h++) {
			String lh = l.substring(0, h);
			if (h <= s)
				exp += gm.thetanative.get(lh)[z];
			else
				exp += gm.thetatourist.get(lh)[z];
		}
		alphan = Math.exp(exp);
		return alphan;
	}
}
