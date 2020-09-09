
/*
 *  Fibonacci Heap PROJECT - Data Structures Course, Tel Aviv University
 * 
 *  By :
 * 		Nathan Bloch
 *		ID: 316130707
 * 		Username: nathanbloch
 * 
 * 		
 * 		Shay Fux
 * 		ID: 313452252
 * 		Username: shayakivafux
 * 
 */
/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over integers.
 * 
 */
public class FibonacciHeap {

	private HeapNode startRoot;     // pointer to the start of the heap
	private HeapNode min;     // pointer to the minimum node in the heap
	private int size;     //represents the total number of nodes in the heap
	private int numOfRoots;     //represents the current number of roots, which is the same as number of trees in heap
	private int numOfMarked;     // represents the number of current marked nodes in the heap

	public static int totalCuts = 0;     //total number of cuts that were executed
	public static int totalLinks = 0;     //total number of links that were executed

	//Constructor for the empty fibonacci heap. THIS IS THE ONLY CONSTRUCTOR
	//ALL INSERTS TO THE HEAP ARE DONE WITH THE INSERT FUNCTION
	//O(1) time complexity
	public FibonacciHeap() {
		this.min = null;
		this.size = 0;
		this.startRoot = null;
		this.numOfRoots = 0;
		this.numOfMarked = 0;
	}
	/**
	 * public boolean isEmpty()
	 *
	 * precondition: none
	 * 
	 * The method returns true if and only if the heap
	 * is empty.
	 *   
	 */

	//This method returns true if and only if the heap is empty.
	//An empty heap has 0 nodes in the heap, and therefore the condition is as follows.
	//O(1) time complexity
	public boolean isEmpty() {
		return this.size() == 0; // should be replaced by student code
	}

	/**
	 * public HeapNode insert(int key)
	 *
	 * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
	 */

	//Insertion function to the heap. The parameter is the the key of the node to be inserted.
	//Assumption: parameter key is unique and there is no node with a such a key in the heap
	//After insertion, minimum node is updated if needed, and the size of the heap incremented.
	//After insertion, the start root of the heap points to the new node.
	//O(1) time complexity
	public HeapNode insert(int key)
	{
		HeapNode node = new HeapNode(key);
		this.numOfRoots += 1;

		if(this.isEmpty()) { //Tree is Empty
			this.min = node;
			this.size++;
			this.startRoot = node;

			return node;
		}
		//If we are here - Tree is not empty..

		//Update Min
		if(key <= this.min.getKey())
			this.min = node;

		if(this.startRoot.getPrev() == null) { // If there is only 1 root - 1 tree
			node.setNext(this.startRoot);
			node.setPrev(this.startRoot);	
			this.startRoot.setNext(node);
			this.startRoot.setPrev(node);

		}
		else { // More than 1 root in the heap
			node.setNext(this.startRoot);
			node.setPrev(this.startRoot.getPrev());
			this.startRoot.getPrev().setNext(node);
			this.startRoot.setPrev(node);
		}

		this.startRoot = node;
		this.size++;	

		return node;
	}

