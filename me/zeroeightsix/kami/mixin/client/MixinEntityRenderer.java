package me.zeroeightsix.kami.mixin.client;

import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.List;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.misc.NoEntityTrace;
import me.zeroeightsix.kami.module.modules.render.AntiFog;
import me.zeroeightsix.kami.module.modules.render.Brightness;
import me.zeroeightsix.kami.module.modules.render.NoHurtCam;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public class MixinEntityRenderer {
   private boolean nightVision = false;

   @Redirect(
      method = {"orientCamera"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/multiplayer/WorldClient;rayTraceBlocks(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/RayTraceResult;"
)
   )
   public RayTraceResult rayTraceBlocks(WorldClient world, Vec3d start, Vec3d end) {
      return ModuleManager.isModuleEnabled("CameraClip") ? null : world.func_72933_a(start, end);
   }

   @Inject(
      method = {"setupFog"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void setupFog(int startCoords, float partialTicks, CallbackInfo callbackInfo) {
      if (AntiFog.enabled() && AntiFog.mode.getValue() == AntiFog.VisionMode.NOFOG) {
         callbackInfo.cancel();
      }

   }

   @Redirect(
      method = {"setupFog"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/ActiveRenderInfo;getBlockStateAtEntityViewpoint(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)Lnet/minecraft/block/state/IBlockState;"
)
   )
   public IBlockState getBlockStateAtEntityViewpoint(World worldIn, Entity entityIn, float p_186703_2_) {
      return AntiFog.enabled() && AntiFog.mode.getValue() == AntiFog.VisionMode.AIR ? Blocks.field_150350_a.field_176228_M : ActiveRenderInfo.func_186703_a(worldIn, entityIn, p_186703_2_);
   }

   @Inject(
      method = {"hurtCameraEffect"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void hurtCameraEffect(float ticks, CallbackInfo info) {
      if (NoHurtCam.shouldDisable()) {
         info.cancel();
      }

   }

   @Redirect(
      method = {"updateLightmap"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/EntityPlayerSP;isPotionActive(Lnet/minecraft/potion/Potion;)Z"
)
   )
   public boolean isPotionActive(EntityPlayerSP player, Potion potion) {
      return (this.nightVision = Brightness.shouldBeActive()) || player.func_70644_a(potion);
   }

   @Redirect(
      method = {"updateLightmap"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/EntityRenderer;getNightVisionBrightness(Lnet/minecraft/entity/EntityLivingBase;F)F"
)
   )
   public float getNightVisionBrightnessMixin(EntityRenderer renderer, EntityLivingBase entity, float partialTicks) {
      return this.nightVision ? Brightness.getCurrentBrightness() : renderer.func_180438_a(entity, partialTicks);
   }

   @Redirect(
      method = {"getMouseOver"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"
)
   )
   public List<Entity> getEntitiesInAABBexcluding(WorldClient worldClient, Entity entityIn, AxisAlignedBB boundingBox, Predicate predicate) {
      return (List)(NoEntityTrace.shouldBlock() ? new ArrayList() : worldClient.func_175674_a(entityIn, boundingBox, predicate));
   }
}
