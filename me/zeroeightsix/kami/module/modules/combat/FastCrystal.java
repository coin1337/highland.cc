package me.zeroeightsix.kami.module.modules.combat;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent.Receive;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.init.Items;

@Module$Info(
   name = "FastCrystal",
   category = Module$Category.COMBAT,
   description = "Place crystals faster"
)
public class FastCrystal extends Module {
   @EventHandler
   private Listener<Receive> receiveListener = new Listener((event) -> {
      if (mc.field_71439_g != null && (mc.field_71439_g.func_184614_ca().func_77973_b() == Items.field_185158_cP || mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_185158_cP)) {
         mc.field_71467_ac = 0;
      }

   }, new Predicate[0]);
}
