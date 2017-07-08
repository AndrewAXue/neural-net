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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// Made by Andrew Xue
// a3xue@edu.uwaterloo.ca
// Neural net framework! Uses stochastic gradient descent + backpropagation as its learning algorithm. I made
// this because tensorflow felt like just throwing black box functions at a problem and hoping it worked. I
// figured if I made everything from scratch I would understand it better.

//TODO
//fisher-yates shuffling algorithm
//matrix multiplication
//Using validation data to prevent overfitting
//Other improvements for escaping local minima
//Implement a learning rate slowdown as the number of batches tested goes
//Heatmap of different numbers
//Alternatives to sigmoid functions (tanx function)
//More more cost functions

//DONE!
//Buttons for training, exporting, graphing and feeding
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
	
	//Scanner for CSV file
	Scanner scanner;
	BufferedReader read;
	
	// Random number chooser for shuffling dataset
	Random shuffle_rand = new Random();
	
	//Which data point currently on
	int data_ind = 0;
	int epoch_ind = 0;
	//Training data
	private class train_data{
		double solution[] = new double[10];
		double pixels[] = new double[782];
		int answer;
		train_data(double tempsolution[],double temppixels[]){
			solution = tempsolution;
			pixels = temppixels;
		}
	}
	train_data all_train_data [] = new train_data[42000];
	
	//Testing data
	double all_test_data [][];
	
	public void load_training_data() throws IOException{
		try {
			read = new BufferedReader(new FileReader("digit_data/train.csv"));
		} catch (FileNotFoundException exp) {
			System.out.println("FILE NOT FOUND!");
			status_text.setText("FILE NOT FOUND!");
			exp.printStackTrace();
		}
		read.readLine();
		for (int i=0;i<42000;i++){
			String string_data[] = read.readLine().split(",");
			
			int correct = Integer.parseInt(string_data[0]);
			double ans[] = {0,0,0,0,0,0,0,0,0,0};
			ans[correct] = 1;
			// Set up the input data and feed it through the network
	    	double doublst[] = new double[784];
	    	
	    	for (int a=0;a<784;a++){
	    		doublst[a] = Double.parseDouble(string_data[a+1])/255.0;
	    		
	    		
	    	}
	    	
	    	all_train_data[i] = new train_data(ans,doublst);
	    	all_train_data[i].answer = correct;
		}
		System.out.println("DONE");
	}
	
	//HYPERPARAMETERS. These should be set when the net is initialized.
	// Learning rate of the net. Higher learning rates lead to quicker results but can "overshoot", lower learning rates
	// are slower but steadier
	double learning_rate;
	// Choosing which cost function to use (quadratic or cross-entropy at time of writing)
	boolean quadratic;
	// Whether to softmax the results
	boolean softmax;
	// Whether status updates should be printed;
	boolean print;
	// Sets limit for maximum number of nodes per layer displayed in visualization
	int maxnodes = 6;
	// Sets the number of epochs to be trained;
	int num_epoch;
	// The size of training batches
	int train_batch_size;
	// The size of testing batches
	int test_batch_size;
	
	
	//VISUALIZATION ASPECTS
	//Window for VISUALIZATION
	JFrame window = new JFrame();
	// Control if VISUALIZATION appears
	boolean visual = true;
	
	//GRAPHING ASPECTS
	// Timer for continuously updating the graph with new batches
	Timer graph_draw = new Timer(0, new actions());
	// Used for graphing. Stores the results of testing batches
	int epoch_results [];
	ArrayList<Integer> batch_results = new ArrayList<Integer>();
	// Denoted the setting. If false, it is on the graphing screen, else it is on the node screen.
	boolean graphing = false;
  
  
	// Buttons used in the VISUALIZATION
	JButton graph_button,train_epoch_button,feed_button,train_all_epoch_button,export_button;
	// Label for printing status reports
	JLabel status_text;
	// Textfield for choosing file name of where to export net
	JTextField export_text;
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
	
	// Writes down all the properties of the net (weights, biases) to a txt file
	protected void export_net(String file){
		try{
			FileWriter write = new FileWriter(file);
			// Writing the hyperparameters of the net, number of layers, learning rate, cost functions used, etc.
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
			write.append(" training batch size "+train_batch_size+" testing batch size "+test_batch_size);
			write.append(" "+epoch_ind+" epochs run");
			write.append("\n");
			for (int i=0;i<numlayer;i++){
				write.append(alllayersize[i]+" ");
			}
			write.append('\n');
			write.append("Node properties\nFormat is bias then x and y coordinates then drawnode\n");
			// Printing characteristics of the nodes
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
			// Printing the characteristics of all the weights
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
		// If the file does not exist or there is some other error, report it
		catch (Exception e){
			System.out.println("Error writing to file");
			status_text.setText("Error writing to file");
			e.printStackTrace();
		}
	}
	
	// Initializes net with a text file (outputted from export_net function). This should only be used for 
	// already trained and exported neural nets. Error and partial derivatives are not initialized/inaccurate
	// so backpropagation and gradient_descent should NOT be called. Only feedforward should be used.
	net(String file) throws FileNotFoundException{
		// Drawing the partial derivatives is automatically suppressed to increase efficiency
		drawdev = false;
		Scanner net_scanner = null;
		try{
			net_scanner = new Scanner(new File(file));
		}
		catch(FileNotFoundException exp){
			System.out.println("File not found!");
			exp.printStackTrace();
		}
		// First line contains the number of layers
		String tempnumlayers = net_scanner.nextLine();
		numlayer = Character.getNumericValue(tempnumlayers.charAt(0));
		// Next line contains the size of each of the layers
		String[] layersizes = net_scanner.nextLine().split(" ");
		
		// Setting up array with all the sizes of the layers
		alllayersize = new int[numlayer];
		for (int i=0;i<numlayer;i++){
			alllayersize[i] = Integer.parseInt(layersizes[i]);
		}
		allnode = new nodeclass[numlayer][];
		System.out.println("Taking "+net_scanner.nextLine());
		net_scanner.nextLine();
		// Taking in node properties from the file
		for (int i=0;i<numlayer;i++){
			allnode[i] = new nodeclass[alllayersize[i]];
			for (int k=0;k<alllayersize[i];k++){
				String nodeprop[] = net_scanner.nextLine().split(" ");
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
		System.out.println("Taking "+net_scanner.nextLine());
		//Initializing array of matrices that stores all the weights
		allweight = new weightclass[alllayersize.length-1][][];
		for (int i=1;i<alllayersize.length;i++){
			allweight[i-1] = new weightclass[alllayersize[i]][alllayersize[i-1]];
			for (int k=0;k<alllayersize[i];k++){
				String tempweight[] = net_scanner.nextLine().split(" ");
				for (int l=0;l<alllayersize[i-1];l++){
					allweight[i-1][k][l] = new weightclass();
					allweight[i-1][k][l].weight = Double.parseDouble(tempweight[l]);
				}
			}
		}
		net_scanner.close();
		
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
	protected void create_window(){
		epoch_results = new int[num_epoch];
		for (int i=0;i<num_epoch;i++){
			epoch_results[i]=-1;
		}
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
		
		
		//JPanel for all the buttons. 
		JPanel buttons = new JPanel();
		graph_button = new JButton("GRAPH!");
		graph_button.addActionListener(new actions());
		graph_button.setToolTipText("Switch to graph view");
		buttons.add(graph_button);
		
		feed_button = new JButton("FEED!");
		feed_button.addActionListener(new actions());
		buttons.add(feed_button);
		
		train_epoch_button = new JButton("TRAIN EPOCH!");
		train_epoch_button.addActionListener(new actions());
		buttons.add(train_epoch_button);
		
		train_all_epoch_button =  new JButton("TRAIN ALL EPOCHS!");
		train_all_epoch_button.addActionListener(new actions());
		buttons.add(train_all_epoch_button);
		
		export_text = new JTextField();
		export_text.addActionListener(new actions());
		export_text.setColumns(15);
		export_text.setText("trained_net");
		buttons.add(export_text);
		
		export_button = new JButton("EXPORT!");
		export_button.addActionListener(new actions());
		export_button.setToolTipText("Exports the net to the given file name on the left. Should be used on a trained net");
		buttons.add(export_button);
		
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
		
		// Currently just a filler for some area below the visualization
		JPanel downfill = new JPanel();
		status_text = new JLabel("VISUALIZATION");
		status_text.setForeground(Color.WHITE);
		status_text.setFont(new Font("Arial Black", Font.BOLD, 25));
		downfill.add(status_text);
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
		
		// Currently just a filler for some are on the right of the visualization
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
	
	
	// Preform different actions depending on the button pressed
	public class actions implements ActionListener{
		public void actionPerformed(ActionEvent action) {
			if (action.getSource() == train_epoch_button){
				train_epoch();
				shuffle(all_train_data);
				data_ind=0;
				int numcorrect = learn_batch(test_batch_size);
				if (numcorrect==-1){
					train_all_epoch_button.setEnabled(false);
					train_epoch_button.setEnabled(false);
					System.out.println("OUT OF DATA!");
					status_text.setText("OUT OF DATA!");
					return;
				}
				epoch_results[epoch_ind] = numcorrect;
				System.out.println(numcorrect+" correct out of "+test_batch_size);
				status_text.setText(numcorrect+" correct out of "+test_batch_size);
				epoch_ind++;
				if (epoch_ind==num_epoch){
					train_epoch_button.setEnabled(false);
					train_all_epoch_button.setEnabled(false);
				}
				window.repaint();
			}
			else if (action.getSource() == train_all_epoch_button){
				graph_draw.start();
			}
			else if (action.getSource() == graph_button){
				// Toggle the graphing boolean
				graphing = !graphing;
				// Depending on if the graphing boolean is now true, change the text of the button
				if (graphing){
					graph_button.setText("NET!");
					graph_button.setToolTipText("Switch to net view");
				}
				else{
					graph_button.setText("GRAPH!");
					graph_button.setToolTipText("Switch to graph view");
				}
				window.repaint();
			}
			else if (action.getSource() == export_button){
				// When the export button is clicked, create and export to the file name provided by the textfield
				String file_name = export_text.getText();
				if (file_name.length()!=0){
					export_net(file_name+".txt");
					System.out.println("Exported successfully to "+file_name+".txt");
					status_text.setText("Exported successfully to "+file_name+".txt");
				}
				else{
					System.out.println("File name cannot be empty!");
					status_text.setText("File name cannot be empty!");
				}
			}
			else if (action.getSource() == graph_draw){
				train_epoch();
				shuffle(all_train_data);
				data_ind=0;
				int numcorrect = learn_batch(test_batch_size);
				if (numcorrect==-1){
					train_all_epoch_button.setEnabled(false);
					train_epoch_button.setEnabled(false);
					System.out.println("OUT OF DATA!");
					status_text.setText("OUT OF DATA!");
					graph_draw.stop();
					return;
				}
				epoch_results[epoch_ind] = numcorrect;
				System.out.println(numcorrect+" correct out of "+test_batch_size);
				status_text.setText(numcorrect+" correct out of "+test_batch_size);
				epoch_ind++;
				if (epoch_ind==num_epoch){
					graph_draw.stop();
					train_epoch_button.setEnabled(false);
					train_all_epoch_button.setEnabled(false);
				}
				window.repaint();
			}
			else if (action.getSource() == export_text){
				export_button.doClick();
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
				// Creating the axis and axis labels
				grap.drawLine(distfromside, visualdim-distfromtop, visualdim-distfromside, visualdim-distfromtop);
				grap.drawLine(distfromside, visualdim-distfromtop, distfromside, distfromtop);
				grap.drawString("Batch", visualdim-distfromside-50, visualdim-distfromtop);
				grap.drawString("0", distfromside, visualdim-distfromtop+20);
				grap.drawString(num_epoch/4+"", distfromside+200, visualdim-distfromtop+20);
				grap.drawString(2*num_epoch/4+"", distfromside+400, visualdim-distfromtop+20);
				grap.drawString(3*num_epoch/4+"", distfromside+600, visualdim-distfromtop+20);
				grap.drawString(num_epoch+"", distfromside+800, visualdim-distfromtop+20);
				grap.drawString("Accuracy (%)", distfromside, visualdim-distfromtop-800+5);
				grap.drawString("0", distfromside-15, visualdim-distfromtop+5);
				grap.drawString("25", distfromside-25, visualdim-distfromtop-200+5);
				grap.drawString("50", distfromside-25, visualdim-distfromtop-400+5);
				grap.drawString("75", distfromside-25, visualdim-distfromtop-600+5);
				grap.drawString("100", distfromside-35, visualdim-distfromtop-800+5);
				// Ratios used to even spread out data points among available space
				double xratio = 800/(double)num_epoch;
				double yratio = 800/(double)test_batch_size;
				// Draws a line graph between all data points (which are the accuracy of each batch)
				for (int i=1;i<num_epoch;i++){
					if (epoch_results[i]==-1)break;
					if (i%2==0){
						grap.drawString(String.valueOf(epoch_results[i]), distfromside+(int)(i*xratio)-15, visualdim-distfromtop-(int) (epoch_results[i]*yratio)-5);
					}
					else{
						grap.drawString(String.valueOf(epoch_results[i]), distfromside+(int)(i*xratio)-15, visualdim-distfromtop-(int) (epoch_results[i]*yratio)+15);
					}
					grap.drawLine(distfromside+(int)((i-1)*xratio), visualdim-distfromtop-(int)(epoch_results[i-1]*yratio), distfromside+(int)(i*xratio), visualdim-distfromtop-(int) (epoch_results[i]*yratio));
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
	
	// Outputs whether the coordinates of two (rectangular)objects collide with each other.
	boolean collision(int x1, int y1, int x2, int y2,int siz1, int siz2){
		return (x1+siz1>=x2&&x1<=x2+siz2&&y1+siz1>=y2&&y1<=y2+siz2);
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
	
	// Shuffle the data set using the fisher yates shuffling algorithm
	protected void shuffle(train_data all_train_data[]){
		for (int i=all_train_data.length-1;i>=0;i--){
			int targ = shuffle_rand.nextInt(i+1);
			train_data temp = all_train_data[i];
			all_train_data[i] = all_train_data[targ];
			all_train_data[targ] = temp;
		}		
	}
	
	// Train through an epoch of data and apply backpropagation for each mini batch trained.
	protected void train_epoch(){
		shuffle(all_train_data);
		int batch=0;
		data_ind=0;
		while(learn_batch(train_batch_size)!=-1){
			if (batch%100==0) System.out.println("Batch: "+batch);
			batch++;  
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
	
	// Created to ensure same input used for automatic and manual testing. Simply feeds forward values
	// and returns a "1" if the actual and expected are the same. Can also print the expected and actual
	// values.
	public int feed_and_set_expected(boolean printresults){
		// If there is no more data, return an error value
		if (data_ind==all_train_data.length)return -1;
		train_data current_data = all_train_data[data_ind];
		feedforward(current_data.pixels);
		expected = current_data.solution;
		// Get the output layer and compare to the expected answer
		double result[] = getoutput();
		int maxind=0;
		for (int i=0;i<10;i++){
			if (result[i]>result[maxind]){
				maxind = i;
			}
		}
		// If applicable, print out the expected and actual values
		if (printresults){
			System.out.println("Expected/Correct: "+current_data.answer+" Actual: "+maxind);
			status_text.setText("Expected/Correct: "+current_data.answer+" Actual: "+maxind);
		}
		data_ind++;
		// If the actual and expected values are the same, return 1
		if (maxind==current_data.answer)return 1;
		return 0;
	}
	
	private class mouseevent implements MouseListener{
		// Toggles whether the weights of a certain node should be shown. Done by clicking the node.
		public void mouseClicked(MouseEvent e) {
			// If the mouse was clicked on one of the nodes, toggle the drawweight property of the node and repaint
			// the window to reflect the change
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
		// If the amount of input data is different then the number of input nodes, print an error
		if (data.length!=alllayersize[0]){
			System.out.println("ERROR: Input layer size different then number of inputs");
			status_text.setText("ERROR: Input layer size different then number of inputs");
		}
		else{
			// Set the input layer to the data received
			for (int i=0;i<alllayersize[0];i++){
				allnode[0][i].avalue = allnode[0][i].zvalue = data[i];
			}
			// Feed the data forward layer by layer
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
			// if applicable, softmax the output layer for better probability distribution
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