package net;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JComponent;
import javax.swing.JFrame;


public class drawnum {
	JFrame window = new JFrame();
	static Scanner scanner;
	static int doublst[] = new int[784];
    
	public static void main(String[] args) throws FileNotFoundException {
		// Starts the program.
		scanner = new Scanner(new File("/Users/Andrew/Downloads/train (1).csv"));
        scanner.useDelimiter(",");
        scanner.nextLine();
        String[] lst = scanner.nextLine().split(",");
		for (int a=0;a<784;a++){
    		doublst[a] = Integer.parseInt(lst[a+1]);
    		System.out.println(doublst[a]);
    	}
		new drawnum().go();
	}
	
	private void go(){
		window.setSize(100, 100);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.add(new PewGrid());
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
					grap.drawString("hi", 100, 100);
					for (int i=0;i<784;i++){
						grap.setColor(new Color(doublst[i],doublst[i],doublst[i]));
						grap.fillRect(i%28, i/28, 1, 1);
					}
					
		}
	}
	
	private class mouseevent implements MouseListener{
		public void mouseClicked(MouseEvent e) {
			if (scanner.hasNextLine()){
				String temp = scanner.nextLine();
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
