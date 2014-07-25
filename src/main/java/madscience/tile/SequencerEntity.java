package madscience.tile;

import madscience.MadEntities;
import madscience.factory.mod.MadMod;
import madscience.factory.slotcontainers.MadSlotContainerTypeEnum;
import madscience.factory.tileentity.MadTileEntityFactoryProduct;
import madscience.factory.tileentity.prefab.MadTileEntityPrefab;
import madscience.items.datareel.ItemDataReelEmpty;
import madscience.items.dna.ItemDecayDNABase;
import madscience.items.genomes.ItemGenomeBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class SequencerEntity extends MadTileEntityPrefab
{
    public SequencerEntity()
    {
        super();
    }

    public SequencerEntity(MadTileEntityFactoryProduct registeredMachine)
    {
        super(registeredMachine);
    }

    public SequencerEntity(String machineName)
    {
        super(machineName);
    }

    @Override
    public boolean canSmelt()
    {
        super.canSmelt();
        
        if (this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT1) == null ||
                this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2) == null)
        {
            return false;
        }
        
        // Check if input slot 1 is a DNA sample.
        ItemStack[] recipeResult = this.getRecipeResult(new MadSlotContainerTypeEnum[]{
                MadSlotContainerTypeEnum.INPUT_INGREDIENT1,
                MadSlotContainerTypeEnum.INPUT_INGREDIENT2,
                MadSlotContainerTypeEnum.OUTPUT_RESULT1});
        
        if (recipeResult == null)
        {
            // Input slot 1 was not a DNA sample.
            return false;
        }

        // Check if input slot 2 is a empty genome data reel or damaged.
        if (this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).isItemDamaged())
        {
            // Check if the data reel inserted to input slot 2 has recipe.
            ItemStack[] slot2SmeltResult = this.getRecipeResult(new MadSlotContainerTypeEnum[]{
                    MadSlotContainerTypeEnum.INPUT_INGREDIENT1,
                    MadSlotContainerTypeEnum.INPUT_INGREDIENT2,
                    MadSlotContainerTypeEnum.OUTPUT_RESULT1});
            if (slot2SmeltResult == null)
            {
                // Input slot 2 was not a damaged genome data reel.
                return false;
            }

            // Check if the DNA sample matches the genome type it is healing.
            if (recipeResult[0].itemID != this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).itemID)
            {
                return false;
            }
        }
        else
        {
            // Item not damaged so check if it is an empty data reel.
            ItemStack itemsInputSlot2 = new ItemStack(MadEntities.DATAREEL_EMPTY);
            if (!itemsInputSlot2.isItemEqual(this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2)))
            {
                return false;
            }
        }

        // Check if output slots are empty and ready to be filled with items.
        if (this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1) == null)
        {
            return true;
        }

        // Check if input slot 2 matches output slot 1.
        if (this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).isItemEqual(this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1)))
        {
            return false;
        }

        // Check if output slot 1 (for DNA samples) is above item stack limit.
        int slot2Result = this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1).stackSize + recipeResult[0].stackSize;
        return (slot2Result <= getInventoryStackLimit() && slot2Result <= recipeResult[0].getMaxStackSize());
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack items)
    {
        if (slot == this.getSlotIDByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT1))
        {
            // Check if we are a DNA sample.
            if (items != null && items.getItem() instanceof ItemDecayDNABase)
            {
                return true;
            }
        }
        
        // Check if input slot 2 is a empty genome data reel.
        if (slot == this.getSlotIDByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2))
        {
            // Input slot 2 - empty genome data reel.
            ItemStack compareDirtyNeedle = new ItemStack(MadEntities.DATAREEL_EMPTY);
            if (compareDirtyNeedle.isItemEqual(items))
            {
                return true;
            }
            
            // Empty genomes are allowed since they will be encoded in this device.
            if (items != null && items.getItem() instanceof ItemDataReelEmpty)
            {
                return true;
            }

            // Check if we are a genome data reel that is unfinished (AKA damaged).
            if (items != null && items.getItem() instanceof ItemGenomeBase && items.isItemDamaged())
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
    }

    @Override
    public void smeltItem()
    {
        super.smeltItem();
        
        // Output 1 - Encoded genome data reel that used to be empty.
        ItemStack[] craftedItem = this.getRecipeResult(new MadSlotContainerTypeEnum[]{
                MadSlotContainerTypeEnum.INPUT_INGREDIENT1,
                MadSlotContainerTypeEnum.INPUT_INGREDIENT2,
                MadSlotContainerTypeEnum.OUTPUT_RESULT1});

        // Check if we should damage the genome (new), or increase health by eating DNA samples.
        if (craftedItem != null && this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2) != null &&
                this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).isItemDamaged() &&
                this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1) == null)
        {
            // Check of the genome is damaged and needs more samples to complete it.
            if (this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).isItemDamaged())
            {
                int currentGenomeStatus = this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).getItemDamage();
                this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).setItemDamage(--currentGenomeStatus);

                // Debug message about data reel health as it is healed by the server.
                MadMod.log().info("WORLD(" + this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).getUnlocalizedName() + "): " + this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).getItemDamage());
            }

            // Check if the genome was healed completely in this last pass and if so complete it.
            if (this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1) == null && !this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2).isItemDamaged())
            {
                this.setInventorySlotContentsByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1, this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2));

                // Remove healed data reel from input stack 2.
                this.decrStackSize(this.getSlotIDByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2), 1);
            }

            // Remove a DNA sample from input stack 1.
            this.decrStackSize(this.getSlotIDByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT1), 1);

            // We leave this function since we don't want the rest to execute just in case.
            return;
        }

        if (craftedItem != null && this.getStackInSlotByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2) != null &&
                this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1) == null)
        {
            // New genomes that are fresh get set to maximum damage.
            craftedItem[0].setItemDamage(craftedItem[0].getMaxDamage());

            // Add encoded genome data reel to output slot 1.
            if (this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1) == null)
            {
                this.setInventorySlotContentsByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1, craftedItem[0].copy());
            }
            else if (this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1).isItemEqual(craftedItem[0]))
            {
                this.getStackInSlotByType(MadSlotContainerTypeEnum.OUTPUT_RESULT1).stackSize += craftedItem[0].stackSize;
            }
            
            // Remove empty data reel from input stack 2.
            this.decrStackSize(this.getSlotIDByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT2), 1);

            // Remove a DNA sample from input stack 1.
            this.decrStackSize(this.getSlotIDByType(MadSlotContainerTypeEnum.INPUT_INGREDIENT1), 1);
        }
    }

    @Override
    public void updateAnimation()
    {
        super.updateAnimation();
        
        // Active state has many textures based on item cook progress.
        if (canSmelt())
        {
            if (this.getAnimationCurrentFrame() <= 9 && worldObj.getWorldTime() % 15L == 0L)
            {
                // Load this texture onto the entity.
                this.setTextureRenderedOnModel("models/" + this.getMachineInternalName() + "/work_" + this.getAnimationCurrentFrame() + ".png");

                // Update animation frame.
                this.incrementAnimationCurrentFrame();
            }
            else if (this.getAnimationCurrentFrame() >= 10)
            {
                // Check if we have exceeded the ceiling and need to reset.
                this.setAnimationCurrentFrame(0);
            }
        }
        else
        {
            // Idle state single texture.
            this.setTextureRenderedOnModel("models/" + this.getMachineInternalName() + "/idle.png");
        }
    }

    @Override
    public void updateEntity()
    {
        // Important to call the class below us!
        super.updateEntity();

        if (this.isPowered() && this.canSmelt())
        {
            // Decrease to amount of energy this item has on client and server.
            this.consumeInternalEnergy(this.getEnergyConsumeRate());
        }

        // Server side processing for furnace.
        if (!this.worldObj.isRemote)
        {
            // First tick for new item being cooked in furnace.
            if (this.getProgressValue() == 0 && this.canSmelt() && this.isPowered())
            {
                // New item pulled from cooking stack to be processed, check how
                // long this item will take to cook.
                this.setProgressMaximum(200);

                // Increments the timer to kickstart the cooking loop.
                this.incrementProgressValue();
            }
            else if (this.getProgressValue() > 0 && this.canSmelt() && this.isPowered())
            {
                // Increments the timer to kickstart the cooking loop.
                this.incrementProgressValue();

                // Check if furnace has exceeded total amount of time to cook.
                if (this.getProgressValue() >= this.getProgressMaximum())
                {
                    // Convert one item into another via 'cooking' process.
                    this.setProgressValue(0);
                    this.smeltItem();
                    this.setInventoriesChanged();
                }
            }
            else
            {
                // Reset loop, prepare for next item or closure.
                this.setProgressValue(0);
            }
        }
    }

    @Override
    public void updateSound()
    {
        super.updateSound();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
    }

    @Override
    public void initiate()
    {
        super.initiate();
    }
}
