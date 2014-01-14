package us.hale.scott.networks.community;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 
 * @author Scott A. Hale, http://www.scotthale.net/
 * 
 * This is the main class that performs the community detection / label propagation method.
 * This code is based on the C code in igraph for the same purpose and on the original article:
 * 
 * Raghavan, U. N., Albert, R., and Kumara, S. 
 * Near linear time algorithm to detect community structures in large-scale networks. 
 * Phys. Rev. E 76, 3 (Sept. 2007), 36106
 * 
 * The code runs on multiple threads to make use of multiple cores in modern CPUs.
 * Adjust the number of threads in the main method.
 * 
 * Please cite the original paper as well as the paper I first wrote this code for:
 * 
 * Hale, S.A. (2014) Global Connectivity and Multilinguals in the Twitter Network. 
 * In Proceedings gs of the 2014 ACM Annual Conference on Human Factors in Computing Systems, 
 * ACM (Montreal, Canada).
 * 
 * More details are at: http://www.scotthale.net/pubs/?chi2014
 * 
 *
 */
public class LabelPropagation {
	
	private Vector<Node> nodeList;
	private Vector<Integer> nodeOrder;
	
	public LabelPropagation() {
		
	}
	
	/**
	 * Read in an edge list.
	 * 
	 * Input file is in "edgelist" format as produced by igraph.
	 * Each line is of the format "id id" with ids numbered sequentially from 1 to numNodes inclusive 
	 * 
	 * @param numNodes The number of nodes in the network (also the maximum id number of a node)
	 * @param file Input file in edgelist format as described above
	 * @throws IOException
	 */
	public void readEdges(int numNodes, String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		nodeList = new Vector<Node>(numNodes);
		nodeOrder = new Vector<Integer>(numNodes);
		for (int i=0; i<numNodes; i++) {
			nodeList.add(new Node(i,i));
			nodeOrder.add(Integer.valueOf(i));
		}
		
		
		System.out.println("Added " + numNodes + " nodes.");
		
		String line = br.readLine();
		while (line!=null) {
			String[] parts = line.split(" ");
			
			int source = Integer.valueOf(parts[0]);
			int target = Integer.valueOf(parts[1]);
			
			nodeList.get(source).addNeighbor(target);
			nodeList.get(target).addNeighbor(source);
			
			line=br.readLine();
		}
		
		System.out.println("All edges read.");
		
		br.close();
	}
	
	/**
	 * 
	 * Writes the current community memberships of each node to file.
	 * File has one line per node in the format "node_id community_id\n" 
	 * 
	 * @param file filename to write output to (existing file will be overwritten)
	 * @throws IOException
	 */
	public void writeMemberships(String file) throws IOException {
		
		System.out.println("Writing membership.");
		
		FileOutputStream fso = new FileOutputStream(file);
		OutputStreamWriter fileWriter = new OutputStreamWriter(fso,Charset.forName("UTF-8"));
		
		Node n;
		for (int i=0; i<nodeList.size(); i++) {
			n=nodeList.get(i);
			fileWriter.write(n.getId()+" "+n.getLabel()+"\n");
		}
		
		System.out.println("Membership list written.");
		
		fileWriter.close();
		fso.close();
	}
	
	/**
	 * Read in existing community memberships as written by writeMemberships.
	 * This is useful to restart an interrupted run or to give initial community memberships.
	 * 
	 * @param file The input file to read
	 * @throws IOException
	 */
	public void readMemberships(String file) throws IOException {
		System.out.println("Reading memberships.");
		
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		String line = br.readLine();
		while (line!=null) {
			String[] parts = line.split(" ");
			
			int nodeId = Integer.valueOf(parts[0]);
			int label = Integer.valueOf(parts[1]);
			
			nodeList.get(nodeId).setLabel(label);
			
			line=br.readLine();
		}
		
		System.out.println("Memberships loaded from file.");

		br.close();
		
	}
	
