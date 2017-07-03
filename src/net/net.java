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
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

// Made by Andrew Xue
// a3xue@edu.uwaterloo.ca
// Neural net framework! Uses stochastic gradient descent + backpropagation as its learning algorithm. I made
// this because tensorflow felt like just throwing black box functions at a problem and hoping it worked. I
// figured if I made everything from scratch I would understand it better.

//TODO
//Using validation data to prevent overfitting
//Improve UI more buttons
//Other improvements for escaping local minima
//Implement a learning rate slowdown as the number of batches tested goes
//Heatmap of different numbers
//Alternatives to sigmoid functions (tanx function)
//More more cost functions

//DONE!
//Graphing performance over epochs
//Ability to switch between graph and net views
//Support for different cost functions
//Softmax
//Outputting network weights + node properties to a seperate file for easy use in other programs
//New constructor that builds neural net off of file (presumably trained one)
//Handle layers with too many nodes
//Cross entropy cost function
//Training batches, and all batches buttons work

//On Hold
//Matrix matrix multiplication for batches

//Known bugs:
//Slow learning rate. Matrix matrix multiplication should alleviate this


public class net{
	Timer graph_draw = new Timer(0, new act());
	boolean print = false;
	int result = -1;
	ArrayList<Integer> results = new ArrayList<Integer>();
	boolean graphing = false;
	//Scanner for CSV file
	Scanner scanner;
	
	//HYPERPARAMETERS. These should be set when the net is initialized.
	// Learning rate of the net. Higher learning rates lead to quicker results but can "overshoot", lower learning rates
	// are slower but steadier
	double learning_rate;
	// Choosing which cost function to use (quadratic or cross-entropy at time of writing)
	boolean quadratic;
	// Stores the size of each batch for training
	int batch_size;
	// Whether to softmax the results
	boolean softmax;
	
	
	//VISUALIZATION ASPECTS
	//Window for VISUALIZATION
	JFrame window = new JFrame();
	// Control if VISUALIZATION appears
	boolean visual = true;
  
  
	// Buttons used in the VISUALIZATION
	JButton graph_button,train_batch_button,feed_button,train_all_batch_button,train_one_button;
	// Sets limit for maximum number of nodes per layer displayed in visualization
	int maxnodes = 6;
	// Whether the partial derivatives should be drawn. Used for importing nets when it should be trained
	boolean drawdev = false;
	
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
		window = new JFrame("VISUALIZATION");
		window.setSize(1000, 1000);
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
		graph_button = new JButton("GRAPH!");
		buttons.add(graph_button);
		graph_button.addActionListener(new act());
		feed_button = new JButton("FEED!");
		buttons.add(feed_button);
		train_one_button = new JButton("TRAIN ONE!");
		train_one_button.addActionListener(new act());
		buttons.add(train_one_button);
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
	
	
	protected void graph_results(int num_epoch){
		/*
		graph = new JFrame("More VISUALIZATION");
		//Set characteristics of window
		graph.setSize(1000, 1000);
		graph.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		graph.setResizable(false);
		graph.setIconImage(new ImageIcon("neural.jpg").getImage());
		
		
		graph.add(new graph_VISUALIZATION(100));
		graph.setVisible(true);
		*/
		graphing = true;
		window.repaint();
		for (int i=0;i<num_epoch;i++){
			results.add(learn_batch(batch_size));
		}
	}
	
	
	
