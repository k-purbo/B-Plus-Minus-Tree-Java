package edu.brandeis.cs127.pa3;

import java.util.Arrays;

/**
    Internal Nodes of B+-Trees.
    @author cs127b
 */
public class InternalNode extends Node{

	/**
       Construct an InternalNode object and initialize it with the parameters.
       @param d degree
       @param p0 the pointer at the left of the key
       @param k1 the key value
       @param p1 the pointer at the right of the key
       @param n the next node
       @param p the previous node
	 */
	
	public InternalNode (int d, Node p0, int k1, Node p1, Node n, Node p){

		super (d, n, p);
		ptrs [0] = p0;
		keys [1] = k1;
		ptrs [1] = p1;
		lastindex = 1;

		if (p0 != null) p0.setParent (new Reference (this, 0, false));
		if (p1 != null) p1.setParent (new Reference (this, 1, false));
	}

	/**
       The minimal number of keys this node should have.
       @return the minimal number of keys a leaf node should have.
	 */
	public int minkeys () {
		if (parentref == null) {
			return 1;
		}
		else {
			int	minptrs = (int) Math.ceil(degree / 2.0);
			return minptrs - 1;
		}
	}

	/**
       Check if this node can be combined with other into a new node without splitting.
       Return TRUE if this node and other can be combined. 
	 */
	public boolean combinable (Node other) {
		
		if (lastindex <= this.minkeys() && other.lastindex <= other.minkeys()) {
			return true;
		}
		else if (lastindex < this.minkeys() || other.lastindex < this.minkeys()) {
			if (lastindex + other.lastindex <= degree - 2) {
				return true;
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
	}


	/**
       Combines contents of this node and its next sibling (next)
       into a single node,
	 */
	public void combine () {

		if (this.combinable(next)) {
			int j = 0;
			int newLastindex = lastindex + next.lastindex + 1;
			for (int i = lastindex + 1; i < newLastindex + 1; i++ ) {
				if (j == 0) {
					int demote = this.getParent().getNode().findPtrIndex(next.keys[1]);
					keys[i] = this.getParent().getNode().keys[demote];
					ptrs[i] = next.ptrs[j];
				}
				else {
					keys[i] = next.keys[j];
					ptrs[i] = next.ptrs[j];
				}
				
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
       one more pointer.  Returns the key that must be inserted
       into parent node.
       @return the value to be inserted to the parent node
	 */
	public int redistribute () {
		int middle= (int) Math.ceil((lastindex + next.lastindex)/ 2.0);
		if (next.ptrs[0] == null ) {
			
			if (middle == lastindex - middle) {
				middle++;
			}
			
			int key = keys[middle];
			keys[middle] = 0;
			int iter = lastindex - middle;
			
			next.ptrs[0] = ptrs[middle];
			
			
			for (int i = 1; i <= iter; i--) {
				next.insertSimple(keys[i + middle], ptrs[i + middle], i);
				ptrs[i + middle] = null;
				keys[i + middle] = 0;
				lastindex--;
			}
			
			if (parentref == null) {
				Node newParent = new InternalNode(degree, this, next.keys[1], next, null, null);
				this.setParent(new Reference(newParent, 0, false));
			}

			return key;
		}
		else {
			middle++;
			
			int key = keys[middle];
			keys[middle] = 0;
			int j = next.lastindex + 1;
			int iter = lastindex - middle;
			
			Node tempPtr = next.ptrs[0];
			next.ptrs[0] = ptrs[middle];
			int tempIndex = this.getParent().getNode().findKeyIndex(next.keys[1]);
			int tempVal = this.getParent().getNode().getKey(tempIndex);
			this.insertSimple(tempVal, tempPtr , next.findKeyIndex(tempVal));
			
			int oldSize = lastindex;
			
			for (int i = oldSize; i > middle; i--) {
				
				next.insertSimple(keys[i], ptrs[i], next.findKeyIndex(keys[i]));
				ptrs[i] = null;
				keys[i] = 0;
				lastindex--;
			}
			
			this.getParent().getNode().keys[tempIndex] = next.keys[1];
			
			return next.keys[1];
		}

	}

	/**
       Inserts (val, ptr) pair into this node
       at keys [i] and ptrs [i].  Called when this
       node is not full.  Differs from {@link LeafNode} routine in
       that updates parent references of all ptrs from index i+1 on.
       @param val the value to insert
       @param ptr the pointer to insert 
       @param i the position to insert the value and pointer
	 */
	public void insertSimple (int val, Node ptr, int i) {
		
		lastindex++;
		int j = lastindex;
		i++;
		while (j > i) {
			
			keys[j] = keys[j - 1];
			ptrs[j] = ptrs[j - 1];
			ptrs[j].getParent().increaseIndex();
			
			j--;
		}
		
		keys[i] = val;
		ptrs[i] = ptr;
		ptr.setParent(new Reference(this, i, false));
	}

	/**
       Deletes keys [i] and ptrs [i] from this node,
       without performing any combination or redistribution afterwards.
       Does so by shifting all keys and pointers from index i+1 on
       one position to the left.  Differs from {@link LeafNode} routine in
       that updates parent references of all ptrs from index i+1 on.
       @param i the index of the key to delete
	 */
	public void deleteSimple (int i) {

		while (i < lastindex) {
			keys[i] = keys[i + 1];
			ptrs[i] = ptrs[i + 1];
			ptrs[i].getParent().decreaseIndex();
		}
		
		keys[i] = 0;
		ptrs[i] = null;
		lastindex--;
	}


	/**
       Uses findPtrInex and calls itself recursively until find the value or find the position 
       where the value should be.
       @return the reference pointing to a leaf node.
	 */
	public Reference search (int val) {
		
		int ptrIndex = this.findPtrIndex(val);
		return ptrs[ptrIndex].search(val);

	}

	/**
       Insert (val, ptr) into this node. Uses insertSimple, redistribute etc.
       Insert into parent recursively if necessary
       @param val the value to insert
       @param ptr the pointer to insert 
	 */
	public void insert (int val, Node ptr) {

		int t = this.findKeyIndex(val);
		if (keys[t] != val) {
			if (this.full()) {
				Node temp = next;
				next = new InternalNode(degree, null, 0, null, this, temp);
				if (temp != null) {
					temp.setPrev(next);
				}

				int promote = this.redistribute();

				this.insertSimple(val, ptr, this.findKeyIndex(val));
				this.getParent().getNode().insert(promote, next);
			}
			else {
				this.insertSimple(val, ptr, this.findKeyIndex(val));
			}
		}
	}

	public void outputForGraphviz() {

		// The name of a node will be its first key value
		// String name = "I" + String.valueOf(keys[1]);
		// name = BTree.nextNodeName();

		// Now, prepare the label string
		String label = "";
		for (int j = 0; j <= lastindex; j++) {
			if (j > 0) label += "|";
			label += "<p" + ptrs[j].myname + ">";
			if (j != lastindex) label += "|" + String.valueOf(keys[j+1]);
			// Write out any link now
			BTree.writeOut(myname + ":p" + ptrs[j].myname + " -> " + ptrs[j].myname + "\n");
			// Tell your child to output itself
			ptrs[j].outputForGraphviz();
		}
		// Write out this node
		BTree.writeOut(myname + " [shape=record, label=\"" + label + "\"];\n");
	}

	/**
       Print out the content of this node
	 */
	void printNode () {

		int j;
		System.out.print("[");
		for (j = 0; j <= lastindex; j++) {

			if (j == 0)
				System.out.print (" * ");
			else
				System.out.print(keys[j] + " * ");

			if (j == lastindex)
				System.out.print ("]");
		}
	}
}