	//This is a side function that is used in the delete-min function.
	//This function actually deletes the node given, and makes his children to be roots in the heap.
	//The function takes care of all different cases possible. The function actually prepares the heap for the
	//consolidation process which will come afterwards.
	//Time complexity is O(log n) at most, when needed to take care of all children of the deleted node.
	public void deleteAndLink(HeapNode parent) { // node is the father deleted
		//Atleast 1 root, parent != null

		// Only 1 root
		if(parent.getNext() == null) { 
			HeapNode child = parent.getChild();
			//Child is the only child; he doesn't have brothers
			if(child.getNext() == null) {
				this.startRoot = child;

				child.setParent(null);
				parent.setChild(null);
				return;
			}
			else { //Child has at-least 1 brother; he is not alone
				HeapNode node = child;
				while(node != child.getPrev()) {
					node.setParent(null);
					node = node.getNext();
				}
				child.getPrev().setParent(null);

				this.startRoot = child;
				parent.setChild(null);

				return;
			}
		}

		//Atleast 2 roots in the heap
		if(parent.getChild() == null){ // If no children - just jump over the the node
			parent.getPrev().setNext(parent.getNext());
			parent.getNext().setPrev(parent.getPrev());
			if (parent==this.startRoot) {  //no children-->next brother will be startroot
				this.startRoot = parent.getNext();
			}
			parent.setNext(null);
			parent.setPrev(null);
			return;
		}

		//Parent has a child
		HeapNode child = parent.getChild(); // not null for sure!
		//Only 1 child..
		if(child.getNext() == null) {
			parent.getPrev().setNext(child);
			child.setPrev(parent.getPrev());

			parent.getNext().setPrev(child);
			child.setNext(parent.getNext());

			if (parent==this.startRoot) {
				this.startRoot = child;
			}
			parent.setChild(null);
			parent.setNext(null);
			parent.setPrev(null);
			child.setParent(null);
			return;
		}
		//If we are here - child has a brother; -> child has a prev/next attributes that are null

		//Here we change all children's parent to be null
		HeapNode node = child;
		while(node != child.getPrev()) {
			node.setParent(null);
			node = node.getNext();
		}
		child.getPrev().setParent(null);

		//Pointers correction -> actual deletion of the node
		parent.getPrev().setNext(child);
		parent.getNext().setPrev(child.getPrev());

		child.getPrev().setNext(parent.getNext());
		child.setPrev(parent.getPrev());

		if (parent==this.startRoot) {
			this.startRoot = child;
		}
		parent.setChild(null);
		parent.setNext(null);
		parent.setPrev(null);
	}

	/**
	 * public void deleteMin()
	 *
	 * Delete the node containing the minimum key.
	 *
	 */

	/*This method deletes the node containing the minimum key.
	After deletion, a process of consolidation is taking action as after each delete-min action.
	This function calls the function deleteAndLink which actually does the deletion process.
	deleteAndLink function handles all different cases that might occur depending on the state of the heap.
	After consolidation, the heap is in the structure as a binomial heap, as consolidation reshapes the heap.
	Time complexity is O(log n) amortized.
	 */
	public void deleteMin() {
		if(this.isEmpty())
			return;

		if(this.size() == 1) {
			this.numOfRoots = 0;
			this.size = 0;
			this.startRoot = null;
			this.min = null;

			return;
		}

		this.deleteAndLink(this.min);
		this.size--;

		int counter =1;
		HeapNode newMinimum = this.startRoot;

		HeapNode node = this.startRoot.getNext();
		if(node == null)
			this.min = this.startRoot;
		else {
			while(node != this.startRoot) {
				if(node.getKey() <= newMinimum.getKey())
					newMinimum = node;
				counter++;
				node = node.getNext();
			}
			this.min = newMinimum;
			this.numOfRoots = counter;
		}

		this.consolidate();

		return;
	}


