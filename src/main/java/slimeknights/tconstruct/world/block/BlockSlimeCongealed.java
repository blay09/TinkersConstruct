package slimeknights.tconstruct.world.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Iterator;
import java.util.List;

import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.world.block.BlockSlime.SlimeType;

public class BlockSlimeCongealed extends Block {

  public BlockSlimeCongealed() {
    super(Material.sponge);
    this.setCreativeTab(TinkerRegistry.tabWorld);
    this.setHardness(0.5f);
    this.slipperiness = 0.5f;
    this.disableStats();
    this.setStepSound(SLIME_SOUND);
  }


  @SideOnly(Side.CLIENT)
  @Override
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
    for(SlimeType type : SlimeType.values()) {
      list.add(new ItemStack(this, 1, type.meta));
    }
  }

  @Override
  protected BlockState createBlockState() {
    return new BlockState(this, BlockSlime.TYPE);
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState().withProperty(BlockSlime.TYPE, SlimeType.fromMeta(meta));
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return ((SlimeType) state.getValue(BlockSlime.TYPE)).meta;
  }

  @Override
  public int damageDropped(IBlockState state) {
    return getMetaFromState(state);
  }

  @Override
  public AxisAlignedBB getCollisionBoundingBox(World worldIn, BlockPos pos, IBlockState state) {
    return AxisAlignedBB.fromBounds(pos.getX(), pos.getY(), pos.getZ(),
                                    pos.getX() + 1.0D, pos.getY() + 0.625D, pos.getZ() + 1.0D);
  }

  @Override
  public void onLanded(World world, Entity entity) {
    if (entity.motionY < 0)
    {
      if (entity.motionY < -0.08F)
      {
        //world.playSoundEffect(entity.posX, entity.posY, entity.posZ, stepSound.getStepSound(),
          //                    stepSound.getVolume() / 2.0F, stepSound.getFrequency() * 0.65F);
      }
      entity.motionY *= -1.2F;
      if(entity instanceof EntityLiving) {
        TinkerCommons.potionSlimeBounce.apply((EntityLivingBase) entity);
      }
      if(entity instanceof EntityItem) {
        entity.onGround = false;
      }
    }
  }

  @Override
  public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
    // no fall damage on congealed slime
    entityIn.fall(fallDistance, 0.0F);
  }

  @Override
  public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
    if (entity instanceof EntityLivingBase)
    {
      ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.jump.id, 1, 2));
    }
  }

  /* Log behaviour for slimetrees */
  @Override
  public boolean canSustainLeaves(IBlockAccess world, BlockPos pos) {
    return true;
  }

  // this causes leaves to decay when you break the block
  @Override
  public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
  {
    byte b0 = 4;
    int i = b0 + 1;

    if (worldIn.isAreaLoaded(pos.add(-i, -i, -i), pos.add(i, i, i)))
    {

      for(BlockPos blockpos1 : BlockPos.getAllInBox(pos.add(-b0, -b0, -b0), pos.add(b0, b0, b0))) {
        IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);

        if(iblockstate1.getBlock().isLeaves(worldIn, blockpos1)) {
          iblockstate1.getBlock().beginLeavesDecay(worldIn, blockpos1);
        }
      }
    }
  }
}
