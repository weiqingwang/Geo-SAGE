package model;

import java.util.ArrayList;

import edu.stanford.nlp.optimization.QNMinimizer;

public class ThetaUserUpdater implements Runnable {
	private ArrayList<Integer> joblist;

	public ThetaUserUpdater(ArrayList<Integer> joblist) {
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
		for (int u : joblist) {
			// System.out.println("update user " + u + ".......");
			DiffFunctionThetauser df = new DiffFunctionThetauser(u);
			gm.thetauser[u] = qnm.minimize(df, Paras.termate, gm.thetauser[u]);
		}
		System.out.println("one thread has completed!");
	}
}
