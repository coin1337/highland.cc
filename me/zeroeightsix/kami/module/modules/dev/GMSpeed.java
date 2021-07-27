package me.zeroeightsix.kami.module.modules.dev;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.entity.EntityPlayerSP;

@Module$Info(
   name = "GMSpeed",
   category = Module$Category.DEV,
   description = "Godmode Speed"
)
public class GMSpeed extends Module {
   private Setting<Double> gmspeed = this.register(Settings.doubleBuilder("Speed").withRange(0.1D, 10.0D).withValue((Number)1.0D).build());

   public void onUpdate() {
      if ((mc.field_71439_g.field_191988_bg != 0.0F || mc.field_71439_g.field_70702_br != 0.0F) && !mc.field_71439_g.func_70093_af() && mc.field_71439_g.field_70122_E) {
         EntityPlayerSP var10000 = mc.field_71439_g;
         var10000.field_70159_w *= (Double)this.gmspeed.getValue();
         var10000 = mc.field_71439_g;
         var10000.field_70179_y *= (Double)this.gmspeed.getValue();
      }

   }
}