	/*This function executes the algorthim of consolidating a fibonacci heap into a binomial heap using the buckets theory.
	First action is to initialize an array of length: log(size) in base phi is created. Each array index infers as the rank of the tree.
	the i'th index of the array represents the bucket of trees from rank i. the algorithm is as follows: 
	For each tree in the heap, go to the index of the tree's rank. if the bucket is empty: put the tree in it.
	otherwise, bucket is full and some tree of the same rank is there. LINK BOTH TREES TO RECEIVE ONE TREEthat has an incremented(by 1) rank. 
	Continue with that tree the same algorithm - until an empty bucket is found for the tree.
	The algorithm ends when all trees of the heap are in the buckets, and the array returned represents the buckets with the trees in it.
	Worst-case run time can be up to O(n), if there are O(n) trees in the heap with O(n) elements in it.
	 */
	public HeapNode[] to_buckets() {
		int n = this.size+1;
		int numBuckets = (int) (Math.log(n) /Math.log(1.618)) + 1 ;
		HeapNode[] buckets = new HeapNode[numBuckets];

		if(this.isEmpty())
			return null;

		if(this.numOfRoots == 1) {
			buckets[this.startRoot.getRank()] = this.startRoot;
			return buckets;

		}

		//Now ATLEAST 2 ROOTS IN THE HEAP

		this.startRoot.getPrev().setNext(null);
		HeapNode x = this.startRoot;
		HeapNode y;
		while(x != null ) {
			y = x;
			x = x.getNext();
			y.setNext(null);
			y.setPrev(null);
			while(buckets[y.getRank()] != null) {
				if(y.getKey() <= buckets[y.getRank()].getKey())
					y = link(y, buckets[y.getRank()]);
				else if(y.getKey() >= buckets[y.getRank()].getKey())
					y = link(buckets[y.getRank()], y);
				buckets[y.getRank() - 1] = null;
			}
			buckets[y.getRank()] = y;		
		}
		return buckets;
	}
	/*
	 * This function receives an array of heap-nodes with all trees of the heap after the to_buckets function.
	 * The to_buckets function consolidates the heap and links as many trees as possible, after this function an array of all remaining trees is created.
	 * The array's i'th index is the tree from rank i in the heap, and there might be no such tree from that rank.
	 * This function receives the array from the to_buckets function, and connects all trees in that array. Actually here we form back a heap from the buckets.
	 * Time complexity is O(log n) at base phi, and actually O(log n). That's because the array's length is: O(log n) in base phi
	 * and in the function we loop over the array and connect the trees into a heap.
	 * Returned value is the new start Root pointer after the consolidation.
	 * Total time complexity is O(log n) at all cases.
	 */
	public HeapNode from_buckets(HeapNode[] buckets) {
		HeapNode x = null;
		for(HeapNode node: buckets) {
			if(node != null) {
				if(x == null) {
					x = node;
					x.setNext(null);
					x.setPrev(null);
				}
				else {
					if(x.getNext() == null) { 
						x.setNext(node);
						x.setPrev(node);

						node.setPrev(x);
						node.setNext(x);

					}
					else { //adding node in the last position
						HeapNode lastNode = x.getPrev();
						x.setPrev(node);
						node.setNext(x);

						lastNode.setNext(node);
						node.setPrev(lastNode);
					}
				}
			}

		}
		return x;
	}

	/*
	 * This is the main consolidation function. The function is called after deletion of a node from the heap. The function does a consolidation process
	 * with the buckets theory, using the methods to_buckets and from_buckets.
	 * Afterwards, minimum attribute pointer is updated and the attribute of numOfRoots is updated also.
	 * After this function there are O(log n) trees in the heap which is the desired goal.
	 * Time complexity is O(log n) amortized as taught at class.
	 * 
	 */
	public void consolidate() {
		HeapNode[] buckets = this.to_buckets();
		this.startRoot = this.from_buckets(buckets);
		//Updating minimum
		if(this.startRoot == null) {
			this.size = 0;
			this.min = null;
			this.numOfRoots = 0;
			return;
		}
		HeapNode min = this.startRoot;
		HeapNode node = this.startRoot.getNext();
		if(node == null) {
			this.min = this.startRoot;
			this.numOfRoots = 1;
			return;
		}
		int counter = 1; //in order to count number of roots
		while(node != this.startRoot) {
			if(node.getKey() <= min.getKey())
				min = node;
			node = node.getNext();
			counter++;
		}

		this.min = min;
		this.numOfRoots = counter;
	}

