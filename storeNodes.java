import java.util.*;

public class storeNodes {
	private List<Integer> arr;
	public storeNodes(int n){
		arr = new ArrayList<Integer>();
	}
	public void insert(Tuple t){
		arr.add(t.x + t.y);
	}
	public boolean find(int x){
		return arr.contains(x);
	}
}
