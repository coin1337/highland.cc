package me.zeroeightsix.kami.module.modules.dev;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;

@Module$Info(
   name = "GMFly",
   category = Module$Category.DEV,
   description = "Godmode Fly"
)
public class GMFly extends Module {
   public void onEnable() {
      this.toggleFly(true);
   }

   public void onDisable() {
      this.toggleFly(false);
   }

   public void onUpdate() {
      this.toggleFly(true);
   }

   private void toggleFly(boolean b) {
      mc.field_71439_g.field_71075_bZ.field_75100_b = b;
      if (!mc.field_71439_g.field_71075_bZ.field_75098_d) {
         mc.field_71439_g.field_71075_bZ.field_75101_c = b;
      }
   }
}
