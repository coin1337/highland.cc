package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;

@Module$Info(
   name = "SafeWalk",
   category = Module$Category.MOVEMENT,
   description = "Keeps you from walking off edges"
)
public class SafeWalk extends Module {
   private static SafeWalk INSTANCE;

   public SafeWalk() {
      INSTANCE = this;
   }

   public static boolean shouldSafewalk() {
      return INSTANCE.isEnabled();
   }
}
