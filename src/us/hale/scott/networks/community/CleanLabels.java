package us.hale.scott.networks.community;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * 
 * @author Scott A. Hale, http://www.scotthale.net/
 * 
 * After community detection has finished, each community has a numeric label assigned to it.
 * These labels are likely not sequential. This class reads in community memeberships,
 * and writes out the same community memberships, but with sequentially numbered labels.  
 *
 */
public class CleanLabels {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		LabelPropagation lp = new LabelPropagation();
		int numNodes = 916836;
		
		//input is "edgelist" format "id id" sorted by first id
		lp.readEdges(numNodes, "edges.list");
		
		lp.readMemberships("iterXXmemberships.txt");
		lp.writeMembershipsSmart("iterXXmemberships_smart.txt");

	}

}