	/*
	 * A static function that does the linking process of two trees from the same rank. 
	 * An assumption is: keys in the tree Node1 < keys in the tree Node2.
	 * We can assume that by sending this function the trees in the right order -> if needed we can reverse the parameters of that method.
	 * This linking function covers all different cases possible , and it increments the total number of links done.
	 * The function returns the root of the linked tree.
	 * Total time Complexity is O(1) as only constant actions are done in this function.
	 */
	public static HeapNode link(HeapNode node1, HeapNode node2) { // Node1.key < node2.key, NO NULLS ACCEPTED
		totalLinks++;
		if(node1.getChild() == null) { // node 1 does not have children
			node1.setChild(node2);
			node2.setParent(node1);
		}
		else if(node1.getChild().getNext() == null) { // node1 has exactly 1 child

			node1.getChild().setPrev(node2);
			node1.getChild().setNext(node2);
			node2.setNext(node1.getChild());
			node2.setPrev(node1.getChild());

			node2.setParent(node1);
			node1.setChild(node2);
		}
		else { // node1 has more than 1 child - no nulls problems				
			node2.setNext(node1.getChild());
			node2.setPrev(node1.getChild().getPrev());

			node1.getChild().getPrev().setNext(node2);
			node1.getChild().setPrev(node2);

			node2.setParent(node1);

			node1.setChild(node2);
		}
		node1.setRank(node1.getRank() + 1);
		return node1;
	}


	/**
	 * public HeapNode findMin()
	 *
	 * Return the node of the heap whose key is minimal. 
	 *
	 */	
	/*
	 * The function returns a heapNode that has the minimum key. If tree is empty - returns null anyway.
	 * Time Complexity is O(1) as this attribute is maintained during each and every other method.
	 */
	public HeapNode findMin() {
		return this.min; 
	} 

	/**
	 * public void meld (FibonacciHeap heap2)
	 *
	 * Meld the heap with heap2
	 *
	 */
	/*
	 * This function recieves a fibonacci heap as an arguments and melds it in the current(this) heap.
	 * Meld algorithm is done in the lazy way as there are no limitations on the number of tree from each rank nor the number of total trees.
	 * The algorithm simply adds heap2 to the end of the current heap by connecting the pointers. 
	 * Number of trees after the action is the sum of trees from both heaps.
	 * The method maintains the needed attributes - size, number of trees, minimum.
	 * Total Time complexity is O(1) - constant time needed for each meld operation.
	 */
	public void meld (FibonacciHeap heap2)
	{
		//Heap 2 is not null...
		if(heap2 == null)
			return;


		this.numOfRoots += heap2.numOfRoots;

		if(heap2.isEmpty()) // No need to meld
			return;
		if(this.isEmpty()) { //this is empty, so change pointers to heap 2 
			this.min = heap2.min;
			this.size = heap2.size;
			this.startRoot = heap2.startRoot;
			return;
		}
		//updating minimum
		if(heap2.min.getKey() < this.min.getKey())
			this.min = heap2.min;

		if(this.numOfRoots == 1 && heap2.numOfRoots == 1) { // THIS AND HEAP 2 have one root each
			this.startRoot.setNext(heap2.startRoot);
			this.startRoot.setPrev(heap2.startRoot);

			heap2.startRoot.setNext(this.startRoot);
			heap2.startRoot.setPrev(this.startRoot);

		}
		if(this.numOfRoots == 1 && heap2.numOfRoots != 1) { //This has 1 root, heap 2 has more than 1
			this.startRoot.setNext(heap2.startRoot);
			this.startRoot.setPrev(heap2.startRoot.getPrev());

			heap2.startRoot.getPrev().setNext(this.startRoot);
			heap2.startRoot.setPrev(this.startRoot);

		}
		if(this.numOfRoots != 1 && heap2.numOfRoots == 1) { // Heap 2 has 1 root, this has more
			heap2.startRoot.setNext(this.startRoot);
			heap2.startRoot.setPrev(this.startRoot.getPrev());

			this.startRoot.getPrev().setNext(heap2.startRoot);
			this.startRoot.setPrev(heap2.startRoot);

		}
		else { // Both have more than 1 tree
			HeapNode lastNodeHeap2 = heap2.startRoot.getPrev();

			this.startRoot.getPrev().setNext(heap2.startRoot);
			heap2.startRoot.setPrev(this.startRoot.getPrev());

			this.startRoot.setPrev(lastNodeHeap2);
			lastNodeHeap2.setNext(this.startRoot);
		}

		this.size += heap2.size;  		
	}

