package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

@Module$Info(
   name = "AntiFog",
   description = "Disables or reduces fog",
   category = Module$Category.RENDER
)
public class AntiFog extends Module {
   public static Setting<AntiFog.VisionMode> mode;
   private static AntiFog INSTANCE;

   public AntiFog() {
      INSTANCE = this;
      this.register(mode);
   }

   public static boolean enabled() {
      return INSTANCE.isEnabled();
   }

   static {
      mode = Settings.e("Mode", AntiFog.VisionMode.NOFOG);
      INSTANCE = new AntiFog();
   }

   public static enum VisionMode {
      NOFOG,
      AIR;
   }
}
