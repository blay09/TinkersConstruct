package tconstruct;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import tconstruct.tools.TinkerTools;
import tconstruct.tools.block.ToolTableBlock;

// Holds all the things oredicted by Tinkers :)
class TinkerOredict {
  private TinkerOredict() {}

  // Things that are not from tinkers but should be oredicted
  public static void ensureOredict() {
    // crafting table
    OreDictionary.registerOre("workbench", Blocks.crafting_table);
  }

  // All the oredicted things in tinkers. Only registers if the item is actually present
  public static void registerTinkerOredict() {
    // TinkerTools Pulse
    oredict("workbench", TinkerTools.toolTables, ToolTableBlock.TableTypes.CraftingStation.meta);

    oredict("partPickHead", TinkerTools.pickHead);
    oredict("partBinding", TinkerTools.binding);
    oredict("partToolRod", TinkerTools.toolRod);
  }



  private static void oredict(String name, Item item) {
    oredict(name, item, OreDictionary.WILDCARD_VALUE);
  }

  private static void oredict(String name, Block block) {
    oredict(name, block, OreDictionary.WILDCARD_VALUE);
  }

  private static void oredict(String name, Item item, int meta) {
    oredict(name, new ItemStack(item, 1, meta));
  }

  private static void oredict(String name, Block block, int meta) {
    oredict(name, new ItemStack(block, 1, meta));
  }

  private static void oredict(String name, ItemStack stack) {
    if(stack != null && stack.getItem() != null) {
      OreDictionary.registerOre(name, stack);
    }
  }
}