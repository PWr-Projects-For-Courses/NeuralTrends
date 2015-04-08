package com.github.yusugomori;

import java.util.Random;

public class StackedDenoisingAutoencoder {
	public int n;
	public int nIns;
	public int[] hiddenLayerSizes;
	public int nOuts;
	public int nLayers;
	public HiddenLayer[] sigmoidLayers;
	public DenoisingAutoencoder[] denoisingAutoencoderLayers;
	public LogisticRegression logLayer;
	public Random random;

	public static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.pow(Math.E, -x));
	}
	
	public StackedDenoisingAutoencoder(int N, int nIns, int[] hiddenLayerSizes, int nOuts, int nLayers, Random random) {
		int input_size;
		
		this.n = N;
		this.nIns = nIns;
		this.hiddenLayerSizes = hiddenLayerSizes;
		this.nOuts = nOuts;
		this.nLayers = nLayers;
		
		this.sigmoidLayers = new HiddenLayer[nLayers];
		this.denoisingAutoencoderLayers = new DenoisingAutoencoder[nLayers];

		if(random == null)	this.random = new Random(1234);
		else this.random = random;
		
		// construct multi-layer
		for(int i=0; i<this.nLayers; i++) {
			if(i == 0) {
				input_size = this.nIns;
			} else {
				input_size = this.hiddenLayerSizes[i-1];
			}
			
			// construct sigmoid_layer
			this.sigmoidLayers[i] = new HiddenLayer(this.n, input_size, this.hiddenLayerSizes[i], null, null, random);
			
			// construct dA_layer
			this.denoisingAutoencoderLayers[i] = new DenoisingAutoencoder(this.n, input_size, this.hiddenLayerSizes[i], this.sigmoidLayers[i].W, this.sigmoidLayers[i].b, null, random);
		}
		
		// layer for output using com.github.yusugomori.LogisticRegression
		this.logLayer = new LogisticRegression(this.n, this.hiddenLayerSizes[this.nLayers -1], this.nOuts);
	}
	
	public void pretrain(int[][] train_X, double lr, double corruption_level, int epochs) {
		int[] layer_input = new int[0];
		int prev_layer_input_size;
		int[] prev_layer_input;
				
		for(int i=0; i< nLayers; i++) {  // layer-wise
			for(int epoch=0; epoch<epochs; epoch++) {  // training epochs
				for(int n=0; n< this.n; n++) {  // input x1...xN
					// layer input
					for(int l=0; l<=i; l++) {
						
						if(l == 0) {
							layer_input = new int[nIns];
							for(int j=0; j< nIns; j++) layer_input[j] = train_X[n][j];
						} else {
							if(l == 1) prev_layer_input_size = nIns;
							else prev_layer_input_size = hiddenLayerSizes[l-2];
							
							prev_layer_input = new int[prev_layer_input_size];
							for(int j=0; j<prev_layer_input_size; j++) prev_layer_input[j] = layer_input[j];
							
							layer_input = new int[hiddenLayerSizes[l-1]];
							
							sigmoidLayers[l-1].sample_h_given_v(prev_layer_input, layer_input);
						}
					}
					
					denoisingAutoencoderLayers[i].train(layer_input, lr, corruption_level);
				}
			}
		}
	}
		
	public void finetune(int[][] train_X, int[][] train_Y, double lr, int epochs) {
		int[] layer_input = new int[0];
		// int prev_layer_input_size;
		int[] prev_layer_input = new int[0];
		
		for(int epoch=0; epoch<epochs; epoch++) {
			for(int n=0; n< this.n; n++) {
				
				// layer input
				for(int i=0; i< nLayers; i++) {
					if(i == 0) {
						prev_layer_input = new int[nIns];
						for(int j=0; j< nIns; j++) prev_layer_input[j] = train_X[n][j];
					} else {
						prev_layer_input = new int[hiddenLayerSizes[i-1]];
						for(int j=0; j< hiddenLayerSizes[i-1]; j++) prev_layer_input[j] = layer_input[j];
					}
					
					layer_input = new int[hiddenLayerSizes[i]];
					sigmoidLayers[i].sample_h_given_v(prev_layer_input, layer_input);
				}
				
				logLayer.train(layer_input, train_Y[n], lr);
			}
			// lr *= 0.95;
		}
	}
	
	public void predict(int[] x, double[] y) {
		double[] layer_input = new double[0];
		// int prev_layer_input_size;
		double[] prev_layer_input = new double[nIns];
		for(int j=0; j< nIns; j++) prev_layer_input[j] = x[j];
	
		double linear_output;
		
		
		// layer activation
		for(int i=0; i< nLayers; i++) {
			layer_input = new double[sigmoidLayers[i].n_out];
			
			for(int k=0; k< sigmoidLayers[i].n_out; k++) {
				linear_output = 0.0;
				
				for(int j=0; j< sigmoidLayers[i].n_in; j++) {
					linear_output += sigmoidLayers[i].W[k][j] * prev_layer_input[j];
				}
				linear_output += sigmoidLayers[i].b[k];
				layer_input[k] = sigmoid(linear_output);
			}
			
			if(i < nLayers -1) {
				prev_layer_input = new double[sigmoidLayers[i].n_out];
				for(int j=0; j< sigmoidLayers[i].n_out; j++) prev_layer_input[j] = layer_input[j];
			}
		}
		
		for(int i=0; i< logLayer.n_out; i++) {
			y[i] = 0;
			for(int j=0; j< logLayer.n_in; j++) {
				y[i] += logLayer.W[i][j] * layer_input[j];
			}
			y[i] += logLayer.b[i];
		}
		
		logLayer.softmax(y);
	}
	

	private static void test_sda() {
		Random rng = new Random(123);
		
		double pretrain_lr = 0.1;
		double corruption_level = 0.3;
		int pretraining_epochs = 1000;
		double finetune_lr = 0.1;
		int finetune_epochs = 500;

		int train_N = 10;
		int test_N = 4;
		int n_ins = 28;
		int n_outs = 2;
		int[] hidden_layer_sizes = {15, 15};
		int n_layers = hidden_layer_sizes.length;
		
		// training data
		int[][] train_X = {
			{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{1, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 0, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 1, 1, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1}
		};

		int[][] train_Y = {
			{1, 0},
			{1, 0},
			{1, 0},
			{1, 0},
			{1, 0},
			{0, 1},
			{0, 1},
			{0, 1},
			{0, 1},
			{0, 1}
		};
		
		// construct com.github.yusugomori.SdA
		StackedDenoisingAutoencoder sda = new StackedDenoisingAutoencoder(train_N, n_ins, hidden_layer_sizes, n_outs, n_layers, rng);
		
		// pretrain
		sda.pretrain(train_X, pretrain_lr, corruption_level, pretraining_epochs);
		
		// finetune
		sda.finetune(train_X, train_Y, finetune_lr, finetune_epochs);
		

		// test data
		int[][] test_X = {
			{1, 1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{1, 1, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1},
			{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1}
		};
		
		double[][] test_Y = new double[test_N][n_outs];
		
		// test
		for(int i=0; i<test_N; i++) {
			sda.predict(test_X[i], test_Y[i]);
			for(int j=0; j<n_outs; j++) {
				System.out.print(test_Y[i][j] + " ");
			}
			System.out.println();
		}
	}
	
	public static void main(String[] args) {
		test_sda();
	}
}
