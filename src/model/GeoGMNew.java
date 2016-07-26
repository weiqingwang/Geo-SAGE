package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.optimization.QNMinimizer;

/*
 * run twitter: change the "CA" with qcs
 * run foursquare: change the qcs with "CA"
 */
public class GeoGMNew {
	private QNMinimizer qnm;
	private static GeoGMNew gm = new GeoGMNew();

	public static GeoGMNew getGM() {
		return gm;
	}

	public int U; // the number of users
	public int R;// the number of locations(corresponds to the locationID)
	public int V;// the number of spatial items(corresponds to placeID)
	public int T;// the number of time slices
	public int W;// the number of vocabulary
	public int N;// the number of overall activities
	// public double alpha;// this is used for the initialization to avoid the
	// // appearance of many 0s.
	public HashMap<Integer, UserProfile> user_items;
	public HashMap<Integer, UserProfile> test_user_items;
	public Hashtable<Integer,ArrayList<Integer>> nearbyItems;
	public Hashtable<Integer, String> homelocations;
	public Hashtable<Integer,String> itemlocations;
	// public int[][] duv;// the number of activeties where user u selects the
	// item v
	public HashMap<String, ArrayList<Integer>> DR;// the list of spatial items
	// at each location
	public HashSet<Integer>[] Duv;// the list of spatial items
									// each user has checked in
	// item v
	public ArrayList<Integer>[] Dvw;// the list of words on item v
//	public ArrayList<Pair>[] Dt;// the set of activities
								// occurring on the
	// each time slice
	public HashMap<String, ArrayList<Pair>> Dln;// the set of activities
												// occurring on each
	// location
	public HashMap<String, ArrayList<Pair>> Dlt;
	private double extSmall = 0.9;
	public double[] theta0;// the global distribution over topics
//	public double[][] thetatime;// the temporary distribution over topics
	public double[][] thetauser;// the distribution over topics for each user
	public double[] phi0; // the global distribution over words
	public double[][] phitopic;// the distribution over words for each topic;
	public double[][] psitopic;// the distribution over spatial items for each
								// topic;
	public double[] psi0;// the global distribution over spatial items
	public HashMap<String, double[]> thetanative;// the distribution over
													// topics for each
													// location on each
													// level
	public HashMap<String, double[]> thetanativeCopy;
	public HashMap<String, double[]> thetatourist;// the distribution over
													// topics for each
													// location on each
													// level
	public HashMap<String, double[]> thetatouristCopy;
	public int[][] duz;// the number of activities assigned to each topic by
						// each user
//	public int[][] dtz;// the number of activities assigned to each topic at
						// each time slice
	public HashMap<String, int[]> dlzn;// the number of activities assigned to
										// each topic at
	// each location at each level assigned by natives
	public HashMap<String, int[]> dlzt;
	public int[][] dzw;// the number of activities where the word w is assigned
						// to the topic z
	public int[] dz;// the number of activities assigned to each topic
	public int[][] dzv;
	public int[] dw;
	public int[] dv;
	public int[] du;
	public int[] hit;
	public double[][] betas;
	public double[][] gammas;
	public HashMap<String, Integer> dln;// the count of activities on each
										// level assigned by natives
	public HashMap<String, Integer> dlt;

//	public HashSet<Integer> preInferedItems;// store the items which do not
	
	public int countTestRandom;

	// need
	// to infer the gamma when we do the
	// preinfer

	// public double[][] betazw;
	// public double[][] gammazv;

