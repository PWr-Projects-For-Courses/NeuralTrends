package com.github.fm_jm.neuraltrends

import org.encog.engine.network.activation.ActivationFunction
import org.encog.engine.network.activation.ActivationSigmoid


class Changable {
    static def folds = [1, 2, 3, 4, 5]
//    static def folds = [1, 2]
//    static def folds = [3]
//    static def folds = [4, 5]

    static int poolSize = 4
//    static String collectionPrefix = "fm_test_"
    static String collectionPrefix = ""

    static ActivationFunction getActivationFunction(){
        new ActivationSigmoid()
    }
}
