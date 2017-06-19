package net;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;


public class net{
	JFrame window = new JFrame();
	layerclass layer[];
	int alllayersize[];
	weightclass  allweight[][][];
	int numlayer;
	Random weightchoose = new Random();
	Random biaschoose = new Random();
	
	Random colorpick = new Random();
	
	int visualdim = 900;
	
	//BELOW VARIABLES ARE FOR USE IN THE VISUALIZTION
	int distfromtop = 50;
	int distfromside = 50;
	int sizenode = 50;
	
	// sigmoid function for "smoothing out" the output values
	double sigmoid(double x){
		return (1/(1+Math.exp(-x)));
	}
	
	// derivative of the sigmoid function used in backpropagation
	double sigmoidprime(double x){
		return (Math.exp(-x))/(Math.pow(1+Math.exp(-x), 2));
	}
		
	// A weight in the neural net. This class will be stored in a 3d array of weights, where the first indice
	// represents all the weights from indice to indice+1 layers, the second indice represents all the weights
	// all the weights effecting the second indice node of the next layer, and the third indice representing the 
	// node which input value is taken from the first layers[indice] node.
	private class weightclass{
		double weight=23;
		double partialdev=0;
	}
		
	// Node in the neural net. The value and bias are stored as doubles.
		// drawweight determines if the weight of the output to the various nodes are shown, and the xpos and ypos
		// stores where the node will be drawn on the window. The color of the node is a randomized "bright" color.
	private class nodeclass{
		double bias;
		double zvalue = 0;
		double avalue = 0;
		double derive = 0;
		
		boolean drawweight = true;
		
		int xpos,ypos;
		
		Color color = new Color((float)(colorpick.nextFloat()/ 2f + 0.5),(float)(colorpick.nextFloat()/ 2f + 0.5),(float)(colorpick.nextFloat()/ 2f + 0.5));

		nodeclass(){
			bias=0;
			//bias = biaschoose.nextDouble()*10-5;
		}
		
		void printnode(){
			System.out.println("Printing Node");
			System.out.println("value: "+zvalue+" Post Sig: "+avalue+" Bias: "+bias);
			System.out.print("Weights: ");
			System.out.println();
		}
		
		void setvalue(double newvalue){
			zvalue = newvalue;
		}
		
		void setbias(double newbias){
			bias = newbias;
		}
		
		double getvalue(){
			return zvalue;
		}
		
		double getbias(){
			return bias;
		}
		
		int getx(){
			return xpos;
		}
		
		int gety(){
			return ypos;
		}
	}
	
	private class layerclass{
		nodeclass node[];
		int size;
		int layerind;
		layerclass(int templayerind, int tempsize){
			//System.out.println("Making layer of size: "+tempsize);
			size = tempsize;
			layerind = templayerind;
			node = new nodeclass[size];
			for (int i=0;i<size;i++){
				node[i] = new nodeclass();
				node[i].ypos = distfromtop+i*(visualdim/size);
				node[i].xpos = distfromside+templayerind*(visualdim/alllayersize.length);
			}
		}
		
		void layerprint(){
			System.out.println("Printing layer");
			for (int i=0;i<size;i++){
				node[i].printnode();
			}
		}
	}
	
	/*
	cons.fill = GridBagConstraints.HORIZONTAL;
	cons.weightx = 0;
	cons.weighty = 0;
	cons.ipadx = 0;
	cons.ipady = 0;
	cons.gridx = 0;
	cons.gridy = 0;
	cons.anchor = GridBagConstraints.FIRST_LINE_START;
	cons.insets = new Insets(0,0,0,0);
	
	FIRST_LINE_START	PAGE_START		FIRST_LINE_END
	LINE_START			CENTER			LINE_END
	LAST_LINE_START		PAGE_END		LAST_LINE_END
	 */
	
