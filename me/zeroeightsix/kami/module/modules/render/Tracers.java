package me.zeroeightsix.kami.module.modules.render;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.HueCycler;
import me.zeroeightsix.kami.util.ColourUtils.Colors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@Module$Info(
   name = "Tracers",
   description = "Draws lines to other living entities",
   category = Module$Category.RENDER
)
public class Tracers extends Module {
   private Setting<Boolean> players = this.register(Settings.b("Players", true));
   private Setting<Boolean> friends = this.register(Settings.b("Friends", true));
   private Setting<Boolean> animals = this.register(Settings.b("Animals", false));
   private Setting<Boolean> mobs = this.register(Settings.b("Mobs", false));
   private Setting<Double> range = this.register(Settings.d("Range", 200.0D));
   private Setting<Float> opacity = this.register(Settings.floatBuilder("Opacity").withRange(0.0F, 1.0F).withValue((Number)1.0F));
   HueCycler cycler = new HueCycler(3600);

   public void onWorldRender(RenderEvent event) {
      GlStateManager.func_179094_E();
      Minecraft.func_71410_x().field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter((entity) -> {
         return !EntityUtil.isFakeLocalPlayer(entity);
      }).filter((entity) -> {
         return entity instanceof EntityPlayer ? (Boolean)this.players.getValue() && mc.field_71439_g != entity : (EntityUtil.isPassive(entity) ? (Boolean)this.animals.getValue() : (Boolean)this.mobs.getValue());
      }).filter((entity) -> {
         return (double)mc.field_71439_g.func_70032_d(entity) < (Double)this.range.getValue();
      }).forEach((entity) -> {
         int colour = this.getColour(entity);
         if (colour == Integer.MIN_VALUE) {
            if (!(Boolean)this.friends.getValue()) {
               return;
            }

            colour = this.cycler.current();
         }

         float r = (float)(colour >>> 16 & 255) / 255.0F;
         float g = (float)(colour >>> 8 & 255) / 255.0F;
         float b = (float)(colour & 255) / 255.0F;
         drawLineToEntity(entity, r, g, b, (Float)this.opacity.getValue());
      });
      GlStateManager.func_179121_F();
   }

   public void onUpdate() {
      this.cycler.next();
   }

   private void drawRainbowToEntity(Entity entity, float opacity) {
      Vec3d eyes = (new Vec3d(0.0D, 0.0D, 1.0D)).func_178789_a(-((float)Math.toRadians((double)Minecraft.func_71410_x().field_71439_g.field_70125_A))).func_178785_b(-((float)Math.toRadians((double)Minecraft.func_71410_x().field_71439_g.field_70177_z)));
      double[] xyz = interpolate(entity);
      double posx = xyz[0];
      double posy = xyz[1];
      double posz = xyz[2];
      double posx2 = eyes.field_72450_a;
      double posy2 = eyes.field_72448_b + (double)mc.field_71439_g.func_70047_e();
      double posz2 = eyes.field_72449_c;
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3042);
      GL11.glLineWidth(1.5F);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      this.cycler.reset();
      this.cycler.setNext(opacity);
      GlStateManager.func_179140_f();
      GL11.glLoadIdentity();
      mc.field_71460_t.func_78467_g(mc.func_184121_ak());
      GL11.glBegin(1);
      GL11.glVertex3d(posx, posy, posz);
      GL11.glVertex3d(posx2, posy2, posz2);
      this.cycler.setNext(opacity);
      GL11.glVertex3d(posx2, posy2, posz2);
      GL11.glVertex3d(posx2, posy2, posz2);
      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      GL11.glColor3d(1.0D, 1.0D, 1.0D);
      GlStateManager.func_179145_e();
   }

   private int getColour(Entity entity) {
      if (entity instanceof EntityPlayer) {
         return Friends.isFriend(entity.func_70005_c_()) ? Integer.MIN_VALUE : Colors.WHITE;
      } else {
         return EntityUtil.isPassive(entity) ? Colors.GREEN : Colors.RED;
      }
   }

   public static double interpolate(double now, double then) {
      return then + (now - then) * (double)mc.func_184121_ak();
   }

   public static double[] interpolate(Entity entity) {
      double posX = interpolate(entity.field_70165_t, entity.field_70142_S) - mc.func_175598_ae().field_78725_b;
      double posY = interpolate(entity.field_70163_u, entity.field_70137_T) - mc.func_175598_ae().field_78726_c;
      double posZ = interpolate(entity.field_70161_v, entity.field_70136_U) - mc.func_175598_ae().field_78723_d;
      return new double[]{posX, posY, posZ};
   }

   public static void drawLineToEntity(Entity e, float red, float green, float blue, float opacity) {
      double[] xyz = interpolate(e);
      drawLine(xyz[0], xyz[1], xyz[2], (double)e.field_70131_O, red, green, blue, opacity);
   }

   public static void drawLine(double posx, double posy, double posz, double up, float red, float green, float blue, float opacity) {
      Vec3d eyes = (new Vec3d(0.0D, 0.0D, 1.0D)).func_178789_a(-((float)Math.toRadians((double)Minecraft.func_71410_x().field_71439_g.field_70125_A))).func_178785_b(-((float)Math.toRadians((double)Minecraft.func_71410_x().field_71439_g.field_70177_z)));
      drawLineFromPosToPos(eyes.field_72450_a, eyes.field_72448_b + (double)mc.field_71439_g.func_70047_e(), eyes.field_72449_c, posx, posy, posz, up, red, green, blue, opacity);
   }

   public static void drawLineFromPosToPos(double posx, double posy, double posz, double posx2, double posy2, double posz2, double up, float red, float green, float blue, float opacity) {
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3042);
      GL11.glLineWidth(1.5F);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glColor4f(red, green, blue, opacity);
      GlStateManager.func_179140_f();
      GL11.glLoadIdentity();
      mc.field_71460_t.func_78467_g(mc.func_184121_ak());
      GL11.glBegin(1);
      GL11.glVertex3d(posx, posy, posz);
      GL11.glVertex3d(posx2, posy2, posz2);
      GL11.glVertex3d(posx2, posy2, posz2);
      GL11.glVertex3d(posx2, posy2 + up, posz2);
      GL11.glEnd();
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      GL11.glColor3d(1.0D, 1.0D, 1.0D);
      GlStateManager.func_179145_e();
   }
}
