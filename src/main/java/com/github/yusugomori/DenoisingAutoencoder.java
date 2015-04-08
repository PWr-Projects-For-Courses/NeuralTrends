package com.github.yusugomori;

import java.util.Random;

public class DenoisingAutoencoder {
	public int n;
	public int nVisible;
	public int nHidden;
	public double[][] weights;
	public double[] hBias;
	public double[] vBias;
	public Random random;
	
	
	public double uniform(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}
	
	public int binomial(int n, double p) {
		if(p < 0 || p > 1) return 0;
		
		int c = 0;
		double r;
		
		for(int i=0; i<n; i++) {
			r = random.nextDouble();
			if (r < p) c++;
		}
		
		return c;
	}
	
	public static double sigmoid(double x) {
		return 1.0 / (1.0 + Math.pow(Math.E, -x));
	}

	public DenoisingAutoencoder(int n, int nVisible, int nHidden,
								double[][] weights, double[] hBias, double[] vBias, Random random) {
		this.n = n;
		this.nVisible = nVisible;
		this.nHidden = nHidden;

		if(random == null)	this.random = new Random(1234);
		else this.random = random;
				
		if(weights == null) {
			this.weights = new double[this.nHidden][this.nVisible];
			double a = 1.0 / this.nVisible;
			
			for(int i=0; i<this.nHidden; i++) {
				for(int j=0; j<this.nVisible; j++) {
					this.weights[i][j] = uniform(-a, a);
				}
			}	
		} else {
			this.weights = weights;
		}
		
		if(hBias == null) {
			this.hBias = new double[this.nHidden];
			for(int i=0; i<this.nHidden; i++) this.hBias[i] = 0;
		} else {
			this.hBias = hBias;
		}
		
		if(vBias == null) {
			this.vBias = new double[this.nVisible];
			for(int i=0; i<this.nVisible; i++) this.vBias[i] = 0;
		} else {
			this.vBias = vBias;
		}	
	}
	
	public void get_corrupted_input(int[] x, int[] tilde_x, double p) {
		for(int i=0; i< nVisible; i++) {
			if(x[i] == 0) {
				tilde_x[i] = 0;
			} else {
				tilde_x[i] = binomial(1, p);
			}
		}
	}
	
	// Encode
	public void get_hidden_values(int[] x, double[] y) {
		for(int i=0; i< nHidden; i++) {
			y[i] = 0;
			for(int j=0; j< nVisible; j++) {
				y[i] += weights[i][j] * x[j];
			}
			y[i] += hBias[i];
			y[i] = sigmoid(y[i]);
		}
	}
	
	// Decode
	public void get_reconstructed_input(double[] y, double[] z) {
		for(int i=0; i< nVisible; i++) {
			z[i] = 0;
			for(int j=0; j< nHidden; j++) {
				z[i] += weights[j][i] * y[j];
			}
			z[i] += vBias[i];
			z[i] = sigmoid(z[i]);
		}
	}
	
	public void train(int[] x, double lr, double corruption_level) {
		int[] tilde_x = new int[nVisible];
		double[] y = new double[nHidden];
		double[] z = new double[nVisible];
		
		double[] L_vbias = new double[nVisible];
		double[] L_hbias = new double[nHidden];
		
		double p = 1 - corruption_level;
		
		get_corrupted_input(x, tilde_x, p);
		get_hidden_values(tilde_x, y);
		get_reconstructed_input(y, z);
		
		// vBias
		for(int i=0; i< nVisible; i++) {
			L_vbias[i] = x[i] - z[i];
			vBias[i] += lr * L_vbias[i] / n;
		}
		
		// hBias
		for(int i=0; i< nHidden; i++) {
			L_hbias[i] = 0;
			for(int j=0; j< nVisible; j++) {
				L_hbias[i] += weights[i][j] * L_vbias[j];
			}
			L_hbias[i] *= y[i] * (1 - y[i]);
			hBias[i] += lr * L_hbias[i] / n;
		}
		
		// weights
		for(int i=0; i< nHidden; i++) {
			for(int j=0; j< nVisible; j++) {
				weights[i][j] += lr * (L_hbias[i] * tilde_x[j] + L_vbias[j] * y[i]) / n;
			}
		}
	}
	
	public void reconstruct(int[] x, double[] z) {
		double[] y = new double[nHidden];
		
		get_hidden_values(x, y);
		get_reconstructed_input(y, z);
	}	
}