	// int N, int U, int R, int V, int T, int W,
	// HashMap<Integer, UserProfile> user_items, int[][] duv,
	// HashMap<Integer, ArrayList<Pair>> Dt,
	// HashMap<String, ArrayList<Pair>> Dln,
	// HashMap<String, ArrayList<Pair>> Dlt, int[] dw, int[] dv, int[] du,
	// Hashtable<String, Integer> dln, Hashtable<String, Integer> dlt
	private GeoGMNew() {
		this.qnm = new QNMinimizer(10, true);
		qnm.terminateOnRelativeNorm(true);
		qnm.terminateOnNumericalZero(true);
		qnm.terminateOnAverageImprovement(true);
		// read from the checkins.txt file to get the number of users,locations,
		// spatial items, time slices and the vocabulary
		// the format of the input file is:
		// userID/tweetID/location/time/placeID/contentInfo/locationID/homelocation/s/city/hometown
		// if (Paras.timeGran.equalsIgnoreCase("month")) {
		// T = 12;
		// } else if (Paras.timeGran.equalsIgnoreCase("season")) {
		// T = 4;
		// }
		FileReader reader;
		this.user_items = new HashMap<Integer, UserProfile>();

		this.Dln = new HashMap<String, ArrayList<Pair>>();// the
															// set
															// of
															// activities
		// occurring on each
		// location
		this.Dlt = new HashMap<String, ArrayList<Pair>>();
		this.N = 0;
		dw = new int[0];
		dv = new int[0];
		du = new int[0];
		dln = new HashMap<String, Integer>();
		dlt = new HashMap<String, Integer>();
		this.nearbyItems=new Hashtable<Integer, ArrayList<Integer>>();
		this.homelocations=new Hashtable<Integer,String>();
		this.itemlocations=new Hashtable<Integer,String>();
//		this.preInferedItems = new HashSet<Integer>();
		this.DR = new HashMap<String, ArrayList<Integer>>();
		// this.betazw=new double[Paras.K][this.W];
		// this.gammazv=new double[Paras.K][this.V];
		try {
			// read from the statistics to get the sizes of all kinds of arrays
			reader = new FileReader("data/twitter/statistics.txt");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			this.U = new Integer(br.readLine()) + 1;
			this.R = new Integer(br.readLine()) + 1;
			this.V = new Integer(br.readLine()) + 1;
			this.W = new Integer(br.readLine()) + 1;
			this.T = new Integer(br.readLine()) + 1;
			this.Duv = new HashSet[this.U];
			for (int u = 0; u < this.U; u++) {
				this.Duv[u] = new HashSet<Integer>();
			}
			this.Dvw = new ArrayList[this.V];
			for (int v = 0; v < this.V; v++) {
				this.Dvw[v] = new ArrayList<Integer>();
			}
			// item v
//			this.Dt = new ArrayList[this.T];// the
											// set
											// of
											// activities
			// occurring on the
			// each time slice
//			for (int t = 0; t < this.T; t++) {
//				this.Dt[t] = new ArrayList<Pair>();
//			}
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, "\t");
				int uid = new Integer(st.nextToken());
				int size = new Integer(st.nextToken());
				UserProfile up = new UserProfile(size);
				user_items.put(uid, up);
			}
			br.close();
			reader.close();
			this.dw = new int[this.W];
			this.dv = new int[this.V];
			this.du = new int[this.U];
			// this.duv = new int[this.U][this.V];
			// initialize the user_items, duz, Dt and Dl
			reader = new FileReader("data/twitter/train.txt");
			br = new BufferedReader(reader);
			str = null;
			// userID/tweetID/location/time/placeID/contentInfo/locationID/homelocation/s/city/hometown
			while ((str = br.readLine()) != null) {
				this.N++;
				StringTokenizer st = new StringTokenizer(str, "\t");
				int uid = new Integer(st.nextToken());
				this.du[uid]++;
				st.nextToken();
				// int spatialItem, int location, ArrayList<Integer> content,
				// String s, int time
				String location = st.nextToken();
				int time = new Integer(st.nextToken());
				int spatialItem = new Integer(st.nextToken());
				String itemlocation=this.itemlocations.get(spatialItem);
				if(itemlocation==null){
					this.itemlocations.put(spatialItem, location);
				}
				this.dv[spatialItem]++;
				String contentsS = st.nextToken();
				StringTokenizer st1 = new StringTokenizer(contentsS, "|");
				ArrayList<Integer> contents = new ArrayList<Integer>();
				while (st1.hasMoreTokens()) {
					int word = new Integer(st1.nextToken());
					contents.add(word);
					this.dw[word]++;
				}
				// if (this.Dvw[spatialItem].size() != 0)
				this.Dvw[spatialItem] = contents;
				st.nextToken();
				// initialize DR and Duv
				ArrayList<Integer> drr = this.DR.get(location);
				if (drr == null) {
					drr = new ArrayList<Integer>();
					drr.add(spatialItem);
					this.DR.put(location, drr);
				} else if (!drr.contains(spatialItem))
					drr.add(spatialItem);
				HashSet<Integer> duvv = this.Duv[uid];
				if (!duvv.contains(spatialItem))
					duvv.add(spatialItem);
				st.nextToken();
				int si = new Integer(st.nextToken());
				String city = st.nextToken();
//				if (Paras.qcs.contains(city)) {
//					this.preInferedItems.add(spatialItem);
//				}
				user_items.get(uid).addOneRecord(spatialItem, location, si,
						time, contents);
				ArrayList<Pair> Dtt = new ArrayList<Pair>();
//				Dtt = this.Dt[time];
				Pair p;
				int index = user_items.get(uid).getSize() - 1;
				p = new Pair(uid, index);
//				if (!Dtt.contains(p))
//					Dtt.add(p);
				// add to Dl
				for (int h = 1; h <= Paras.H; h++) {
					String lh = location.substring(0, h);
					// infer whether the user is a native or tourist on this
					// level
					if (h <= si) {
						// the user is a native here
						Dtt = this.Dln.get(lh);
						Integer count = dln.get(lh);
						if (Dtt == null) {
							Dtt = new ArrayList<Pair>();
							Dtt.add(p);
							this.Dln.put(lh, Dtt);
							this.dln.put(lh, new Integer(1));
						} else {
							// add one pair to Dtt
							Dtt.add(p);
							this.Dln.put(lh, Dtt);
							count++;
							this.dln.put(lh, count);
						}
					} else {
						// the user is a tourist
						Dtt = this.Dlt.get(lh);
						Integer count = this.dlt.get(lh);
						if (Dtt == null) {
							Dtt = new ArrayList<Pair>();
							Dtt.add(p);
							this.Dlt.put(lh, Dtt);
							this.dlt.put(lh, new Integer(1));
						} else {
							// add one pair to Dtt
							Dtt.add(p);
							this.Dlt.put(lh, Dtt);
							count++;
							this.dlt.put(lh, count);
						}
					}

				}
			}
			br.close();
			reader.close();
			// initialize the user_items, duz, Dt and Dl
			reader = new FileReader("data/twitter/test.txt");
			br = new BufferedReader(reader);
			str = null;
			// userID/tweetID/location/time/placeID/contentInfo/locationID/homelocation/s/city/hometown
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, "\t");
				st.nextToken();
				st.nextToken();
				// int spatialItem, int location, ArrayList<Integer> content,
				// String s, int time
				String location = st.nextToken();
				st.nextToken();
				int spatialItem = new Integer(st.nextToken());
				st.nextToken();
				st.nextToken();
				// initialize DR and Duv
//				ArrayList<Integer> drr = this.DR.get(location);
//				if (drr == null) {
//					drr = new ArrayList<Integer>();
//					drr.add(spatialItem);
//					this.DR.put(location, drr);
//				} else if (!drr.contains(spatialItem))
//					drr.add(spatialItem);
			}
			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// output the DR
//		Iterator<String> itt = this.DR.keySet().iterator();
//		int count500 = 0;
//		int count1000 = 0;
//		int count50 = 0;
//		int count20 = 0;
//		int count10=0;
//		while (itt.hasNext()) {
//			String loc = itt.next();
//			ArrayList<Integer> items = this.DR.get(loc);
//			int size = items.size();
//			// System.out.println(size);
//			if (size >= 1000)
//				count1000++;
//			else if (size >= 500)
//				count500++;
//			else if (size >= 50)
//				count50++;
//			else if (size >= 20)
//				count20++;
//			else if(size>=10)
//				count10++;
//		}
//		System.out.println(">=1000: " + count1000);
//		System.out.println(">=500: " + count500);
//		System.out.println(">=50: " + count50);
//		System.out.println(">=20: " + count20);
//		System.out.println(">=10: " + count10);
//		System.out.println("the total number of region is: " + this.DR.size());
		// this.alpha = 50 / Paras.K;
		this.theta0 = new double[Paras.K];
