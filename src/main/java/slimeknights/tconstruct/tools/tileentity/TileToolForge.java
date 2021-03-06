package slimeknights.tconstruct.tools.tileentity;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import slimeknights.tconstruct.tools.client.GuiToolForge;
import slimeknights.tconstruct.tools.inventory.ContainerToolForge;

public class TileToolForge extends TileToolStation {

  public TileToolForge() {
    inventoryTitle = "gui.toolforge.name";
  }

  @Override
  @SideOnly(Side.CLIENT)
  public GuiContainer createGui(InventoryPlayer inventoryplayer, World world, BlockPos pos) {
    return new GuiToolForge(inventoryplayer, world, pos, this);
  }

  @Override
  public Container createContainer(InventoryPlayer inventoryplayer, World world, BlockPos pos) {
    return new ContainerToolForge(inventoryplayer, this);
  }
}
