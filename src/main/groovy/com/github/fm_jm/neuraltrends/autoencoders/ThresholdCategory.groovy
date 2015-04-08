package com.github.fm_jm.neuraltrends.autoencoders

import com.github.yusugomori.DenoisingAutoencoder
import com.github.yusugomori.StackedDenoisingAutoencoder


class ThresholdCategory {
    static int[] threshold(Iterable<Double> toCut){
        threshold((double[])toCut.toList().toArray())
    }
    static int[] threshold(double[] toCut){
        toCut.collect { it>=0.5 ? 1 :0 }.toArray()
    }

    static int[] reconstruct(DenoisingAutoencoder autoencoder, int[] x){
        double[] out = new double[x.length];
        autoencoder.reconstruct(x, out)
        threshold(out)
    }

    static int[] predict(StackedDenoisingAutoencoder autoencoder, int[] x){
        double[] out = new double[autoencoder.nOuts];
        autoencoder.predict(x, out)
        threshold(out)
    }
}
