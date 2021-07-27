package me.zeroeightsix.kami;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Stream;
import me.zero.alpine.EventBus;
import me.zero.alpine.EventManager;
import me.zeroeightsix.kami.KamiMod.1;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.CommandManager;
import me.zeroeightsix.kami.event.ForgeEventProcessor;
import me.zeroeightsix.kami.gui.kami.KamiGUI;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.AlignedComponent.Alignment;
import me.zeroeightsix.kami.gui.rgui.component.container.use.Frame;
import me.zeroeightsix.kami.gui.rgui.util.ContainerHelper;
import me.zeroeightsix.kami.gui.rgui.util.Docking;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.setting.SettingsRegister;
import me.zeroeightsix.kami.setting.config.Configuration;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
   modid = "rusher",
   name = "RusherGod.CC",
   version = "+"
)
public class KamiMod {
   public static final String MODID = "rusher";
   public static final String MODNAME = "RusherGod.CC";
   public static final String MODVER = "+";
   public static final String KAMI_HIRAGANA = "rusher";
   public static final String KAMI_KATAKANA = "rusher";
   public static final String KAMI_KANJI = "rusher";
   private static final String KAMI_CONFIG_NAME_DEFAULT = "KAMIConfig.json";
   public static final Logger log = LogManager.getLogger("KAMI");
   public static final EventBus EVENT_BUS = new EventManager();
   @Instance
   private static KamiMod INSTANCE;
   public KamiGUI guiManager;
   public CommandManager commandManager;
   private Setting<JsonObject> guiStateSetting = Settings.custom("gui", new JsonObject(), new 1(this)).buildAndRegister("");

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      log.info("\n\nInitializing KAMI +");
      ModuleManager.initialize();
      Stream var10000 = ModuleManager.getModules().stream().filter((module) -> {
         return module.alwaysListening;
      });
      EventBus var10001 = EVENT_BUS;
      var10000.forEach(var10001::subscribe);
      MinecraftForge.EVENT_BUS.register(new ForgeEventProcessor());
      LagCompensator.INSTANCE = new LagCompensator();
      Wrapper.init();
      this.guiManager = new KamiGUI();
      this.guiManager.initializeGUI();
      this.commandManager = new CommandManager();
      Friends.initFriends();
      SettingsRegister.register("commandPrefix", Command.commandPrefix);
      loadConfiguration();
      log.info("Settings loaded");
      ModuleManager.updateLookup();
      ModuleManager.getModules().stream().filter(Module::isEnabled).forEach(Module::enable);
      log.info("KAMI Mod initialized!\n");
   }

   public static String getConfigName() {
      Path config = Paths.get("KAMILastConfig.txt");
      String kamiConfigName = "KAMIConfig.json";

      try {
         BufferedReader reader = Files.newBufferedReader(config);
         Throwable var39 = null;

         try {
            kamiConfigName = reader.readLine();
            if (!isFilenameValid(kamiConfigName)) {
               kamiConfigName = "KAMIConfig.json";
            }
         } catch (Throwable var33) {
            var39 = var33;
            throw var33;
         } finally {
            if (reader != null) {
               if (var39 != null) {
                  try {
                     reader.close();
                  } catch (Throwable var32) {
                     var39.addSuppressed(var32);
                  }
               } else {
                  reader.close();
               }
            }

         }
      } catch (NoSuchFileException var37) {
         try {
            BufferedWriter writer = Files.newBufferedWriter(config);
            Throwable var4 = null;

            try {
               writer.write("KAMIConfig.json");
            } catch (Throwable var31) {
               var4 = var31;
               throw var31;
            } finally {
               if (writer != null) {
                  if (var4 != null) {
                     try {
                        writer.close();
                     } catch (Throwable var30) {
                        var4.addSuppressed(var30);
                     }
                  } else {
                     writer.close();
                  }
               }

            }
         } catch (IOException var35) {
            var35.printStackTrace();
         }
      } catch (IOException var38) {
         var38.printStackTrace();
      }

      return kamiConfigName;
   }

   public static void loadConfiguration() {
      try {
         loadConfigurationUnsafe();
      } catch (IOException var1) {
         var1.printStackTrace();
      }

   }

   public static void loadConfigurationUnsafe() throws IOException {
      String kamiConfigName = getConfigName();
      Path kamiConfig = Paths.get(kamiConfigName);
      if (Files.exists(kamiConfig, new LinkOption[0])) {
         Configuration.loadConfiguration(kamiConfig);
         JsonObject gui = (JsonObject)INSTANCE.guiStateSetting.getValue();
         Iterator var3 = gui.entrySet().iterator();

         while(var3.hasNext()) {
            Entry<String, JsonElement> entry = (Entry)var3.next();
            Optional<Component> optional = INSTANCE.guiManager.getChildren().stream().filter((component) -> {
               return component instanceof Frame;
            }).filter((component) -> {
               return ((Frame)component).getTitle().equals(entry.getKey());
            }).findFirst();
            if (optional.isPresent()) {
               JsonObject object = ((JsonElement)entry.getValue()).getAsJsonObject();
               Frame frame = (Frame)optional.get();
               frame.setX(object.get("x").getAsInt());
               frame.setY(object.get("y").getAsInt());
               Docking docking = Docking.values()[object.get("docking").getAsInt()];
               if (docking.isLeft()) {
                  ContainerHelper.setAlignment(frame, Alignment.LEFT);
               } else if (docking.isRight()) {
                  ContainerHelper.setAlignment(frame, Alignment.RIGHT);
               } else if (docking.isCenterVertical()) {
                  ContainerHelper.setAlignment(frame, Alignment.CENTER);
               }

               frame.setDocking(docking);
               frame.setMinimized(object.get("minimized").getAsBoolean());
               frame.setPinned(object.get("pinned").getAsBoolean());
            } else {
               System.err.println("Found GUI config entry for " + (String)entry.getKey() + ", but found no frame with that name");
            }
         }

         getInstance().getGuiManager().getChildren().stream().filter((component) -> {
            return component instanceof Frame && ((Frame)component).isPinneable() && component.isVisible();
         }).forEach((component) -> {
            component.setOpacity(0.0F);
         });
      }
   }

   public static void saveConfiguration() {
      try {
         saveConfigurationUnsafe();
      } catch (IOException var1) {
         var1.printStackTrace();
      }

   }

   public static void saveConfigurationUnsafe() throws IOException {
      JsonObject object = new JsonObject();
      INSTANCE.guiManager.getChildren().stream().filter((component) -> {
         return component instanceof Frame;
      }).map((component) -> {
         return (Frame)component;
      }).forEach((frame) -> {
         JsonObject frameObject = new JsonObject();
         frameObject.add("x", new JsonPrimitive(frame.getX()));
         frameObject.add("y", new JsonPrimitive(frame.getY()));
         frameObject.add("docking", new JsonPrimitive(Arrays.asList(Docking.values()).indexOf(frame.getDocking())));
         frameObject.add("minimized", new JsonPrimitive(frame.isMinimized()));
         frameObject.add("pinned", new JsonPrimitive(frame.isPinned()));
         object.add(frame.getTitle(), frameObject);
      });
      INSTANCE.guiStateSetting.setValue(object);
      Path outputFile = Paths.get(getConfigName());
      if (!Files.exists(outputFile, new LinkOption[0])) {
         Files.createFile(outputFile);
      }

      Configuration.saveConfiguration(outputFile);
      ModuleManager.getModules().forEach(Module::destroy);
   }

   public static boolean isFilenameValid(String file) {
      File f = new File(file);

      try {
         f.getCanonicalPath();
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   public static KamiMod getInstance() {
      return INSTANCE;
   }

   public KamiGUI getGuiManager() {
      return this.guiManager;
   }

   public CommandManager getCommandManager() {
      return this.commandManager;
   }
}
