package me.zeroeightsix.kami.module.modules.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

@Module$Info(
   name = "Nametags",
   description = "Draws descriptive nametags above entities",
   category = Module$Category.RENDER
)
public class Nametags extends Module {
   private Setting<Boolean> players = this.register(Settings.b("Players", true));
   private Setting<Boolean> animals = this.register(Settings.b("Animals", false));
   private Setting<Boolean> mobs = this.register(Settings.b("Mobs", false));
   private Setting<Double> range = this.register(Settings.d("Range", 200.0D));
   private Setting<Float> scale = this.register(Settings.floatBuilder("Scale").withMinimum(0.5F).withMaximum(10.0F).withValue((Number)1.0F).build());
   private Setting<Boolean> health = this.register(Settings.b("Health", true));
   RenderItem itemRenderer;

   public Nametags() {
      this.itemRenderer = mc.func_175599_af();
   }

   public void onWorldRender(RenderEvent event) {
      if (mc.func_175598_ae().field_78733_k != null) {
         GlStateManager.func_179098_w();
         GlStateManager.func_179140_f();
         GlStateManager.func_179097_i();
         Minecraft.func_71410_x().field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter((entity) -> {
            return !EntityUtil.isFakeLocalPlayer(entity);
         }).filter((entity) -> {
            return entity instanceof EntityPlayer ? (Boolean)this.players.getValue() && mc.field_71439_g != entity : (EntityUtil.isPassive(entity) ? (Boolean)this.animals.getValue() : (Boolean)this.mobs.getValue());
         }).filter((entity) -> {
            return (double)mc.field_71439_g.func_70032_d(entity) < (Double)this.range.getValue();
         }).sorted(Comparator.comparing((entity) -> {
            return -mc.field_71439_g.func_70032_d(entity);
         })).forEach(this::drawNametag);
         GlStateManager.func_179090_x();
         RenderHelper.func_74518_a();
         GlStateManager.func_179145_e();
         GlStateManager.func_179126_j();
      }
   }

