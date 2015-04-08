package com.github.yusugomori;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class StackedAutoencoderTest {
    Logger logger = LoggerFactory.getLogger(StackedAutoencoderTest.class);

    @Test
    public void testRun(){
        Random rng = new Random(123);

        double pretrain_lr = 0.1;
        double corruption_level = 0.3;
        int pretraining_epochs = 1000;
        double finetune_lr = 0.1;
        int finetune_epochs = 500;

        int train_N = 10;
        int test_N = 4;
        int n_visible = 28;
        int n_output = 2;
        int[] n_hidden = {30, 20, 10};
        int hiddenCnt = n_hidden.length;

        StackedDenoisingAutoencoder sda =
                new StackedDenoisingAutoencoder(train_N, n_visible, n_hidden, n_output, hiddenCnt, rng);

        sda.pretrain(AutoEncoderData.stackedTrainingInput, pretrain_lr, corruption_level, pretraining_epochs);

        sda.finetune(AutoEncoderData.stackedTrainingInput, AutoEncoderData.stackedTrainingOutput, finetune_lr, finetune_epochs);

        double[][] test_Y = new double[test_N][n_output];

        for(int i=0; i<test_N; i++) {
            sda.predict(AutoEncoderData.stackedTestInput[i], test_Y[i]);
            StringBuffer buffer = new StringBuffer();
            for(int j=0; j<n_output; j++) {
                buffer.append(String.format("%.5f ", test_Y[i][j]));
            }
            logger.info(buffer.toString());
        }

    }

}
