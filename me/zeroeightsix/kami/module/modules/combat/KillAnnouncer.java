package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.util.Wrapper;

@Module$Info(
   name = "KillAnnouncer",
   category = Module$Category.MISC,
   description = "Sends a message when enabled"
)
public class KillAnnouncer extends Module {
   protected void onEnable() {
      if (mc.field_71439_g != null) {
         Wrapper.getPlayer().func_71165_d("GG! Highland Over Everything!");
         this.disable();
      }

   }

   protected void onDisable() {
   }
}
