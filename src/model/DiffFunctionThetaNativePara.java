package model;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.optimization.DiffFunction;

public class DiffFunctionThetaNativePara implements DiffFunction {
	private double[] thetanative;
	private String lh;
	private GeoGMNew gm;
	private ArrayList<Pair> ps;
	private int maxthreads;
	private double normal=1;
	
	public DiffFunctionThetaNativePara(String lh){
		this.lh=lh;
		this.gm=GeoGMNew.getGM();
		this.ps=this.gm.Dln.get(lh);
		this.maxthreads=Paras.MAXTHREADSL;
	}

	@Override
	public int domainDimension() {
		// TODO Auto-generated method stub
		return Paras.K;
	}

	@Override
	public double valueAt(double[] arg0) {
		this.thetanative=arg0;
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxthreads, maxthreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
		// TODO Auto-generated method stub
		int count=0;
		ArrayList<Pair>[] joblists = new ArrayList[maxthreads];
		for (int i = 0; i < maxthreads; i++)
		{
			joblists[i] = new ArrayList<Pair>();
		}
		for(int i=0;i<ps.size();i++){
			Pair p=ps.get(i);
			joblists[count].add(p);
			count = (count + 1) % maxthreads;
		}
		for (int i = 0; i < maxthreads; i++)
		{
			executor.submit(new AlphaSetter(joblists[i],this.lh));
		}
		executor.shutdown();
		try {
			while(!executor.awaitTermination(60, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		this.thetanative=arg0;
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxthreads, maxthreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
		// TODO Auto-generated method stub
		int count=0;
		ArrayList<Pair>[] joblists = new ArrayList[maxthreads];
		for (int i = 0; i < maxthreads; i++)
		{
			joblists[i] = new ArrayList<Pair>();
		}
		for(int i=0;i<ps.size();i++){
			Pair p=ps.get(i);
			joblists[count].add(p);
			count = (count + 1) % maxthreads;
		}
		for (int i = 0; i < maxthreads; i++)
		{
			executor.submit(new AlphaSetter(joblists[i],this.lh));
		}
		executor.shutdown();
		try {
			executor.awaitTermination(60, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double[] r=new double[arg0.length];
		double max=0;
		for(int z=0;z<arg0.length;z++){
			double dlz=gm.dlzn.get(this.lh)[z];
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
			r[z]=(alphasum-dlz)*normal;
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
		for(int w=0;w<arg0.length;w++){
			r[w]*=normal;
		}
		return r;
	}
	
	private class AlphaSetter implements Runnable{
		private ArrayList<Pair> joblist;
		private String lh;
		
		public AlphaSetter(ArrayList<Pair> joblist, String lh){
			this.joblist = joblist;
			this.lh=lh;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//for the begin u
			if(joblist.size()==0){
				return;
			}
			for (Pair pair : joblist)
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
					double alphant=this.getAlphaN(u, s, l, zz);
					alphans[zz]=alphant;
					if(zz==z){
						alphan=alphant;
					}
					alphad+=alphant;
				}
				up.setAlphan(v, alphan);
				up.setAlphad(v, alphad);
				up.setAlphans(v, alphans);
//				GeoGM.getGM().user_items.get(u).setAlphad(v, alphad);
			}
		}
		
		public double getAlphaN(int u, int s, String l, int z){
			double alphan=0;
			//infer alphan
			double exp=0;
			exp+=(gm.theta0[z]+gm.thetauser[u][z]);
			for(int h=1;h<=Paras.H;h++){
				String lh=l.substring(0, h);
				if(h<=s&&lh.equals(this.lh))
					exp+=thetanative[z];
				else if(h<=s&&(!lh.equals(this.lh)))
					exp+=gm.thetanativeCopy.get(lh)[z];
				else
					exp+=gm.thetatourist.get(lh)[z];
			}
			alphan=Math.exp(exp);
			return alphan;
		}
		
	}
}