	/**
	 * public int size()
	 *
	 * Return the number of elements in the heap
	 *   
	 */

	/*
	 * The method returns the size of the heap. The size of heap is defined  to be the total current number of elements in the heap.
	 * Time Complexity is O(1) - constant time. The size attribute is maintained throughout all operations and therefore time complexity is constant.
	 */
	public int size()
	{
		return this.size;
	}

	/**
	 * public int[] countersRep()
	 *
	 * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
	 * 
	 */
	/*
	 * The function returns a counter array where the i'th entry is the number of trees of order i in the heap.
	 * If the heap is empty - returns an empty array of size 0.
	 * Algorithm is as follows: first find the maximal order of the tree in the heap by checking all trees in the heap. Might take up to O(n) Worst case.
	 * Then initialize a counters array of the size: max{Ranks} + 1
	 * Afterwards, for each tree in heap, if the tree is of order i, increment by 1 the i'th entry of the array.
	 * After the completion of checking each tree - return the counter array.
	 * WORST-CASE SCENARIO: O(n) trees, each with one element. Each step in the algorithm will be take O(n) time.
	 * Time Complexity at the WORST CASE: O(n). Each step of the algorithm might take O(n) time at the Worst Case, so the total complexity is O(n).
	 */
	public int[] countersRep() {
		if(this.isEmpty())
			return new int[0];
		if(this.numOfRoots == 1) { //If there is only 1 root..
			int[] arr = new int[this.startRoot.getRank()+1];
			arr[this.startRoot.getRank()] = 1;
			return arr;
		}
		//Atleast two roots
		int max = this.startRoot.rank;
		HeapNode node = this.startRoot.getNext();
		while(node != this.startRoot) {
			max = Math.max(max, node.getRank());
			node = node.getNext();
		}

		int[] arr = new int[max+1];
		node = this.startRoot.getNext();	
		arr[this.startRoot.getRank()] += 1; //initialize first because we start from the next one
		while(node != this.startRoot) {
			arr[node.getRank()] += 1;
			node = node.getNext();
		}

		return arr;
	}

	/**
	 * public void delete(HeapNode x)
	 *
	 * Deletes the node x from the heap. 
	 *
	 */

	/*
	 * The deletion function from the heap. The function decreases the node's key to be the minimal in the heap and then uses the delete-min function.
	 * Deletion is implemented by decrease key and then  delete-min.
	 * Time complexity is O(log n) amortized.
	 * 
	 */
	public void delete(HeapNode x) {
		int delta = x.getKey() - (this.min.getKey() - 50);
		this.decreaseKey(x, delta);
		this.deleteMin();

	}

	/**
	 * public void decreaseKey(HeapNode x, int delta)
	 *
	 * The function decreases the key of the node x by delta. The structure of the heap should be updated
	 * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
	 */

	/*
	 * The decrease key method. The function receives a heap node x, and and interger delta. It sets the the key of x to be -> x.getKey() - delta
	 * It updates the min attribute(pointer) if needed. If the node x is a root of some tree -> it doesnt perform any further action as the tree stays the same.
	 * Otherwise, the node x has a parent. If no harm was made to the heap, meaning that the decrease of x's key didn't break the heap's variant -
	 * meaning that x.key > x.parent.key even after the decrease, then nothing needs to be as the heap still hold his variant.
	 * If heap's variant is broken, then as taught at the lectures, a series of cascading cuts has to be done. Cascading cuts happen only when the heap's variant is violated.
	 * Time Complexity is O(1) amortized as taught at class.
	 */
	public void decreaseKey(HeapNode x, int delta) {
		//set the key of x
		x.setKey(x.getKey() - delta);
		//check minimums..

		if(x.getKey() < this.min.getKey()) {
			this.min = x;
		}

		//Case where x is a root of a tree
		if(x.getParent() == null) {
			return;
		}
		//x has a parent
		if (x.getKey()>= x.getParent().getKey()) {//no harm taken-->no need to cuscate
			return;
		}
		cascading_cut(x, x.getParent());
		return; // should be replaced by student code
	}

