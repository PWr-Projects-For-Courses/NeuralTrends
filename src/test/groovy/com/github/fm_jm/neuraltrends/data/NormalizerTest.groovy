package com.github.fm_jm.neuraltrends.data


class NormalizerTest extends GroovyTestCase {
    void testNormalize() {
        assert Normalizer.normalize(0.6, 0.0, 2.0) == 0.3
        assert Normalizer.normalize(0.0, 0.0, 2.0) == 0.0
        assert Normalizer.normalize(2.0, 0.0, 2.0) == 1.0
        assert Normalizer.normalize(0.0, -1.0, 1.0) == 0.5
        assert Normalizer.normalize(1.0, -1.0, 1.0) == 1.0
        assert Normalizer.normalize(-1.0, -1.0, 1.0) == 0.0
        def caught
        try {
            Normalizer.normalize(-1.0, 0.0, 2.0)
        } catch (Throwable t){
            caught = t
        }
        assert caught
    }
}
