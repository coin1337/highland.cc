package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.world.GameType;

@Module$Info(
   name = "FakeCreative",
   description = "Fake GMC",
   category = Module$Category.MISC
)
public class FakeCreative extends Module {
   public void onEnable() {
      mc.field_71442_b.func_78746_a(GameType.CREATIVE);
   }

   public void onDisable() {
      mc.field_71442_b.func_78746_a(GameType.SURVIVAL);
   }
}
