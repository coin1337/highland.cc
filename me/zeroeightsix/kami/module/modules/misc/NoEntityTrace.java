package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;

@Module$Info(
   name = "NoEntityTrace",
   category = Module$Category.MISC,
   description = "Blocks entities from stopping you from mining"
)
public class NoEntityTrace extends Module {
   private Setting<NoEntityTrace.TraceMode> mode;
   private static NoEntityTrace INSTANCE;

   public NoEntityTrace() {
      this.mode = this.register(Settings.e("Mode", NoEntityTrace.TraceMode.DYNAMIC));
      INSTANCE = this;
   }

   public static boolean shouldBlock() {
      return INSTANCE.isEnabled() && (INSTANCE.mode.getValue() == NoEntityTrace.TraceMode.STATIC || mc.field_71442_b.field_78778_j);
   }

   private static enum TraceMode {
      STATIC,
      DYNAMIC;
   }
}
