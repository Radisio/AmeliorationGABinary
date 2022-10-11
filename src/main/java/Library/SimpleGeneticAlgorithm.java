package Library;

import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Data
public class SimpleGeneticAlgorithm {

    private double uniformRate = 0.5;
    private double allOrNoneRate = 0.25;
    private double mutationFlipRate = 0.025;
    private double mutationAddRate = 0.025;
    private double mutationSubRate = 0.025;
    private int tournamentSize = 5;
    private static boolean elitism;
    private static byte[] solution;
    public static final int WHEEL = 1;
    public static final int TOURNAMENT = 2;
    public static final int BEST_FIT = 3;
    public static final int CROSS_OVER_ALL_OR_NONE = 1;
    public static final int CROSS_OVER_CASE_BY_CASE = 2;
    public static final int CROSS_OVER_KEEP_FROM_BEST = 3;
    private BiFunction<Population, Integer, List<Individual>> evolveSelection;
    private BiFunction<Population, Integer, List<Individual>> parentSelection;
    private BiFunction<Individual, Individual, Individual> crossOverMethod;
    private static int nbParents;
    public boolean runAlgorithm(int populationSize, String solution, int maxIterations)
    {
        return runAlgorithm(populationSize, solution, maxIterations, TOURNAMENT, 1.0/27.0, BEST_FIT, CROSS_OVER_ALL_OR_NONE);
    }

    public boolean runAlgorithm(int populationSize, String solution, int maxIterations, int evolveSelectionMethod, double percentageParentsToKeep, int parentsSelectionMethod,
                                int crossOverMethod) {
       /* if (solution.length() != Library.SimpleGeneticAlgorithm.solution.length) {
            throw new RuntimeException("The solution needs to have " + Library.SimpleGeneticAlgorithm.solution.length + " bytes");
        }*/
        setCrossOverMethod(crossOverMethod);
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
            Individual indiv1 = this.evolveSelection.apply(pop,1).get(0);
            Individual indiv2 = this.evolveSelection.apply(pop,1).get(0);
            Individual newIndiv = this.crossOverMethod.apply(indiv1, indiv2);
            newPopulation.getIndividuals().add(i, newIndiv);
        }

        for (int i = elitismOffset; i < newPopulation.getIndividuals().size(); i++) {
            mutate(newPopulation.getIndividual(i));
        }

