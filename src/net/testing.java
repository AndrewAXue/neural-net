package net;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class testing {
	static net test;
	double dataset[][] = new double[60000][782];
	double expected[][] = new double [60000][10];
	static Random xpick = new Random();
	static boolean autoa = true;
	
	public static void main(String[] args) throws FileNotFoundException {
		
		
       
		int temp[] = {784,80,10};
		test = new net(temp);
		test.auto = autoa;
		test.visual = true;
		test.scanner = new Scanner(new File("/Users/andrew.xue/Downloads/train.csv"));
		test.scanner.useDelimiter(",");
		test.scanner.nextLine();
		
		int numiter = 100;
		if (test.auto){
			for (int i=0;i<419;i++){
				int please=0;
				for (int k=0;k<numiter;k++){
					String[] lst = test.scanner.nextLine().split(",");
					int correct = Integer.parseInt(lst[0]);
					//System.out.println(correct);
					double ans[] = {0,0,0,0,0,0,0,0,0,0};
					ans[correct] = 1;
			    	double doublst[] = new double[784];
			    	for (int a=0;a<784;a++){
			    		doublst[a] = Double.parseDouble(lst[a+1])/255;
			    	}
					test.feedforward(doublst,ans);
					test.backpropagate();
					double out[] = test.getoutput();
					int maxind = 0;
					for (int z=0;z<10;z++){
						if (out[maxind]<out[z]){
							maxind=z;
						}
					}
					if (maxind==correct){
						please++;
					}
				}
				System.out.println("PLEASE "+i+" "+please+" out of "+numiter);
				test.gradient_descent(numiter);
			}
		}
		
		 
		/*
		int temp[] = {784,30,10};
		test = new net(temp);
		test.auto = autoa;
		if (test.auto){
			for (int i=0;i<10000;i++){
				for (int k=0;k<50;k++){
					test.feed();
					test.backpropagate();
				}
				test.gradient_descent(50);
			}
		}
		*/
		
		
		
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