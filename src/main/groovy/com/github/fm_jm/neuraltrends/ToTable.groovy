package com.github.fm_jm.neuraltrends

import com.github.fm_jm.neuraltrends.evaluation.MongoWrapper
import com.github.fm_jm.neuraltrends.evaluation.Results

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
    static multipliers = [0.75]
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


    static void baseline() {
        println "Baseline"
        println characters.openTable
        printer "epochs", "eval"
        epochs.each{ int e ->
            int gen = 0
            if (e)
                foldNos.each { int fold ->
                    populations.each { int pop ->
                        heuristics.each { String h ->
                            (h == "ea" ? crossRates : [0]).each { cp ->
                                Results results = Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                                printer e, results?.f ?: "N/A"
                            }
                        }
                    }

                }
        }
        println characters.closeTable
    }

    static void ea() {
        println "EA"
        println characters.openTable
        printer "generations", "population", "cp", "eval"
        int e = 0
        String h = "ea"
        generations.each{ int gen ->
            if (gen)
                foldNos.each { int fold ->
                    populations.each { int pop ->
                        crossRates.each { cp ->
                            Results results = Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                            printer gen,
                                pop,
                                cp,
                                results?.f ?: "N/A"
                        }
                    }

                }
        }
        println characters.closeTable
    }

    static void pso() {
        println "PSO"
        println characters.openTable
        printer "generations", "population", "eval"
        int e = 0
        String h = "pso"
        double cp = 0
        generations.each{ int gen ->
            if (gen)
                foldNos.each { int fold ->
                    populations.each { int pop ->
                        Results results = Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                        printer gen,
                            pop,
                            cp,
                            results?.f ?: "N/A"
                    }

                }
        }
        println characters.closeTable
    }

    static void bp_ea() {
        println "BP+EA"
        println characters.openTable
        printer "epochs", "generations", "population", "cp", "eval"
        String h = "ea"
        generations.each{ int gen ->
            epochs.each{ int e ->
                if (e) {
                    if (gen)
                        foldNos.each { int fold ->
                            populations.each { int pop ->
                                crossRates.each { cp ->
                                    Results results = Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                                    printer e,
                                        gen,
                                        pop,
                                        cp,
                                        results?.f ?: "N/A"
                                }
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
        printer "epochs", "generations", "population", "eval"
        String h = "pso"
        double cp = 0
        generations.each{ int gen ->
            epochs.each{ int e ->
                if (e) {
                    if (gen)
                        foldNos.each { int fold ->
                            populations.each { int pop ->
                                Results results = Results.retrieve(collection, getKey(fold, e, gen, pop, h, cp))
                                printer e,
                                    gen,
                                    pop,
                                    results?.f ?: "N/A"
                            }


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