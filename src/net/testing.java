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
			test.scanner = new Scanner(new File("digit_data/train.csv"));
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
		test.scanner = new Scanner(new File("digit_data/train.csv"));
		test.scanner.nextLine();
		
<<<<<<< HEAD
		
=======
>>>>>>> 4cadd74ee2a0f095df9addf83b933ac6bccb48e5
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
<<<<<<< HEAD

=======
		//	for (int i=0;i<30;i++){
>>>>>>> 4cadd74ee2a0f095df9addf83b933ac6bccb48e5
				for (int a=0;a<420;a++){
					for (int k=0;k<10;k++)
					test.learn_batch(test.batch_size);
				} 
				test.scanner.close();
<<<<<<< HEAD
			
=======
			//}
>>>>>>> 4cadd74ee2a0f095df9addf83b933ac6bccb48e5
		}
		*/
		test = new net("digit_exported_nets/traied_net.txt");
		test.create_window();
		test.scanner = new Scanner(new File("digit_data/train.csv"));
		/*
		int temp[] = {784,70,10};
		test = new net(temp);
		test.scanner = new Scanner(new File("digit_data/train.csv"));
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