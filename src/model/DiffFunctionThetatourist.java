package model;

import java.util.ArrayList;

import edu.stanford.nlp.optimization.DiffFunction;

public class DiffFunctionThetatourist implements DiffFunction {
	private double[] thetatourist;
	private String lh;
	private GeoGMNew gm;
	private ArrayList<Pair> ps;
	private double normal=1;
	
	public DiffFunctionThetatourist(String lh){
		this.lh=lh;
		this.gm=GeoGMNew.getGM();
		this.ps=this.gm.Dlt.get(lh);
	}

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return Paras.K;
	}

	@Override
	public double valueAt(double[] arg0) {
		this.thetatourist=arg0;
		this.setAlpha();
		double fl=0;
//		System.out.println("valueAT");
		for(int i=0;i<ps.size();i++){
			Pair p=ps.get(i);
			int u=p.getFirst();
			int v=p.getSec();
			UserProfile up=gm.user_items.get(u);
			double alphad=up.getAlphad(v);
			double alphan=up.getAlphan(v);
			double alpha=Math.log(alphad/alphan);
			fl+=alpha;
		}
		return fl*normal;
	}

	@Override
	public double[] derivativeAt(double[] arg0) {
		this.thetatourist=arg0;
		this.setAlpha();
		double[] r=new double[arg0.length];
		double max=0;
		for(int z=0;z<arg0.length;z++){
			double dlz=gm.dlzt.get(this.lh)[z];
			double alphasum=0;
			for(int i=0;i<ps.size();i++){
				Pair p=ps.get(i);
				int u=p.getFirst();
				int v=p.getSec();
				UserProfile up=gm.user_items.get(u);
				double alpha=0;
				double alphan=up.getAlphans(v)[z];
				double alphad=up.getAlphad(v);
				alpha=alphan/alphad;
				alphasum+=alpha;
			}
			r[z]=(alphasum-dlz);
			if(Math.abs(r[z])>max){
				max=Math.abs(r[z]);
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
		return r;
	}
	
	public void setAlpha() {
		// TODO Auto-generated method stub
		// for the begin u
		for (Pair pair : ps)
		{
			UserProfile up=gm.user_items.get(pair.getFirst());
			int u = pair.getFirst();
			int v = pair.getSec();
			int s=up.getS(v);
			int z=up.getZ(v);
			String l=up.getL(v);
			double alphad=0;
			double alphan=0;
			double[] alphans=new double[Paras.K];
			for(int zz=0;zz<Paras.K;zz++){
				double alphant=this.getAlphaN(u, s, l,  zz);
				alphans[zz]=alphant;
				if(zz==z){
					alphan=alphant;
				}
				alphad+=alphant;
			}
			up.setAlphan(v, alphan);
			up.setAlphad(v, alphad);
			up.setAlphans(v, alphans);
		}
	}

	public double getAlphaN(int u, int s, String l, int z){
		double alphan=0;
		//infer alphan
		double exp=0;
		exp+=(gm.theta0[z]+gm.thetauser[u][z]);
		for(int h=1;h<=Paras.H;h++){
			String lh=l.substring(0, h);
			if(h<=s)
				exp+=gm.thetanative.get(lh)[z];
			else if(h>s && lh.equals(this.lh))
				exp+=this.thetatourist[z];
			else
				exp+=gm.thetatouristCopy.get(lh)[z];
		}
		alphan=Math.exp(exp);
		return alphan;
	}

}
