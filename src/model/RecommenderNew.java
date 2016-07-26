package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RecommenderNew implements Runnable {
	private GeoGMNew gm;
	private ArrayList<Pair> joblist;
	private int index;
	private ArrayList<Pair> hitPairs;
//	private double normal=1e5;
	public RecommenderNew(ArrayList<Pair> joblist, int index){
		this.gm=GeoGMNew.getGM();
		this.joblist=joblist;
		this.index=index;
		this.hitPairs=new ArrayList<Pair>();
	}
	
//	public static void main(String[] args){
//		System.out.println(Math.pow(4, 0.5));
//	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//for each pair, infer the hit
		for(Pair pair:joblist){
			int u = pair.getFirst();
			int j=pair.getSec();
			UserProfile up=gm.test_user_items.get(u);
			String l = up.getL(j);
			int targetV = up.getV(j);
//			ArrayList<Integer> contents = up.getContents(j);
			int s = up.getS(j);
			// pick up another 1000 unrated items in this location
			// int[] unratedItems = new int[1000];
			ArrayList<Integer> totalItems = gm.nearbyItems.get(targetV);
			HashSet<Integer> ratedItems = gm.Duv[u];
			ArrayList<Integer> unratedItems = new ArrayList<Integer>();//store the unratedItems in total items
			HashSet<Integer> sampledIndexes = new HashSet<Integer>();
			ArrayList<Integer> sampledItems=new ArrayList<Integer>();
			for(int item:totalItems){
				if((!ratedItems.contains(item))&&(item!=targetV)){
					unratedItems.add(item);
				}
			}
			int size = unratedItems.size();
			int count = 0;
			if (size <= Paras.unratedItemSize) {
				// then we need to test all the items in the totalItems
				for (int item : unratedItems) {
					sampledItems.add(item);
				}
			} else {
				while (count < Paras.unratedItemSize) {
					// sample an index from 0 to size-1 randomly
					int sampledIndex = (int) (Math.random() * (size));
					// if the item at the sampled index is not rated and
					// also
					// not sampled yet, add it to the unrated items and
					// increase
					// the count by 1
					int sampledItem = unratedItems.get(sampledIndex);
					if (!sampledIndexes.contains(sampledIndex)) {
						sampledItems.add(sampledItem);
						sampledIndexes.add(sampledIndex);
						count++;
					}
				}
			}
			// for targetV and unrated items, we infer ratings for them
			// infer the rating for the target item
			double ratingt = 0;
			for (int z = 0; z < Paras.K; z++) {
				double ratingz = 0;
				ratingz = gm.inferAlpha(u, s, l, z);
				ArrayList<Integer> words = gm.Dvw[targetV];
				int w=words.get(0);
				double ratingw=gm.betas[z][w];
//				double wordSize=1.0/words.size();
//				for (int wi=1;wi<words.size();wi++) {
//					w=words.get(wi);
//					ratingw *= (gm.betas[z][w]);
//				}
//				ratingw=Math.pow(ratingw, wordSize);
				double wordSize=words.size();
				for (int wi=1;wi<words.size();wi++) {
					w=words.get(wi);
					ratingw += (gm.betas[z][w]);
				}
				ratingw=ratingw/wordSize;
				ratingz*=ratingw;
				double gamma=0;
				gamma=gm.gammas[z][targetV];
//				if(gm.preInferedItems.contains(targetV)){
//					gamma=gm.gammas[z][targetV];
//				}
//				else{
//					gamma=gm.inferGamma(z, targetV);
//				}
				ratingz *= gamma;
				ratingt += ratingz;
			}
			// System.out.println("the rating for the target item " +
			// targetV
			// + " is: " + ratingt);
//			double[] ratings = new double[unratedItems.size()];
			int countR = 0;
			for (int i = 0; i < sampledItems.size(); i++) {
				int v = sampledItems.get(i);
				boolean preinfered=false;
//				if(gm.preInferedItems.contains(v))
					preinfered=true;
				double rating = 0;
				//get the s(slu), iteml for item v
				String iteml=gm.itemlocations.get(v);
				String homel=gm.homelocations.get(u);
				int slu=-1;
				StringBuffer slub=new StringBuffer();
				int h=0;
				for(;h<Paras.H;h++){
					char lt=iteml.charAt(h);
					char hl=homel.charAt(h);
					if(lt==hl){
						//0 indicates that this user is native
						slub.append("0");
					}
					else{
						slub.append("1");
						break;
					}
				}
				for(++h;h<Paras.H;h++){
					slub.append("1");
				}
				String ss=slub.toString();
				slu=ss.indexOf("1");
				if(slu==-1){
					slu=ss.length();
				}
				for (int z = 0; z < Paras.K; z++) {
					double ratingz = 0;
					ratingz = gm.inferAlpha(u, slu, iteml, z);
//					ratingz *= normal;
					ArrayList<Integer> words = gm.Dvw[v];
					int w=words.get(0);
					double ratingw=gm.betas[z][w];
//					double wordSize=1.0/words.size();
//					for (int wi=1;wi<words.size();wi++) {
//						w=words.get(wi);
//						ratingw *= (gm.betas[z][w]);
//					}
//					ratingw=Math.pow(ratingw, wordSize);
					double wordSize=words.size();
					for (int wi=1;wi<words.size();wi++) {
						w=words.get(wi);
						ratingw += (gm.betas[z][w]);
					}
					ratingw=ratingw/wordSize;
					ratingz*=ratingw;
					double gamma=0;
					if(preinfered)
						gamma=gm.gammas[z][v];
					else
						gamma=gm.inferGamma(z, v);
					ratingz *= gamma;
					rating += ratingz;
				}
//				ratings[i] = rating;
				// count the number of the items "rating" with larger rating
				// than targetV
				if (rating > ratingt) {
					countR++;
					ArrayList<Integer> words = gm.Dvw[targetV];
					
				}
				// System.out.println("the rating for the item " + v +
				// " is: "
				// + rating);
			}
			// if the rating is smaller than Paras.k, then hit++
			if (countR < Paras.k) {
				gm.hit[index]++;
				this.hitPairs.add(pair);
			}
//			System.out.println(u+"\t"+l);
		}
		try {
			// Iterator<String> states = Paras.qcs.iterator();
			String filename="data/twitter/hitCases"+Paras.k+".csv";
			FileWriter writer = new FileWriter(filename,
					true);
			BufferedWriter bw = new BufferedWriter(writer);
			for(Pair pair:this.hitPairs){
				StringBuffer sb=new StringBuffer();
				int u = pair.getFirst();
				sb.append(u+"\t");
				int j=pair.getSec();
				UserProfile up=gm.test_user_items.get(u);
				String l = up.getL(j);
				sb.append(l+"\t");
				int targetV = up.getV(j);
				sb.append(targetV+"\t");
				int s = up.getS(j);
				sb.append(s+"\t");
				ArrayList<Integer> regionItems=gm.DR.get(l);
				ArrayList<Integer> nearbyItems=gm.nearbyItems.get(targetV);
				if(regionItems==null){
					sb.append(0+"\t");
				}
				else{
					sb.append(regionItems.size()+"\t");
				}
				if(nearbyItems==null){
					sb.append(0+"\t");
				}
				else{
					sb.append(nearbyItems.size()+"\n");
				}
				bw.write(sb.toString());
			}
			bw.close();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("one thread ends");
	}

}
