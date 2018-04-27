import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;

public class BPlus {
	
	/*Fields*/
	//root - points to the root of this tree
	private Object root;
	//T- holds the T value of the tree. needed to ensure the leaf's size is legal
	private final int T;
	private storeNodes sn;	
	
	/*Behavior*/
	/*Constructors*/ 
	public BPlus(int t, int n){
		this.root= null;
		this.T=t;
		this.sn= new storeNodes(n);
	}//BPlus(int, int)
	
	
	//insets the key 'x' into the tree
	public void insert(double x){
		this.sn.insert(x);
		if (this.root == null){	//if this the first key, makes a leaf out of it
			Link newLink= new Link(x);
			this.root = new Leaf(newLink, this.T, null, null,  null);
		}
		else {	//if this isen't the first key
			if (this.root instanceof Leaf){	//if the root is a leaf, adds it to that leaf
				((Leaf)this.root).insert(new Link(x));
				if (((Leaf)this.root).overflow()){	//root is a Leaf, and also needs splitting. only happens once for each BPlus tree
					splitRoot();
				}
			}
			else {	//if root is a junction
				Object insertionPlace= ((Junction)this.root).find(x, true);	//finds where x should be inserted
				if (insertionPlace instanceof Leaf){	//the sons of the root are leafs
					Leaf insertPlace= (Leaf)insertionPlace;//inserts the new key
					insertPlace.insert(new Link(x));
					if (insertPlace.overflow()){	//if case the leaf is too big, splits it
						((Junction)this.root).splitSon(insertionPlace);
					}
					if (((Junction)this.root).overflow()){	//in case the root is now too big, splits it
						splitRoot();
					}
				}
				else {	//the sons of the root are junctions
					Leaf temp= searchHelper(x, insertionPlace, true);	//finds where x should be inserted
					temp.insert(new Link(x));	//inserts the new key
					if (temp.overflow())	//if case the leaf is too big, splits it
						(temp.getParent()).splitSon(temp);
					Junction tempJunction= temp.getParent();
					boolean stop= false;
					while ((!stop) && (tempJunction != this.root)){
						if (tempJunction.overflow()){	//makes sure that all the junctionhs are of legal size
							(tempJunction.getParent()).splitSon(tempJunction);	//otherwise, splits them
							tempJunction= tempJunction.getParent();
						}
						else
							stop= true;
					}
					if ((tempJunction == this.root) && (((Junction)this.root).overflow())){	//if the root is now too big, splits it
						splitRoot();
					}
				}
			}
		}
	}
	
	
	//splits the roon into 2 junctions/leafs and makes a new root
	private void splitRoot(){
		if (this.root instanceof Leaf){
			Leaf newLeaf= ((Leaf)this.root).split(null);	//splits the root
			Vector<Link> newElement= new Vector<Link>();
			newElement.add(((Leaf)this.root).getLast());
			Vector<Object> newPointers= new Vector<Object>();
			newPointers.add(this.root);
			newPointers.add(newLeaf);
			Junction newRoot= new Junction(newElement, this.T, null, newPointers);	//updates all of the second half's fields
			((Leaf)this.root).setParent(newRoot);
			newLeaf.setParent(newRoot);
			this.root= newRoot;	//makes the newly creates root the only root for this tree
		}
		else {	//if the root is a junction
			Junction newJunction= ((Junction)this.root).split(null);	//splits the root
			Vector<Link> newElement= new Vector<Link>();
			newElement.add(((Junction)this.root).getLast());
			Vector<Object> newPointers= new Vector<Object>();
			newPointers.add(this.root);
			newPointers.add(newJunction);
			Junction newRoot= new Junction(newElement, this.T, null, newPointers);	//updates all of the second half's fields
			for (int i=0; i < newPointers.size(); i++){	//sets the parents for the split junctions
				Object temp= newPointers.elementAt(i);
				if (temp instanceof Leaf){
					Leaf tempLeaf= (Leaf)temp;
					tempLeaf.setParent(newRoot);
				}
				else {
					Junction tempJunction= (Junction)temp;
					tempJunction.setParent(newRoot);
				}
			}
			((Junction)this.root).setParent(newRoot);
			newJunction.setParent(newRoot);
			((Junction)this.root).removeLastElement();
			this.root= newRoot;	//makes the newly creates root the only root for this tree
		}
	}//splitRoot
	
	
	//finds 'x' whithin this tree. returns 'null' if x is not in this tree
	public Link search(int x){
		if (this.sn.find(x)){	
			Leaf node= searchHelper(x, this.root, false);	//looks for the leaf where 'x' should be located
			Link temp=((Leaf)node).find(x);	//looks for 'x' in that leaf
			if (temp.getElement() == x)	//if the link found is x, returns its location
				return temp;
		}
		return null;	//if x is not found, returns 'null'
	}//search(int)
	
	
	//this method is used to lower the load off 'search', and also so it can be called recursively
	private Leaf searchHelper(double x, Object node, boolean isInserting){
		while (!(node instanceof Leaf)){	//while we have not found a leaf, keeps going deeper into the tree
			node= ((Junction)node).find(x, isInserting);
		}
		return (Leaf)node;
	}//searchHelper(int, Object, boolean)
		
