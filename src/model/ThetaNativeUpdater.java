package model;

import java.util.ArrayList;

import edu.stanford.nlp.optimization.QNMinimizer;

public class ThetaNativeUpdater implements Runnable {
	private ArrayList<String> joblist;

	public ThetaNativeUpdater(ArrayList<String> joblist) {
		this.joblist = joblist;
	}

	public void run() {
		// TODO Auto-generated method stub
		if (joblist.size() == 0) {
			System.out.println("Don't worry!");
			return;
		}
		GeoGMNew gm=GeoGMNew.getGM();
		QNMinimizer qnm = new QNMinimizer(10, true);
		qnm.terminateOnRelativeNorm(true);
		qnm.terminateOnNumericalZero(true);
		qnm.terminateOnAverageImprovement(true);
		qnm.shutUp();
		for (String lh : joblist) {
//			 System.out.println("update location " + lh + ".......");
			DiffFunctionThetaNative df = new DiffFunctionThetaNative(lh);
			double[] temp=gm.thetanative.get(lh);
			temp = qnm.minimize(df, Paras.termate, gm.thetanative.get(lh));
		}
		System.out.println("one thread has completed!");
	}
}