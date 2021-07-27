package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.item.ItemExpBottle;

@Module$Info(
   name = "EXPFast",
   category = Module$Category.COMBAT,
   description = "Makes EXP Faster for PvP"
)
public class EXPFast extends Module {
   public void onUpdate() {
      if (mc.field_71439_g.field_71071_by.func_70448_g().func_77973_b() instanceof ItemExpBottle) {
         mc.field_71467_ac = 0;
      }

   }
}