	//finds the order of 'x' in this tree. assumes 'x' is present in this tree
	public int order(double x){		
		Object node= this.root;
		int ans= 0;
		while (!(node instanceof Leaf)){	//while we haven't found the key's leaf
			Junction junctionNode= (Junction)node;
			Vector<Link> elements= junctionNode.getElements();
			Vector<Object> pointers= junctionNode.getPointers();
			int i;	//i will hold the location of the key
			for (i= 0; ((i != -1) && (i < elements.size())); i++){	//sums the keys we skipped over
				if (x > elements.elementAt(i).getElement()){
					if (pointers.elementAt(i) instanceof Leaf)
						ans+= ((Leaf)pointers.elementAt(i)).getSize();
					else
						ans+= ((Junction)pointers.elementAt(i)).numOfElements();
				}
				else {
					node= pointers.elementAt(i);
					i= -2;
				}
			}
			if (i != -1){
				node= pointers.lastElement();
			}
		}
		Leaf leafNode= (Leaf)node;	//this is the leaf where the key is located
		Vector<Link> data= leafNode.getData();
		for (int i=0; i < leafNode.getSize(); i++){	//adds the number of keys that are before 'x' in his leaf
			if (x >= data.elementAt(i).getElement()){
				ans++;
			}
			else
				i= leafNode.getSize();
		}
		return ans - 1;
	}//order(int)
	
	private int myHight(Object node){
		int counter = 0;
		while(!(node instanceof Leaf)){
			node = (((Junction)node).getPointers().elementAt(0));
			counter++;
		}
		
		return counter;
	}
	private String MyTree;
	private void printAllTree(Object node){
		if(!(node instanceof Leaf)){
			MyTree += "My level is: " +   myHight(node) + "\n";
	        Iterator<Link> itr = ((Junction)node).getElements().iterator();
	        while(itr.hasNext()){
	        	MyTree += itr.next() + " ";
	        }
	        MyTree += "\n";
	        for(int i = 0;i < (((Junction)node).getPointers().size());i++){
	        	printAllTree(((Junction)node).getPointers().elementAt(i));
	        }
		}else{
			MyTree += "My level is: " + 0 + "\n" + node + "\n";
			return;
		}
	}
	
	//prints the tree
	private String printTree(){
		String ans= "";
		Object node= this.root;
		
		MyTree = new String();
		printAllTree(this.root);
		writeToFile("MyTree.dat",MyTree);

		while (!(node instanceof Leaf))
			node= (((Junction)node).getPointers()).elementAt(0);
		Leaf leafNode= (Leaf)node;
		while (leafNode != null){
			ans= ans + (leafNode.toString()).substring(0, (leafNode.toString()).length()) + "#";
			leafNode= leafNode.getNext();
		}
		return ans.substring(0, ans.length()-1);
	}//printTree()
	
	/**
	 * this method runs the tree's building
	 * @param args - array of input arguments
	 * @args[0] - input file name
	 * @args[1] - the T of this tree
	 * @args[2] - the key who's order we will find
	 * @args[3] - output file name	
	 */ 
	public static void main(String[] args) {
		String inputFileName= args[0];	//stores all the given arguments
		int T = Integer.decode(args[1]);
		int someNode = Integer.decode(args[2]);
		String outputFileName= args[3];
		String inputData= readFromFile(inputFileName);
		int numOfElements= findNumOfElements(inputData);
		BPlus bPTree= new BPlus(T, numOfElements);
		StringTokenizer tokens= new StringTokenizer(inputData, " ");
		while (tokens.hasMoreTokens()){	//runs the inserting of the tree
			String token= tokens.nextToken();
			double myToken = -1;
			try {	//since the input files end in "\n", we must work around that
				myToken = Double.parseDouble(token);
			}
			catch (Exception e){}
			if(myToken != -1){
				bPTree.insert(myToken);
			}
		}
		String outputData= bPTree.printTree();	//creates the output data
		outputData= outputData +  "\n" + "Node number: " + someNode + " Have " + bPTree.order(someNode) + " children's";
		writeToFile(outputFileName, outputData);
	}//main
	
	
	//reads from a file and converts it to string
	private static String readFromFile(String fileName){
		String tContent = "";
		//Must wrap working with files with try/catch
		try{
			//Creating a file object
			File tFile = new File(fileName);
			//Init inputstream
			FileInputStream fstream = new FileInputStream(tFile);
			DataInputStream in = new DataInputStream(fstream);
			//Creating a buffered reader.
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null){	
				tContent = tContent + strLine + "\n";//concatenating the line to content string
			}
			//Close the input stream
			in.close();
		}
		catch(Exception e)//Catch exception if any
		{
			System.err.println("Error: " + e.getMessage());
		}
		return tContent;
	}//readFromFile(String)

		
	//reads from a string and converts it to a file
	private static void writeToFile(String filename ,String data){
		//Must wrap working with files with try/catch
		try {
			//Creating a file object
			File tFile = new File(filename);
			// if file doesn't exists, then create it
			if (!tFile.exists()) {
				tFile.createNewFile();
			}
			//Init fileWriter
			FileWriter fw = new FileWriter(tFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			//Writing the data to the file
			bw.write(data);
			//Close the output stream
			bw.close();
 
		} catch (IOException e) {//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
		return;
	}//writeToFile(String, String)

	
	//finds how many keys are in the input file
	private static int findNumOfElements(String inputData){
		int sum= 0;
		StringTokenizer tokens= new StringTokenizer(inputData, " ");
		while (tokens.hasMoreTokens()){
			tokens.nextToken();
			sum++;
		}			
		return sum;
	}//findNumOfElements(String)
	

}//BPlus

	