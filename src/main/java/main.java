public class main {

    public static void main(String[] args) {
        SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm();
        sga.runAlgorithm(27, "1101110111011101110111011101110111011101110111011101110111011101", 30, SimpleGeneticAlgorithm.TOURNAMENT, 20.0/100.0, SimpleGeneticAlgorithm.TOURNAMENT);
    }
}
