package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.render.Chams;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({RenderLiving.class})
public class MixinRenderLiving {
   @Inject(
      method = {"doRender"},
      at = {@At("HEAD")}
   )
   private void injectChamsPre(EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
      if (ModuleManager.isModuleEnabled("Chams") && Chams.renderChams(entity)) {
         GL11.glEnable(32823);
         GL11.glPolygonOffset(1.0F, -1000000.0F);
      }

   }

   @Inject(
      method = {"doRender"},
      at = {@At("RETURN")}
   )
   private <S extends EntityLivingBase> void injectChamsPost(EntityLiving entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
      if (ModuleManager.isModuleEnabled("Chams") && Chams.renderChams(entity)) {
         GL11.glPolygonOffset(1.0F, 1000000.0F);
         GL11.glDisable(32823);
      }

   }
}