	public class act implements ActionListener{
		
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == train_one_button){
				feed_and_set_expected(true);
			}
			else if (e.getSource() == train_batch_button){
				int numcorrect = learn_batch(batch_size);
				if (numcorrect==-1){
					train_batch_button.setEnabled(false);
					train_all_batch_button.setEnabled(false);
					train_one_button.setEnabled(false);
					System.out.println("OUT OF DATA!");
					return;
				}
				System.out.println(numcorrect+" correct out of "+batch_size);
				results.add(numcorrect);
				window.repaint();
			}
			else if (e.getSource() == train_all_batch_button){
				graph_draw.start();
			}
			else if (e.getSource() == graph_button){
				//graph_results(100);
				graphing = !graphing;
				if (graphing){
					graph_button.setText("NET!");
				}
				else{
					graph_button.setText("GRAPH!");
				}
				window.repaint();
			}
			else if (e.getSource() == graph_draw){
				int numcorrect = learn_batch(batch_size);
				if (numcorrect==-1){
					train_batch_button.setEnabled(false);
					train_all_batch_button.setEnabled(false);
					train_one_button.setEnabled(false);
					System.out.println("OUT OF DATA!");
					graph_draw.stop();
					return;
				}
				System.out.println(numcorrect+" correct out of "+batch_size);
				results.add(numcorrect);
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
			if (!graphing){
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
			else{
				grap.drawLine(distfromside, visualdim-distfromtop, visualdim-distfromside, visualdim-distfromtop);
				grap.drawLine(distfromside, visualdim-distfromtop, distfromside, distfromtop);
				grap.drawString("Epoch", visualdim-distfromside-50, visualdim-distfromtop);
				grap.drawString("0", distfromside, visualdim-distfromtop+20);
				grap.drawString(results.size()/4+"", distfromside+200, visualdim-distfromtop+20);
				grap.drawString(2*results.size()/4+"", distfromside+400, visualdim-distfromtop+20);
				grap.drawString(3*results.size()/4+"", distfromside+600, visualdim-distfromtop+20);
				grap.drawString(results.size()+"", distfromside+800, visualdim-distfromtop+20);
				grap.drawString("Accuracy (%)", distfromside, visualdim-distfromtop-800+5);
				grap.drawString("0", distfromside-15, visualdim-distfromtop+5);
				grap.drawString("25", distfromside-25, visualdim-distfromtop-200+5);
				grap.drawString("50", distfromside-25, visualdim-distfromtop-400+5);
				grap.drawString("75", distfromside-25, visualdim-distfromtop-600+5);
				grap.drawString("100", distfromside-35, visualdim-distfromtop-800+5);
				double xratio = 800/(double)results.size();
				double yratio = 800/(double)batch_size;
				if (results.size()!=0&&results.get(results.size()-1)==-1){
					results.remove(results.size()-1);
				}
				for (int i=1;i<results.size();i++){
					//grap.fillRect(distfromside+(int)(i*xratio),visualdim-distfromtop-(int) (results[i]*yratio), 1, 1);
					grap.drawLine(distfromside+(int)((i-1)*xratio), visualdim-distfromtop-(int)(results.get(i-1)*yratio), distfromside+(int)(i*xratio), visualdim-distfromtop-(int) (results.get(i)*yratio));
					//grap.drawLine(distfromside+(int)((i-1)*(double)((visualdim-distfromside-distfromside)/results.length)), visualdim-distfromtop-(int)((visualdim-distfromtop-distfromtop)*((double)results[i-1]/(double)batch_size)), distfromside+(int)((i)*(double)((visualdim-distfromside-distfromside)/results.length)), visualdim-distfromtop-(int)((visualdim-distfromtop-distfromtop)*((double)results[i]/(double)batch_size)));
				}
			}
		}
	}
	/*
	private class graph_VISUALIZATION extends JComponent {
		int num_epoch;
		graph_VISUALIZATION(int temp_num_epoch) {
            setPreferredSize(new Dimension(visualdim, visualdim));
            num_epoch = temp_num_epoch;
        }
		
		public void paintComponent(Graphics g){
			super.paintComponents(g);
			Graphics2D grap = (Graphics2D) g; 	
			grap.setColor(Color.BLACK);
			grap.fillRect(0, 0, 1000, 1000);
			grap.setColor(Color.WHITE);
			grap.setFont(new Font("Arial Black", Font.BOLD, 15));
			grap.drawLine(distfromside, visualdim-distfromtop, visualdim-distfromside, visualdim-distfromtop);
			grap.drawLine(distfromside, visualdim-distfromtop, distfromside, distfromtop);
			if (result!=-1){
				grap.drawLine(0, 300, result, 300);
			}
			
			for (int i=0;i<num_epoch;i++){
				int numcorrect = learn_batch(batch_size);
				if (numcorrect==-1){
					train_batch_button.setEnabled(false);
					train_all_batch_button.setEnabled(false);
					train_one_button.setEnabled(false);
					
					System.out.println("OUT OF DATA!");
					return;
				}
				System.out.println(numcorrect);
			}
			
		}
	}
	*/
	
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
			else if (softmax){
				write.append(" softmax log-likelihood ");
			}
			else{
				write.append(" cross-entropy ");
			}
			write.append(" batch size "+batch_size);
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
	// and returns a "1" if the actual and expected are the same. Can also print the expected and actual
	// values.
	public int feed_and_set_expected(boolean printresults){
		if (!scanner.hasNextLine())return -1;
		String[] lst = scanner.nextLine().split(",");
		int correct = Integer.parseInt(lst[0]);
		double ans[] = {0,0,0,0,0,0,0,0,0,0};
		ans[correct] = 1;
    	double doublst[] = new double[784];
    	for (int a=0;a<784;a++){
    		doublst[a] = Double.parseDouble(lst[a+1])/255.0;
    	}
		feedforward(doublst);
		expected = ans;
		
		double result[] = getoutput();
		int maxind=0;
		for (int i=0;i<10;i++){
			if (result[i]>result[maxind]){
				maxind = i;
			}
		}
		if (printresults){
			System.out.println("Expected/Correct: "+correct+" Actual: "+maxind);
		}
		if (maxind==correct)return 1;
		return 0;
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
	
	// Repeats the feed_and_set_expected() for batch_size times and in addition prints out a status report of the
	// numbers classified correctly in the batch.
	protected int learn_batch(int batch_size){
		int corr=0;
		for (int i=0;i<batch_size;i++){
			int result = feed_and_set_expected(false);
			if (result==-1)return -1;
			corr+=result;
			backpropagate();
		}
		// After all the errors of the batch have been backpropagated, gradient_descent is called in order for the net to
		// learn
		gradient_descent(batch_size);
		return corr;
	}
	
	private class mouseevent implements MouseListener{
		// Toggles whether the weights of a certain node should be shown. Done by clicking the node.
		public void mouseClicked(MouseEvent e) {
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
					nodeclass active = allnode[i+1][k];
					for (int a=0;a<allweight[i][0].length;a++){
						newz+=allnode[i][a].avalue*allweight[i][k][a].weight;
					}
					active.zvalue = newz+active.bias;
					active.avalue = sigmoid(active.zvalue);
				}
			}
			if (softmax){
				double sum = 0;
				double expvalues[] =  new double[alllayersize[numlayer-1]];
				for (int i=0;i<alllayersize[numlayer-1];i++){
					expvalues[i] = Math.exp(allnode[numlayer-1][i].zvalue);
					sum+=expvalues[i];
				
				}
				for (int i=0;i<alllayersize[numlayer-1];i++){
					allnode[numlayer-1][i].avalue = expvalues[i]/sum;
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
			//cross entropy and log-likelihood has the same derivative with respect to avalue
			else if (softmax||!quadratic) error[numlayer-1][i] = allnode[numlayer-1][i].avalue-expected[i];
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
				allnode[i][k].biasdev = 0;
			}
		}
		//Updating weights
		for (int i=0;i<numlayer-1;i++){
			for (int k=0;k<allweight[i].length;k++){
				for (int a=0;a<allweight[i][0].length;a++){
					allweight[i][k][a].weight-=learning_rate/(double)batch_size*allweight[i][k][a].weightdev;
					allweight[i][k][a].weightdev = 0;
				}
			}
		}
		//Resetting partial derivatives
		//Currently being done in line
		//cleardev();
		window.repaint();
	}
}