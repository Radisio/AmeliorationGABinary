import Library.SimpleGeneticAlgorithm;

public class Program {

    public static void main(String[] args) {
        SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm();
        sga.runAlgorithm(50, "110111011101110111011101110111011101110111", 200, SimpleGeneticAlgorithm.TOURNAMENT, 20.0/100.0,
                SimpleGeneticAlgorithm.TOURNAMENT,
                SimpleGeneticAlgorithm.CROSS_OVER_KEEP_FROM_BEST);
    }
}
