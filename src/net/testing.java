package net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class testing {
	static net test;
	static Random xpick = new Random();
	static boolean autoa = false;
	static boolean visuala = true;
	
	public static void main(String[] args) throws IOException {
		long startTime = System.currentTimeMillis();

		/*
		for (double i=0.5;i<=5.0;i+=0.5){
			System.out.println("Learning rate: "+i);
			int temp[] = {784,30,10};
			test = new net(temp);
			test.load_training_data();
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
		/*
		int temp[] = {784,30,10};
		test = new net(temp);
		test.load_training_data();
		

		test.batch_size = 100;
		test.learning_rate = 3;
		test.quadratic = false;
		test.softmax = true;

		test.scanner = new Scanner(new File("train.csv"));
		test.scanner.nextLine();
		test.auto = autoa;
		test.print = true;
		if (visuala){
			test.create_window();
		}
		if (test.auto){

		//	for (int i=0;i<30;i++){
				for (int a=0;a<420;a++){
					for (int k=0;k<10;k++)
					test.learn_batch(test.batch_size);
				} 
				test.scanner.close();

			//}
		}
		*/
		int temp[] = {784,70,10};
		test = new net(temp);
		
			test.load_training_data();
	
		
		test.train_batch_size = 10;
		test.test_batch_size = 10000;
		test.learning_rate = 3;
		test.quadratic = false;
		test.softmax = true;
		test.num_epoch = 30;
		
		test.initialize_values();
		test.auto = autoa;
		
		if (visuala){
			test.create_window();
		}
		
		if (test.auto){
			test.graph_draw.start();
		}
		
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println(totalTime);
	}
}