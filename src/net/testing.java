package net;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

public class testing {
	static net test;
	static Random xpick = new Random();
	static boolean autoa = false;
	static boolean visuala = true;
	
	public static void main(String[] args) throws FileNotFoundException {
		

		/*
		for (double i=0.5;i<=5.0;i+=0.5){
			System.out.println("Learning rate: "+i);
			int temp[] = {784,30,10};
			test = new net(temp);
			test.scanner = new Scanner(new File("train.csv"));
			test.learning_rate = i;
			test.batch_size = 100;
			test.quadratic = false;
			test.softmax = true;
			test.scanner.nextLine();
			test.auto = autoa;
			if (visuala){
				test.create_window();
			}
			
			if (test.auto){
				for (int a=0;a<420;a++){
					if (a==410){
						test.print = true;
					}
					test.learn_batch(test.batch_size);
				}
				System.out.println();
				test.scanner.close();
			}
		}
		*/
		
		int temp[] = {784,30,10};
		test = new net(temp);
		
		
		test.batch_size = 100;
		test.learning_rate = 3;
		test.quadratic = true;
		test.softmax = false;

		test.scanner = new Scanner(new File("train.csv"));
		test.scanner.nextLine();
		test.auto = autoa;
		test.print = true;
		if (visuala){
			test.create_window();
		}
		test.graphing = true;
		if (test.auto){

				for (int a=0;a<420;a++){
					for (int k=0;k<10;k++)
					test.learn_batch(test.batch_size);
				} 
				test.scanner.close();
			
		}
		
		/*
		int temp[] = {784,70,10};
		test = new net(temp);
		test.scanner = new Scanner(new File("train.csv"));
		test.scanner.nextLine();

		test.batch_size = 100;
		test.learning_rate = 3;
		test.quadratic = false;
		test.softmax = true;
		
		test.auto = autoa;
		test.print = true;
		if (visuala){
			test.create_window();
		}
		test.graphing = true;
		if (test.auto){
			for (int a=0;a<420;a++){
				System.out.println(a+" "+test.learn_batch(test.batch_size));
			} 
			System.out.println();
			test.scanner.close();
		}
		*/
	}
}