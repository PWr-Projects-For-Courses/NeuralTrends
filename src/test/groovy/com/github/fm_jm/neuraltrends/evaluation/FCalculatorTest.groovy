package com.github.fm_jm.neuraltrends.evaluation

import com.github.fm_jm.neuraltrends.BasicNetworkCategory
import com.github.fm_jm.neuraltrends.EncogEncoderTest

import groovy.util.logging.Slf4j

@Slf4j("bard")
class FCalculatorTest extends GroovyTestCase {
    void testForEncoder(){
        def network = EncogEncoderTest.getTrainedNetwork(20)
        use (BasicNetworkCategory) {
            int[][] results = EncogEncoderTest.data.collect {
                network.activate(it)
            } as int[][]
            bard.info "F: ${FCalculator.F(EncogEncoderTest.data, results)}"
        }
    }
}
