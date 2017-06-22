package net;

import java.util.Random;

public class testing {
	static net test;
	double dataset[][] = new double[60000][782];
	double expected[][] = new double [60000][10];
	static Random xpick = new Random();
	static boolean autoa = true;
	
	public static void main(String[] args) {
		//y = 3x+5;
		int temp[] = {1,50,5,10,1};
		test = new net(temp);
		test.auto = autoa;
		if (test.auto){
		for (int i=0;i<1000;i++){
			for (int k=0;k<10;k++){
					test.feed();
					test.backpropagate();
				}
				test.gradient_descent(1);
			}
		}
		
		
		
		/*
		for (int i=0;i<10000000;i++){
			int x = xpick.nextInt(10);
			double lst[] = {x};
			double exp[] = {func(x)/10000.0};
			test.feedforward(lst, exp);
			test.backpropagate();
			test.gradient_descent();
			System.out.println(test.error[2][0]);
		}
		*/
		
		//test.netprint();
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