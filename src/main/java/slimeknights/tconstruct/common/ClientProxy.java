package slimeknights.tconstruct.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.registry.GameData;

import java.lang.reflect.Field;
import java.util.Locale;

import slimeknights.mantle.network.AbstractPacket;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.Util;
import slimeknights.tconstruct.library.client.CustomFontRenderer;
import slimeknights.tconstruct.library.client.CustomTextureCreator;
import slimeknights.tconstruct.library.client.ItemBlockModelSetter;
import slimeknights.tconstruct.library.client.model.MaterialModelLoader;
import slimeknights.tconstruct.library.client.model.ModifierModelLoader;
import slimeknights.tconstruct.library.client.model.ToolModelLoader;
import slimeknights.tconstruct.library.client.particle.EntitySlimeFx;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.utils.TagUtil;
import slimeknights.tconstruct.shared.TinkerCommons;

public abstract class ClientProxy extends CommonProxy {

  public static CustomFontRenderer fontRenderer;

  protected static final ToolModelLoader loader = new ToolModelLoader();
  protected static final MaterialModelLoader materialLoader = new MaterialModelLoader();
  protected static final ModifierModelLoader modifierLoader = new ModifierModelLoader();

  public static void initClient() {
    // i wonder if this is OK :D
    ModelLoaderRegistry.registerLoader(loader);
    ModelLoaderRegistry.registerLoader(materialLoader);
    ModelLoaderRegistry.registerLoader(modifierLoader);

    MinecraftForge.EVENT_BUS.register(new ItemBlockModelSetter());
  }

  public static void initRenderer() {

    CustomTextureCreator creator = new CustomTextureCreator();

    MinecraftForge.EVENT_BUS.register(creator);
    ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(creator);

    fontRenderer = new CustomFontRenderer(Minecraft.getMinecraft().gameSettings,
                                          new ResourceLocation("textures/font/ascii.png"),
                                          Minecraft.getMinecraft().renderEngine);
    if(Minecraft.getMinecraft().gameSettings.language != null) {
      fontRenderer.setUnicodeFlag(Minecraft.getMinecraft().getLanguageManager().isCurrentLocaleUnicode());
      fontRenderer.setBidiFlag(Minecraft.getMinecraft().getLanguageManager().isCurrentLanguageBidirectional());
    }
    ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(fontRenderer);

    try {
      Class clazz = Class.forName("codechicken.lib.gui.GuiDraw");
      Field field = clazz.getDeclaredField("fontRenderer");
      field.set(null, fontRenderer);
    } catch(ClassNotFoundException e) {
      TConstruct.log.debug("Could not integrate FondRenderer with NEI");
    } catch(NoSuchFieldException e) {
      TConstruct.log.debug("Could not integrate FondRenderer with NEI");
    } catch(IllegalAccessException e) {
      TConstruct.log.debug("Could not integrate FondRenderer with NEI");
    }
  }

  protected ResourceLocation registerModel(Item item, String... customVariants) {
    return registerModel(item, 0, customVariants);
  }

  /**
   * Registers a model variant for you. :3 The model-string is obtained through the game registry.
   */
  protected ResourceLocation registerModel(Item item, int meta, String... customVariants) {
    // get the registered name for the object
    Object o = GameData.getItemRegistry().getNameForObject(item);

    // are you trying to add an unregistered item...?
    if(o == null) {
      TConstruct.log.error("Trying to register a model for an unregistered item: %s" + item.getUnlocalizedName());
      // bad boi
      return null;
    }

    ResourceLocation location = (ResourceLocation) o;

    location = new ResourceLocation(location.getResourceDomain(), location.getResourcePath());

    // and plop it in.
    // This here is needed for the model to be found ingame when the game looks for a model to render an Itemstack (Item:Meta)
    ModelLoader.setCustomModelResourceLocation(item, meta,
                                               new ModelResourceLocation(location,
                                                                         "inventory"));

    // We have to readd the default variant if we have custom variants, since it wont be added otherwise
    if(customVariants.length > 0) {
      ModelLoader.registerItemVariants(item, location);
    }

    for(String customVariant : customVariants) {
      String custom = location.getResourceDomain() + ":" + customVariant;
      ModelLoader.registerItemVariants(item, new ResourceLocation(custom));
    }

    return location;
  }

  protected void registerItemModel(ItemStack item, String name) {

    // tell Minecraft which textures it has to load. This is resource-domain sensitive
    if(!name.contains(":")) {
      name = Util.resource(name);
    }

    ModelLoader.registerItemVariants(item.getItem(), new ResourceLocation(name));
    // tell the game which model to use for this item-meta combination
    ModelLoader.setCustomModelResourceLocation(item.getItem(), item
        .getMetadata(), new ModelResourceLocation(name, "inventory"));
  }

