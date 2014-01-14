package us.hale.scott.networks.community;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Callable;

public class LabelPropagationWorker implements Callable<Boolean>{//, Future<Boolean> {

	private Vector<Integer> dominantLabels;
	private Vector<Integer> labelCounts;
	private Random randGen;
	
	private int nodeId;

	// Shared
	private Vector<Node> nodeList;

	public LabelPropagationWorker(Vector<Node> nodeList) {
		dominantLabels = new Vector<Integer>();
		labelCounts = new Vector<Integer>(nodeList.size());
		
		for (int i=0; i<nodeList.size(); i++) {
			labelCounts.add(Integer.valueOf(0));
		}
		randGen = new Random();
		
		this.nodeList = nodeList;//Shared reference
		
		//Node to process
		//this.nodeId=nodeId;
		
		System.out.println("Worker created.");
		
	}
	
	public void setNodeToProcess(int nodeId) {
		this.nodeId=nodeId;
	}

	@Override
	public Boolean call() {
		
		if (nodeId==-1) {
			return Boolean.FALSE;
		}

		boolean continueRunning = false;

		Collections.fill(labelCounts, Integer.valueOf(0));
		dominantLabels.clear();

		Node currentNode = nodeList.get(nodeId);
		int maxCount = 0;
		// Neighbors
		for (Integer neighborId : currentNode.getNeighbors()) {
			// for (int j=0; j<currentNode.getNeighbors().size(); j++) {
			int nLabel = nodeList.get(neighborId).getLabel();
			if (nLabel == 0)
				continue; // No label yet (only if initial labels are given?)

			int nLabelCount = labelCounts.get(nLabel) + 1;
			labelCounts.set(nLabel, nLabelCount);// Careful of wrapping
													// un-wrapping here!

			if (maxCount < nLabelCount) {
				maxCount = nLabelCount;
				dominantLabels.clear();
				dominantLabels.add(nLabel);
			} else if (maxCount == nLabelCount) {
				dominantLabels.add(nLabel);
			}
		}

		if (dominantLabels.size() > 0) {
			// Randomly select from dominant labels
			int rand = randGen.nextInt(dominantLabels.size());
			rand = dominantLabels.get(rand);

			// Check if *current* label of node is also dominant
			if (labelCounts.get(currentNode.getLabel()) != maxCount) {
				// it's not. continue
				continueRunning = true;
			}
			currentNode.setLabel(rand);
		}
		return Boolean.valueOf(continueRunning);
	}

}