   private void drawNametag(Entity entityIn) {
      GlStateManager.func_179094_E();
      Vec3d interp = EntityUtil.getInterpolatedRenderPos(entityIn, mc.func_184121_ak());
      float yAdd = entityIn.field_70131_O + 0.5F - (entityIn.func_70093_af() ? 0.25F : 0.0F);
      double x = interp.field_72450_a;
      double y = interp.field_72448_b + (double)yAdd;
      double z = interp.field_72449_c;
      float viewerYaw = mc.func_175598_ae().field_78735_i;
      float viewerPitch = mc.func_175598_ae().field_78732_j;
      boolean isThirdPersonFrontal = mc.func_175598_ae().field_78733_k.field_74320_O == 2;
      GlStateManager.func_179137_b(x, y, z);
      GlStateManager.func_179114_b(-viewerYaw, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b((float)(isThirdPersonFrontal ? -1 : 1) * viewerPitch, 1.0F, 0.0F, 0.0F);
      float f = mc.field_71439_g.func_70032_d(entityIn);
      float m = f / 8.0F * (float)Math.pow(1.258925437927246D, (double)(Float)this.scale.getValue());
      GlStateManager.func_179152_a(m, m, m);
      FontRenderer fontRendererIn = mc.field_71466_p;
      GlStateManager.func_179152_a(-0.025F, -0.025F, 0.025F);
      String str = entityIn.func_70005_c_() + ((Boolean)this.health.getValue() ? " " + Command.SECTIONSIGN() + "c" + Math.round(((EntityLivingBase)entityIn).func_110143_aJ() + (entityIn instanceof EntityPlayer ? ((EntityPlayer)entityIn).func_110139_bj() : 0.0F)) : "");
      int i = fontRendererIn.func_78256_a(str) / 2;
      GlStateManager.func_179147_l();
      GlStateManager.func_187428_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
      GlStateManager.func_179090_x();
      Tessellator tessellator = Tessellator.func_178181_a();
      BufferBuilder bufferbuilder = tessellator.func_178180_c();
      GL11.glTranslatef(0.0F, -20.0F, 0.0F);
      bufferbuilder.func_181668_a(7, DefaultVertexFormats.field_181706_f);
      bufferbuilder.func_181662_b((double)(-i - 1), 8.0D, 0.0D).func_181666_a(0.0F, 0.0F, 0.0F, 0.5F).func_181675_d();
      bufferbuilder.func_181662_b((double)(-i - 1), 19.0D, 0.0D).func_181666_a(0.0F, 0.0F, 0.0F, 0.5F).func_181675_d();
      bufferbuilder.func_181662_b((double)(i + 1), 19.0D, 0.0D).func_181666_a(0.0F, 0.0F, 0.0F, 0.5F).func_181675_d();
      bufferbuilder.func_181662_b((double)(i + 1), 8.0D, 0.0D).func_181666_a(0.0F, 0.0F, 0.0F, 0.5F).func_181675_d();
      tessellator.func_78381_a();
      bufferbuilder.func_181668_a(2, DefaultVertexFormats.field_181706_f);
      bufferbuilder.func_181662_b((double)(-i - 1), 8.0D, 0.0D).func_181666_a(0.1F, 0.1F, 0.1F, 0.1F).func_181675_d();
      bufferbuilder.func_181662_b((double)(-i - 1), 19.0D, 0.0D).func_181666_a(0.1F, 0.1F, 0.1F, 0.1F).func_181675_d();
      bufferbuilder.func_181662_b((double)(i + 1), 19.0D, 0.0D).func_181666_a(0.1F, 0.1F, 0.1F, 0.1F).func_181675_d();
      bufferbuilder.func_181662_b((double)(i + 1), 8.0D, 0.0D).func_181666_a(0.1F, 0.1F, 0.1F, 0.1F).func_181675_d();
      tessellator.func_78381_a();
      GlStateManager.func_179098_w();
      GlStateManager.func_187432_a(0.0F, 1.0F, 0.0F);
      fontRendererIn.func_78276_b(str, -i, 10, entityIn instanceof EntityPlayer ? (Friends.isFriend(entityIn.func_70005_c_()) ? 1175057 : 16777215) : 16777215);
      GlStateManager.func_187432_a(0.0F, 0.0F, 0.0F);
      GL11.glTranslatef(0.0F, 20.0F, 0.0F);
      GlStateManager.func_179152_a(-40.0F, -40.0F, 40.0F);
      ArrayList<ItemStack> equipment = new ArrayList();
      entityIn.func_184214_aD().forEach((itemStack) -> {
         if (itemStack != null) {
            equipment.add(itemStack);
         }

      });
      ArrayList<ItemStack> armour = new ArrayList();
      entityIn.func_184193_aE().forEach((itemStack) -> {
         if (itemStack != null) {
            armour.add(itemStack);
         }

      });
      Collections.reverse(armour);
      equipment.addAll(armour);
      if (equipment.size() == 0) {
         GlStateManager.func_179121_F();
      } else {
         Collection<ItemStack> a = (Collection)equipment.stream().filter((itemStack) -> {
            return !itemStack.func_190926_b();
         }).collect(Collectors.toList());
         GlStateManager.func_179137_b((double)((float)(a.size() - 1) / 2.0F * 0.5F), 0.6D, 0.0D);
         a.forEach((itemStack) -> {
            GlStateManager.func_179123_a();
            RenderHelper.func_74519_b();
            GlStateManager.func_179139_a(0.5D, 0.5D, 0.0D);
            GlStateManager.func_179140_f();
            this.itemRenderer.field_77023_b = -5.0F;
            this.itemRenderer.func_181564_a(itemStack, itemStack.func_77973_b() == Items.field_185159_cQ ? TransformType.FIXED : TransformType.NONE);
            this.itemRenderer.field_77023_b = 0.0F;
            GlStateManager.func_179152_a(2.0F, 2.0F, 0.0F);
            GlStateManager.func_179099_b();
            GlStateManager.func_179109_b(-0.5F, 0.0F, 0.0F);
         });
         GlStateManager.func_179121_F();
      }
   }
}
