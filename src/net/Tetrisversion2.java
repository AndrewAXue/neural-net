package net;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Tetrisversion2 {
	JFrame window ;
	public static void main(String[] args) {
		for (int x=0; x<=9;x++){
			gamestate.add(new ArrayList<Integer>());
		}
		System.out.println(gamestate);
		new Tetrisversion2().go();
	}
	static ArrayList<ArrayList<Integer>> gamestate = new ArrayList<ArrayList<Integer>>();
	
	public void go() { // making the initial window and implementing a MouseListener
		window = new JFrame("Tetris");
		window.setSize(507,735);
		//game space is (100-400)x(50,650)
		//30x30 sized squares
		window.setTitle("Tetris");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.add(new tetgrid());
		window.addKeyListener(new keyevents());
		try{Thread.sleep(100000);}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	class tetgrid extends JComponent {
		public void paintComponent(Graphics g){	
			Graphics2D grap = (Graphics2D) g;
			grap.setColor(Color.BLACK);
			grap.fillRect(0, 0, 1000,1000);//setting up black background
			grap.setColor(Color.WHITE);
			for (int x=100; x<=400; x+=30){//setting up vertical grid lines
				grap.drawLine(x, 50, x, 650);}
			for (int y=50; y<=650; y+=30){//setting up horizontal grid lines
				grap.drawLine(100, y, 400, y);}
		}}
	
	private class keyevents implements KeyListener{
		public void keyPressed(KeyEvent event) {}
		public void keyReleased(KeyEvent event) {}
		public void keyTyped(KeyEvent event) {}
	}
}
