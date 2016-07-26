package model;

import java.util.ArrayList;

public class PreInferer implements Runnable {
	private GeoGMNew gm;
	private ArrayList<Integer> joblist;

	public PreInferer(ArrayList<Integer> joblist) {
		this.gm = GeoGMNew.getGM();
		this.joblist = joblist;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		for (int z : joblist) {
			for (int w = 0; w < gm.W; w++) {
				// infer beta
				gm.betas[z][w] = gm.inferBeta(z, w);
			}
			for (int v = 0; v < gm.V; v++) {
//				if (gm.preInferedItems.contains(v))
					gm.gammas[z][v] = gm.inferGamma(z, v);
			}
		}
	}

}
