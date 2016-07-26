package model;

import java.util.ArrayList;

import edu.stanford.nlp.optimization.QNMinimizer;

public class ThetaTouristUpdater implements Runnable {
	private ArrayList<String> joblist;

	public ThetaTouristUpdater(ArrayList<String> joblist) {
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
			// System.out.println("update user " + u + ".......");
			DiffFunctionThetatourist df = new DiffFunctionThetatourist(lh);
			double[] temp=gm.thetatourist.get(lh);
			temp = qnm.minimize(df, Paras.termate, gm.thetatourist.get(lh));
		}
		System.out.println("one thread has completed!");
	}
}