	/**
	 * Like writeMemberships, but community membership labels are renumbered to be sequential 
	 * from 1 to the number of communities prior to writing the output to file.
	 *  
	 * @param file filename to write output to (existing file will be overwritten)
	 * @throws IOException
	 */
	public void writeMembershipsSmart(String file) throws IOException {
		
		System.out.println("Writing membership smart.");
		
		
		Map<Integer,Integer> labelMap = new HashMap<Integer,Integer>();
		int labelCount=0;
		for (int i=0; i<nodeList.size(); i++) {
			int label = nodeList.get(i).getLabel();
			Integer val =  labelMap.get(Integer.valueOf(label));
			if (val==null) {
				labelCount++;
				labelMap.put(Integer.valueOf(label), Integer.valueOf(labelCount));
			}
		}
		System.out.println("Found " + labelCount + " communities.");
		
		FileOutputStream fso = new FileOutputStream(file);
		OutputStreamWriter fileWriter = new OutputStreamWriter(fso,Charset.forName("UTF-8"));
		
		Node n;
		for (int i=0; i<nodeList.size(); i++) {
			n=nodeList.get(i);
			fileWriter.write(n.getId()+" "+labelMap.get(Integer.valueOf(n.getLabel())).intValue() +"\n");
		}
		
		System.out.println("Smart membership list written.");
		
		fileWriter.close();
		fso.close();
	}
	
	/**
	 * Start the community detection process.
	 * 
	 * @param basepath Intermediate community memberships will be written to files in this directory after each pass.
	 * 					Specify "null" to not write intermediate membership information.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 */
	public void findCommunities(String basepath, int numThreads) throws InterruptedException, ExecutionException, IOException {
		
		/*memberships = new Vector<Integer>(nodeList.size());
		for (int i=0; i<nodeList.size(); i++) {
		      memberships.set(i, Integer.valueOf(i));
		}*/
		
		ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);		

		
		Vector<LabelPropagationWorker> workers = new Vector<LabelPropagationWorker>(numThreads);
		for (int j=0; j<numThreads; j++) {
			workers.add(new LabelPropagationWorker(nodeList));
		}
		
		int iter=0;
		int nodesChanged=100;
		while (nodesChanged>0) {
			nodesChanged=0;
			
			System.out.println("Running " + (++iter) + " iteration at " + System.currentTimeMillis() + ".");
			
			Collections.shuffle(nodeOrder);//DO NOT SHUFFLE nodeList
			
			for (int i=0; i<nodeList.size(); i+=numThreads) {
				for (int j=0; j<numThreads; j++) {
					if ((j+i)<nodeList.size()) {
						workers.get(j).setNodeToProcess(nodeOrder.get(i+j).intValue());
					} else {
						workers.get(j).setNodeToProcess(-1);
					}
				}
				List<Future<Boolean>> results = threadPool.invokeAll(workers);
				
				for (int j=0; j<results.size(); j++) {
					Boolean r = results.get(j).get();
					if (r!=null && r.booleanValue()==true) {
						nodesChanged++;
						if (nodesChanged==1) System.out.println("Another pass will be needed.");
						break;
					}
				}
			}
			
			
			//Pass complete
			if (basepath!=null) {
				writeMemberships(basepath+"iter" + iter +"memberships.txt");
				System.out.println(nodesChanged + " nodes were changed in the last iteration.");
			}
				
		}
		
		System.out.println("Detection complete!");
		
		threadPool.shutdown();
		
	}
	


	/**
	 * Kick off the whole program. 
	 * 
	 * * Read in the network in edge.list format.
	 * * Find communities and write out community memberships after each pass
	 * 
	 * For large networks, it will take a very long time before no nodes change labels in a pass.
	 * However, the majority of nodes should fix their labels quickly.
	 * 
	 * If all nodes do fix their labels, this method then:
	 * * Writes out the final membership information
	 * * Writes out the final membership information with community labels renumbered sequentially from 1 to N,
	 * 		where N is the number of communities found
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
		LabelPropagation lp = new LabelPropagation();
		
		int numNodes = 916836; //Number of nodes in the network
		int numThreads=6; //Number of threads to use
		
		//input is "edgelist" format "id id" sorted by first id (ids are sequentially numbered 1 to numNodes inclusive)
		lp.readEdges(numNodes, "edges.list");
		lp.findCommunities("base_output_path",numThreads); //directory to save current list of communities to after each pass as well as final output files
		lp.writeMemberships("membership.txt");
		lp.writeMembershipsSmart("memberships_renumbered.txt");
		
	}
	

}
