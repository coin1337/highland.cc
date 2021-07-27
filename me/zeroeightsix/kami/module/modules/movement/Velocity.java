package me.zeroeightsix.kami.module.modules.movement;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.KamiEvent.Era;
import me.zeroeightsix.kami.event.events.EntityEvent.EntityCollision;
import me.zeroeightsix.kami.event.events.PacketEvent.Receive;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;

@Module$Info(
   name = "Velocity",
   description = "Modify knockback impact",
   category = Module$Category.MOVEMENT
)
public class Velocity extends Module {
   private Setting<Float> horizontal = this.register(Settings.f("Horizontal", 0.0F));
   private Setting<Float> vertical = this.register(Settings.f("Vertical", 0.0F));
   @EventHandler
   private Listener<Receive> packetEventListener = new Listener((event) -> {
      if (event.getEra() == Era.PRE) {
         if (event.getPacket() instanceof SPacketEntityVelocity) {
            SPacketEntityVelocity velocityx = (SPacketEntityVelocity)event.getPacket();
            if (velocityx.func_149412_c() == mc.field_71439_g.field_145783_c) {
               if ((Float)this.horizontal.getValue() == 0.0F && (Float)this.vertical.getValue() == 0.0F) {
                  event.cancel();
               }

               velocityx.field_149415_b = (int)((float)velocityx.field_149415_b * (Float)this.horizontal.getValue());
               velocityx.field_149416_c = (int)((float)velocityx.field_149416_c * (Float)this.vertical.getValue());
               velocityx.field_149414_d = (int)((float)velocityx.field_149414_d * (Float)this.horizontal.getValue());
            }
         } else if (event.getPacket() instanceof SPacketExplosion) {
            if ((Float)this.horizontal.getValue() == 0.0F && (Float)this.vertical.getValue() == 0.0F) {
               event.cancel();
            }

            SPacketExplosion velocity = (SPacketExplosion)event.getPacket();
            velocity.field_149152_f *= (Float)this.horizontal.getValue();
            velocity.field_149153_g *= (Float)this.vertical.getValue();
            velocity.field_149159_h *= (Float)this.horizontal.getValue();
         }
      }

   }, new Predicate[0]);
   @EventHandler
   private Listener<EntityCollision> entityCollisionListener = new Listener((event) -> {
      if (event.getEntity() == mc.field_71439_g) {
         if ((Float)this.horizontal.getValue() == 0.0F && (Float)this.vertical.getValue() == 0.0F) {
            event.cancel();
            return;
         }

         event.setX(-event.getX() * (double)(Float)this.horizontal.getValue());
         event.setY(0.0D);
         event.setZ(-event.getZ() * (double)(Float)this.horizontal.getValue());
      }

   }, new Predicate[0]);
}
