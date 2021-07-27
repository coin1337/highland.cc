package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.math.MathHelper;

@Module$Info(
   name = "ElytraFlight",
   description = "Allows infinite elytra flying",
   category = Module$Category.MOVEMENT
)
public class ElytraFlight extends Module {
   private Setting<ElytraFlight.ElytraFlightMode> mode;

   public ElytraFlight() {
      this.mode = this.register(Settings.e("Mode", ElytraFlight.ElytraFlightMode.BOOST));
   }

   public void onUpdate() {
      if (mc.field_71439_g.func_184613_cA()) {
         switch((ElytraFlight.ElytraFlightMode)this.mode.getValue()) {
         case BOOST:
            if (mc.field_71439_g.func_70090_H()) {
               mc.func_147114_u().func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.START_FALL_FLYING));
               return;
            }

            EntityPlayerSP var10000;
            if (mc.field_71474_y.field_74314_A.func_151470_d()) {
               var10000 = mc.field_71439_g;
               var10000.field_70181_x += 0.08D;
            } else if (mc.field_71474_y.field_74311_E.func_151470_d()) {
               var10000 = mc.field_71439_g;
               var10000.field_70181_x -= 0.04D;
            }

            float yaw;
            if (mc.field_71474_y.field_74351_w.func_151470_d()) {
               yaw = (float)Math.toRadians((double)mc.field_71439_g.field_70177_z);
               var10000 = mc.field_71439_g;
               var10000.field_70159_w -= (double)(MathHelper.func_76126_a(yaw) * 0.05F);
               var10000 = mc.field_71439_g;
               var10000.field_70179_y += (double)(MathHelper.func_76134_b(yaw) * 0.05F);
            } else if (mc.field_71474_y.field_74368_y.func_151470_d()) {
               yaw = (float)Math.toRadians((double)mc.field_71439_g.field_70177_z);
               var10000 = mc.field_71439_g;
               var10000.field_70159_w += (double)(MathHelper.func_76126_a(yaw) * 0.05F);
               var10000 = mc.field_71439_g;
               var10000.field_70179_y -= (double)(MathHelper.func_76134_b(yaw) * 0.05F);
            }
            break;
         case FLY:
            mc.field_71439_g.field_71075_bZ.field_75100_b = true;
         }

      }
   }

   protected void onDisable() {
      if (!mc.field_71439_g.field_71075_bZ.field_75098_d) {
         mc.field_71439_g.field_71075_bZ.field_75100_b = false;
      }
   }

   private static enum ElytraFlightMode {
      BOOST,
      FLY;
   }
}
