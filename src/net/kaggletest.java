package net;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class kaggletest {
	public static void main(String[] args) throws IOException {
		FileWriter write = new FileWriter("digit_data/answer.csv");
		write.append("ImageID,Label\n");
		net test = new net("digit_exported_nets/epoch-five.txt");
		Scanner scanner = new Scanner(new File("digit_data/test.csv"));
		scanner.nextLine();
		for (int a=0;a<28000;a++){
			String line[] = scanner.nextLine().split(",");
			double doublst[] = new double[784];
			for (int i=0;i<784;i++){
				doublst[i] = Double.parseDouble(line[i]);
			}
			test.feedforward(doublst);
			double result[] = test.getoutput();
			int maxind=0;
			for (int i=0;i<10;i++){
				if (result[i]>result[maxind]){
					maxind = i;
				}
			}
			System.out.println(a);
			write.append((a+1)+","+maxind+"\n");
		}
		write.close();
		scanner.close();
	}
}
