package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;

@Module$Info(
   name = "Sprint",
   description = "Automatically makes the player sprint",
   category = Module$Category.MOVEMENT
)
public class Sprint extends Module {
   public void onUpdate() {
      try {
         if (!mc.field_71439_g.field_70123_F && mc.field_71439_g.field_191988_bg > 0.0F) {
            mc.field_71439_g.func_70031_b(true);
         } else {
            mc.field_71439_g.func_70031_b(false);
         }
      } catch (Exception var2) {
      }

   }
}