	/*
	 * This is the function that cuts a node from it's parent. The function's parameters are node x, and his parent y. We cut x from y.
	 * Each cut we increment the totalCuts static counter. It counts the total number of cuts done so far.
	 * In addition we maintain a static integer attribute that represents the current number of marked nodes in the heap.
	 * Cutting the node x from the parent y, means moving the sub-tree of x to be a root in the heap -> x will be the root of a new tree in heap.
	 * After making the sub-tree of x a tree, the start of the heap will point to that tree, and the total number of trees in the heap is incremented by 1.
	 * Time complexity is O(1) - constant time.
	 */
	public void cut(HeapNode x, HeapNode y) { //x is the one we cut, we cut x from parent y
		totalCuts++;
		if(x.isMark())
			this.numOfMarked--;

		x.setParent(null);
		x.setMark(false);
		y.setRank(y.getRank()-1);

		if(x.getNext() == null) { //x is single child
			y.setChild(null);
		}
		else {
			if(x == y.getChild()) {
				y.setChild(x.getNext()); 
			}
			if (x.getNext()==x.getPrev()) {//x has only one bro
				x.getPrev().setNext(null);
				x.getNext().setPrev(null);
			}
			else {
				x.getPrev().setNext(x.getNext());
				x.getNext().setPrev(x.getPrev());
			}
			x.setNext(null);
			x.setPrev(null);
		}
		if (this.numOfRoots==1) {//only one root
			x.setNext(this.startRoot);
			x.setPrev(this.startRoot);
			this.startRoot.setNext(x);
			this.startRoot.setPrev(x);
		}
		else {//at least 2 roots in the heap
			x.setNext(this.startRoot);
			x.setPrev(this.startRoot.getPrev());

			this.startRoot.getPrev().setNext(x);
			this.startRoot.setPrev(x);
		}

		this.startRoot = x;
		this.numOfRoots++;
	}

	/*
	 * Cascading cut method. The method uses the cut function. The method is called after a decrease-key action when the result of it 
	 * led to the invariant to break. The cascading cut method fixes the invariant of the heap by cutting that problematic node.
	 * The function cuts all marked nodes one by one until one of two: I. an unmarked node is reached or II. the root of the tree is reached.
	 * This algorithm improves the amortized complexity time of decrease-key - > amortized time is O(1).
	 * When a unmarked node y is reached, we mark it as it lost his child, and then increment the numOfMarked static attribute.
	 * The method performs a series of cascading cuts that might cut all nodes until the root.
	 * Time complexity is O(1) amortized.
	 * 
	 */
	public void cascading_cut(HeapNode x, HeapNode y) { // y is the parent of x
		cut(x, y);
		if(y.getParent() != null) {
			if(!y.isMark()) {
				y.setMark(true);
				this.numOfMarked++;
			}
			else {
				cascading_cut(y,y.getParent());
			}
		}

	}

	/**
	 * public int potential() 
	 *
	 * This function returns the current potential of the heap, which is:
	 * Potential = #trees + 2*#marked
	 * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
	 */

	/*
	 * This function returns the potential of the heap. We saw that the potential function is as follows:
	 * Potential = #trees + 2*#marks.
	 * Therefore the returned value is: numOfRoots attribute + 2*(numOfMarked attribute)
	 * Time complexity is O(1) at the WC as we maintain the required attribute throughout all operations on the heap.
	 */
	public int potential() 
	{    
		return this.numOfRoots + 2*this.numOfMarked; // should be replaced by student code
	}

	/**
	 * public static int totalLinks() 
	 *
	 * This static function returns the total number of link operations made during the run-time of the program.
	 * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
	 * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
	 * in its root.
	 */