  /**
   * Registers a multimodel that should be loaded via our multimodel loader The model-string is obtained through the
   * game registry.
   */
  protected ResourceLocation registerToolModel(Item item) {
    ResourceLocation itemLocation = getItemLocation(item);
    if(itemLocation == null) {
      return null;
    }

    String path = "tools/" + itemLocation.getResourcePath() + ToolModelLoader.EXTENSION;

    return registerToolModel(item, new ResourceLocation(itemLocation.getResourceDomain(), path));
  }

  protected ResourceLocation registerToolModel(Item item, final ResourceLocation location) {
    if(!location.getResourcePath().endsWith(ToolModelLoader.EXTENSION)) {
      TConstruct.log.error("The material-model " + location.toString() + " does not end with '"
                           + ToolModelLoader.EXTENSION
                           + "' and will therefore not be loaded by the custom model loader!");
    }

    return registerIt(item, location);
  }

  public ResourceLocation registerMaterialItemModel(Item item) {
    ResourceLocation itemLocation = getItemLocation(item);
    if(itemLocation == null) {
      return null;
    }
    return registerMaterialModel(item, new ResourceLocation(itemLocation.getResourceDomain(),
                                                            itemLocation.getResourcePath()
                                                            + MaterialModelLoader.EXTENSION));
  }

  public ResourceLocation registerMaterialModel(Item item, final ResourceLocation location) {
    if(!location.getResourcePath().endsWith(MaterialModelLoader.EXTENSION)) {
      TConstruct.log.error("The material-model " + location.toString() + " does not end with '"
                           + MaterialModelLoader.EXTENSION
                           + "' and will therefore not be loaded by the custom model loader!");
    }

    return registerIt(item, location);
  }

  public void registerModifierModel(IModifier modifier, ResourceLocation location) {
    modifierLoader.registerModifierFile(modifier.getIdentifier(), location);
  }

  public ResourceLocation registerItemModel(Item item) {
    ResourceLocation itemLocation = getItemLocation(item);
    if(itemLocation == null) {
      return null;
    }

    return registerIt(item, itemLocation);
  }

  private static ResourceLocation registerIt(Item item, final ResourceLocation location) {
    // plop it in.
    // This here is needed for the model to be found ingame when the game looks for a model to render an Itemstack
    // we use an ItemMeshDefinition because it allows us to do it no matter what metadata we use
    ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition() {
      @Override
      public ModelResourceLocation getModelLocation(ItemStack stack) {
        return new ModelResourceLocation(location, "inventory");
      }
    });

    // We have to readd the default variant if we have custom variants, since it wont be added otherwise and therefore not loaded
    ModelBakery.registerItemVariants(item, location);

    return location;
  }

  public static ResourceLocation getItemLocation(Item item) {
    return Util.getItemLocation(item);
  }

  @Override
  public void sendPacketToServerOnly(AbstractPacket packet) {
    TinkerNetwork.sendToServer(packet);
  }

  @Override
  public void spawnSlimeParticle(World world, double x, double y, double z) {
    Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySlimeFx(world, x,y,z, TinkerCommons.matSlimeBallBlue.getItem(), TinkerCommons.matSlimeBallBlue.getItemDamage()));
  }

  @Override
  public void preventPlayerSlowdown(Entity player, float originalSpeed, Item item) {
    // has to be done in onUpdate because onTickUsing is too early and gets overwritten. bleh.
    if(player instanceof EntityPlayerSP) {
      EntityPlayerSP playerSP = (EntityPlayerSP) player;
      ItemStack usingItem = playerSP.inventory.getCurrentItem();
      if(usingItem != null && usingItem.getItem() == item) {
        // no slowdown from charging it up
        playerSP.movementInput.moveForward *= originalSpeed * 5.0F;
        playerSP.movementInput.moveStrafe *= originalSpeed * 5.0F;
      }
    }
  }

  public static class PatternMeshDefinition implements ItemMeshDefinition {

    private final ResourceLocation baseLocation;

    public PatternMeshDefinition(ResourceLocation baseLocation) {
      this.baseLocation = baseLocation;
    }

    @Override
    public ModelResourceLocation getModelLocation(ItemStack stack) {
      Item item = Pattern.getPartFromTag(stack);
      String suffix = "";
      if(item != null) {
        suffix = Pattern.getTextureIdentifier(item);
      }

      return new ModelResourceLocation(new ResourceLocation(baseLocation.getResourceDomain(),
                                                            baseLocation.getResourcePath() + suffix),
                                       "inventory");
    }
  }
}
