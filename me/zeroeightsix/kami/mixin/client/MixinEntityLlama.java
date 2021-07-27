package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.module.ModuleManager;
import net.minecraft.entity.passive.EntityLlama;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityLlama.class})
public class MixinEntityLlama {
   @Inject(
      method = {"canBeSteered"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void canBeSteered(CallbackInfoReturnable<Boolean> returnable) {
      if (ModuleManager.isModuleEnabled("EntitySpeed")) {
         returnable.setReturnValue(true);
      }

   }
}