	net(int arr[]){
		alllayersize = arr;
		allweight = new weightclass[alllayersize.length-1][][];
		for (int i=1;i<alllayersize.length;i++){
			allweight[i-1] = new weightclass[alllayersize[i]][alllayersize[i-1]];
			for (int k=0;k<alllayersize[i];k++){
				for (int l=0;l<alllayersize[i-1];l++){
					allweight[i-1][k][l] = new weightclass();
				}
			}
		}
		allweight[0][0][1].weight = 5;
		
		window.setSize(1000, 1000);
		window.setTitle("VISUALIZATION");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		
		window.setIconImage(new ImageIcon("neural.jpg").getImage());
		
		JPanel everything = new JPanel(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		
		
		JPanel visuals = new JPanel();
		visuals.setBackground(Color.BLACK);
		visuals.add(new visualization());
		visuals.addMouseListener(new mouseevent());
		
		cons.fill = GridBagConstraints.NONE;
		cons.weightx = 0;
		cons.weighty = 0;
		cons.ipadx = visualdim;
		cons.ipady = visualdim;
		cons.gridx = 0;
		cons.gridy = 1;
		cons.anchor = GridBagConstraints.LINE_START;
		cons.insets = new Insets(0,0,0,0);		
		everything.add(visuals,cons);
		
		JPanel stats = new JPanel();
		stats.setBackground(Color.PINK);
		
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		cons.ipadx = 0;
		cons.ipady = 30;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.anchor = GridBagConstraints.PAGE_START;
		cons.insets = new Insets(0,0,0,0);
		
		everything.add(stats,cons);
		
		
		JPanel downfill = new JPanel();
		downfill.setBackground(Color.BLUE);
		
		
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		cons.ipadx = 0;
		cons.ipady = 30;
		cons.gridx = 0;
		cons.gridy = 2;
		cons.anchor = GridBagConstraints.PAGE_START;
		cons.insets = new Insets(0,0,0,0);
		everything.add(downfill,cons);
		
		
		JPanel rightfill = new JPanel();
		rightfill.setBackground(Color.ORANGE);
	
		
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		cons.ipadx = 1000-visualdim;
		cons.ipady = 0;
		cons.gridx = 1;
		cons.gridy = 1;
		cons.anchor = GridBagConstraints.PAGE_START;
		cons.insets = new Insets(0,0,0,0);
		everything.add(rightfill,cons);
		
		window.add(everything);
		window.setVisible(true);
		
		
		numlayer = arr.length;
		layer = new layerclass[numlayer];
		for (int i=0;i<alllayersize.length;i++){
			layer[i] = new layerclass(i,arr[i]);
			if (i==0){
				for (int k=0;k<arr[0];k++){
					layer[0].node[k].bias = 0;
				}
			}
		}
	}
	
	// Graphics aspect of the framework
	private class visualization extends JComponent {
		
		visualization() {
            setPreferredSize(new Dimension(visualdim, visualdim));
        }
		
		public void paintComponent(Graphics g){
			super.paintComponents(g);
			Graphics2D grap = (Graphics2D) g; 
			grap.setBackground(Color.BLACK);			
			grap.setColor(Color.WHITE);
			grap.setFont(new Font("Arial Black", Font.BOLD, 15));
			
			
			for (int i=0;i<allweight.length;i++){
				for (int k=0;k<allweight[i][0].length;k++){
					nodeclass active = layer[i].node[k];
					for (int a=0;a<allweight[i].length;a++){
						grap.setColor(active.color);
						int x1 = active.xpos+sizenode/2;
						int y1 = active.ypos+sizenode/2;
						int x2 = layer[i+1].node[a].xpos+sizenode/2;
						int y2 = layer[i+1].node[a].ypos+sizenode/2;
						grap.drawLine(x1,y1,x2,y2);
						if (layer[i].node[k].drawweight){								
							double slope = (double)(y1-y2)/(double)(x1-x2);
							double yinter = (y1-slope*x1);
							double newx = x1+(x2-x1)/2-55;
							grap.drawString("Weight: "+Math.round(100.0*allweight[i][a][k].weight)/100.0, (int)newx, (int)((newx)*slope+yinter));
						}	
					}
				}
			}
			
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
					//Drawing lines between nodes that show connections. Also writes down the weight of the connection
					nodeclass active = layer[i].node[k];
					
					grap.setColor(Color.WHITE);
					//Drawing Stats: Bias + Value and the actual node
					grap.drawString("Bias: "+Math.round(100.0*layer[i].node[k].getbias())/100.0, layer[i].node[k].xpos, layer[i].node[k].ypos);
					grap.setColor(active.color);
					grap.fillOval(active.xpos, layer[i].node[k].ypos, sizenode, sizenode);
					grap.setColor(Color.WHITE);
					grap.drawString("Value: "+Math.round(100.0*layer[i].node[k].getvalue())/100.0+"("+Math.round(100.0*layer[i].node[k].avalue)/100.0+")", layer[i].node[k].xpos, 20+layer[i].node[k].ypos+sizenode);	
				}
			}
			
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
									
				}
			}
		}
	}
	
	// Prints out the weights and biases of all the nodes of the net
	void netprint(){
		for (int i=0;i<numlayer;i++){
			layer[i].layerprint();
		}
	}
	
	// Outputs whether the coordinates of two objects collide with each other.
	boolean collision(int x1, int y1, int x2, int y2,int siz1, int siz2){
		return (x1+siz1>=x2&&x1<=x2+siz2&&y1+siz1>=y2&&y1<=y2+siz2);
	}
	
	private class mouseevent implements MouseListener{
		// Toggles whether the weights of a certain node should be shown. Done by clicking the node.
		public void mouseClicked(MouseEvent e) {
			System.out.println(e.getPoint());
			if (e.getX()<50){
				double lst[] = {1,1};
				feedforward(lst);
			}
			boolean done = false;
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = layer[i].node[k];
					if (collision(active.getx(),active.gety(),e.getX(),e.getY(),sizenode,0)){
						active.drawweight = !active.drawweight;
						window.repaint();
						break;
					}
				}
				if (done) break;
			}
		}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		
	}

	//Given a list of inputs of the same size as the input layer, feeds the values through the net
	protected void feedforward(double lst[]){
		if (lst.length!=alllayersize[0]){
			System.out.println("ERROR: Input layer size different then number of inputs");
		}
		else{
			for (int i=0;i<alllayersize[0];i++){
				layer[0].node[i].avalue = layer[0].node[i].zvalue = lst[i];
			}
			for (int i=0;i<allweight.length;i++){
				for (int k=0;k<allweight[i].length;k++){
					double newz = 0;
					for (int a=0;a<allweight[i][0].length;a++){
						newz+=layer[i].node[a].avalue*allweight[i][k][a].weight;
					}
					layer[i+1].node[k].zvalue = newz;
					layer[i+1].node[k].avalue = sigmoid(layer[i+1].node[k].zvalue);
				}
			}
			window.repaint();
		}
	}
	
	//Returns the output layer of the net. Should be called after a feedforward is called.
	protected double[] getoutput(){
		double output[] = new double[alllayersize[alllayersize.length-1]];
		for (int i=0;i<alllayersize[alllayersize.length-1];i++){
			output[i] = layer[alllayersize.length-1].node[i].zvalue;
		}
		return output;
	}
	
	//Using backpropagation, each of the weights and bias's are assigned error value. Altogether making a gradient. 
	protected void backpropagate(double cost){
		
	}
	
	
	
}