package me.zeroeightsix.kami.module.modules.misc;

import java.util.Random;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.util.EnumHand;

@Module$Info(
   name = "AntiAFK",
   category = Module$Category.MISC,
   description = "Moves in order not to get kicked. (May be invisible client-sided)"
)
public class AntiAFK extends Module {
   private Setting<Boolean> swing = this.register(Settings.b("Swing", true));
   private Setting<Boolean> turn = this.register(Settings.b("Turn", true));
   private Random random = new Random();

   public void onUpdate() {
      if (!mc.field_71442_b.func_181040_m()) {
         if (mc.field_71439_g.field_70173_aa % 40 == 0 && (Boolean)this.swing.getValue()) {
            mc.func_147114_u().func_147297_a(new CPacketAnimation(EnumHand.MAIN_HAND));
         }

         if (mc.field_71439_g.field_70173_aa % 15 == 0 && (Boolean)this.turn.getValue()) {
            mc.field_71439_g.field_70177_z = (float)(this.random.nextInt(360) - 180);
         }

         if (!(Boolean)this.swing.getValue() && !(Boolean)this.turn.getValue() && mc.field_71439_g.field_70173_aa % 80 == 0) {
            mc.field_71439_g.func_70664_aZ();
         }

      }
   }
}
