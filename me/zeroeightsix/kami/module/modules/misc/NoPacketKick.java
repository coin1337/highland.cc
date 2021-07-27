package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;

@Module$Info(
   name = "NoPacketKick",
   category = Module$Category.MISC,
   description = "Prevent large packets from kicking you"
)
public class NoPacketKick {
   private static NoPacketKick INSTANCE;

   public NoPacketKick() {
      INSTANCE = this;
   }

   public static boolean isEnabled() {
      NoPacketKick var10000 = INSTANCE;
      return isEnabled();
   }
}
