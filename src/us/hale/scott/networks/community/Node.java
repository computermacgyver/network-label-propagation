package us.hale.scott.networks.community;

import java.util.HashSet;
import java.util.Set;

public class Node {
	
	private int id;
	private int label;
	private Set<Integer> neighbors;
	
	public Node(int id, int label) {
		this.id=id;
		this.label=label;
		this.neighbors = new HashSet<Integer>();
	}

	public int getId() {
		return id;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public Set<Integer> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(Set<Integer> neighbors) {
		this.neighbors = neighbors;
	}
	
	public void addNeighbor(int id) {
		this.neighbors.add(Integer.valueOf(id));
	}
	
	



}
