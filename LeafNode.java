package edu.brandeis.cs127.pa3;

import java.util.Arrays;

/**
   LeafNodes of B+ trees
 */
public class LeafNode extends Node {

	/**
       Construct a LeafNode object and initialize it with the parameters.
       @param d the degree of the leafnode
       @param k the first key value of the node
       @param n the next node 
       @param p the previous node
	 */
	public LeafNode (int d, int k, Node n, Node p){
		super (d, n, p);
		keys [1] = k;
		lastindex = 1;
	}      


	public void outputForGraphviz() {

		// The name of a node will be its first key value
		// String name = "L" + String.valueOf(keys[1]);
		// name = BTree.nextNodeName();

		// Now, prepare the label string
		String label = "";
		for (int j = 0; j < lastindex; j++) {
			if (j > 0) label += "|";
			label += String.valueOf(keys[j+1]);
		}
		// Write out this node
		BTree.writeOut(myname + " [shape=record, label=\"" + label + "\"];\n");
	}

	/** 
	the minimum number of keys the leafnode should have.
	 */
	public int minkeys () {
		int	min = (int) Math.ceil(degree - 1 / 2.0);

		return min;
	}

	/**
       Check if this node can be combined with other into a new node without splitting.
       Return TRUE if this node and other can be combined. 
       @return true if this node can be combined with other; otherwise false.
	 */
	public boolean combinable (Node other){
		return (lastindex + other.lastindex <= degree - 1);

	}

	/**
       Combines contents of this node and its next sibling (nextsib)
       into a single node
	 */
	public void combine () {

		if (this.combinable(next)) {
			int j = 1;
			int newLastindex = lastindex + next.lastindex;
			for (int i = lastindex + 1; i < newLastindex + 1; i++ ) {
				keys[i] = next.keys[j];
				j++;
			}
			Node parent = next.getParent().getNode();
			parent.delete(parent.findKeyIndex(keys[lastindex + 1]));
			lastindex = newLastindex;
			next = next.next;
			if (next != null) {
				next.prev = this;
			}
			
		}
	}

	/**
       Redistributes keys and pointers in this node and its
       next sibling so that they have the same number of keys
       and pointers, or so that this node has one more key and
       one more pointer,.  
       @return int Returns key that must be inserted
       into parent node.
	 */
	public int redistribute (){ 
		
		int middle= (int) Math.ceil(lastindex / 2.0);
		if (middle == lastindex - middle) {
			middle++;
		}

		int iter = lastindex - middle;
		Node temp = next;
		next = new LeafNode(degree, 0, this, temp);
		next.lastindex = 0;
		if (temp != null) {
			temp.prev = next;
		}
		for (int i = 0; i <= iter; i++) {
				next.keys[i + 1] = keys[i + middle];
				keys[i + middle] = 0;
				next.lastindex++;
				lastindex--;
		}
	
		if (parentref == null) {
			Node newParent = new InternalNode(degree, this, next.keys[1], next, null, null);
			this.setParent(new Reference(newParent, 0, false));
		}

		return next.keys[1];
	}

	/**
       Insert val into this node at keys [i].  (Ignores ptr) Called when this
       node is not full.
       @param val the value to insert to current node
       @param ptr not used now, use null when call this method 
       @param i the index where this value should be
	 */
	public void insertSimple (int val, Node ptr, int i){

		lastindex++;
		int j = lastindex;
		i++;
		while (j > i) {
			
			keys[j] = keys[j - 1];
			j--;
		}
		
		keys[i] = val;
	}


	/**
       Deletes keys [i] and ptrs [i] from this node,
       without performing any combination or redistribution afterwards.
       Does so by shifting all keys from index i+1 on
       one position to the left.  
	 */
	public void deleteSimple (int i){

		while (i < lastindex) {
			keys[i] = keys[i + 1];
		}
		
		keys[i] = 0;
		ptrs[i] = null;
		lastindex--;
	} 

	/**
       Uses findKeyIndex, and if val is found, returns the reference with match set to true, otherwise returns
       the reference with match set to false.
       @return a Reference object referring to this node. 
	 */
	public Reference search (int val){
		
		int keyIndex = this.findKeyIndex(val);
		if (keys[keyIndex] == val) {
			return (new Reference (this, keyIndex, true));
		}
		else {
			return (new Reference (this, keyIndex, false));
		}
	}

	/**
       Insert val into this, creating split
       and recursive insert into parent if necessary
       Note that ptr is ignored.
       @param val the value to insert
       @param ptr (not used now, use null when calling this method)
	 */
	public void insert (int val, Node ptr) {
		
		

		if (this.full()) {
			Node temp = next;
			next = new InternalNode(degree, null, 0, null, this, temp);
			if (temp != null) {
				temp.setPrev(next);
			}
			int promote = this.redistribute();
		
			
			if (val < next.keys[1]) {
				this.insertSimple(val, null, this.findKeyIndex(val));
				

			}
			else {
				
				next.insertSimple(val, null, next.findKeyIndex(val));
			}



			this.getParent().getNode().insert(promote, next);
		}
		else {
			this.insertSimple(val, null, this.findKeyIndex(val));

		}

	}


	/**
       Print to stdout the content of this node
	 */
	void printNode (){
		System.out.print ("[");
		for (int i = 1; i < lastindex; i++) 
			System.out.print (keys[i]+" ");
		System.out.print (keys[lastindex] + "]");
	}
}