	/*
	 * A static function that returns a counter of the total number of link operations done till that point during the run-time of the program.
	 * Time complexity is O(1) as we maintain the needed static attribute - a counter of total link operations done.
	 * 
	 */
	public static int totalLinks()
	{    
		return totalLinks; // should be replaced by student code
	}

	/**
	 * public static int totalCuts() 
	 *
	 * This static function returns the total number of cut operations made during the run-time of the program.
	 * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
	 */

	/*
	 * A static function that returns a counter of the total number of cut operations done till that point during the run-time of the program.
	 * Time complexity is O(1) as we maintain the needed static attribute - a counter of total cut operations done.
	 * 
	 */
	public static int totalCuts()
	{    
		return totalCuts; // should be replaced by student code
	}

	/**
	 * public static int[] kMin(FibonacciHeap H, int k) 
	 *
	 * This static function returns the k minimal elements in a binomial tree H.
	 * The function should run in O(k(logk + deg(H)). 
	 */
	/*
	 * work exactly the same as the previous insert method with one change meant for k-min method:
	 * when we insert a node to the minimum heap we maintain in k-min method we need to keep a pointer
	 * to the original heap so we change the signature of the method that we can update the field kminpointer
	 * to node in the original heap.
	 */
	public HeapNode insert(int key,HeapNode pointer){
		HeapNode node = new HeapNode(key);
		node.kminpointer = pointer;//this is meant for K-min only!! keep a pointer to the orginal heap 
		this.numOfRoots += 1;

		if(this.isEmpty()) { //Tree is Empty
			this.min = node;
			this.size++;
			this.startRoot = node;

			return node;
		}
		//If we are here - Tree is not empty..

		//Update Min
		if(key <= this.min.getKey())
			this.min = node;

		if(this.startRoot.getPrev() == null) { // If there is only 1 root - 1 tree
			node.setNext(this.startRoot);
			node.setPrev(this.startRoot);	
			this.startRoot.setNext(node);
			this.startRoot.setPrev(node);

		}
		else { // More than 1 root in the heap
			node.setNext(this.startRoot);
			node.setPrev(this.startRoot.getPrev());
			this.startRoot.getPrev().setNext(node);
			this.startRoot.setPrev(node);
		}

		this.startRoot = node;
		this.size++;	

		return node;
	}
	/*
	 * In this method we return the k items in the binomial tree input with the minimal key.
	 * first we initialized an array with length k , then we initialized a new fibonacchi heap which we will maintain as minimum heap to find the k-minimal keys.
	 * First, we insert the key of the root of the binomial tree to minimum heap and keep a pointer to this node in the binomial tree in kminpointer field.
	 * Then we do k times:
	 * 		insert at the i'th iteration the minimum node in the minimum heap to the i'th place in the array(0<=i<=k-1).
	 * 		delete-min in the minimum-heap and then if the node has children in the original binomial tree insert them to the minimum heap by the insertominheap method(we have a pointer to the original heap)
	 * Time complexity: we do k times delete-min in the minimum heap and then ,if has, we insert his children in the binomial heap to the minimum heap
	 * at w.c the minimum node will have O(Deg(H)) children in the original binomial tree ,therefore the insertion of them to the minimum heap cost O(k*Deg(H)) time and deletion cost O(log(k*Deg(H))=O(log(k)+log(Deg(H)))--> deletion cost=O(log(k)+Deg(H)).
	 * in k iterations the w.c time complexity is: O(k(log(k)+Deg(H))).
	 */

