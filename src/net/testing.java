package net;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class testing {
	static net test;
	static Random xpick = new Random();
	static boolean autoa = true;
	static boolean visuala = false;
	
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
		/*
		for (double i=0.5;i<=5.0;i+=0.5){
			System.out.println("Learning rate: "+i);
			int temp[] = {784,70,10};
			test = new net(temp);
			test.learning_rate = i;
			test.scanner = new Scanner(new File("train.csv"));
			test.batch_size = 100;
			test.scanner.nextLine();
			test.auto = autoa;
			if (visuala){
				test.create_window();
			}
			
			if (test.auto){
				for (int a=0;a<420;a++){
					if (a==414){
						test.print = true;
					}
					test.learn_batch(test.batch_size);
				}
				System.out.println();
				test.scanner.close();
			}
		}
		*/
		
		
		int temp[] = {784,70,10};
		test = new net(temp);
		test.scanner = new Scanner(new File("train.csv"));
		test.batch_size = 100;
		test.scanner.nextLine();
		test.auto = autoa;
		test.print = true;
		if (visuala){
			test.create_window();
		}
		
		if (test.auto){
			for (int a=0;a<420;a++){
				if (a==350){
					test.learning_rate = 0.5;
				}
				System.out.print(a+" ");
				test.learn_batch(test.batch_size);
			}
			System.out.println();
			test.scanner.close();
		}
		
		test.export_net("TESTSEVEN.txt");
		 
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