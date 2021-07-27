package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.entity.EntityPlayerSP;

@Module$Info(
   name = "Speed",
   description = "Modify player speed on ground.",
   category = Module$Category.MOVEMENT
)
public class Speed extends Module {
   private Setting<Float> speed = this.register(Settings.f("Speed", 0.0F));

   public void onUpdate() {
      if ((mc.field_71439_g.field_191988_bg != 0.0F || mc.field_71439_g.field_70702_br != 0.0F) && !mc.field_71439_g.func_70093_af() && mc.field_71439_g.field_70122_E) {
         mc.field_71439_g.func_70664_aZ();
         EntityPlayerSP var10000 = mc.field_71439_g;
         var10000.field_70159_w *= 1.4D;
         var10000 = mc.field_71439_g;
         var10000.field_70181_x *= 0.4D;
         var10000 = mc.field_71439_g;
         var10000.field_70179_y *= 1.4D;
      }

   }
}
