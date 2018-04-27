


public class Main {
	public static void main(String[] args) {
		
		BPlus.mainBPT(args);
		if(BPlus.bPTree.search(5.1) != null){
			System.out.println(5.1 + " here");
		}else{
			System.out.println(5.1 + "not here");
		}
		if(BPlus.bPTree.search(2.7) != null){
			System.out.println(2.7 + " here");
		}else{
			System.out.println(2.7 + " not here");
		}
	}
}