	public static int[] kMin(FibonacciHeap H, int k) {
		FibonacciHeap minheap = new FibonacciHeap();
		int[] arrkeys = new int[k];
		if (H.isEmpty()) {//startroot==null
			return new int[0];//need to check the instructions
		}
		//there is a root!=null
		if (H.startRoot.getChild()==null) {//Binomial tree with rank 0
			int[] arr1 = {H.startRoot.getKey()};
			return arr1;
		}
		//At this point the root has at least one child
		//HeapNode pointer = H.startRoot;//keep a pointer to the root -we will promote to his child each iteration 
		//need to keep a pointer to node at the binomial heap!!maybe need to add a field in the contructor of heapnode
		minheap.insert(H.startRoot.getKey(),H.startRoot);//insert the root which has the minimal key in binomial tree and keep a pointer to the the orginal binomial tree
		int i = 0;
		while (i<k) {//repeat k times
			HeapNode currnetminumumi = minheap.min;//current minimum at iteration i
			arrkeys[i] = currnetminumumi.getKey();//insert the cuurent minimum key to the array at iteration i
			minheap.deleteMin();
			if (currnetminumumi.kminpointer.getChild()!=null) {//the currnet minimal node kmin field doesn't have children-->do nothing and keep going the loop
				minheap.insertominheap(currnetminumumi.kminpointer.getChild());//this method don't take nulls!,insert the children of kmin field at currnet minimal node to minheap- max Deg(H) children
			}
			i++;
		}
		return arrkeys;
	}
	/*
	 * This method insert the children(and keep a pointer to the original heap) of a minimal node to the minimum heap that we maintain in k-min method.
	 * Time complexity: in w.c we insert the children of the root of the binomial tree with Deg(H)-the degree of the binomial tree ,
	 * therefore the time complexity in the w.c is Deg(H).
	 */
	public void insertominheap(HeapNode start) {//start!=null and he is the child of the return minimum from deletemin from heapmin
		HeapNode nextchild = start.getNext();
		this.insert(start.getKey(),start);//keep pointer to start at original binomial heap
		if (nextchild ==null) {//start is a single child
			return;
		}
		while (nextchild != start ) {
			this.insert(nextchild.getKey(),nextchild);//keep pointer to nextchild at original binomial heap
			nextchild = nextchild.getNext();
		}
	}

	/**
	 * public class HeapNode
	 * 
	 * If you wish to implement classes other than FibonacciHeap
	 * (for example HeapNode), do it in this file, not in 
	 * another file 
	 *  
	 */
	public class HeapNode{

		//THIS MUST BE PUBLIC
		public int key;     //represents the key of the node

		//ALL OTHERS CAN BE PRIVATE
		private int rank;     //represents the rank of the node
		private boolean mark;     //The marked status of the node: FALSE is not marked, TRUE is marked
		private HeapNode child;     //pointer to the child of the node
		private HeapNode next;     //pointer to the next of the node
		private HeapNode prev;     //pointer to the previous node
		private HeapNode parent;     //pointer to the parent of the node
		public HeapNode kminpointer;	//pointer to the node in the original fibonnachi heap (meant for k-min method)



		//Constructor of a node. The parameter is the key of the node.
		//O(1) time complexity
		public HeapNode(int key) {
			this.key = key;
			this.rank = 0;
			this.mark = false;

		}

		//THIS MUST BE PUBLIC
		public int getKey() {
			return this.key;
		}

		//ALL OTHER GETTERS / SETTERS - PRIVATE!
		private int getRank() {
			return rank;
		}

		private void setRank(int rank) {
			this.rank = rank;
		}

		private boolean isMark() {
			return mark;
		}

		private void setMark(boolean mark) {
			this.mark = mark;
		}

		private HeapNode getChild() {
			return child;
		}

		private void setChild(HeapNode child) {
			this.child = child;
		}

		private HeapNode getNext() {
			return next;
		}

		private void setNext(HeapNode next) {
			this.next = next;
		}

		private HeapNode getPrev() {
			return prev;
		}

		private void setPrev(HeapNode prev) {
			this.prev = prev;
		}

		private HeapNode getParent() {
			return parent;
		}

		private void setParent(HeapNode parent) {
			this.parent = parent;
		}

		private void setKey(int key) {
			this.key = key;
		}
	}
}
