package com.github.fm_jm.neuraltrends.data

import java.util.regex.Matcher
import java.util.regex.Pattern


class DataLoader {

//    final static int ATTRIBUTES = 499
    final static int ATTRIBUTES = 72
//    final static int ATTRIBUTES = 294


    static DataSet getDataSet(int foldNo, DataSet.Type type){
        String suffix = type == DataSet.Type.TEST ? "tst" : "tra"
//        getDataSet("corel5k-5-${foldNo}${suffix}.dat")
//        getDataSet("emotions-5-${foldNo}${suffix}.dat")
//        getDataSet("scene-5-${foldNo}${suffix}.dat")
        getNormalizedDataSet("emotions-5-${foldNo}${suffix}.dat")
    }

    static DataSet getDataSet(String filename){
        def inputs = []
        def outputs = []
        URL url = this.classLoader.getResource(filename)
        File file = new File(url.file)
        file.eachLine {String line ->
            if (!(line.startsWith("@") || line.empty)){
                def splitted = line.split(",")
                inputs.add(splitted.getAt(0..ATTRIBUTES-1).collect {it.toDouble()})
                outputs.add(splitted.getAt(ATTRIBUTES..splitted.size()-1).collect {it.toInteger()})
            }
        }
        new DataSet(inputs as double[][], outputs as int[][])
    }
    static Pattern pattern = Pattern.compile("[\\[]([-]?\\d+[.]\\d+),([-]?\\d+[.]\\d+)[\\]]")
    static Normalizer getNormalizer(String domainStr){
        Matcher m = pattern.matcher(domainStr)
        m.find()
        if (m.matches()){
            new Normalizer(m.group(1).toDouble(), m.group(2).toDouble())
        } else
            Normalizer.dummyNormalizer
    }

    static DataSet getNormalizedDataSet(String filename){
        def inputs = []
        def outputs = []
        URL url = this.classLoader.getResource(filename)
        File file = new File(url.file)
        List<Normalizer> normalizers = []
        file.eachLine {String line ->
            if (!(line.startsWith("@") || line.empty)){
                def splitted = line.split(",")
                int idx = 0
                inputs.add(splitted.getAt(0..ATTRIBUTES-1).collect {normalizers[idx++].normalize(it.toDouble())})
                outputs.add(splitted.getAt(ATTRIBUTES..splitted.size()-1).collect {it.toInteger()})
            } else if (line?.startsWith("@attribute")){
                def domainStr = line.split(" ").last()
                normalizers << getNormalizer(domainStr)
            }

        }
        new DataSet(inputs[0..10] as double[][], outputs[0..10] as int[][])
    }
}
