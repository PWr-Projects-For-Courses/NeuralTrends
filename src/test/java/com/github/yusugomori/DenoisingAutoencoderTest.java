package com.github.yusugomori;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class DenoisingAutoencoderTest {
    Logger logger = LoggerFactory.getLogger(DenoisingAutoencoderTest.class);

    static public int n_visible = 20;
    static public int n_hidden = 5;

    static public DenoisingAutoencoder getTrainedAutoencoder(){
        Random rng = new Random(123);

        double learning_rate = 0.1;
        double corruption_level = 0.3;
        int training_epochs = 100;

        DenoisingAutoencoder da = new DenoisingAutoencoder(AutoEncoderData.train_N, n_visible, n_hidden, null, null, null, rng);

        // train
        for(int epoch=0; epoch<training_epochs; epoch++) {
            for(int i=0; i<AutoEncoderData.train_N; i++) {
                da.train(AutoEncoderData.trainingInput[i], learning_rate, corruption_level);
            }
        }
        return da;
    }

    @Test
    public void testRun() {
        DenoisingAutoencoder da = getTrainedAutoencoder();

        double[][] reconstructed_X = new double[AutoEncoderData.test_N][n_visible];

        // test
        for(int i=0; i<AutoEncoderData.test_N; i++) {
            da.reconstruct(AutoEncoderData.testInput[i], reconstructed_X[i]);
            StringBuffer buffer = new StringBuffer();
            for(int j=0; j<n_visible; j++) {
                buffer.append(String.format("%.5f ", reconstructed_X[i][j]));
            }
            logger.info(buffer.toString());
        }
    }
}