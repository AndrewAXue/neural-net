package net;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JComponent;
import javax.swing.JFrame;

//Simple class to help display test digits.

public class drawnum {
	JFrame window = new JFrame();
	static Scanner scanner;
	//28x28 pixels each represented by a single value denoting the darkness in terms of rgb
	static int doublst[] = new int[784];
    
	public static void main(String[] args) throws FileNotFoundException {
		// Initializes a scanner to take in data from the csv file
		scanner = new Scanner(new File("/Users/andrew/Downloads/train (1).csv"));
        scanner.useDelimiter(",");
        scanner.nextLine();
        String[] lst = scanner.nextLine().split(",");
        // Transforms the values on the csv from strings to ints
		for (int a=0;a<784;a++){
    		doublst[a] = Integer.parseInt(lst[a+1]);
    	}
		new drawnum().go();
	}
	
	void go(){
		// Opens a default window and adds graphics
		window.setSize(28, 80);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.add(new PewGrid());
		// Adds a listener for mouse related events
		window.addMouseListener(new mouseevent());
		window.repaint();
		//move();
	}
	
	private class PewGrid extends JComponent {
		public void paintComponent(Graphics g){
			Graphics2D grap = (Graphics2D) g;
			grap.setColor(Color.WHITE);
			grap.fillRect(0, 0, 1000,1000);//setting up black background
			grap.setFont(new Font("Arial Black", Font.BOLD, 30));
			// Fills in the image of the digit pixel by pixel. The darkness of the pixel is denoted by doublst[i]
			for (int i=0;i<784;i++){
				grap.setColor(new Color(doublst[i],doublst[i],doublst[i]));
				grap.fillRect(i%28, i/28, 1, 1);
			}
		}
	}
	
	private class mouseevent implements MouseListener{
		public void mouseClicked(MouseEvent e) {
			// Scans in the next line in the csv and repeats the process above
			if (scanner.hasNextLine()){
				String[] lst = scanner.nextLine().split(",");
				for (int a=0;a<784;a++){
	        		doublst[a] = Integer.parseInt(lst[a+1]);
	        	}
				window.repaint();
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}
}
