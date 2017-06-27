package net;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class testing {
	static net test;
	static Random xpick = new Random();
	static boolean autoa = true;
	static boolean visuala = true;
	
	public static void main(String[] args) throws FileNotFoundException {
		/*
		net test = new net("TESTTWO.txt");
		test.scanner = new Scanner(new File("test.csv"));
        test.scanner.useDelimiter(",");
        System.out.println(test.scanner.nextLine());
		test.create_window();
		*/
		/*
		net test = new net("TESTTHREE.txt");
		test.create_window();
	
		test.scanner = new Scanner(new File("mynums.csv"));
		test.scanner.nextLine();
		*/
		
		
		int temp[] = {784,70,10};
		test = new net(temp);
		
		test.scanner = new Scanner(new File("train.csv"));
		test.scanner.nextLine();
		test.auto = autoa;
		if (visuala){
			test.create_window();
		}
		
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
					test.feedforward(doublst);
					test.set_expected(ans);
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
				System.out.println(i+" correct "+please+" out of "+numiter);
				test.gradient_descent(numiter);
			}
		}
		//test.scanner.close();
		test.export_net("TESTFOUR.txt");
		 
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
}