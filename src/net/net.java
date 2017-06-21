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
	nodeclass allnode[][];
	int alllayersize[];
	weightclass  allweight[][][]; 
	double expected[];
	double error[][];
	boolean backprop = false;
	int numlayer;
	double learning_rate = 0.15;
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
	// represents all the weights from indice to indice+1 layers, the second indice represents
	// all the weights effecting the second indice node of the next layer, and the third indice representing the 
	// node which input value is taken from the first layers[indice] node.
	private class weightclass{
		double weight=weightchoose.nextDouble()*2-1;
		//double weight = weightchoose.nextInt(10)-5;
		double weightdev=0;
	}
		
	// Node in the neural net. The value and bias are stored as doubles.
		// drawweight determines if the weight of the output to the various nodes are shown, and the xpos and ypos
		// stores where the node will be drawn on the window. The color of the node is a randomized "bright" color.
	protected class nodeclass{
		double bias = 0;
		double zvalue = 0;
		double avalue = 0;
		double biasdev = 0;
		
		boolean drawweight=true;
		
		int xpos,ypos;
		Color color = new Color((float)(colorpick.nextFloat()/ 2f + 0.5),(float)(colorpick.nextFloat()/ 2f + 0.5),(float)(colorpick.nextFloat()/ 2f + 0.5));

		nodeclass(){
			bias=biaschoose.nextDouble()*2-1;
		}
		
		void printnode(){
			System.out.println("Printing Node");
			System.out.println("value: "+zvalue+" Post Sig: "+avalue+" Bias: "+bias);
			System.out.println();
		}
		
		void setvalue(double newvalue){
			zvalue = newvalue;
		}
		
		void setbias(double newbias){
			bias = newbias;
		}
		
		double getvalue(){
			return avalue;
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
		for (int i=0;i<arr.length;i++){
			if (arr[i]==0){
				throw new RuntimeException("No layers should have 0 nodes.");
			}
		}
		allweight = new weightclass[alllayersize.length-1][][];
		for (int i=1;i<alllayersize.length;i++){
			allweight[i-1] = new weightclass[alllayersize[i]][alllayersize[i-1]];
			for (int k=0;k<alllayersize[i];k++){
				for (int l=0;l<alllayersize[i-1];l++){
					allweight[i-1][k][l] = new weightclass();
				}
			}
		}
		
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
		allnode = new nodeclass[numlayer][];
		for (int i=0;i<numlayer;i++){
			allnode[i] = new nodeclass[alllayersize[i]];
			for (int k=0;k<alllayersize[i];k++){
				allnode[i][k] = new nodeclass();
				if (i==0){
					allnode[i][k].bias=0;
				}
				allnode[i][k].ypos = distfromtop+k*(visualdim/alllayersize[i]);
				allnode[i][k].xpos = distfromside+i*(visualdim/alllayersize.length);
			}
		}
		
		for (int i=0;i<allweight.length;i++){
			System.out.println("Printing layer "+i);
			for (int k=0;k<allweight[i].length;k++){
				for (int j=0;j<allweight[i][k].length;j++){
					System.out.print(allweight[i][k][j].weight+" ");
				}
				System.out.println();
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
					nodeclass active = allnode[i][k];
					for (int a=0;a<allweight[i].length;a++){
						grap.setColor(active.color);
						int x1 = active.xpos+sizenode/2;
						int y1 = active.ypos+sizenode/2;
						int x2 = allnode[i+1][a].xpos+sizenode/2;
						int y2 = allnode[i+1][a].ypos+sizenode/2;
						grap.drawLine(x1,y1,x2,y2);
						if (allnode[i][k].drawweight){								
							double slope = (double)(y1-y2)/(double)(x1-x2);
							double yinter = (y1-slope*x1);
							double newx = x1+(x2-x1)/2-55;
							grap.drawString("Weight: "+Math.round(1000000.0*allweight[i][a][k].weight)/1000000.0, (int)newx, (int)((newx)*slope+yinter));
							if (backprop)grap.drawString("Weightdev: "+Math.round(1000000.0*allweight[i][a][k].weightdev)/1000000.0, (int)newx, (int)((newx)*slope+yinter+20));
						}	
					}
				}
			}
			
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
					//Drawing lines between nodes that show connections. Also writes down the weight of the connection
					nodeclass active = allnode[i][k];
					
					grap.setColor(Color.WHITE);
					//Drawing Stats: Bias + Value and the actual node
					grap.drawString("Bias: "+Math.round(1000000.0*allnode[i][k].getbias())/1000000.0, allnode[i][k].xpos, allnode[i][k].ypos);
					grap.setColor(active.color);
					grap.fillOval(active.xpos, allnode[i][k].ypos, sizenode, sizenode);
					grap.setColor(Color.WHITE);
					grap.drawString("Value: "+Math.round(1000000.0*allnode[i][k].getvalue())/1000000.0+"("+Math.round(1000000.0*allnode[i][k].avalue)/1000000.0+")", allnode[i][k].xpos, 20+allnode[i][k].ypos+sizenode);
					if (backprop)grap.drawString("Biasdev: "+Math.round(1000000.0*allnode[i][k].biasdev)/1000000.0, allnode[i][k].xpos, 40+allnode[i][k].ypos+sizenode);	
					if (backprop)grap.drawString("Error: "+Math.round(1000000.0*error[i][k])/1000000.0, allnode[i][k].xpos, 60+allnode[i][k].ypos+sizenode);	
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
		System.out.println(alllayersize.length+" Layers");
		for (int i=0;i<alllayersize.length;i++){
			System.out.print(alllayersize[i]+" ");
		}
		System.out.println();
		for (int i=0;i<alllayersize.length;i++){
			System.out.println("Printing layer "+i);
			for (int k=0;k<alllayersize[i];k++){
				allnode[i][k].printnode();
			}
		}
	}
	
	// Outputs whether the coordinates of two objects collide with each other.
	boolean collision(int x1, int y1, int x2, int y2,int siz1, int siz2){
		return (x1+siz1>=x2&&x1<=x2+siz2&&y1+siz1>=y2&&y1<=y2+siz2);
	}
	
	static double func(double x){
		return 3*x+5;
	}
	
	public void feed(){
		double rand = weightchoose.nextInt(10);
		double lst[] = {rand};
		double exp[] = {0};
		feedforward(lst,exp);
	}
	
	private void cleardev(){
		for (int i=0;i<numlayer;i++){
			for (int k=0;k<alllayersize[i];k++){
				error[i][k] = 0;
				allnode[i][k].biasdev = 0;
				if (i!=numlayer-1){
					for (int a=0;a<alllayersize[i+1];a++){
						allweight[i][a][k].weightdev = 0; 
					}
				}
			}
		}
	}
	
	private class mouseevent implements MouseListener{
		// Toggles whether the weights of a certain node should be shown. Done by clicking the node.
		public void mouseClicked(MouseEvent e) {
			System.out.println(e.getPoint());
			if (e.isControlDown()){
				cleardev();
				window.repaint();
			}
			if (e.getX()<50){
				/*double lst[] = new double[alllayersize[0]];
				for (int i=0;i<lst.length;i++){
					lst[i] = 0;
				}
				double exp[] = new double[alllayersize[numlayer-1]];
				for (int i=0;i<exp.length;i++){
					exp[i] = 5;
				}
				System.out.println("called");
				*/
				feed();
				window.repaint();
			}
			if (e.getY()<50){
				backpropagate();
				window.repaint();
			}
			if (e.getX()>850){
				gradient_descent(1);
				window.repaint();
			}
			if (e.getY()>850){
				feed();
				backpropagate();
				gradient_descent(1);
				window.repaint();
			}
			boolean done = false;
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = allnode[i][k];
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
	protected void feedforward(double lst[],double tempexpected[]){
		expected = tempexpected;
		if (lst.length!=alllayersize[0]){
			System.out.println("ERROR: Input layer size different then number of inputs");
		}
		else{
			for (int i=0;i<alllayersize[0];i++){
				allnode[0][i].avalue = allnode[0][i].zvalue = lst[i];
			}
			for (int i=0;i<allweight.length;i++){
				for (int k=0;k<allweight[i].length;k++){
					double newz = 0;
					for (int a=0;a<allweight[i][0].length;a++){
						newz+=allnode[i][a].avalue*allweight[i][k][a].weight;
					}
					allnode[i+1][k].zvalue = newz+allnode[i+1][k].bias;
					allnode[i+1][k].avalue = sigmoid(allnode[i+1][k].zvalue);
				}
			}
		}
	}
	
	//Returns the output layer of the net. Should be called after a feedforward is called.
	protected double[] getoutput(){
		double output[] = new double[alllayersize[alllayersize.length-1]];
		for (int i=0;i<alllayersize[alllayersize.length-1];i++){
			output[i] = allnode[alllayersize.length-1][i].zvalue;
		}
		return output;
	}
	
	//Backpropagates errors through the neural net. At the date this was written, the assumed cost function
	//is the quadratic cost function, and all derivatives will reflect this. This first calculates the 
	//partial derivative of all the weights and biases, and returns the gradient. This gradient should then
	//be averaged over the number of inputs.
	protected void backpropagate(){
		backprop = true;
		error = new double[numlayer][];
		for (int i=0;i<numlayer;i++){
			error[i] = new double[alllayersize[i]];
		}
		//BP1
		for (int i=0;i<alllayersize[numlayer-1];i++){
			error[numlayer-1][i] = (allnode[numlayer-1][i].avalue-expected[i])*sigmoidprime(allnode[numlayer-1][i].zvalue);
			allnode[numlayer-1][i].biasdev += error[numlayer-1][i];
			//System.out.println(sigmoidprime(allnode[numlayer-1][i].zvalue));
		}
		for (int i=numlayer-2;i>=0;i--){
			for (int k=0;k<alllayersize[i];k++){
				double newerror=0;
				for (int a=0;a<alllayersize[i+1];a++){
					newerror += allweight[i][a][k].weight*error[i+1][a]*sigmoidprime(allnode[i][k].zvalue);
				}
				//System.out.println(newerror);	
				error[i][k] = newerror;
				allnode[i][k].biasdev += error[i][k];
			}
		}
		for (int i=0;i<numlayer-1;i++){
			for (int k=0;k<alllayersize[i];k++){
				for (int a=0;a<alllayersize[i+1];a++){
					allweight[i][a][k].weightdev += allnode[i][k].avalue*error[i+1][a];
				}
			}
		}
		window.repaint();
	}
	
	// Adjusts all the weights and biases using (stochastic) gradient descent. Must be called after back-
	// propagation is called. 
	protected void gradient_descent(int batch_size){
		//Updating biases
		for (int i=1;i<numlayer;i++){
			for (int k=0;k<alllayersize[i];k++){
				allnode[i][k].bias-=learning_rate/(double)batch_size*allnode[i][k].biasdev;
				
			}
		}
		for (int i=0;i<numlayer-1;i++){
			for (int k=0;k<allweight[i].length;k++){
				for (int a=0;a<allweight[i][0].length;a++){
					allweight[i][k][a].weight-=learning_rate/(double)batch_size*allweight[i][k][a].weightdev;
				}
			}
		}
		window.repaint();
	}
}