package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;

@Module$Info(
   name = "AntiWeather",
   description = "Removes rain from your world",
   category = Module$Category.MISC
)
public class AntiWeather extends Module {
   public void onUpdate() {
      if (!this.isDisabled()) {
         if (mc.field_71441_e.func_72896_J()) {
            mc.field_71441_e.func_72894_k(0.0F);
         }

      }
   }
}
