package edu.brandeis.cs127.pa3;

import java.util.Arrays;

public class Test {

	public static void main(String[] args) {
		Node n1 = new InternalNode(3, null, 0, null, null, null);
		n1.lastindex--;
		System.out.println(Arrays.toString(n1.keys));
		System.out.println(n1.lastindex);

	}
}
