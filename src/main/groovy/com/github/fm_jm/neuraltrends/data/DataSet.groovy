package com.github.fm_jm.neuraltrends.data

import groovy.transform.Canonical

@Canonical
class DataSet {

    static enum Type{
        TRAIN,
        TEST
    }

    int[][] inputs
    int[][] outputs

    public int inputSize(){
        inputs[0].size()
    }

    public int outputSize(){
        outputs[0].size()
    }

    public int size(){
        inputs.length
    }
}
