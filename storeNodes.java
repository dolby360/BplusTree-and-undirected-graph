import java.util.*;

public class storeNodes {
	private List<Double> arr;
	public storeNodes(double n){
		arr = new ArrayList<Double>();
	}
	public void insert(double t){
		arr.add(t);
	}
	public boolean find(double x){
		return arr.contains(x);
	}
}
