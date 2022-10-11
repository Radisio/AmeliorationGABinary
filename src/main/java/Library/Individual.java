package Library;

import lombok.Data;

@Data
public class Individual {

    protected int geneLength;
    private byte[] genes;
    private int fitness = 0;

    public Individual() {
        int max = 128;
        int min = 1;
        int range = max - min +1;
        geneLength = (int)(Math.random() * range) + min;
        genes = new byte[geneLength];
        for (int i = 0; i < genes.length; i++) {
            byte gene = (byte) Math.round(Math.random());
            genes[i] = gene;
        }
    }

    public Individual(byte[] genes)
    {
        setGenes(genes);
        setGeneLength(genes.length);
    }
    protected byte getSingleGene(int index) {
        return genes[index];
    }

    protected void setSingleGene(int index, byte value) {
        genes[index] = value;
        fitness = 0;
    }

    public int getFitness() {
        if (fitness == 0) {
            fitness = SimpleGeneticAlgorithm.getFitness(this);
        }
        return fitness;
    }

    public void addGene(int index, byte val)
    {
        geneLength +=1;
        byte[] newGenes = new byte[geneLength];
        for(int i = 0;i<geneLength;i++)
        {
            if(i == index)
                newGenes[i]= val;
            else if (i > index)
                newGenes[i] = genes[i-1];
            else {
                newGenes[i]= genes[i];
            }
        }
        genes = newGenes;
    }

    public void subGene(int index)
    {
        byte[] newGenes = new byte[geneLength-1];
        for (int i =0; i<geneLength;i++)
        {
            if(i==index)
                continue;
            if(i>index)
                newGenes[i-1] = genes[i];
            else {
                newGenes[i] = genes[i];
            }
        }
        geneLength -=1;
        genes = newGenes;
    }

    @Override
    public String toString() {
        String geneString = "";
        for (int i = 0; i < genes.length; i++) {
            geneString += getSingleGene(i);
        }
        return geneString;
    }

}