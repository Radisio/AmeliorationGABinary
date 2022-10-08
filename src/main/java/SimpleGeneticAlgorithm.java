
import lombok.Data;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Data
public class SimpleGeneticAlgorithm {

    private static final double uniformRate = 0.5;
    private static final double mutationRate = 0.025;
    private static final int tournamentSize = 5;
    private static boolean elitism;
    private static byte[] solution = new byte[64];
    public static final int WHEEL = 1;
    public static final int TOURNAMENT = 2;
    public static final int BEST_FIT = 3;
    private BiFunction<Population, Integer, List<Individual>> evolveSelection;
    private BiFunction<Population, Integer, List<Individual>> parentSelection;
    public static int nbParents;
    public boolean runAlgorithm(int populationSize, String solution, int maxIterations)
    {
        return runAlgorithm(populationSize, solution, maxIterations, TOURNAMENT, 1.0/27.0, BEST_FIT);
    }

    public boolean runAlgorithm(int populationSize, String solution, int maxIterations, int evolveSelectionMethod, double percentageParentsToKeep, int parentsSelectionMethod) {
        if (solution.length() != SimpleGeneticAlgorithm.solution.length) {
            throw new RuntimeException("The solution needs to have " + SimpleGeneticAlgorithm.solution.length + " bytes");
        }
        setSolution(solution);
        setNbParents(populationSize, percentageParentsToKeep);
        Population myPop = new Population(populationSize, true);
        setEvolveSelection(evolveSelectionMethod);
        setParentSelection(parentsSelectionMethod);
        int generationCount = 1;
        while (myPop.getFittest().getFitness() < getMaxFitness() && generationCount<maxIterations) {
            System.out.println("Generation: " + generationCount + " Correct genes found: " + myPop.getFittest().getFitness());
            myPop = evolvePopulation(myPop);
            generationCount++;
        }
        if(generationCount==maxIterations)
            System.out.println("Limit of iterations reached !");
        else
            System.out.println("Solution found!");
        System.out.println("Generation: " + generationCount);
        System.out.println("Correct genes found : " + myPop.getFittest().getFitness());
        System.out.println("Genes: ");
        System.out.println(myPop.getFittest());
        return true;
    }

    public Population evolvePopulation(Population pop) {
        int elitismOffset;
        Population newPopulation = new Population(pop.getIndividuals().size(), false);

        if (elitism) {
            newPopulation.getIndividuals().addAll(parentSelection.apply(pop, nbParents));
            elitismOffset = nbParents;
        } else {
            elitismOffset = 0;
        }

        for (int i = elitismOffset; i < pop.getIndividuals().size(); i++) {
            //Individual indiv1 = tournamentSelection(pop);
            //Individual indiv2 = tournamentSelection(pop);
            Individual indiv1 = this.evolveSelection.apply(pop,1).get(0);
            Individual indiv2 = this.evolveSelection.apply(pop,1).get(0);
            Individual newIndiv = crossover(indiv1, indiv2);
            newPopulation.getIndividuals().add(i, newIndiv);
        }

        for (int i = elitismOffset; i < newPopulation.getIndividuals().size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }

    private Individual crossover(Individual indiv1, Individual indiv2) {
        Individual newSol = new Individual();
        for (int i = 0; i < newSol.getDefaultGeneLength(); i++) {
            if (Math.random() <= uniformRate) {
                newSol.setSingleGene(i, indiv1.getSingleGene(i));
            } else {
                newSol.setSingleGene(i, indiv2.getSingleGene(i));
            }
        }
        return newSol;
    }

    private void mutate(Individual indiv) {
        for (int i = 0; i < indiv.getDefaultGeneLength(); i++) {
            if (Math.random() <= mutationRate) {
                byte gene = (byte) Math.round(Math.random());
                indiv.setSingleGene(i, gene);
            }
        }
    }

    private List<Individual> wheelSelection(Population pop, int nb)
    {
        /// Compute total fitness
        int totalFitness = pop.getIndividuals().stream().map(x -> x.getFitness()).collect(Collectors.summingInt(Integer::intValue));
        /// return random EnumeratedDistribution
        List<Pair<Individual, Double>> individualWProba = new ArrayList<Pair<Individual, Double>>();
        pop.getIndividuals().forEach(x -> individualWProba.add(new Pair(x,(double)x.getFitness()/(double)totalFitness)));
        return (List<Individual>) (Object) Arrays.asList(new EnumeratedDistribution<Individual>(individualWProba).sample(nb));
    }

    private List<Individual> tournamentSelection(Population pop, int nb) {
        List<Individual> returnedList = new ArrayList<>();
        for (int j = 0; j<nb;j++) {
            Population tournament = new Population(tournamentSize, false);
            for (int i = 0; i < tournamentSize; i++) {
                int randomId = (int) (Math.random() * pop.getIndividuals().size());
                tournament.getIndividuals().add(i, pop.getIndividual(randomId));
            }
            returnedList.add(tournament.getFittest());
        }
        return returnedList;
    }
    private List<Individual> bestFitSelection(Population pop, int nb)
    {
        List<Individual> fittests = pop.getIndividuals();
        Collections.sort(fittests, Comparator.comparingInt(Individual::getFitness));
        Collections.reverse(fittests);
        fittests.subList(0, nb);
        return fittests;
    }

    protected static int getFitness(Individual individual) {
        int fitness = 0;
        for (int i = 0; i < individual.getDefaultGeneLength() && i < solution.length; i++) {
            if (individual.getSingleGene(i) == solution[i]) {
                fitness++;
            }
        }
        return fitness;
    }

    protected int getMaxFitness() {
        int maxFitness = solution.length;
        return maxFitness;
    }

    protected void setSolution(String newSolution) {
        solution = new byte[newSolution.length()];
        for (int i = 0; i < newSolution.length(); i++) {
            String character = newSolution.substring(i, i + 1);
            if (character.contains("0") || character.contains("1")) {
                solution[i] = Byte.parseByte(character);
            } else {
                solution[i] = 0;
            }
        }
    }

    protected void setEvolveSelection(int selectionType)
    {
        this.evolveSelection = getMethod(selectionType);
    }

    protected void setParentSelection(int selectionType)
    {
        this.parentSelection = getMethod(selectionType);
    }


    protected BiFunction<Population, Integer, List<Individual>> getMethod(int selectionType)
    {
        switch(selectionType)
        {
            case WHEEL:
            {
                return this::wheelSelection;
            }
            case TOURNAMENT:
            {
                return this::tournamentSelection;
            }
            default:
                return this::bestFitSelection;

        }
    }
    protected void setNbParents(int populationSize ,double percentage)
    {
        if(percentage>0.0) {
            this.nbParents = (int) Math.round(populationSize * percentage);
            elitism=true;
        }
        else{
            nbParents = -1;
            elitism=false;
        }
    }

}