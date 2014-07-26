package madscience.factory.rendering;

import java.util.LinkedHashMap;
import java.util.Map;

import madscience.factory.mod.MadMod;
import madscience.factory.model.MadModel;
import madscience.factory.model.MadModelFile;
import madscience.factory.model.MadTechneModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MadRenderingFactoryProduct
{
    /** Unique ID for our model to render in the world. */
    private int renderingID = -1;
    
    /** Default texture that is rendered on the model if no other is specified. */
    private ResourceLocation textureResource = null;
    
    /** Primary reference to all loaded models. */
    private Map<String, MadTechneModel> modelRenderingReference = null;
    
    public MadRenderingFactoryProduct(MadModel renderInformation)
    {
        super();
        
        // Master model list we will manipulate with packets from server.
        this.modelRenderingReference = new LinkedHashMap<String, MadTechneModel>();
        
        // Unique rendering ID for each instance of a renderer product.
        this.renderingID = RenderingRegistry.getNextAvailableRenderId();
        
        // Load default texture from this machine as a resource location for renderer to bind to when referenced.
        this.textureResource = new ResourceLocation(MadMod.ID, renderInformation.getMachineTexture());
        
        // Load default set of models we will clone for each new created instance.
        for (MadModelFile productModel : renderInformation.getMachineModelsFilesClone())
        {
            this.modelRenderingReference.put(productModel.getModelName(), (MadTechneModel) AdvancedModelLoader.loadModel(productModel.getModelPath()));            
        }
    }
    
    /** Tells the MadTechneModel rendering system to render all the parts that makeup a given model. */
    public void renderMadModelParts()
    {
        // Loop through our keys from reference models (which are updated by packet system).
        for (MadTechneModel modelPart : this.modelRenderingReference.values())
        {
            if (modelPart != null)
            {
                if (modelPart.isVisible())
                {
                    modelPart.renderAll();
                }
            }
        }
    }

    public int getRenderingID()
    {
        return renderingID;
    }

    public ResourceLocation getTextureResource()
    {
        return textureResource;
    }
    
    /** Updates rendering product with proper visibility status per instance. Returns false if no change was needed, true if change was made. */
    public boolean setRenderVisibilityByName(String modelName, boolean visible)
    {
        // Attempt to locate the piece based on it's name.
        if (modelRenderingReference.containsKey(modelName))
        {
            // Grab the model piece from reference.
            MadTechneModel queriedModel = modelRenderingReference.get(modelName);
            
            if (queriedModel.isVisible() != visible)
            {
                queriedModel.setVisible(visible);
                
                // Update the reference.
                MadTechneModel replacedModel = modelRenderingReference.put(modelName, queriedModel);
                
                // Check that list was updated to match what was inputed.
                if (!queriedModel.equals(replacedModel))
                {
                    throw new IllegalArgumentException("Could not update model piece '" + modelName + "' visibility. Something is wrong with rendering reference mapping!");
                }
                
                return true;
            }
        }
        
        return false;
    }

    public Map<String, MadTechneModel> getModelRenderingReference()
    {
        return modelRenderingReference;
    }
}