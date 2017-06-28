package net;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Random;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

// Made by Andrew Xue
// a3xue@edu.uwaterloo.ca
// Neural net framework! Uses stochastic gradient descent + backpropagation as its learning algorithm. I made
// this because tensorflow felt like just throwing black box functions at a problem and hoping it worked. I
// figured if I made everything from scratch I would understand it better.

//TODO
//Softmax
//Matrix matrix multiplication for batches
//Improve UI more buttons
//Other improvements for escaping local minima
//Implement a learning rate slowdown as the number of batches tested goes
//Heatmap of different numbers
//Support for different cost functions
//Alternatives to sigmoid functions (tanx function)

//DONE!
//Outputting network weights + node properties to a seperate file for easy use in other programs
//New constructor that builds neural net off of file (presumably trained one)
//Handle layers with too many nodes
//Cross entropy cost function

//Known bugs:
//Slow learning rate. Matrix matrix multiplication should alleviate this
//Relatively low accuracy 85-90 after trained. Cross entropy should help, maybe playing with learning rates more
//Backprop and possibly gradient descent are exceptionally slow. Backprop should be as computationally expensive
//	as feedforward, which is significantly quicker.
//Digits inputted in digitrecognize, when drawn in drawnum are slightly off center.


public class net{
	boolean print = false;
	//Scanner for CSV file
	Scanner scanner;
	
	//HYPERPARAMETERS. These should be set when the net is initialized.
	// Learning rate of the net. Higher learning rates lead to quicker results but can "overshoot", lower learning rates
	// are slower but steadier
	double learning_rate = 3;
	// Choosing which cost function to use (quadratic or cross-entropy at time of writing)
	boolean quadratic = false;
	// Stores the size of each batch for training
	int batch_size;
	// Whether to softmax the results
	boolean softmax = true;
	
	
	//VISUALIZATION ASPECTS
	// Buttons used in the VISUALIZATION
	JButton train_batch_button,feed_button,train_all_batch_button,feed_test;
	// Sets limit for maximum number of nodes per layer displayed in visualization
	int maxnodes = 6;
	// Whether the partial derivatives should be drawn. Used for importing nets when it should be trained
	boolean drawdev = false;
	//Window for VISUALIZATION
	JFrame window = new JFrame();
	//Size of VISUALIZATION
	int visualdim = 900;
	// Distance from the top of the JPanel to the first node in  each layer
	int distfromtop = 50;
	// Distance from the side of the panel to the first and last layer
	int distfromside = 50;
	// Size of each node
	int sizenode = 50;
	
	
	//Characteristics of the net
	//Number of layers and size of each one
	int numlayer;
	int alllayersize[];
	
	//Randoms for choosing initial values for nodes
	Random weightchoose = new Random();
	Random biaschoose = new Random();
	Random colorpick = new Random();
	
	//Properties of each node
	nodeclass allnode[][];
	//Weights of all the weights
	weightclass allweight[][][];
	
	//Error of each node (backpropagation)
	double error[][];
	//Expected output vector
	double expected[];
	//Whether to use automatic testing or not
	boolean auto = false;
	
	// sigmoid function for "smoothing out" the output values
	double sigmoid(double x){
		return (1/(1+Math.exp(-x)));
	}
	
	// derivative of the sigmoid function used in backpropagation
	double sigmoidprime(double x){
		double sig = sigmoid(x);
		return sig*(1-sig);
	}
		
	// A weight in the neural net. This class will be stored in a 3d array of weights, where the first indice
	// represents all the weights from indice to indice+1 layers, the second indice represents
	// all the weights effecting the second indice node of the next layer, and the third indice representing the 
	// node which input value is taken from the first layers[indice] node.
	private class weightclass{
		double weight=weightchoose.nextDouble()*2-1;
		double weightdev=0;
	}
		
	// Node in the neural net. The value and bias are stored as doubles.
		// drawweight determines if the weight of the output to the various nodes are shown, and the xpos and ypos
		// stores where the node will be drawn on the window. The color of the node is a randomized "bright" color.
	protected class nodeclass{
		double bias = 0;
		double biasdev = 0;
		double zvaluematrix[];
		double avaluematrix[];
		//Weighted sum of all the inputed + bias of the node
		double zvalue = 0;
		//avalue is the output of the node and by definition avalue = sigmoid(zvalue)
		double avalue = 0;
		
