import org.opt4j.core.Individual
import org.opt4j.core.Objective
import org.opt4j.core.Objectives
import org.opt4j.core.genotype.DoubleGenotype
import org.opt4j.core.optimizer.Archive
import org.opt4j.core.problem.Creator
import org.opt4j.core.problem.Decoder
import org.opt4j.core.problem.Evaluator
import org.opt4j.core.problem.ProblemModule
import org.opt4j.core.start.Opt4JTask
import org.opt4j.optimizers.ea.EvolutionaryAlgorithmModule
import org.opt4j.optimizers.mopso.MOPSOModule
import org.opt4j.viewer.ViewerModule

/**
 * genotype - we'll use the DoubleGenotype, it's basically List<Double>, perfect for us
 * creator - for heuristic learning from scratch randoming the weights will be fine, starting from pre-backpropagated
 *   weights will be more tricky because of the module-based architecture
 * phenotype - that could actually be a neural network with weights from the genotype
 * decoder - it will construct the nwtork using given weights
 * evaluator - it will, guess what, evaluate the phenotype (network)
 * objective - I'm thinking single numerical objective, error or f-score or something like that
 *   minimized or maximized dependign on what value we use
 *
 * PSO and EA are already implemented in opt4j, we can modify mutProb, crossProb etc, it seems good enough
 *
 * Our asses were smart enough not to write anything about custom genetic algorithm in the
 * topic declaration, so fuck that with a ten foot stick
 *
 *
 * Now, about this example script - I implemented the 2d rastrigin function optimization problem,
 * as described in http://en.wikipedia.org/wiki/Rastrigin_function
 *
 * classes/modules should be self-explanatory with the above introduction
 *
 * near the bottom of the file we've got the meat - initializing problem, optimizer and running that shit
 *
 * I'll mark lines in which you can choose whether to use EA or PSO, just (un)comment the correct line
 * We've also got that awesome viewer, it will provide some tasty graphs for presentations and that god-forsaken paper
 */

class RastriginCreator implements Creator<DoubleGenotype> {

    Random random = new Random()
    @Override
    DoubleGenotype create() {
        DoubleGenotype genotype = new DoubleGenotype(-5.12, 5.12)
        genotype.init(random, 2)
        return genotype
    }
}

class RastriginDecoder implements Decoder<DoubleGenotype, Double>{

    @Override
    Double decode(DoubleGenotype genotype) {
        double x = genotype.get(0)
        double y = genotype.get(1)
        return 20 + x*x - 10*Math.cos(2*Math.PI*x) + y*y - 10*Math.cos(2*Math.PI*y)
    }
}

class RastriginEvaluator implements Evaluator<Double>{

    @Override
    Objectives evaluate(Double phenotype) {
        Objectives objectives = new Objectives()
        objectives.add('objective', Objective.Sign.MIN, phenotype.doubleValue())
        return objectives
    }
}


class RastriginModule extends ProblemModule{

    @Override
    protected void config() {
        bindProblem(RastriginCreator.class, RastriginDecoder.class, RastriginEvaluator.class)
    }
}



EvolutionaryAlgorithmModule ea = new EvolutionaryAlgorithmModule()
ea.generations = 100
ea.alpha = 50

MOPSOModule pso = new MOPSOModule()
pso.iterations = 100
pso.particles = 50



RastriginModule rastr = new RastriginModule()
ViewerModule viewer = new ViewerModule();
viewer.setCloseOnStop(false);
Opt4JTask task = new Opt4JTask(false);

/**
 * below be choosing point, young parawan, it is, mhm
 */
//task.init(ea,rastr, viewer)
task.init(pso, rastr, viewer)

try {
    task.execute();  // <- whole optimization happens here
    Archive archive = task.getInstance(Archive.class);    // in archive we've got the best specimen
    for (Individual individual : archive) {
        println individual.objectives.values
        println individual.genotype
        println individual.phenotype
        println()
    }
} catch (Exception e) {
    e.printStackTrace();
} finally {
    task.close();
}

