package madscience.items.genomes;

import madscience.factory.mod.MadMod;

public class GenomeSheep extends ItemGenomeBase
{

    public GenomeSheep(int id, int primaryColor, int secondaryColor)
    {
        super(id, primaryColor, secondaryColor);
        this.setCreativeTab(MadMod.getCreativeTab());
    }

}