		//Whether weight value and weight dev should be drawn
		boolean drawweight=false;
		//Whether node should be drawn
		boolean drawnode=false;
		//Stores x and y coordinates of visualization for the node
		int xpos,ypos;
		//Random "bright" color choice
		Color color = new Color((float)(colorpick.nextFloat()/ 2f + 0.5),(float)(colorpick.nextFloat()/ 2f + 0.5),(float)(colorpick.nextFloat()/ 2f + 0.5));
		//Initializes with random weight from -1 to 1
		nodeclass(){
			bias=biaschoose.nextDouble()*2-1;
		}
		
		void printnode(){
			System.out.println("Printing Node");
			System.out.println("value: "+zvalue+" Post Sig: "+avalue+" Bias: "+bias);
			System.out.println();
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
	
	// Initializes net with a text file (outputted from export_net function). This should only be used for 
	// already trained and exported neural nets. Error and partial derivatives are not initialized/inaccurate
	// so backpropagation and gradient_descent should NOT be called. Only feedforward should be used.
	net(String file) throws FileNotFoundException{
		// Drawing the partial derivatives is automatically suppressed to increase efficiency
		drawdev = false;
		Scanner scanner = new Scanner(new File(file));
		// First line contains the number of layers
		String tempnumlayers = scanner.nextLine();
		numlayer = Character.getNumericValue(tempnumlayers.charAt(0));
		// Next line contains the size of each of the layers
		String[] layersizes = scanner.nextLine().split(" ");
		
		// Setting up array with all the sizes of the layers
		alllayersize = new int[numlayer];
		for (int i=0;i<numlayer;i++){
			alllayersize[i] = Integer.parseInt(layersizes[i]);
		}
		allnode = new nodeclass[numlayer][];
		System.out.println("Taking "+scanner.nextLine());
		scanner.nextLine();
		// Taking in node properties from the file
		for (int i=0;i<numlayer;i++){
			allnode[i] = new nodeclass[alllayersize[i]];
			for (int k=0;k<alllayersize[i];k++){
				String nodeprop[] = scanner.nextLine().split(" ");
				nodeclass active;
				allnode[i][k] = active = new nodeclass();
				active.bias = Double.parseDouble(nodeprop[0]);
				active.xpos = Integer.parseInt(nodeprop[1]);
				active.ypos = Integer.parseInt(nodeprop[2]);
				if (Integer.parseInt(nodeprop[3])==1){
					active.drawnode = true;
				}
				else{
					active.drawnode = false;
				}
			}
		}
		System.out.println("Taking "+scanner.nextLine());
		//Initializing array of matrices that stores all the weights
		allweight = new weightclass[alllayersize.length-1][][];
		for (int i=1;i<alllayersize.length;i++){
			allweight[i-1] = new weightclass[alllayersize[i]][alllayersize[i-1]];
			for (int k=0;k<alllayersize[i];k++){
				String tempweight[] = scanner.nextLine().split(" ");
				for (int l=0;l<alllayersize[i-1];l++){
					allweight[i-1][k][l] = new weightclass();
					allweight[i-1][k][l].weight = Double.parseDouble(tempweight[l]);
				}
			}
		}
		scanner.close();
		
	}
	
	// Initializing neural net with arr.length layers and arr[i] nodes for the ith layer. Also opens up a window and
	// starts VISUALIZATION.
	net(int arr[]){
		
		alllayersize = arr;
		numlayer = arr.length;
		// Creating an error matrix
		error = new double[numlayer][];
		for (int i=0;i<numlayer;i++){
			error[i] = new double[alllayersize[i]];
		}
		//Initializing array of matrices that stores all the weights
		allweight = new weightclass[alllayersize.length-1][][];
		for (int i=1;i<alllayersize.length;i++){
			allweight[i-1] = new weightclass[alllayersize[i]][alllayersize[i-1]];
			for (int k=0;k<alllayersize[i];k++){
				for (int l=0;l<alllayersize[i-1];l++){
					allweight[i-1][k][l] = new weightclass();
				}
			}
		}
		//Initializing matrix that stores properties of nodes
		allnode = new nodeclass[numlayer][];
		for (int i=0;i<numlayer;i++){
			if (alllayersize[i]==0){
				throw new RuntimeException("No layers should have 0 nodes.");
			}
			allnode[i] = new nodeclass[alllayersize[i]];
			for (int k=0;k<alllayersize[i];k++){
				nodeclass active = allnode[i][k] = new nodeclass();
				active.drawnode=true;
				if (i==0){
					active.bias=0;
				}
				active.ypos = distfromtop+k*(visualdim/alllayersize[i]);
				active.xpos = distfromside+i*(visualdim/alllayersize.length);
			}	
			if (alllayersize[i]<=maxnodes){
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = allnode[i][k] = new nodeclass();
					active.drawnode=true;
					if (i==0){
						active.bias=0;
					}
					active.ypos = distfromtop+k*(visualdim/alllayersize[i]);
					active.xpos = distfromside+i*(visualdim/alllayersize.length);
				}	
			}
			else{
				//If there are too many nodes (numnodes>maxnodes) take every nth node so that only maxnodes are 
				//displayed
				int interval = alllayersize[i]/maxnodes;
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = allnode[i][k] = new nodeclass();
					active.drawnode=false;
					if (i==0){
						active.bias=0;
					}
				}
				for (int k=0;k<maxnodes;k++){
					allnode[i][k*interval].drawnode=true;
					allnode[i][k*interval].ypos = distfromtop+k*(visualdim/maxnodes);
					allnode[i][k*interval].xpos = distfromside+i*(visualdim/alllayersize.length);
				}
			}
		}
		
			
		//Printing weight matrices
		/*
		for (int i=0;i<allweight.length;i++){
			System.out.println("Printing layer "+i);
			for (int k=0;k<allweight[i].length;k++){
				for (int j=0;j<allweight[i][k].length;j++){
					System.out.print(allweight[i][k][j].weight+" ");
				}
				System.out.println();
			}
		}
		*/
	}
	
