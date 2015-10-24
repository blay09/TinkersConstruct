package slimeknights.tconstruct.world.block;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

import slimeknights.tconstruct.library.TinkerRegistry;

public class BlockSlime extends net.minecraft.block.BlockSlime {
  public static final PropertyEnum TYPE = PropertyEnum.create("type", SlimeType.class);

  public BlockSlime() {
    this.setCreativeTab(TinkerRegistry.tabWorld);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
    for(SlimeType type : SlimeType.values()) {
      list.add(new ItemStack(this, 1, type.meta));
    }
  }

  @Override
  protected BlockState createBlockState() {
    return new BlockState(this, TYPE);
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    // vanilla slime if it's green slime
    if(SlimeType.fromMeta(meta) == SlimeType.GREEN) {
      return new BlockState(Blocks.slime_block, TYPE).getBaseState();
      //return Blocks.slime_block.getStateFromMeta(0);
    }
    return this.getDefaultState().withProperty(TYPE, SlimeType.fromMeta(meta));
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return ((SlimeType) state.getValue(TYPE)).meta;
  }

  @Override
  public int damageDropped(IBlockState state) {
    return super.damageDropped(state);
  }

  @Override
  public Item getItemDropped(IBlockState state, Random rand, int fortune) {
    // green slime returns the vanilla slime block
    if(state.getValue(TYPE) == SlimeType.GREEN) {
      return Item.getItemFromBlock(Blocks.slime_block);
    }
    return super.getItemDropped(state, rand, fortune);
  }

  public enum SlimeType implements IStringSerializable {
    GREEN,
    BLUE,
    PURPLE,
    BLOOD,
    MAGMA;

    SlimeType() {
      this.meta = this.ordinal();
    }

    public final int meta;

    public static SlimeType fromMeta(int meta) {
      if(meta < 0 || meta > values().length) {
        meta = 0;
      }

      return values()[meta];
    }

    @Override
    public String getName() {
      return this.toString();
    }
  }
}