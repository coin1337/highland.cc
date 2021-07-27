package me.zeroeightsix.kami.module.modules.combat;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

@Module$Info(
   name = "Criticals",
   category = Module$Category.COMBAT,
   description = "Automatically does critical attacks"
)
public class Criticals extends Module {
   @EventHandler
   private Listener<AttackEntityEvent> attackEntityEventListener = new Listener((event) -> {
      if (!mc.field_71439_g.func_70090_H() && !mc.field_71439_g.func_180799_ab()) {
         if (mc.field_71439_g.field_70122_E) {
            mc.field_71439_g.field_71174_a.func_147297_a(new Position(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + 0.1625D, mc.field_71439_g.field_70161_v, false));
            mc.field_71439_g.field_71174_a.func_147297_a(new Position(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u, mc.field_71439_g.field_70161_v, false));
            mc.field_71439_g.field_71174_a.func_147297_a(new Position(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + 4.0E-6D, mc.field_71439_g.field_70161_v, false));
            mc.field_71439_g.field_71174_a.func_147297_a(new Position(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u, mc.field_71439_g.field_70161_v, false));
            mc.field_71439_g.field_71174_a.func_147297_a(new Position(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + 1.0E-6D, mc.field_71439_g.field_70161_v, false));
            mc.field_71439_g.field_71174_a.func_147297_a(new Position(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u, mc.field_71439_g.field_70161_v, false));
            mc.field_71439_g.field_71174_a.func_147297_a(new CPacketPlayer());
            mc.field_71439_g.func_71009_b(event.getTarget());
         }

      }
   }, new Predicate[0]);
}
