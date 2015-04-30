package crazypants.enderio.material;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.enderio.core.common.util.BlockCoord;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.BlockEio;
import crazypants.enderio.EnderIO;
import crazypants.enderio.ModObject;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.painter.PainterUtil;
import crazypants.enderio.machine.painter.TileEntityPaintedBlock;

public class BlockFusedQuartz extends BlockEio {

  public static int renderId;

  private final static Type[] metaMapping = new Type[16];
  public enum Type {

    FUSED_QUARTZ("fusedQuartz", "enderio:fusedQuartz", "enderio:fusedQuartzFrame", "enderio:fusedQuartzItem"),
    GLASS("fusedGlass", "enderio:fusedGlass", Config.clearGlassSameTexture ? "enderio:fusedQuartzFrame" : "enderio:fusedGlassFrame", "enderio:fusedGlassItem"),
    ENLIGHTENED_FUSED_QUARTZ("enlightenedFusedQuartz", "enderio:fusedQuartz", "enderio:fusedQuartzFrame", "enderio:fusedQuartzItem"),
    ENLIGHTENED_GLASS("enlightenedFusedGlass", "enderio:fusedGlass", Config.clearGlassSameTexture ? "enderio:fusedQuartzFrame" : "enderio:fusedGlassFrame",
        "enderio:fusedGlassItem");

    final String unlocalisedName;
    final String blockIcon;
    final String frameIcon;
    final String itemIcon;

    private Type(String unlocalisedName, String blockIcon, String frameIcon, String itemIcon) {
      this.unlocalisedName = unlocalisedName;
      this.frameIcon = frameIcon;
      this.blockIcon = blockIcon;
      this.itemIcon = itemIcon;
      metaMapping[this.ordinal()] = this;
    }

    public boolean connectTo(int otherMeta) {
      if(otherMeta == ordinal() || Config.clearGlassConnectToFusedQuartz) {
        return true;
      }

      switch (this) {
      case FUSED_QUARTZ:
        return otherMeta == ENLIGHTENED_FUSED_QUARTZ.ordinal();
      case ENLIGHTENED_FUSED_QUARTZ:
        return otherMeta == FUSED_QUARTZ.ordinal();
      case GLASS:
        return otherMeta == ENLIGHTENED_GLASS.ordinal();
      case ENLIGHTENED_GLASS:
        return otherMeta == GLASS.ordinal();
      }

      return false;
    }

    public static Type byMeta(int meta) {
      return metaMapping[meta] != null ? metaMapping[meta] : GLASS;
    }
  }

  public static BlockFusedQuartz create() {
    BlockFusedQuartz result = new BlockFusedQuartz();
    result.init();
    return result;
  }

  IIcon[] blockIcon;
  IIcon[] itemsIcons;
  IIcon[] frameIcons;

  private BlockFusedQuartz() {
    super(ModObject.blockFusedQuartz.unlocalisedName, TileEntityPaintedBlock.class, Material.glass);
    setStepSound(Block.soundTypeGlass);
  }

  @Override
  protected void init() {
    GameRegistry.registerBlock(this, ItemFusedQuartz.class, name);
    if(teClass != null) {
      GameRegistry.registerTileEntity(teClass, name + "TileEntity");
    }
  }

  @Override
  public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
    int meta = world.getBlockMetadata(x, y, z);
    Type type = Type.byMeta(meta);
    if (type == Type.FUSED_QUARTZ || type == Type.ENLIGHTENED_FUSED_QUARTZ) {
      return 2000;
    } else {
      return super.getExplosionResistance(par1Entity);
    }
  }

  @Override
  public boolean renderAsNormalBlock() {
    return false;
  }

  @Override
  public boolean isOpaqueCube() {
    return false;
  }

  @Override
  public int getRenderType() {
    return renderId;
  }

  //TODO:1.7 this makes it go splat
  //  @Override
  //  @SideOnly(Side.CLIENT)
  //  public int getRenderBlockPass() {
  //    return 1;
  //  }
  //
  //  @Override
  //  public boolean canRenderInPass(int pass) {
  //    FusedQuartzRenderer.renderPass = pass;
  //    return true;
  //  }

  @Override
  public int getLightOpacity(IBlockAccess world, int x, int y, int z) {
    return 0;
  }

  @Override
  public int getLightValue(IBlockAccess world, int x, int y, int z) {
    Block block = world.getBlock(x, y, z);
    if(block != this) {
      return super.getLightValue(world, x, y, z);
    }
    int meta = world.getBlockMetadata(x, y, z);
    if(meta > 1) {
      return 15;
    }
    return super.getLightValue(world, x, y, z);
  }

  @Override
  public int damageDropped(int par1) {
    return par1;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
    for (int j = 0; j < Type.values().length; ++j) {
      par3List.add(new ItemStack(par1, 1, j));
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
    Block block = world.getBlock(x, y, z);
    int meta = world.getBlockMetadata(x, y, z);
    if(block == this) {
      BlockCoord here = new BlockCoord(x, y, z).getLocation(ForgeDirection.VALID_DIRECTIONS[side].getOpposite());
      int myMeta = world.getBlockMetadata(here.x, here.y, here.z);
      return !Type.byMeta(myMeta).connectTo(meta);
    }
    return true;
  }

  @Override
  public boolean isBlockSolid(IBlockAccess p_149747_1_, int p_149747_2_, int p_149747_3_, int p_149747_4_, int p_149747_5_) {
    return true;
  }

  @Override
  public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
    if(side == ForgeDirection.UP) { //stop drips
      return false;
    }
    return true;
  }

  @Override
  public boolean canPlaceTorchOnTop(World arg0, int arg1, int arg2, int arg3) {
    return true;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerBlockIcons(IIconRegister iIconRegister) {
    //This little oddity is so the standard rendering used for items and breaking effects
    //uses the item texture, while the custom renderer uses 'realBlockIcon' to render the 'non-frame' part of the block.
    Type[] ts = Type.values();
    blockIcon = new IIcon[ts.length];
    itemsIcons = new IIcon[ts.length];
    frameIcons = new IIcon[ts.length];

    for (int i = 0; i < ts.length; i++) {
      blockIcon[i] = iIconRegister.registerIcon(ts[i].blockIcon);
      itemsIcons[i] = iIconRegister.registerIcon(ts[i].itemIcon);
      frameIcons[i] = iIconRegister.registerIcon(ts[i].frameIcon);
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public IIcon getIcon(int par1, int meta) {
    meta = MathHelper.clamp_int(meta, 0, blockIcon.length - 1);
    return blockIcon[meta];
  }

  public IIcon getItemIcon(int meta) {
    meta = MathHelper.clamp_int(meta, 0, itemsIcons.length - 1);
    return itemsIcons[meta];
  }

  public IIcon getDefaultFrameIcon(int meta) {
    meta = MathHelper.clamp_int(meta, 0, frameIcons.length - 1);
    return frameIcons[meta];
  }

  @Override
  protected boolean shouldWrench(World world, int x, int y, int z, EntityPlayer entityPlayer, int side) {
    return false;
  }

  private ItemStack createItemStackForSourceBlock(int quartzBlockMeta, Block sourceBlock, int sourceBlockMetadata) {
    if(sourceBlock == null) {
      return null;
    }
    ItemStack result = new ItemStack(EnderIO.instance.itemFusedQuartzFrame, 1, quartzBlockMeta);
    PainterUtil.setSourceBlock(result, sourceBlock, sourceBlockMetadata);
    return result;
  }

}
