package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MongoWrapper
import com.github.fm_jm.neuraltrends.evaluation.Results
import org.apache.commons.math3.stat.StatUtils

class ToTable {

    static def CSV = [
        openTable: "",
        closeTable: "\n\n",
        separator: ";",
        newline: ""
    ]

    static def LATEX = [
        openTable: "\\begin{table}[H] \n\\begin{tabular} \n",
        closeTable: "\n\\end{table} \n\\end{tabular} \n\n",
        separator: "&",
        newline: "\\\\"
    ]

    static def characters = CSV

    static def collection = "results_new"

    static def printer(Object... args){
        println args.join(characters.separator) + characters.newline
    }

    static heuristics = ["ea", "pso"]
    static foldNos = (1..5)
    static generations = [0, 100, 200, 500]
    static populations = [100, 200, 500]
    static epochs = [0, 1000, 2000, 3000]
    static crossRates = [0.5, 0.75, 0.9]

    static Map getKey(int foldNo, int epochs, int generations, int population, String heuristic,
                      double crossRate = 0){
        Map heuristicParams = [generations: generations, popSize: population]
        if (crossRate != 0)
            heuristicParams.cross = crossRate
        [foldNo: foldNo, layers: 0, epochs: epochs, heuristic: heuristic, params: heuristicParams]
    }

    static Map<String, String> getStats(results){
        results ? [
            mean: StatUtils.mean(results),
            stdDev: Math.sqrt(StatUtils.variance(results))
        ] : [ mean: "N/A", stdDev: "N/A" ]
    }


    static void baseline() {
        println "Baseline"
        println characters.openTable
        printer "epochs", "mean", "stdDev"
        epochs.each{ int e ->
            int gen = 0
            if (e) {
                def res = []
                foldNos.each { int fold ->
                    populations.each { int pop ->
                        heuristics.each { String h ->
                            (h == "ea" ? crossRates : [0]).each { cp ->
                                res << Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                            }
                        }
                    }

                }
                def stats = getStats(res.findAll().collect { it.f } as double[])
                printer e, stats.mean, stats.stdDev
            }
        }
        println characters.closeTable
    }

    static void ea() {
        println "EA"
        println characters.openTable
        printer "generations", "population", "cp", "mean", "stdDev"
        int e = 0
        String h = "ea"
        generations.each { int gen ->
            if (gen) {
                populations.each { int pop ->
                    crossRates.each { cp ->
                        def res = []
                        foldNos.each { int fold ->
                            res << Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                        }
                        def stats = getStats(res.findAll().collect { it.f } as double[])
                        printer gen,
                            pop,
                            cp,
                            stats.mean, stats.stdDev
                    }

                }
            }
        }
        println characters.closeTable
    }

    static void pso() {
        println "PSO"
        println characters.openTable
        printer "generations", "population", "mean", "stdDev"
        int e = 0
        String h = "pso"
        double cp = 0
        generations.each{ int gen ->
            if (gen) {
                populations.each { int pop ->
                    def res = []
                    foldNos.each { int fold ->
                        res << Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                    }
                    def stats = getStats(res.findAll().collect { it.f } as double[])
                    printer gen,
                        pop,
                        stats.mean, stats.stdDev
                }
            }
        }
        println characters.closeTable
    }

    static void bp_ea() {
        println "BP+EA"
        println characters.openTable
        printer "epochs", "generations", "population", "cp", "mean", "stdDev"
        String h = "ea"
        generations.each{ int gen ->
            epochs.each{ int e ->
                if (e*gen) {
                    populations.each { int pop ->
                        crossRates.each { cp ->
                            def res = []
                            foldNos.each { int fold ->
                                res << Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                            }
                            def stats = getStats(res.findAll().collect { it.f } as double[])
                            printer e,
                                gen,
                                pop,
                                cp,
                                stats.mean, stats.stdDev
                        }


                    }
                }
            }
        }
        println characters.closeTable
    }

    static void bp_pso() {
        println "BP+PSO"
        println characters.openTable
        printer "epochs", "generations", "population", "mean", "stdDev"
        String h = "pso"
        double cp = 0
        generations.each{ int gen ->
            epochs.each{ int e ->
                if (e*gen) {
                    populations.each { int pop ->
                        def res = []
                        foldNos.each { int fold ->
                            res << Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))

                        }
                        def stats = getStats(res.findAll().collect { it.f } as double[])
                        printer e,
                            gen,
                            pop,
                            stats.mean, stats.stdDev


                    }
                }
            }
        }
        println characters.closeTable
    }

    static void main(String[] args){
        baseline()
        ea()
        pso()
        bp_ea()
        bp_pso()
        MongoWrapper.close()
    }

}