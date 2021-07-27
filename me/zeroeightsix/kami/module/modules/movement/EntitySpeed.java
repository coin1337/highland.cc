package me.zeroeightsix.kami.module.modules.movement;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.util.MovementInput;
import net.minecraft.world.chunk.EmptyChunk;

@Module$Info(
   name = "EntitySpeed",
   category = Module$Category.MOVEMENT,
   description = "Abuse client-sided movement to shape sound barrier breaking rideables"
)
public class EntitySpeed extends Module {
   private Setting<Float> speed = this.register(Settings.f("Speed", 1.0F));
   private Setting<Boolean> antiStuck = this.register(Settings.b("AntiStuck"));
   private Setting<Boolean> flight = this.register(Settings.b("Flight", false));
   private Setting<Boolean> wobble = this.register(Settings.booleanBuilder("Wobble").withValue(true).withVisibility((b) -> {
      return (Boolean)this.flight.getValue();
   }).build());
   private static Setting<Float> opacity = Settings.f("Boat opacity", 0.5F);

   public EntitySpeed() {
      this.register(opacity);
   }

   public void onUpdate() {
      if (mc.field_71441_e != null && mc.field_71439_g.func_184187_bx() != null) {
         Entity riding = mc.field_71439_g.func_184187_bx();
         if (!(riding instanceof EntityPig) && !(riding instanceof AbstractHorse)) {
            if (riding instanceof EntityBoat) {
               this.steerBoat(this.getBoat());
            }
         } else {
            this.steerEntity(riding);
         }
      }

   }

   private void steerEntity(Entity entity) {
      if (!(Boolean)this.flight.getValue()) {
         entity.field_70181_x = -0.4D;
      }

      if ((Boolean)this.flight.getValue()) {
         if (mc.field_71474_y.field_74314_A.func_151470_d()) {
            entity.field_70181_x = (double)(Float)this.speed.getValue();
         } else if (mc.field_71474_y.field_74351_w.func_151470_d() || mc.field_71474_y.field_74368_y.func_151470_d()) {
            entity.field_70181_x = (Boolean)this.wobble.getValue() ? Math.sin((double)mc.field_71439_g.field_70173_aa) : 0.0D;
         }
      }

      this.moveForward(entity, (double)(Float)this.speed.getValue() * 3.8D);
      if (entity instanceof EntityHorse) {
         entity.field_70177_z = mc.field_71439_g.field_70177_z;
      }

   }

   private void steerBoat(EntityBoat boat) {
      if (boat != null) {
         boolean forward = mc.field_71474_y.field_74351_w.func_151470_d();
         boolean left = mc.field_71474_y.field_74370_x.func_151470_d();
         boolean right = mc.field_71474_y.field_74366_z.func_151470_d();
         boolean back = mc.field_71474_y.field_74368_y.func_151470_d();
         if (!forward || !back) {
            boat.field_70181_x = 0.0D;
         }

         if (mc.field_71474_y.field_74314_A.func_151470_d()) {
            boat.field_70181_x += (double)((Float)this.speed.getValue() / 2.0F);
         }

         if (forward || left || right || back) {
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

            if (angle != -1) {
               float yaw = mc.field_71439_g.field_70177_z + (float)angle;
               boat.field_70159_w = EntityUtil.getRelativeX(yaw) * (double)(Float)this.speed.getValue();
               boat.field_70179_y = EntityUtil.getRelativeZ(yaw) * (double)(Float)this.speed.getValue();
            }
         }
      }
   }

   public void onRender() {
      EntityBoat boat = this.getBoat();
      if (boat != null) {
         boat.field_70177_z = mc.field_71439_g.field_70177_z;
         boat.func_184442_a(false, false, false, false);
      }
   }

   private EntityBoat getBoat() {
      return mc.field_71439_g.func_184187_bx() != null && mc.field_71439_g.func_184187_bx() instanceof EntityBoat ? (EntityBoat)mc.field_71439_g.func_184187_bx() : null;
   }

   private void moveForward(Entity entity, double speed) {
      if (entity != null) {
         MovementInput movementInput = mc.field_71439_g.field_71158_b;
         double forward = (double)movementInput.field_192832_b;
         double strafe = (double)movementInput.field_78902_a;
         boolean movingForward = forward != 0.0D;
         boolean movingStrafe = strafe != 0.0D;
         float yaw = mc.field_71439_g.field_70177_z;
         if (!movingForward && !movingStrafe) {
            this.setEntitySpeed(entity, 0.0D, 0.0D);
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
               } else {
                  forward = -1.0D;
               }
            }

            double motX = forward * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F))) + strafe * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F)));
            double motZ = forward * speed * Math.sin(Math.toRadians((double)(yaw + 90.0F))) - strafe * speed * Math.cos(Math.toRadians((double)(yaw + 90.0F)));
            if (this.isBorderingChunk(entity, motX, motZ)) {
               motZ = 0.0D;
               motX = 0.0D;
            }

            this.setEntitySpeed(entity, motX, motZ);
         }
      }

   }

   private void setEntitySpeed(Entity entity, double motX, double motZ) {
      entity.field_70159_w = motX;
      entity.field_70179_y = motZ;
   }

   private boolean isBorderingChunk(Entity entity, double motX, double motZ) {
      return (Boolean)this.antiStuck.getValue() && mc.field_71441_e.func_72964_e((int)(entity.field_70165_t + motX) >> 4, (int)(entity.field_70161_v + motZ) >> 4) instanceof EmptyChunk;
   }

   public static float getOpacity() {
      return (Float)opacity.getValue();
   }
}
