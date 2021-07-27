package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation;

@Module$Info(
   category = Module$Category.MOVEMENT,
   description = "Makes the player fly",
   name = "Flight"
)
public class Flight extends Module {
   private Setting<Float> speed = this.register(Settings.f("Speed", 10.0F));
   private Setting<Flight.FlightMode> mode;

   public Flight() {
      this.mode = this.register(Settings.e("Mode", Flight.FlightMode.VANILLA));
   }

   protected void onEnable() {
      if (mc.field_71439_g != null) {
         switch((Flight.FlightMode)this.mode.getValue()) {
         case VANILLA:
            mc.field_71439_g.field_71075_bZ.field_75100_b = true;
            if (mc.field_71439_g.field_71075_bZ.field_75098_d) {
               return;
            } else {
               mc.field_71439_g.field_71075_bZ.field_75101_c = true;
            }
         default:
         }
      }
   }

   public void onUpdate() {
      switch((Flight.FlightMode)this.mode.getValue()) {
      case VANILLA:
         mc.field_71439_g.field_71075_bZ.func_75092_a((Float)this.speed.getValue() / 100.0F);
         mc.field_71439_g.field_71075_bZ.field_75100_b = true;
         if (mc.field_71439_g.field_71075_bZ.field_75098_d) {
            return;
         }

         mc.field_71439_g.field_71075_bZ.field_75101_c = true;
         break;
      case STATIC:
         mc.field_71439_g.field_71075_bZ.field_75100_b = false;
         mc.field_71439_g.field_70159_w = 0.0D;
         mc.field_71439_g.field_70181_x = 0.0D;
         mc.field_71439_g.field_70179_y = 0.0D;
         mc.field_71439_g.field_70747_aH = (Float)this.speed.getValue();
         EntityPlayerSP var10000;
         if (mc.field_71474_y.field_74314_A.func_151470_d()) {
            var10000 = mc.field_71439_g;
            var10000.field_70181_x += (double)(Float)this.speed.getValue();
         }

         if (mc.field_71474_y.field_74311_E.func_151470_d()) {
            var10000 = mc.field_71439_g;
            var10000.field_70181_x -= (double)(Float)this.speed.getValue();
         }
         break;
      case PACKET:
         boolean forward = mc.field_71474_y.field_74351_w.func_151470_d();
         boolean left = mc.field_71474_y.field_74370_x.func_151470_d();
         boolean right = mc.field_71474_y.field_74366_z.func_151470_d();
         boolean back = mc.field_71474_y.field_74368_y.func_151470_d();
         int angle;
         if (left && right) {
            angle = forward ? 0 : (back ? 180 : -1);
         } else if (forward && back) {
            angle = left ? -90 : (right ? 90 : -1);
         } else {
            angle = left ? -90 : (right ? 90 : 0);
            if (forward) {
               angle /= 2;
            } else if (back) {
               angle = 180 - angle / 2;
            }
         }

         if (angle != -1 && (forward || left || right || back)) {
            float yaw = mc.field_71439_g.field_70177_z + (float)angle;
            mc.field_71439_g.field_70159_w = EntityUtil.getRelativeX(yaw) * 0.20000000298023224D;
            mc.field_71439_g.field_70179_y = EntityUtil.getRelativeZ(yaw) * 0.20000000298023224D;
         }

         mc.field_71439_g.field_70181_x = 0.0D;
         mc.field_71439_g.field_71174_a.func_147297_a(new PositionRotation(mc.field_71439_g.field_70165_t + mc.field_71439_g.field_70159_w, mc.field_71439_g.field_70163_u + (Minecraft.func_71410_x().field_71474_y.field_74314_A.func_151470_d() ? 0.0622D : 0.0D) - (Minecraft.func_71410_x().field_71474_y.field_74311_E.func_151470_d() ? 0.0622D : 0.0D), mc.field_71439_g.field_70161_v + mc.field_71439_g.field_70179_y, mc.field_71439_g.field_70177_z, mc.field_71439_g.field_70125_A, false));
         mc.field_71439_g.field_71174_a.func_147297_a(new PositionRotation(mc.field_71439_g.field_70165_t + mc.field_71439_g.field_70159_w, mc.field_71439_g.field_70163_u - 42069.0D, mc.field_71439_g.field_70161_v + mc.field_71439_g.field_70179_y, mc.field_71439_g.field_70177_z, mc.field_71439_g.field_70125_A, true));
      }

   }

   protected void onDisable() {
      switch((Flight.FlightMode)this.mode.getValue()) {
      case VANILLA:
         mc.field_71439_g.field_71075_bZ.field_75100_b = false;
         mc.field_71439_g.field_71075_bZ.func_75092_a(0.05F);
         if (mc.field_71439_g.field_71075_bZ.field_75098_d) {
            return;
         } else {
            mc.field_71439_g.field_71075_bZ.field_75101_c = false;
         }
      default:
      }
   }

   public double[] moveLooking() {
      return new double[]{(double)(mc.field_71439_g.field_70177_z * 360.0F / 360.0F * 180.0F / 180.0F), 0.0D};
   }

   public static enum FlightMode {
      VANILLA,
      STATIC,
      PACKET;
   }
}
