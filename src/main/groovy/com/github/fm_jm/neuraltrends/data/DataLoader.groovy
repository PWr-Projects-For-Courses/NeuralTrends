package com.github.fm_jm.neuraltrends.data



class DataLoader {

    final static int ATTRIBUTES = 499



    static DataSet getDataSet(int foldNo, DataSet.Type type){
        String suffix = type == DataSet.Type.TEST ? "tst" : "tra"
        getDataSet("corel5k-5-${foldNo}${suffix}.dat")
    }

    static DataSet getDataSet(String filename){
        def inputs = []
        def outputs = []
        URL url = this.classLoader.getResource(filename)
        File file = new File(url.file)
        file.eachLine {String line ->
            if (!(line.startsWith("@") || line.empty)){
                def splitted = line.split(",")
                inputs.add(splitted.getAt(0..ATTRIBUTES-1).collect {it.toInteger()})
                outputs.add(splitted.getAt(ATTRIBUTES..splitted.size()-1).collect {it.toInteger()})
            }
        }
        new DataSet(inputs as int[][], outputs as int[][])
    }

}