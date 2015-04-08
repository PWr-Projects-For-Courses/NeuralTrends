package com.github.fm_jm.neuraltrends.autoencoders

import com.github.yusugomori.AutoEncoderData
import com.github.yusugomori.DenoisingAutoencoderTest
import com.github.yusugomori.StackedAutoencoderTest

import groovy.util.logging.Slf4j

@Slf4j("bard")
class ThresholdCategoryTest extends GroovyTestCase {
    void testThreshold() {
        assert ThresholdCategory.threshold([0.7, 0.1, 0.0, 1.0, 0.5, 0.4]) == [1, 0, 0, 1, 1, 0].toArray()
    }

    void testReconstruct() {
        def da = DenoisingAutoencoderTest.getTrainedAutoencoder()
        use(ThresholdCategory){
            AutoEncoderData.testInput.each { int[] x ->
                bard.info("${da.reconstruct(x)}")
            }
        }
    }

    void testPredict(){
        def sda = StackedAutoencoderTest.getStackedAutoencoder()
        use(ThresholdCategory){
            AutoEncoderData.stackedTestInput.each { int[] x ->
                bard.info("${sda.predict(x)}")
            }
        }
    }
}
