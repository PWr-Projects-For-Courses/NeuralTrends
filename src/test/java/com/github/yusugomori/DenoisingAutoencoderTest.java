package com.github.yusugomori;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.logging.SimpleFormatter;

public class DenoisingAutoencoderTest {
    Logger logger = LoggerFactory.getLogger(DenoisingAutoencoderTest.class);

    @Test
    public void testRun() {
        Random rng = new Random(123);

        double learning_rate = 0.1;
        double corruption_level = 0.3;
        int training_epochs = 100;

        int train_N = 10;
        int test_N = 2;
        int n_visible = 20;
        int n_hidden = 5;

        int[][] train_X = {
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 0}
        };

        DenoisingAutoencoder da = new DenoisingAutoencoder(train_N, n_visible, n_hidden, null, null, null, rng);

        // train
        for(int epoch=0; epoch<training_epochs; epoch++) {
            for(int i=0; i<train_N; i++) {
                da.train(train_X[i], learning_rate, corruption_level);
            }
        }

        // test data
        int[][] test_X = {
                {1, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 1, 1, 0}
        };

        double[][] reconstructed_X = new double[test_N][n_visible];

        // test
        for(int i=0; i<test_N; i++) {
            da.reconstruct(test_X[i], reconstructed_X[i]);
            StringBuffer buffer = new StringBuffer();
            for(int j=0; j<n_visible; j++) {
                buffer.append(String.format("%.5f ", reconstructed_X[i][j]));
            }
            logger.info(buffer.toString());
        }
    }
}