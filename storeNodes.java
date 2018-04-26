import java.util.*;

public class storeNodes {
	private List<Integer> arr;
	public storeNodes(int n){
		arr = new ArrayList<Integer>();
	}
	public void insert(int t){
		arr.add(t);
	}
	public boolean find(int x){
		return arr.contains(x);
	}
}
