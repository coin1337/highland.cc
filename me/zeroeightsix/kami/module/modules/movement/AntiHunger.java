package me.zeroeightsix.kami.module.modules.movement;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent.Send;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.network.play.client.CPacketPlayer;

@Module$Info(
   name = "AntiHunger",
   category = Module$Category.MOVEMENT,
   description = "Lose hunger less fast. Might cause ghostblocks."
)
public class AntiHunger extends Module {
   @EventHandler
   public Listener<Send> packetListener = new Listener((event) -> {
      if (event.getPacket() instanceof CPacketPlayer) {
         ((CPacketPlayer)event.getPacket()).field_149474_g = false;
      }

   }, new Predicate[0]);
}
