package me.zeroeightsix.kami.module.modules.dev;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.util.MovementInput;

@Module$Info(
   name = "GMEntitySpeed",
   category = Module$Category.DEV,
   description = "Godmode EntitySpeed"
)
public class GMEntitySpeed extends Module {
   private Setting<Double> gmentityspeed = this.register(Settings.doubleBuilder("Speed").withRange(0.1D, 10.0D).withValue((Number)1.0D).build());

   private static void speedEntity(Entity entity, Double speed) {
      if (entity instanceof EntityLlama) {
         entity.field_70177_z = mc.field_71439_g.field_70177_z;
         ((EntityLlama)entity).field_70759_as = mc.field_71439_g.field_70759_as;
      }

      MovementInput movementInput = mc.field_71439_g.field_71158_b;
      double forward = (double)movementInput.field_192832_b;
      double strafe = (double)movementInput.field_78902_a;
      float yaw = mc.field_71439_g.field_70177_z;
      if (forward == 0.0D && strafe == 0.0D) {
         entity.field_70159_w = 0.0D;
         entity.field_70179_y = 0.0D;
      } else {
         if (forward != 0.0D) {
            if (strafe > 0.0D) {
               yaw += (float)(forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
               yaw += (float)(forward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (forward > 0.0D) {
               forward = 1.0D;
            } else if (forward < 0.0D) {
               forward = -1.0D;
            }
         }

         entity.field_70159_w = forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F)));
         entity.field_70179_y = forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F)));
         if (entity instanceof EntityMinecart) {
            EntityMinecart em = (EntityMinecart)entity;
            em.func_70016_h(forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))), em.field_70181_x, forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))));
         }
      }

   }

   public void onUpdate() {
      try {
         if (mc.field_71439_g.func_184187_bx() != null) {
            speedEntity(mc.field_71439_g.func_184187_bx(), (Double)this.gmentityspeed.getValue());
         }
      } catch (Exception var2) {
         System.out.println("ERROR: Dude we kinda have a problem here:");
         var2.printStackTrace();
      }

   }
}
