package me.zeroeightsix.kami.module.modules.dev;

import java.util.Objects;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.play.client.CPacketVehicleMove;

@Module$Info(
   name = "GMVanish",
   category = Module$Category.DEV,
   description = "Godmode Vanish"
)
public class GMVanish extends Module {
   private Entity entity;

   public void onEnable() {
      if (mc.field_71439_g != null && mc.field_71439_g.func_184187_bx() != null) {
         this.entity = mc.field_71439_g.func_184187_bx();
         mc.field_71439_g.func_184210_p();
         mc.field_71441_e.func_72900_e(this.entity);
      } else {
         this.disable();
      }
   }

   public void onUpdate() {
      if (!this.isDisabled() && mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         if (mc.field_71439_g.func_184187_bx() == null) {
            this.disable();
         } else {
            if (this.entity != null) {
               this.entity.field_70165_t = mc.field_71439_g.field_70165_t;
               this.entity.field_70163_u = mc.field_71439_g.field_70163_u;
               this.entity.field_70161_v = mc.field_71439_g.field_70161_v;

               try {
                  ((NetHandlerPlayClient)Objects.requireNonNull(mc.func_147114_u())).func_147297_a(new CPacketVehicleMove(this.entity));
               } catch (Exception var2) {
                  System.out.println("ERROR: Dude we kinda have a problem here:");
                  var2.printStackTrace();
               }
            }

         }
      }
   }
}
