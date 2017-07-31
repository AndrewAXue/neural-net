package net;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

//Simple class to help display test digits.

public class digitrecognize {
	JFrame window = new JFrame();
	boolean mousepressed = false;
	int image[] = new int[784];
	static Scanner scanner;
	Point location;
	JButton button = new JButton();
	net please;
	//28x28 pixels each represented by a single value denoting the darkness in terms of rgb
    
	public static void main(String[] args){
		new digitrecognize().go();
	}
	
	void go(){
		try {
			please = new net("digit_exported_nets/L2100.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// Opens a default window and adds graphics
		window.setSize(400, 200);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		JPanel everything = new JPanel();
		JPanel graphics = new JPanel();
		graphics.add(new PewGrid());
		graphics.setPreferredSize(new Dimension(28, 28));
		graphics.addMouseListener(new mouseevent());
		graphics.addMouseMotionListener(new mouseevent());
		everything.add(graphics);
		everything.add(button);
		// Adds a listener for mouse related events
		window.add(everything);
		window.repaint();
		location = graphics.getLocationOnScreen();
		//move();
	}
	
	private class PewGrid extends JComponent {
		PewGrid(){
			setPreferredSize(new Dimension(28, 28));
		}
		public void paintComponent(Graphics g){
			Graphics2D grap = (Graphics2D) g;
			grap.setColor(Color.WHITE);
			grap.setBackground(Color.BLACK);
			grap.setFont(new Font("Arial Black", Font.BOLD, 30));
			// Fills in the image of the digit pixel by pixel. The darkness of the pixel is denoted by doublst[i]
			for (int i=0;i<784;i++){
				grap.setColor(new Color((int)(image[i]),(int)(image[i]),(int)(image[i])));
				grap.fillRect(i%28, i/28, 1, 1);
			}
		}
	}
	
	private class mouseevent implements MouseListener,MouseMotionListener{
		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {
			System.out.println(e.getPoint());
			//image[e.getX()][e.getY()-5] = 255;
			mousepressed = true;
			window.repaint();
		}
		public void mouseReleased(MouseEvent e) {
			double doublst[] = new double[784];
			for (int i=0;i<784;i++){
				doublst[i] = image[i]/255.0;
			}
			please.feedforward(doublst);
			double result[] = please.getoutput();
			int ans = 0;
			for (int i=0;i<10;i++){
				System.out.println(result[i]);
				if (result[i]>result[ans]){
					ans = i;
				}
			}
			System.out.println(ans);
			try {
				FileWriter write = new FileWriter("mynums.csv");
			
				write.append("fill\n");
				write.append("fill,"+image[0]);
				for (int i=1;i<784;i++){
					write.append(","+image[i]);
				}
				write.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
		public void mouseDragged(MouseEvent e) {
			int coord = (e.getY()-5)*28+e.getX();
			image[coord] = 225;
			if (coord>=28){
				image[coord-28] = 150;
			}
			if (coord<756){
				image[coord+28] = 150;
			}
			if (coord%28!=0){
				image[coord-1] = 150;
			}
			if (coord%28!=27){
				image[coord+1] = 220;
			}
			window.repaint();
		}
		public void mouseMoved(MouseEvent e) {
		}
	}
}
