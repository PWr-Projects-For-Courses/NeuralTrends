package com.github.fm_jm.neuraltrends.data

import groovy.transform.Canonical

@Canonical
class Normalizer {
    double from
    double to

    double normalize(double d){
        assert d>=from && d<=to
        (d-from)/(to-from)
    }

    static double normalize(double d, double from, double to){
        new Normalizer(from, to).normalize(d)
    }

    static final Normalizer dummyNormalizer = new Normalizer(0.0, 1.0)
}
