package madscience.tileentities.magloader;

import madscience.MadFurnaces;
import madscience.MadScience;
import madscience.util.MadTechneModel;
import madscience.util.MadUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class MagLoaderRender extends TileEntitySpecialRenderer implements ISimpleBlockRenderingHandler, IItemRenderer
{
    private enum TransformationTypes
    {
        DROPPED, INVENTORY, NONE, THIRDPERSONEQUIPPED
    }

    private MagLoaderEntity ENTITY;
    private MadTechneModel MODEL = (MadTechneModel) AdvancedModelLoader.loadModel(MadScience.MODEL_PATH + MadFurnaces.MAGLOADER_INTERNALNAME + "/" + MadFurnaces.MAGLOADER_INTERNALNAME + ".mad");
    public int RENDERID = RenderingRegistry.getNextAvailableRenderId();
    private ResourceLocation TEXTURE = new ResourceLocation(MadScience.ID, "models/" + MadFurnaces.MAGLOADER_INTERNALNAME + "/empty.png");

    @Override
    public int getRenderId()
    {
        return RENDERID;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        switch (type)
        {
        case ENTITY:
        case EQUIPPED:
        case EQUIPPED_FIRST_PERSON:
            return true;
        case INVENTORY:
        default:
            return false;
        }
    }

    public void renderAModelAt(MagLoaderEntity tileEntity, double x, double y, double z, float f)
    {
        // Grab the individual tile entity in the world.
        ENTITY = tileEntity;
        if (ENTITY == null)
        {
            return;
        }

        // Changes the objects rotation to match whatever the player was facing.
        int rotation = 180;
        switch (ENTITY.getBlockMetadata() % 4)
        {
        case 0:
            rotation = 0;
            break;
        case 3:
            rotation = 90;
            break;
        case 2:
            rotation = 180;
            break;
        case 1:
            rotation = 270;
            break;
        }

        // Begin OpenGL render push.
        GL11.glPushMatrix();

        // Left and right positives center the object and the middle one raises
        // it up to connect with bottom of connecting block.
        GL11.glTranslatef((float) x + 0.5F, (float) y + 0.25F, (float) z + 0.5F);

        // Scale the magazine loader down to fit within normal size limitations.
        GL11.glScalef(0.5F, 0.5F, 0.5F);

        // Using this and the above select the tile entity will always face.
        switch (rotation)
        {
        case 0:
            GL11.glRotatef(-rotation, 0.0F, 1.0F, 0.0F);
            break;
        case 90:
            GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
            break;
        case 180:
            GL11.glRotatef(-rotation, 0.0F, 1.0F, 0.0F);
            break;
        case 270:
            GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
            break;
        }

        if (ENTITY != null && ENTITY.TEXTURE != null && !ENTITY.TEXTURE.isEmpty())
        {
            // Apply our custom texture from asset directory.
            bindTexture(new ResourceLocation(MadScience.ID, ENTITY.TEXTURE));
        }
        else
        {
            // Default texture that is used for item also.
            bindTexture(TEXTURE);
        }

        GL11.glPushMatrix();
        MODEL.renderAll();
        GL11.glPopMatrix();

        // All pieces of the model are hidden by default.
        MODEL.parts.get("push0").showModel = false;
        MODEL.parts.get("push1").showModel = false;
        MODEL.parts.get("push2").showModel = false;
        MODEL.parts.get("push3").showModel = false;
        MODEL.parts.get("push4").showModel = false;
        MODEL.parts.get("push5").showModel = false;
        MODEL.parts.get("Bullet0").showModel = false;
        MODEL.parts.get("Bullet1").showModel = false;
        MODEL.parts.get("Bullet2").showModel = false;
        MODEL.parts.get("Bullet3").showModel = false;
        MODEL.parts.get("Bullet4").showModel = false;
        MODEL.parts.get("Magazine").showModel = false;
        MODEL.parts.get("MagazineBase").showModel = false;   
        
        // Visible when an empty magazine(s) is in the machine.
        if (ENTITY != null && ENTITY.clientMagazineCount > 0)
        {
            MODEL.parts.get("Magazine").showModel = true;
            MODEL.parts.get("MagazineBase").showModel = true;
        }
        
        this.MODEL.render((Entity) null, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);

        MODEL.renderAll();
        GL11.glPopMatrix();
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        return;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        GL11.glPushMatrix();

        // Use the same texture we do on the block normally.
        Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        // adjust rendering space to match what caller expects
        TransformationTypes transformationToBeUndone = TransformationTypes.NONE;
        switch (type)
        {
        case EQUIPPED:
        {
            float scale = 1.0F;
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.1F, 0.3F, 0.3F);
            // GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            GL11.glEnable(GL11.GL_CULL_FACE);
            transformationToBeUndone = TransformationTypes.THIRDPERSONEQUIPPED;
            break;
        }
        case EQUIPPED_FIRST_PERSON:
        {
            float scale = 1.0F;
            GL11.glScalef(scale, scale, scale);
            GL11.glTranslatef(0.2F, 0.9F, 0.5F);
            GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
            break;
        }
        case INVENTORY:
        {
            float scale = 1.0F;
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(270.0F, 0.0F, 0.5F, 0.0F);
            transformationToBeUndone = TransformationTypes.INVENTORY;
            break;
        }
        case ENTITY:
        {
            float scale = 1.0F;
            GL11.glScalef(scale, scale, scale);
            GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            transformationToBeUndone = TransformationTypes.DROPPED;
            break;
        }
        default:
            break;
        }

        // Renders the model in the gameworld at the correct scale.
        this.MODEL.renderAll();
        GL11.glPopMatrix();

        switch (transformationToBeUndone)
        {
        case NONE:
        {
            break;
        }
        case DROPPED:
        {
            GL11.glTranslatef(0.0F, -0.5F, 0.0F);
            float scale = 1.0F;
            GL11.glScalef(scale, scale, scale);
            break;
        }
        case INVENTORY:
        {
            GL11.glTranslatef(0.5F, 0.5F, 0.5F);
            break;
        }
        case THIRDPERSONEQUIPPED:
        {
            GL11.glDisable(GL11.GL_CULL_FACE);
        }
        default:
            break;
        }
    }

    @Override
    public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float scale)
    {
        this.renderAModelAt((MagLoaderEntity) tileEntity, x, y, z, scale);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        // Overridden by our tile entity.
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory()
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        switch (type)
        {
        case ENTITY:
        {
            return (helper == ItemRendererHelper.ENTITY_BOBBING || helper == ItemRendererHelper.ENTITY_ROTATION || helper == ItemRendererHelper.BLOCK_3D);
        }
        case EQUIPPED:
        {
            return (helper == ItemRendererHelper.BLOCK_3D || helper == ItemRendererHelper.EQUIPPED_BLOCK);
        }
        case EQUIPPED_FIRST_PERSON:
        {
            return (helper == ItemRendererHelper.EQUIPPED_BLOCK);
        }
        case INVENTORY:
        {
            return (helper == ItemRendererHelper.INVENTORY_BLOCK);
        }
        default:
        {
            return false;
        }
        }
    }
}