package madscience.items.dna;

import madscience.factory.mod.MadMod;

public class DNACreeper extends ItemDecayDNABase
{

    public DNACreeper(int id, int primaryColor, int secondaryColor)
    {
        super(id, primaryColor, secondaryColor);
        this.setCreativeTab(MadMod.getCreativeTab());
    }

}