        return newPopulation;
    }

    private Individual crossover(Individual indiv1, Individual indiv2, int size) {
        List<Byte> bytes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (Math.random() <= uniformRate) {
                bytes.add(indiv1.getSingleGene(i));
            } else {
                bytes.add(indiv2.getSingleGene(i));
            }
        }
        return new Individual(ArrayUtils.toPrimitive(bytes.toArray(new Byte[bytes.size()])));
    }

    private Individual crossoverAllOrNone(Individual indiv1, Individual indiv2)
    {
        if(indiv1.geneLength == indiv2.geneLength)
        {
            return crossover(indiv1, indiv2, indiv1.geneLength);
        }
        else{
            int sizeMax = indiv1.geneLength;
            byte[] bytes = indiv1.getGenes();
            int sizeMin = indiv2.geneLength;

            if(indiv1.geneLength < indiv2.geneLength) {
                sizeMax = indiv2.geneLength;
                bytes = indiv2.getGenes();
                sizeMin = indiv1.geneLength;
            }
            List<Byte> newGenes = new ArrayList<>();
            for(int i = 0 ; i< sizeMin;i++)
            {
                if (Math.random() <= uniformRate) {
                    newGenes.add(indiv1.getSingleGene(i));
                } else {
                    newGenes.add(indiv2.getSingleGene(i));
                }
            }
            if(Math.random() <= allOrNoneRate)
            {
                /// All
                for(int i = sizeMin; i<sizeMax;i++)
                {
                    newGenes.add(bytes[i]);
                }
            }
            return new Individual(ArrayUtils.toPrimitive(newGenes.toArray(new Byte[newGenes.size()])));
        }

    }

    private Individual crossOverCaseByCase(Individual indiv1, Individual indiv2)
    {
        if(indiv1.geneLength == indiv2.geneLength)
        {
            return crossover(indiv1, indiv2, indiv1.geneLength);
        }
        else{
            int sizeMax = indiv1.geneLength;
            byte[] bytes = indiv1.getGenes();
            int sizeMin = indiv2.geneLength;

            if(indiv1.geneLength < indiv2.geneLength) {
                sizeMax = indiv2.geneLength;
                bytes = indiv2.getGenes();
                sizeMin = indiv1.geneLength;
            }
            List<Byte> newGenes = new ArrayList<>();
            for(int i = 0 ; i< sizeMin;i++)
            {
                if (Math.random() <= uniformRate) {
                    newGenes.add(indiv1.getSingleGene(i));
                } else {
                    newGenes.add(indiv2.getSingleGene(i));
                }
            }
            for(int i = sizeMin, j= sizeMin; i<sizeMax;i++)
            {
                if(Math.random() <= uniformRate) {
                    newGenes.add(bytes[i]);
                    j++;
                }
            }

            return new Individual(ArrayUtils.toPrimitive(newGenes.toArray(new Byte[newGenes.size()])));
        }
    }

    private Individual crossOverKeepFromBest(Individual indiv1, Individual indiv2)
    {
        if(indiv1.geneLength == indiv2.geneLength)
        {
            return crossover(indiv1, indiv2, indiv1.geneLength);
        }
        else{
            int fitnessIndiv1 = indiv1.getFitness();
            int fitnessIndiv2 = indiv2.getFitness();
            int sizeMax = indiv1.geneLength;
            byte[] bytes = indiv1.getGenes();
            int sizeMin = indiv2.geneLength;
            if(fitnessIndiv1 == fitnessIndiv2)
            {
                return crossOverCaseByCase(indiv1, indiv2);
            }
            if(fitnessIndiv1 > fitnessIndiv2)
            {
                if(indiv1.getGeneLength() < indiv2.getGeneLength())
                {
                    sizeMax = -1;
                    sizeMin = indiv1.geneLength;
                }
            }
            else{
                if(indiv1.getGeneLength() > indiv2.getGeneLength())
                {
                    sizeMax = -1;
                }
                else{
                    sizeMax = indiv2.getGeneLength();
                    sizeMin = indiv1.getGeneLength();
                    bytes = indiv2.getGenes();
                }
            }
            List<Byte> newGenes = new ArrayList<>();
            for(int i = 0 ; i< sizeMin;i++)
            {
                if (Math.random() <= uniformRate) {
                    newGenes.add(indiv1.getSingleGene(i));
                } else {
                    newGenes.add(indiv2.getSingleGene(i));
                }
            }
            for(int i = sizeMin, j= sizeMin; i<sizeMax;i++)
            {
                if(Math.random() <= uniformRate) {
                    newGenes.add(bytes[i]);
                    j++;
                }
            }

            return new Individual(ArrayUtils.toPrimitive(newGenes.toArray(new Byte[newGenes.size()])));
        }
    }

    private byte flip(byte b)
    {
        return b==1 ? (byte)0:(byte)1;
    }

    private void mutate(Individual indiv) {
        for (int i = 0; i < indiv.getGeneLength(); i++) {
            if (Math.random() <= mutationFlipRate) {
                byte gene = flip(indiv.getSingleGene(i));
                indiv.setSingleGene(i, gene);
            }
        }
        if (Math.random() <= mutationAddRate) {
            indiv.addGene((int)(Math.round(Math.random() * indiv.geneLength)),(byte)(Math.round(Math.random())));
        }
        if (Math.random() <= mutationSubRate) {
            indiv.subGene((int)(Math.round(Math.random() * (indiv.geneLength-1))));
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
        for (int i = 0; i < individual.getGeneLength() && i < solution.length; i++) {
            if (individual.getSingleGene(i) == solution[i]) {
                fitness++;
            }
        }
        fitness -=Math.abs(individual.getGeneLength()- solution.length);

        return fitness>=0?fitness:0;
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
            nbParents = (int) Math.round(populationSize * percentage);
            elitism=true;
        }
        else{
            nbParents = -1;
            elitism=false;
        }
    }

    protected void setCrossOverMethod(int crossOverMethod)
    {
        switch(crossOverMethod)
        {
            case CROSS_OVER_ALL_OR_NONE:
            {
                this.crossOverMethod = this::crossoverAllOrNone;
                break;
            }
            case CROSS_OVER_CASE_BY_CASE:{
                this.crossOverMethod = this::crossOverCaseByCase;
                break;
            }
            case CROSS_OVER_KEEP_FROM_BEST:
            {
                this.crossOverMethod = this::crossOverKeepFromBest;
                break;
            }
        }
    }

    public double getUniformRate() {
        return uniformRate;
    }

    public void setUniformRate(double uniformRate) {
        this.uniformRate = uniformRate;
    }

    public double getAllOrNoneRate() {
        return allOrNoneRate;
    }

    public void setAllOrNoneRate(double allOrNoneRate) {
        this.allOrNoneRate = allOrNoneRate;
    }

    public double getMutationFlipRate() {
        return mutationFlipRate;
    }

    public void setMutationFlipRate(double mutationFlipRate) {
        this.mutationFlipRate = mutationFlipRate;
    }

    public double getMutationAddRate() {
        return mutationAddRate;
    }

    public void setMutationAddRate(double mutationAddRate) {
        this.mutationAddRate = mutationAddRate;
    }

    public double getMutationSubRate() {
        return mutationSubRate;
    }

    public void setMutationSubRate(double mutationSubRate) {
        this.mutationSubRate = mutationSubRate;
    }

    public int getTournamentSize() {
        return tournamentSize;
    }

    public void setTournamentSize(int tournamentSize) {
        this.tournamentSize = tournamentSize;
    }
}