//		this.thetatime = new double[this.T][Paras.K];
		this.thetauser = new double[this.U][Paras.K];
		this.phi0 = new double[this.W];
		this.phitopic = new double[Paras.K][this.W];
		this.psi0 = new double[this.V];
		this.psitopic = new double[Paras.K][this.V];
		this.thetanative = new HashMap<String, double[]>();
		this.thetatourist = new HashMap<String, double[]>();
	}

	/*
	 * initialize phi0 according to the global word distribution; psi0 according
	 * to the global spatial items distribution
	 * 
	 * how to initialize topic z phitopic and psitopic? according to Yin's work.
	 * we assign each twitter randomly to a topic, based on this, we can get
	 * other parameters by counting the numbers.
	 */
	public void initializeCount() {
		// double normal=1e3;
		// double normalz=this.N*normal/Paras.K;
		// double normalu=this.N*normal/this.U;
		// double normall=this.N*normal/this.R;
		// double normalt=this.N*normal/this.T;
		this.duz = new int[this.U][Paras.K];
//		this.dtz = new int[this.T][Paras.K];
		this.dlzn = new HashMap<String, int[]>();
		this.dlzt = new HashMap<String, int[]>();
		this.dzw = new int[Paras.K][this.W];
		this.dz = new int[Paras.K];
		this.dzv = new int[Paras.K][this.V];
		// initialize the phi0 and psi0
		for (int i = 0; i < this.W; i++) {
			// this.phi0[i] = (double) this.dw[i] / this.N;
			double value = this.dw[i];
			if (value == 0) {
				value = gm.extSmall;
				// System.out.println(i);
			}
			this.phi0[i] = Math.log(value);
			System.out.println(value + "\t" + this.phi0[i]);
		}
		for (int i = 0; i < this.V; i++) {
			// this.psi0[i] = (double) this.dv[i] / this.N;
			double value = this.dv[i];
			if (value == 0)
				value = gm.extSmall;
			this.psi0[i] = Math.log(value);
		}
		// assign each twitter randomly to a topic
		for (int i = 0; i < U; i++) {
			UserProfile up = this.user_items.get(i);
			// System.out.println(i);
			int length = up.getSize();

			for (int j = 0; j < length; j++) {
				int ran = (int) (Math.random() * (Paras.K));
				this.dz[ran]++;
				up.setZ(j, ran);
				// update the statistics related to topic assignment
				this.duz[i][ran]++;
//				this.dtz[up.getT(j)][ran]++;
				// update dlz for each level
				String location = up.getL(j);
				for (int h = 1; h <= Paras.H; h++) {
					String lh = location.substring(0, h);
					int s = up.getS(j);
					int[] counts = new int[Paras.K];
					if (h <= s) {
						counts = this.dlzn.get(lh);
						if (counts == null) {
							counts = new int[Paras.K];
							counts[ran] = 1;
							this.dlzn.put(lh, counts);
						} else {
							counts[ran]++;
						}
					} else {
						counts = this.dlzt.get(lh);
						if (counts == null) {
							counts = new int[Paras.K];
							counts[ran] = 1;
							this.dlzt.put(lh, counts);
						} else {
							counts[ran]++;
						}
					}
				}
				int item = up.getV(j);
				this.dzv[ran][item]++;
				// this.dzw
				ArrayList<Integer> words = up.getContents(j);
				for (int word : words) {
					this.dzw[ran][word]++;
				}
			}
		}
		for (int i = 0; i < Paras.K; i++) {
			if (this.dz[i] == 0)
				this.theta0[i] = Math.log(gm.extSmall);
			else
				this.theta0[i] = Math.log(this.dz[i]);
			// int sum = 0;
			// for (int j = 0; j < this.W; j++) {
			// sum += (this.dzw[i][j]);
			// }
			for (int j = 0; j < this.W; j++) {
				double value = this.dzw[i][j];
				if (value == 0) {
					value = gm.extSmall;
				}
				this.phitopic[i][j] = Math.log(value) - this.phi0[j];
			}
			// sum = 0;
			// for (int j = 0; j < this.V; j++) {
			// sum += (this.dzv[i][j]);
			// }
			for (int j = 0; j < this.V; j++) {
				// this.psitopic[i][j] = (double) (this.dzv[i][j] - dv[j]) /
				// normalz;
				double value = this.dzv[i][j];
				if (value == 0) {
					value = gm.extSmall;
				}
				this.psitopic[i][j] = Math.log(value) - this.psi0[j];
			}
			for (int j = 0; j < this.U; j++) {
				double value = this.duz[j][i];
				if (value == 0) {
					value = gm.extSmall;
				}
				this.thetauser[j][i] = Math.log(value);
			}
//			for (int j = 0; j < this.T; j++) {
//				ArrayList<Pair> obj = this.Dt[j];
//				if (obj == null) {
//					// this.thetatime[j][i] = 0;
//					this.thetatime[j][i] = Math.log(gm.extSmall);
//				} else {
//					this.thetatime[j][i] = Math.log(obj.size());
//				}
//			}
		}
		// for (int z = 0; z < Paras.K; z++) {
		// for (int i = 0; i < 50; i++)
		// System.out.print(this.phitopic[z][i] + "\t");
		// System.out.println();
		// }
		// initialize thetanative and thetatourist based on dlzn, dlzt and dln,
		// dlt
		Iterator<String> it = dlzn.keySet().iterator();
		while (it.hasNext()) {
			String location = it.next();
			int[] topiccounts = this.dlzn.get(location);
			double[] pros = new double[Paras.K];
			for (int i = 0; i < Paras.K; i++) {
				double value = topiccounts[i];
				if (value == 0) {
					pros[i] = Math.log(gm.extSmall);
				} else {
					pros[i] = Math.log(value);
				}
			}
			this.thetanative.put(location, pros);
		}
		it = dlzt.keySet().iterator();
		while (it.hasNext()) {
			String location = it.next();
			int[] topiccounts = this.dlzt.get(location);
			double[] pros = new double[Paras.K];
			for (int i = 0; i < Paras.K; i++) {
				double value = topiccounts[i];
				if (value == 0) {
					pros[i] = Math.log(gm.extSmall);
				} else {
					pros[i] = Math.log(value);
				}
			}
			this.thetatourist.put(location, pros);
		}
	}

	public void initializeRandom() {

	}

	public void sampleTopic(int i, int j) {
		// remove the previous topic assignment and modify the statistics
		UserProfile user = this.user_items.get(i);
		String location = user.getL(j);
		int topic = user.getZ(j);
		int s = user.getS(j);
		int item = user.getV(j);
//		int time = user.getT(j);
		ArrayList<Integer> contents = user.getContents(j);
		// the related statistics are: duz, dtz, dzw, dz, dzv, dlzn, dlzt
		// infer and assign the new topic assignment according to the
		// probability distribution
		// probability
		double[] ps = new double[Paras.K];
		for (int z = 0; z < Paras.K; z++) {
			double exp = 0;
			exp = this.theta0[z] + this.thetauser[i][z];
			for (int h = 1; h <= Paras.H; h++) {
				String lh = location.substring(0, h);
				if (h <= s) {
					exp += this.thetanative.get(lh)[z];
				} else
					exp += this.thetatourist.get(lh)[z];
			}
			ps[z] = Math.exp(exp);
		}
		// sampling
		for (int ii = 1; ii < Paras.K; ii++)
			ps[ii] += ps[ii - 1];
		double t = Math.random() * ps[Paras.K - 1];
		int topicnew = 0;
		for (; topicnew < Paras.K; topicnew++) {
			if (t < ps[topicnew])
				break;
		}
		// test
		if (topicnew >= Paras.K) {
			System.err.println(ps[Paras.K - 1] + " " + t);
		}
		// modify the statistics
		this.user_items.get(i).setZ(j, topicnew);
		this.duz[i][topic]--;
		this.duz[i][topicnew]++;
//		this.dtz[time][topic]--;
//		this.dtz[time][topicnew]++;
		for (int word : contents) {
			this.dzw[topic][word]--;
			this.dzw[topicnew][word]++;
		}
		this.dz[topic]--;
		this.dz[topicnew]++;
		this.dzv[topic][item]--;
		this.dzv[topicnew][item]++;
		for (int h = 1; h <= Paras.H; h++) {
			String lh = location.substring(0, h);
			if (h <= s) {
				this.dlzn.get(lh)[topic]--;
				this.dlzn.get(lh)[topicnew]++;
			} else {
				this.dlzt.get(lh)[topic]--;
				this.dlzt.get(lh)[topicnew]++;
			}
		}
	}

	// public void sampleWords(int i, int j) {
	// UserProfile user = this.user_items.get(i);
	// int topic = user.getZ(j);
	// int item = user.getV(j);
	// HashSet<Integer> contents = this.Dvw[item];
	// Iterator<Integer> it = contents.iterator();
	// while (it.hasNext()) {
	// int word = it.next();
	// int wordnew = this.sampleSingleWord(topic);
	// this.dzw[topic][word]--;
	// this.dw[word]--;
	// this.dzw[topic][wordnew]++;
	// this.dw[wordnew]++;
	// }
	// }

	// public int sampleSingleWord(int topic) {
	// double[] ps = new double[this.W];
	// for (int w = 0; w < this.W; w++) {
	// double exp = 0;
	// exp = this.phi0[w] + this.phitopic[topic][w];
	// ps[w] = Math.exp(exp);
	// }
	// // sampling
	// for (int ii = 1; ii < this.W; ii++)
	// ps[ii] += ps[ii - 1];
	// double t = Math.random() * ps[this.W - 1];
	// int word = 0;
	// for (; word < this.W; word++) {
	// if (t < ps[word])
	// break;
	// }
	// // test
	// if (word >= this.W) {
	// System.err.println(ps[this.W - 1] + " " + t);
	// }
	// return word;
	// }

	// public void sampleItem(int i, int j) {
	// // remove the previous item assignment and modify the statistics
	// UserProfile user = this.user_items.get(i);
	// int topic = user.getZ(j);
	// int item = user.getV(j);
	// // the related statistics are: dzv, dv
	// double[] ps = new double[this.V];
	// for (int v = 0; v < this.V; v++) {
	// double exp = 0;
	// exp = this.psi0[v] + this.psitopic[topic][v];
	// ps[v] = Math.exp(exp);
	// }
	// // sampling
	// for (int ii = 1; ii < this.V; ii++)
	// ps[ii] += ps[ii - 1];
	// double t = Math.random() * ps[this.V - 1];
	// int itemnew = 0;
	// for (; itemnew < this.V; itemnew++) {
	// if (t < ps[itemnew])
	// break;
	// }
	// // test
	// if (itemnew >= this.V) {
	// System.err.println(ps[this.V - 1] + " " + t);
	// }
	// // modify the statistics
	// this.user_items.get(i).setItem(j, itemnew);
	// this.dzv[topic][item]--;
	// this.dv[item]--;
	// this.dzv[topic][itemnew]++;
	// this.dv[itemnew]++;
	// }

	public void updateTheta0() {
		System.out.println("update theta 0.....");
		DiffFunctionTheta0 df = new DiffFunctionTheta0();
		// this.theta0 = qnm.minimize(df, 1e-10, this.theta0);
		this.theta0 = this.qnm.minimize(df, Paras.termate, this.theta0);
		// System.out.println("update theta 0 finished");
	}

	public void updateThetaUser() {
		int maxthreads = Paras.MAXTHREADSM;
		System.out.println("update theta user.....");
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxthreads,
				maxthreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
		// TODO Auto-generated method stub
		int count = 0;
		ArrayList<Integer>[] joblists = new ArrayList[maxthreads];
		for (int i = 0; i < maxthreads; i++) {
			joblists[i] = new ArrayList<Integer>();
		}
		for (int u = 0; u < gm.U; u++) {
			joblists[count].add(u);
			count = (count + 1) % maxthreads;
		}
		for (int i = 0; i < maxthreads; i++) {
			executor.submit(new ThetaUserUpdater(joblists[i]));
		}
		executor.shutdown();
		try {
			// while (!executor.isTerminated())
			while (!executor.awaitTermination(60, TimeUnit.SECONDS))
				;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("update theta user finished");
	}

	public void updateThetaNative() {
		System.out.println("update theta native.........");
		// choose the location with more than 10000 pairs
		// copy the thetanative
		this.thetanativeCopy = (HashMap<String, double[]>) thetanative.clone();
		HashSet<String> largeLs = new HashSet<String>();
		Iterator<String> it1 = this.dln.keySet().iterator();
		System.out.println("begin large L....");
		while (it1.hasNext()) {
			String lh = it1.next();
			int count = this.dln.get(lh);
			if (count > 1000) {
				largeLs.add(lh);
				DiffFunctionThetaNativePara df = new DiffFunctionThetaNativePara(
						lh);
				System.out.println(count);
				double[] temp = this.thetanative.get(lh);
				temp = qnm.minimize(df, Paras.termate, temp);
				this.thetanative.put(lh, temp);
			}
		}

		// int maxthreads = Paras.MAXTHREADSM;
		// System.out.println("update theta native.........");
		// ThreadPoolExecutor executor = new ThreadPoolExecutor(maxthreads,
		// maxthreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
		// // TODO Auto-generated method stub
		// int count = 0;
		// ArrayList<String>[] joblists = new ArrayList[maxthreads];
		// for (int i = 0; i < maxthreads; i++) {
		// joblists[i] = new ArrayList<String>();
		// }
		Iterator<String> it = this.thetanative.keySet().iterator();
		System.out.println("begin small L....");
		while (it.hasNext()) {
			String lh = it.next();
			if (!largeLs.contains(lh)) {
				System.out.println(this.dln.get(lh));
				DiffFunctionThetaNative df = new DiffFunctionThetaNative(lh);
				double[] temp = gm.thetanative.get(lh);
				temp = qnm.minimize(df, Paras.termate, temp);
				this.thetanative.put(lh, temp);
				// joblists[count].add(lh);
				// count = (count + 1) % maxthreads;

			}
		}
		// for (int i = 0; i < maxthreads; i++) {
		// executor.execute(new ThetaNativeUpdater(joblists[i]));
		// }
		// executor.shutdown();
		// try {
		// // while (!executor.isTerminated())
		// while (!executor.awaitTermination(60, TimeUnit.SECONDS))
		// ;
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	public void updateThetaTourist() {
		System.out.println("update theta tourist.........");
		// choose the location with more than 10000 pairs
		this.thetatouristCopy = (HashMap<String, double[]>) this.thetatourist
				.clone();
		HashSet<String> largeLs = new HashSet<String>();
		Iterator<String> it1 = this.dlt.keySet().iterator();
		while (it1.hasNext()) {
			String lh = it1.next();
			int count = this.dlt.get(lh);
			if (count > 1000) {
				// System.out.println(lh + ":" + count);
				largeLs.add(lh);
				DiffFunctionThetatouristPara df = new DiffFunctionThetatouristPara(
						lh);
				double[] temp = this.thetatourist.get(lh);
				temp = qnm.minimize(df, Paras.termate, temp);
				this.thetatourist.put(lh, temp);
			}
		}
		Iterator<String> it = this.thetatourist.keySet().iterator();
		while (it.hasNext()) {
			String lh = it.next();
			if (!largeLs.contains(lh)) {
				DiffFunctionThetatourist df = new DiffFunctionThetatourist(lh);
				double[] temp = gm.thetatourist.get(lh);
				temp = qnm.minimize(df, Paras.termate, gm.thetatourist.get(lh));
				this.thetatourist.put(lh, temp);
			}
		}
	}

	public void updatePhi0() {
		System.out.println("update phi 0..........");
		DiffFunctionPhi0 df = new DiffFunctionPhi0();
		this.phi0 = qnm.minimize(df, Paras.termate, this.phi0);
		// System.out.println("update phi 0 finished");
	}

	public void updatePhiTopic() {
		System.out.println("update phi topic...........");
		for (int z = 0; z < Paras.K; z++) {
			DiffFunctionPhitopic df = new DiffFunctionPhitopic(z);
			this.phitopic[z] = qnm
					.minimize(df, Paras.termate, this.phitopic[z]);
		}
		// System.out.println("update phi topic finished");
	}

	public void updatePsi0() {
		System.out.println("update psi 0.........");
		DiffFunctionPsi0 df = new DiffFunctionPsi0();
		this.psi0 = qnm.minimize(df, Paras.termate, this.psi0);
		// System.out.println("update psi 0 finished");
	}

	public void updatePsiTopic() {
		System.out.println("update psi topic..........");
		// this.qnm.shutUp();
		for (int z = 0; z < Paras.K; z++) {
			DiffFunctionPsitopic df = new DiffFunctionPsitopic(z);
			this.psitopic[z] = qnm
					.minimize(df, Paras.termate, this.psitopic[z]);
		}
		// System.out.println("update psi topic finished");
	}

	public double inferAlpha(int u, int s,  String l, int z) {
		double n = this.inferAlphaN(u, s, l, z);
		double d = 0;
		for (int zz = 0; zz < Paras.K; zz++) {
			d += this.inferAlphaN(u, s, l, zz);
		}
		return n / d;
	}

	public double inferAlphaN(int u, int s, String l, int z) {
		double exp = 0;
		exp += (this.theta0[z] + this.thetauser[u][z]);
		double[] thetanativel;
		double[] thetatouristl;
		for (int h = 1; h <= Paras.H; h++) {
			String lh = l.substring(0, h);
			if (h <= s) {
				thetanativel = this.thetanative.get(lh);
				if (thetanativel == null) {
					thetatouristl = this.thetatourist.get(lh);
					if (thetatouristl == null) {
						continue;
					} else {
						exp += thetatouristl[z];
					}
				} else {
					exp += thetanativel[z];
				}
			} else {
				thetatouristl = this.thetatourist.get(lh);
				if (thetatouristl == null) {
					thetanativel = this.thetanative.get(lh);
					if (thetanativel == null) {
						continue;
					} else {
						exp += thetanativel[z];
					}
				} else {
					exp += this.thetatourist.get(lh)[z];
				}
			}
		}
		return Math.exp(exp);
	}

	public double inferBeta(int z, int w) {
		double n = this.inferBetaN(z, w);
		double d = 0;
		for (int ww = 0; ww < this.W; ww++) {
			d += this.inferBetaN(z, ww);
		}
		return n / d;
	}

	public double inferBetaN(int z, int w) {
		double exp = 0;
		exp += (this.phi0[w] + this.phitopic[z][w]);
		return Math.exp(exp);
	}

	public double inferGamma(int z, int v) {
		double n = this.inferGammaN(z, v);
		double d = 0;
		for (int vv = 0; vv < this.V; vv++) {
			d += this.inferGammaN(z, vv);
		}
		return n / d;
	}

	public double inferGammaN(int z, int v) {
		double exp = 0;
		exp += (this.psi0[v] + this.psitopic[z][v]);
		return Math.exp(exp);
	}

	public void train() {
		int it = 0;
		while (it < Paras.iter) {
			System.out.println("the " + it + "'s iteration.......");
			long begintime = System.currentTimeMillis();
			this.qnm.shutUp();
			if (it % 5 == 0) {
				this.updateTheta0();
				this.updateThetaUser();
				this.updateThetaNative();
				this.updateThetaTourist();
				this.updatePhi0();
				this.updatePhiTopic();
				this.updatePsi0();
				this.updatePsiTopic();
			}
			// M step
			// if (it % 7 == 1) {
			// this.updateTheta0();
			// }
			// this.updateThetaUser();
			// if (it % 5 == 0) {
			// this.updateThetaTime();
			// }
			// if (it % 10 == 1) {
			// this.updateThetaNative();
			// }
			// if (it % 7 == 1) {
			// this.updateThetaTourist();
			// }
			// if (it % 7 == 1) {
			// this.updatePhi0();
			// }
			// this.updatePhiTopic();
			// if (it % 7 == 1) {
			// this.updatePsi0();
			// }
			// this.updatePsiTopic();
			// // E step
			for (int i = 0; i < this.U; i++) {
				for (int j = 0; j < this.user_items.get(i).getSize(); j++) {
					this.sampleTopic(i, j);
				}
			}
			it++;
			System.out.println("over");
			long duration = System.currentTimeMillis() - begintime;
			System.out.println(this.formatDuring(duration));
		}
	}

	public static void main(String[] args) {
		long begintime = System.currentTimeMillis();
		 gm.initializeCount();
		 gm.train();
		 gm.output_model();
//		gm.readModel();
//		gm.readBG();
		 gm.preInfer();
		for(Paras.k=20;Paras.k>=2;Paras.k=Paras.k-2)
			gm.recommend();
		long duration = System.currentTimeMillis() - begintime;
		System.out.println(gm.formatDuring(duration));
	}

	/*
	 * this method infer the beta and gamma offline
	 */
	public void preInfer() {
		System.out.println("preinfer....");
		this.betas = new double[Paras.K][this.W];
		this.gammas = new double[Paras.K][this.V];
		int maxthreads = 15;
		ThreadPoolExecutor executor = new ThreadPoolExecutor(maxthreads,
				maxthreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
		// TODO Auto-generated method stub
		int count = 0;
		ArrayList<Integer>[] joblists = new ArrayList[maxthreads];
		for (int i = 0; i < maxthreads; i++)
			joblists[i] = new ArrayList<Integer>();
		for (int z = 0; z < Paras.K; z++) {
			joblists[count].add(z);
			count = (count + 1) % maxthreads;
		}
		for (int i = 0; i < maxthreads; i++) {
			executor.submit(new PreInferer(joblists[i]));
		}
		executor.shutdown();
		try {
			// while (!executor.isTerminated())
			while (!executor.awaitTermination(60, TimeUnit.SECONDS))
				;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// output betas and gammas
		try {
			// theta0, the whole document has only one line and there are
			// Paras.K values in this line
			// phitopic, similar to phi0
			FileWriter writer = new FileWriter(
					"data/twitter/parameters/betas.csv");
			BufferedWriter bw = new BufferedWriter(writer);
			for (int w = 0; w < this.W; w++) {
				for (int z = 0; z < Paras.K; z++) {
					bw.write(this.betas[z][w] + ",");
				}
				bw.write("\n");
			}
			bw.close();
			writer.close();
			// psitopic, similar to phitopic
			writer = new FileWriter("data/twitter/parameters/gammas.csv");
			bw = new BufferedWriter(writer);
			for (int v = 0; v < this.V; v++) {
				for (int z = 0; z < Paras.K; z++) {
					bw.write(this.gammas[z][v] + ",");
				}
				bw.write("\n");
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
	}

	/*
	 * this method read from the output_model, and initialize the variables we
	 * need in recommendation
	 */
	public void readModel() {
		FileReader reader;
		try {
			reader = new FileReader("data/twitter/parameters/theta0.csv");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				int count = 0;
				while (st.hasMoreTokens()) {
					this.theta0[count] = new Double(st.nextToken());
					count++;
				}
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/thetauser.csv");
			br = new BufferedReader(reader);
			str = null;
			int uu = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				int zz = 0;
				while (st.hasMoreTokens()) {
					this.thetauser[uu][zz] = new Double(st.nextToken());
					zz++;
				}
				uu++;
			}
			br.close();
			reader.close();
//			reader = new FileReader("data/twitter/parameters/thetatime.csv");
//			br = new BufferedReader(reader);
//			str = null;
//			uu = 0;
//			while ((str = br.readLine()) != null) {
//				StringTokenizer st = new StringTokenizer(str, ",");
//				int zz = 0;
//				while (st.hasMoreTokens()) {
//					this.thetatime[uu][zz] = new Double(st.nextToken());
//					zz++;
//				}
//				uu++;
//			}
//			br.close();
//			reader.close();
			reader = new FileReader("data/twitter/parameters/thetanative.csv");
			br = new BufferedReader(reader);
			str = null;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				String l = st.nextToken();
				double[] diss = new double[Paras.K];
				int zz = 0;
				while (st.hasMoreTokens()) {
					diss[zz] = new Double(st.nextToken());
					zz++;
				}
				this.thetanative.put(l, diss);
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/thetatourist.csv");
			br = new BufferedReader(reader);
			str = null;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				String l = st.nextToken();
				double[] diss = new double[Paras.K];
				int zz = 0;
				while (st.hasMoreTokens()) {
					diss[zz] = new Double(st.nextToken());
					zz++;
				}
				this.thetatourist.put(l, diss);
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/phi0.csv");
			br = new BufferedReader(reader);
			str = null;
			uu = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				this.phi0[uu] = new Double(st.nextToken());
				uu++;
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/phitopic.csv");
			br = new BufferedReader(reader);
			str = null;
			uu = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				int zz = 0;
				while (st.hasMoreTokens()) {
					this.phitopic[zz][uu] = new Double(st.nextToken());
					zz++;
				}
				uu++;
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/psi0.csv");
			br = new BufferedReader(reader);
			str = null;
			uu = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				this.psi0[uu] = new Double(st.nextToken());
				uu++;
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/psitopic.csv");
			br = new BufferedReader(reader);
			str = null;
			uu = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				int zz = 0;
				while (st.hasMoreTokens()) {
					this.psitopic[zz][uu] = new Double(st.nextToken());
					zz++;
				}
				uu++;
			}
			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readBG() {
		this.betas = new double[Paras.K][this.W];
		this.gammas = new double[Paras.K][this.V];
		FileReader reader;
		BufferedReader br;
		try {
			reader = new FileReader("data/twitter/parameters/betas.csv");
			br = new BufferedReader(reader);
			String str = null;
			int ww = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				int zz = 0;
				while (st.hasMoreTokens()) {
					this.betas[zz][ww] = new Double(st.nextToken());
					zz++;
				}
				ww++;
			}
			br.close();
			reader.close();
			reader = new FileReader("data/twitter/parameters/gammas.csv");
			br = new BufferedReader(reader);
			str = null;
			int vv = 0;
			while ((str = br.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(str, ",");
				int zz = 0;
				while (st.hasMoreTokens()) {
					this.gammas[zz][vv] = new Double(st.nextToken());
					zz++;
				}
				vv++;
			}
			br.close();
			reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void recommend() {
		System.out.println("recommend....");
		this.test_user_items = new HashMap<Integer, UserProfile>();
		FileReader reader;
		int countTest = 0;
		int maxthreads = 20;
		this.hit = new int[maxthreads];
		try {
			// Iterator<String> states = Paras.qcs.iterator();
			FileWriter writer = new FileWriter("data/twitter/result.txt", true);
			BufferedWriter bw = new BufferedWriter(writer);
			// while (states.hasNext()) {
			// String state = states.next();
			// System.out.println(state);
			reader = new FileReader("data/twitter/test.txt");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			// this.test_user_items = new HashMap<Integer, UserProfile>();
			// for (int i = 0; i < maxthreads; i++)
			// this.hit[i] = 0;
			while ((str = br.readLine()) != null) {
				countTest++;
				// System.out.println(countTest);
				StringTokenizer st = new StringTokenizer(str, "\t");
				// the format of the input file is:
				// userID/tweetID/location/time/placeID/contentInfo/locationID/homelocation/s/city/hometown
				int u = new Integer(st.nextToken());
				st.nextToken();
				String l = st.nextToken();
				int t = new Integer(st.nextToken());
				int targetV = new Integer(st.nextToken());
				String contentsS = st.nextToken();
				StringTokenizer st1 = new StringTokenizer(contentsS, "|");
				ArrayList<Integer> contents = new ArrayList<Integer>();
				while (st1.hasMoreTokens())
					contents.add(new Integer(st1.nextToken()));
				this.Dvw[targetV] = contents;
				st.nextToken();
				String homelocation=st.nextToken();
				this.homelocations.put(u, homelocation);
				int s = new Integer(st.nextToken());
				UserProfile up = this.test_user_items.get(u);
				if (up == null) {
					up = new UserProfile(1000);
					up.addOneRecord(targetV, l, s, t, null);
					this.test_user_items.put(u, up);
				} else {
					up.addOneRecord(targetV, l, s, t, null);
				}
				st.nextToken();
				st.nextToken();
				ArrayList<Integer> items=this.nearbyItems.get(targetV);
				if(items==null){
					String itemss=st.nextToken();
					items=new ArrayList<Integer>();
					StringTokenizer stt=new StringTokenizer(itemss,"|");
					while(stt.hasMoreTokens()){
						items.add(Integer.parseInt(stt.nextToken()));
					}
					this.nearbyItems.put(targetV, items);
				}
//				ArrayList<Integer> drr = this.DR.get(l);
//				if (drr == null) {
//					drr = new ArrayList<Integer>();
//					drr.add(targetV);
//					this.DR.put(l, drr);
//				} else if (!drr.contains(targetV))
//					drr.add(targetV);
			}
			// multiple threads
			// int maxthreads = Paras.MAXTHREADSL;
			ThreadPoolExecutor executor = new ThreadPoolExecutor(maxthreads,
					maxthreads, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
			// TODO Auto-generated method stub
			int count = 0;
			ArrayList<Pair>[] joblists = new ArrayList[maxthreads];
			for (int i = 0; i < maxthreads; i++) {
				joblists[i] = new ArrayList<Pair>();
			}
			Iterator<Integer> itu = this.test_user_items.keySet().iterator();
			while (itu.hasNext()) {
				int u = itu.next();
				UserProfile up = this.test_user_items.get(u);
				int sv = up.getSize();
				for (int j = 0; j < sv; j++) {
					joblists[count].add(new Pair(u, j));
					count = (count + 1) % maxthreads;
				}
			}
			for (int i = 0; i < maxthreads; i++) {
				executor.submit(new RecommenderNew(joblists[i], i));
			}
			executor.shutdown();
			try {
				// while (!executor.isTerminated())
				while (!executor.awaitTermination(60, TimeUnit.SECONDS))
					;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int hitsum = 0;
			for (int h : hit) {
				hitsum += h;
			}
			double recall = (double) hitsum / countTest;
			bw.write("\n" + "On the data set of twitter, When the k is: "+Paras.k+", H is: "
					+ Paras.H + ", the K is: " + Paras.K
					+ ", the recall is: " + recall);
			br.close();
			reader.close();
			bw.close();
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("the total number of test records is: "+countTest);
//		System.out.println("the total number of records where there is less than 20 items is: "+this.countTestRandom);
	}

	// public void recommend_bck() {
	// System.out.println("recommend....");
	// double normal = 1e5;
	// // long begintime = System.currentTimeMillis();
	// // System.out.println("begin to recommend....");
	// // online recommendation
	// FileReader reader;
	// int countTest = 0;
	// int hit = 0;
	// try {
	// reader = new FileReader("data/twitter/test.txt");
	// BufferedReader br = new BufferedReader(reader);
	// FileWriter writer = new FileWriter("data/twitter/result.txt", true);
	// BufferedWriter bw = new BufferedWriter(writer);
	// String str = null;
	// while ((str = br.readLine()) != null) {
	// countTest++;
	// System.out.println(countTest);
	// StringTokenizer st = new StringTokenizer(str, "\t");
	// // the format of the input file is:
	// //
	// userID/tweetID/location/time/placeID/contentInfo/locationID/homelocation/s/city/hometown
	// int u = new Integer(st.nextToken());
	// st.nextToken();
	// String l = st.nextToken();
	// int t = new Integer(st.nextToken());
	// int targetV = new Integer(st.nextToken());
	// String ct = st.nextToken();
	// StringTokenizer st1 = new StringTokenizer(ct, "|");
	// ArrayList<Integer> contents = new ArrayList<Integer>();
	// while (st1.hasMoreTokens())
	// contents.add(new Integer(st1.nextToken()));
	// int lid = new Integer(st.nextToken());
	// st.nextToken();
	// int s = new Integer(st.nextToken());
	// // pick up another 1000 unrated items in this location
	// // int[] unratedItems = new int[1000];
	// ArrayList<Integer> unratedItems = new ArrayList<Integer>();
	// ArrayList<Integer> totalItems = this.DR.get(l);
	// HashSet<Integer> sampledIndexes = new HashSet<Integer>();
	// int size = totalItems.size();
	// HashSet<Integer> ratedItems = this.Duv[u];
	// int actualUIS = size - ratedItems.size();
	// int count = 0;
	// if (actualUIS <= Paras.unratedItemSize) {
	// // then we need to test all the items in the totalItems
	// for (int item : totalItems) {
	// unratedItems.add(item);
	// }
	// } else {
	// while (count < Paras.unratedItemSize) {
	// // sample an index from 0 to size-1 randomly
	// int sampledIndex = (int) (Math.random() * (size));
	// // if the item at the sampled index is not rated and
	// // also
	// // not sampled yet, add it to the unrated items and
	// // increase
	// // the count by 1
	// int sampledItem = totalItems.get(sampledIndex);
	// if ((!ratedItems.contains(sampledItem))
	// && (!sampledIndexes.contains(sampledIndex))) {
	// unratedItems.add(sampledItem);
	// sampledIndexes.add(sampledIndex);
	// count++;
	// }
	// }
	// }
	// // for targetV and unrated items, we infer ratings for them
	// // infer the rating for the target item
	// double ratingt = 0;
	// for (int z = 0; z < Paras.K; z++) {
	// double ratingz = 0;
	// ratingz = this.inferAlpha(u, s, t, l, z);
	// ArrayList<Integer> words = this.Dvw[targetV];
	// for (int w : words) {
	// ratingz *= this.inferBeta(z, w) * normal;
	// }
	// ratingz *= this.inferGamma(z, targetV);
	// ratingt += ratingz;
	// }
	// // System.out.println("the rating for the target item " +
	// // targetV
	// // + " is: " + ratingt);
	// double[] ratings = new double[unratedItems.size()];
	// int countR = 0;
	// for (int i = 0; i < unratedItems.size(); i++) {
	// int v = unratedItems.get(i);
	// double rating = 0;
	// for (int z = 0; z < Paras.K; z++) {
	// double ratingz = 0;
	// ratingz = this.inferAlpha(u, s, t, l, z);
	// ratingz *= normal;
	// ArrayList<Integer> words = this.Dvw[targetV];
	// for (int w : words) {
	// ratingz *= this.inferBeta(z, w);
	// }
	// ratingz *= this.inferGamma(z, v);
	// rating += ratingz;
	// }
	// ratings[i] = rating;
	// // count the number of the items "rating" with larger rating
	// // than targetV
	// if (rating > ratingt) {
	// countR++;
	// }
	// // System.out.println("the rating for the item " + v +
	// // " is: "
	// // + rating);
	// }
	// // if the rating is smaller than Paras.k, then hit++
	// if (countR < Paras.k) {
	// hit++;
	// }
	// }
	// // infer the recall
	// double recall = (double) hit / countTest;
	// // System.out.println("When the k is: " + Paras.k
	// // + ", the recall is: " + recall);
	// bw.write("\n" + "When the k is: " + Paras.k + ", the recall is: "
	// + recall);
	// // System.out.println("over");
	// // long duration = System.currentTimeMillis() - begintime;
	// // System.out.println(this.formatDuring(duration));
	// br.close();
	// reader.close();
	// bw.close();
	// writer.close();
	// } catch (FileNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	/*
	 * the parameters we need to output include theta0, thetauser, thetatime,
	 * thetanative, thetatourist, phi0, phitopic, psi0, psitopic
	 */
	public void output_model() {
		System.out.println("output model ...");

		// store the object, we write each parameter into a .csv file
		try {
			// theta0, the whole document has only one line and there are
			// Paras.K values in this line
			FileWriter writer = new FileWriter(
					"data/twitter/parameters/theta0.csv");
			BufferedWriter bw = new BufferedWriter(writer);
			for (double dis : this.theta0) {
				bw.write(dis + ",");
			}
			bw.close();
			writer.close();
			// the whole document has U line and there are Paras.K values in
			// each line
			writer = new FileWriter("data/twitter/parameters/thetauser.csv");
			bw = new BufferedWriter(writer);
			for (double[] disu : this.thetauser) {
				for (double dis : disu) {
					bw.write(dis + ",");
				}
				bw.write("\n");
			}
			bw.close();
			writer.close();
			// thetatime, similar to thetauser
//			writer = new FileWriter("data/twitter/parameters/thetatime.csv");
//			bw = new BufferedWriter(writer);
//			for (double[] dist : this.thetatime) {
//				for (double dis : dist) {
//					bw.write(dis + ",");
//				}
//				bw.write("\n");
//			}
//			bw.close();
//			writer.close();
			// thetanative, we add the location to the begining of each line
			writer = new FileWriter("data/twitter/parameters/thetanative.csv");
			bw = new BufferedWriter(writer);
			Iterator<String> it = this.thetanative.keySet().iterator();
			while (it.hasNext()) {
				String l = it.next();
				bw.write(l + ",");
				double[] diss = this.thetanative.get(l);
				for (double dis : diss) {
					bw.write(dis + ",");
				}
				bw.write("\n");
			}
			bw.close();
			writer.close();
			// thetatourist, similar to thetanative
			writer = new FileWriter("data/twitter/parameters/thetatourist.csv");
			bw = new BufferedWriter(writer);
			it = this.thetatourist.keySet().iterator();
			while (it.hasNext()) {
				String l = it.next();
				bw.write(l + ",");
				double[] diss = this.thetatourist.get(l);
				for (double dis : diss) {
					bw.write(dis + ",");
				}
				bw.write("\n");
			}
			bw.close();
			writer.close();
			// phi0, similar to theta0, however, each vertical row represents
			// the distribution over all the words for each topic
			writer = new FileWriter("data/twitter/parameters/phi0.csv");
			bw = new BufferedWriter(writer);
			for (double dis : this.phi0) {
				bw.write(dis + "\n");
			}
			bw.close();
			writer.close();
			// phitopic, similar to phi0
			writer = new FileWriter("data/twitter/parameters/phitopic.csv");
			bw = new BufferedWriter(writer);
			for (int w = 0; w < this.W; w++) {
				for (int z = 0; z < Paras.K; z++) {
					bw.write(this.phitopic[z][w] + ",");
				}
				bw.write("\n");
			}
			bw.close();
			writer.close();
			// psi0, similar to phi0
			writer = new FileWriter("data/twitter/parameters/psi0.csv");
			bw = new BufferedWriter(writer);
			for (double dis : this.psi0) {
				bw.write(dis + "\n");
			}
			bw.close();
			writer.close();
			// psitopic, similar to phitopic
			writer = new FileWriter("data/twitter/parameters/psitopic.csv");
			bw = new BufferedWriter(writer);
			for (int v = 0; v < this.V; v++) {
				for (int z = 0; z < Paras.K; z++) {
					bw.write(this.psitopic[z][v] + ",");
				}
				bw.write("\n");
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

		System.out.println("output model ... done");
	}

	public String formatDuring(long mss) {
		// long days = mss / (1000 * 60 * 60 * 24);
		long hours = mss / (1000 * 60 * 60);
		long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
		long seconds = (mss % (1000 * 60)) / 1000;
		return hours + " hours " + minutes + " minutes " + seconds
				+ " seconds ";
	}
}