	protected void create_window(){
		//Set characteristics of window
		window.setSize(1000, 1000);
		window.setTitle("VISUALIZATION");
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setIconImage(new ImageIcon("neural.jpg").getImage());
		
		//JPanel for all the individual pieces
		JPanel everything = new JPanel(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		
		//JPanel for the VISUALIZATION
		JPanel visuals = new JPanel();
		visuals.setBackground(Color.BLACK);
		visuals.add(new VISUALIZATION());
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
		
		//Currently just filling portions of the window to be used in the future for stats and buttons
		
		JPanel buttons = new JPanel();
		feed_test = new JButton("TEST!");
		feed_test.addActionListener(new act());
		buttons.add(feed_test);
		feed_button = new JButton("FEED!");
		buttons.add(feed_button);
		train_batch_button = new JButton("TRAIN BATCH!");
		train_batch_button.addActionListener(new act());
		buttons.add(train_batch_button);
		train_all_batch_button =  new JButton("TRAIN ALL BATCHES!");
		train_all_batch_button.addActionListener(new act());
		buttons.add(train_all_batch_button);
		buttons.setBackground(Color.PINK);
		
		cons.fill = GridBagConstraints.BOTH;
		cons.weightx = 1;
		cons.weighty = 1;
		cons.ipadx = 0;
		cons.ipady = 30;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.anchor = GridBagConstraints.PAGE_START;
		cons.insets = new Insets(0,0,0,0);
		
		everything.add(buttons,cons);
		
		
		JPanel downfill = new JPanel();
		downfill.add(new JTextField());
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
	}
	
	public class act implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == train_batch_button){
				learn_batch(batch_size);
			}
			else if (e.getSource() == train_all_batch_button){
				for (int i=0;i<100;i++){
					System.out.print(i+" ");
					learn_batch(batch_size);
				}
			}
			else if (e.getSource() == feed_test){
				String line[] = scanner.nextLine().split(",");
				double doublst[] = new double[784];
				for (int i=0;i<784;i++){
					doublst[i] = Double.parseDouble(line[i]);
				}
				feedforward(doublst);
				window.repaint();
				double result[] = getoutput();
				int maxind=0;
				for (int i=0;i<10;i++){
					if (result[i]>result[maxind]){
						maxind = i;
					}
				}
				System.out.println("ANSWER "+maxind);
			}
			
		}
		
	}
	
	// Graphics aspect of the framework
	private class VISUALIZATION extends JComponent {
		
		VISUALIZATION() {
            setPreferredSize(new Dimension(visualdim, visualdim));
        }
		
		public void paintComponent(Graphics g){
			super.paintComponents(g);
			Graphics2D grap = (Graphics2D) g; 	
			grap.setColor(Color.WHITE);
			grap.setFont(new Font("Arial Black", Font.BOLD, 15));
			
			//Painting the weights and lines between nodes
			for (int i=0;i<allweight.length;i++){
				for (int k=0;k<allweight[i][0].length;k++){
					nodeclass active = allnode[i][k];
					if (active.drawnode){
						for (int a=0;a<allweight[i].length;a++){
							if (allnode[i+1][a].drawnode){
								grap.setColor(active.color);
								int x1 = active.xpos+sizenode/2;
								int y1 = active.ypos+sizenode/2;
								int x2 = allnode[i+1][a].xpos+sizenode/2;
								int y2 = allnode[i+1][a].ypos+sizenode/2;
								//Creating lines between nodes where there are weights
								if (x1>30&&x2>30){
									grap.drawLine(x1,y1,x2,y2);
									if (allnode[i][k].drawweight){								
										double slope = (double)(y1-y2)/(double)(x1-x2);
										double yinter = (y1-slope*x1);
										double newx = x1+(x2-x1)/2-55;
										// Writing properties of the weights approximately halfway between the layers
										grap.drawString("Weight: "+Math.round(1000000.0*allweight[i][a][k].weight)/1000000.0, (int)newx, (int)((newx)*slope+yinter));
										if (drawdev)grap.drawString("Weightdev: "+Math.round(1000000.0*allweight[i][a][k].weightdev)/1000000.0, (int)newx, (int)((newx)*slope+yinter+20));
									}	
								}
							}
						}
					}
					
				}
				
			}
			//Drawing nodes
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = allnode[i][k];
					if (active.drawnode){
						grap.setColor(active.color);
						//Drawing Bias and Bias partial derivitive above node
						if (drawdev){
							grap.drawString("Bias: "+Math.round(1000000.0*active.getbias())/1000000.0, active.xpos, active.ypos-20);
							grap.drawString("Biasdev: "+Math.round(1000000.0*active.biasdev)/1000000.0, active.xpos, active.ypos);	
							//Writing down characteristics of the node including the value pre and post sigmoid function and error
							if (i!=0) grap.drawString("Value: "+Math.round(1000000.0*active.zvalue)/1000000.0+"("+Math.round(1000000.0*active.avalue)/1000000.0+")", active.xpos, 20+active.ypos+sizenode);
							else grap.drawString("Value: "+Math.round(1000000.0*active.zvalue)/1000000.0, active.xpos, 20+active.ypos+sizenode);	
							grap.drawString("Error: "+Math.round(1000000.0*error[i][k])/1000000.0, active.xpos, 40+active.ypos+sizenode);
						}
						else{
							grap.drawString("Bias: "+Math.round(1000000.0*active.getbias())/1000000.0, active.xpos, active.ypos);
							if (i!=0) grap.drawString("Value: "+Math.round(1000000.0*active.zvalue)/1000000.0+"("+Math.round(1000000.0*active.avalue)/1000000.0+")", active.xpos, 15+active.ypos+sizenode);
							else grap.drawString("Value: "+Math.round(1000000.0*active.zvalue)/1000000.0, active.xpos, 15+active.ypos+sizenode);	
						}
							
						//Drawing the actual node
						grap.fillOval(active.xpos, active.ypos, sizenode, sizenode);
					}
					
					
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
	
	// Writes down all the properties of the net (weights, biases) to a txt file
	protected void export_net(String file){
		try{
			FileWriter write = new FileWriter(file);
			/*
			 * double learning_rate = 1.5;
				// Choosing which cost function to use
				boolean quadratic = false;
				// Stores the size of each batch for training
				int batch_size;
				// Whether to softmax the results
				boolean softmax = false;
			 */
			write.append(numlayer+" layers "+" learning rate: "+learning_rate);
			if (quadratic){
				write.append(" quadratic ");
			}
			else{
				write.append(" cross-entropy ");
			}
			write.append(" batch size "+batch_size);
			if (softmax){
				write.append("softmax");
			}
			write.append("\n");
			for (int i=0;i<numlayer;i++){
				write.append(alllayersize[i]+" ");
			}
			write.append('\n');
			write.append("Node properties\nFormat is bias then x and y coordinates then drawnode\n");
			for (int i=0;i<numlayer;i++){
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = allnode[i][k];
					write.append(Double.toString(active.bias)+" "+active.xpos+" "+active.ypos+" " );
					if (allnode[i][k].drawnode){
						write.append("1\n");
					}
					else{
						write.append("0\n");
					}
				}
			}
			write.append("Weight properties\n");
			for (int i=0;i<allweight.length;i++){
				for (int k=0;k<allweight[i].length;k++){
					for (int j=0;j<allweight[i][k].length;j++){
						write.append(allweight[i][k][j].weight+" ");
					}
					write.append("\n");
				}
			}
			write.close();
		}
		catch (Exception e){
			System.out.println("Error writing to file");
			e.printStackTrace();
		}
	}
	
	// Outputs whether the coordinates of two (rectangular)objects collide with each other.
	boolean collision(int x1, int y1, int x2, int y2,int siz1, int siz2){
		return (x1+siz1>=x2&&x1<=x2+siz2&&y1+siz1>=y2&&y1<=y2+siz2);
	}
	
	// Created to ensure same input used for automatic and manual testing. Simply feeds forward values
	public void feed_and_set_expected(){
		//double rand = 0.5;
		/*
		double rand = weightchoose.nextDouble();
		//equation is 3x^2-5x+6
		double lst[] = {rand,rand,rand};
		double exp[] = {0.5,0.5};
		feedforward(lst,exp);
		*/
		String[] lst = scanner.nextLine().split(",");
		int correct = Integer.parseInt(lst[0]);
		double ans[] = {0,0,0,0,0,0,0,0,0,0};
		ans[correct] = 1;
    	double doublst[] = new double[784];
    	for (int a=0;a<784;a++){
    		doublst[a] = Double.parseDouble(lst[a+1])/255;
    	}
		feedforward(doublst);
		set_expected(ans);
	}
	 
	
	// Used in combination with feed for training only. When doing testing questions, this does not need
	// to be called.
	void set_expected(double[] tempexpected){
		expected = tempexpected;
	}
	
	// Clears all the partial derivatives. Should be called after gradient descent
	private void cleardev(){
		for (int i=0;i<numlayer;i++){
			for (int k=0;k<alllayersize[i];k++){
				// error matrix reset is redundant 
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
	
	protected void learn_batch(int batch_size){
		int corr=0;
		for (int i=0;i<batch_size;i++){
			
			feed_and_set_expected();
			int maxind=0;
			for (int z=0;z<10;z++){
				if (allnode[2][maxind].avalue<allnode[2][z].avalue){
					maxind=z;
				}
			}
			int choice=0;
			for (int a=0;a<10;a++){
				if (expected[a]==1){
					choice=a;
					break;
				}
			}
			if (choice==maxind){
				corr++;
			}
			backpropagate();
		}
		gradient_descent(batch_size);
		if (print)
		System.out.println("CORRECT: "+corr);
	}
	
	private class mouseevent implements MouseListener{
		// Toggles whether the weights of a certain node should be shown. Done by clicking the node.
		public void mouseClicked(MouseEvent e) {
			if (auto){
				System.out.println("Automatic testing is running");
				
			}
			//System.out.println(e.getPoint());
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
				feed_and_set_expected();
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
				learn_batch(100);
				window.repaint();
			}
			boolean done = false;
			for (int i=0;i<alllayersize.length;i++){
				for (int k=0;k<alllayersize[i];k++){
					nodeclass active = allnode[i][k];
					if (active.drawnode&&collision(active.getx(),active.gety(),e.getX(),e.getY(),sizenode,0)){
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
	protected void feedforward(double data[]){
		if (data.length!=alllayersize[0]){
			System.out.println("ERROR: Input layer size different then number of inputs");
		}
		else{
			for (int i=0;i<alllayersize[0];i++){
				allnode[0][i].avalue = allnode[0][i].zvalue = data[i];
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
			if (softmax){
				double sum = 0;
				for (int i=0;i<alllayersize[numlayer-1];i++){
					sum+=Math.exp(allnode[numlayer-1][i].zvalue);
				
				}
				for (int i=0;i<alllayersize[numlayer-1];i++){
					allnode[numlayer-1][i].avalue = Math.exp(allnode[numlayer-1][i].zvalue)/sum;
				}
			}
		}
		/*
		System.out.print("Expected: "+expected[0]);
		System.out.printf("Act: %f",getoutput()[0]);
		System.out.println();
		System.out.println("Cost: "+Math.pow((expected[0]-getoutput()[0]),2)/2);
		*/
	}
	
	//Work in progress matrix matrix multiplication. The function would not be much quicker as more advanced
	//linear algebra theorums would have to be used to really see an impact.
	/*
	protected void feed_batch(double [][] data){
		if (data[0].length!=alllayersize[0]){
			System.out.println("ERROR: Input layer size different then number of inputs");
		}
		else{
			for (int i=0;i<alllayersize[0];i++){
				allnode[0][i].avaluematrix = allnode[0][i].zvaluematrix = new double[data.length];
				for (int k=0;k<data.length;k++){
					allnode[0][i].avaluematrix[k] = allnode[0][i].zvaluematrix[k] = data[k][i];
				}
			}
			for (int i=0;i<numlayer;i++){
				for (int k=0;k<allnode[i][0].avaluematrix.length;k++){
					for (int a=0;a<alllayersize[i];a++){
						 
					}
				}
			}
		}
	}
	*/
	
	//Returns the output layer of the net. Should be called after a feedforward is called.
	protected double[] getoutput(){
		double output[] = new double[alllayersize[alllayersize.length-1]];
		for (int i=0;i<alllayersize[alllayersize.length-1];i++){
			output[i] = allnode[alllayersize.length-1][i].avalue;
		}
		return output;
	}
	
	//Backpropagate() should only be called after a feedfoward and a set expected value is called. Otherwise
	//undefined behaviour will occur.
	
	//Backpropagates errors through the neural net. Either the quadratic or cross-entropy cost function can
	//be used. Generally the cross-entropy cost function is better, so it is default. This first calculates the 
	//partial derivative of all the weights and biases, and returns the gradient. This gradient should then
	//be averaged over the number of inputs and adjustments will be made in the gradient_descent function.
	//The notations BPX refer to the specific fundemental backpropagation algorithms
	protected void backpropagate(){
		//BP1
		if (expected==null){
			throw new NullPointerException("\nExpected array is empty! This is caused when backpropagate is called before the array of expected value is set");
		}
		for (int i=0;i<alllayersize[numlayer-1];i++){
			if (quadratic) error[numlayer-1][i] = (allnode[numlayer-1][i].avalue-expected[i])*sigmoidprime(allnode[numlayer-1][i].zvalue);
			else error[numlayer-1][i] = allnode[numlayer-1][i].avalue-expected[i];
			allnode[numlayer-1][i].biasdev += error[numlayer-1][i];
		}
		expected = null;
		//BP2
		
		for (int i=numlayer-2;i>=0;i--){
			for (int k=0;k<alllayersize[i];k++){
				double newerror=0;
				nodeclass active = allnode[i][k];
				double sigmoidprime = sigmoidprime(active.zvalue);
				
				for (int a=0;a<alllayersize[i+1];a++){
					
					newerror += allweight[i][a][k].weight*error[i+1][a]*sigmoidprime;
					//BP4
					allweight[i][a][k].weightdev += active.avalue*error[i+1][a];
					
				}
				
				error[i][k] = newerror;
				//BP3
				active.biasdev += newerror;
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
		//Updating weights
		for (int i=0;i<numlayer-1;i++){
			for (int k=0;k<allweight[i].length;k++){
				for (int a=0;a<allweight[i][0].length;a++){
					allweight[i][k][a].weight-=learning_rate/(double)batch_size*allweight[i][k][a].weightdev;
				}
			}
		}
		//Resetting partial derivatives
		cleardev();
		window.repaint();
	}
}