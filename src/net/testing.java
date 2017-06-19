package net;

public class testing {
	static net test;
	double dataset[][] = new double[60000][782];
	double expected[][] = new double [60000][10];
	
	public static void main(String[] args) {
		int arr[][] = new int[2][];
		arr[0] = new int[1];
		arr[1] = new int[2];
		for (int i=0;i<arr.length;i++){
			for (int k=0;k<arr[i].length;k++){
				System.out.println(arr[i][k]);
			}
			System.out.println("HI");
		}
		System.out.println("WE GOOD");
		int temp[] = {2,3};
		test = new net(temp);
		test.netprint();
	}
	
	private double cost(int ind, int numinp){
		double answer = 0;
		for (int i=0;i<numinp;i++){
			test.feedforward(dataset[i]);
			double output[] = test.getoutput();
			for (int k=0;k<10;k++){
				output[k]-=expected[i][k];
			}
			answer+=length(output);
		}
		answer*=(1/(2*numinp));
		return answer;
	}
	
	private double length(double lst[]){
		double answer=0;
		for (int i=0;i<lst.length;i++){
			answer+=Math.pow(lst[i],2);
		}
		return answer;
	}
}
