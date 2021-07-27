package me.zeroeightsix.kami.mixin.client;

import java.util.List;
import me.zeroeightsix.kami.module.modules.render.ExtraTab;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({GuiPlayerTabOverlay.class})
public class MixinGuiPlayerTabOverlay {
   @Redirect(
      method = {"renderPlayerlist"},
      at = @At(
   value = "INVOKE",
   target = "Ljava/util/List;subList(II)Ljava/util/List;"
)
   )
   public List subList(List list, int fromIndex, int toIndex) {
      return list.subList(fromIndex, ExtraTab.INSTANCE.isEnabled() ? Math.min((Integer)ExtraTab.INSTANCE.tabSize.getValue(), list.size()) : toIndex);
   }

   @Inject(
      method = {"getPlayerName"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, CallbackInfoReturnable returnable) {
      if (ExtraTab.INSTANCE.isEnabled()) {
         returnable.cancel();
         returnable.setReturnValue(ExtraTab.getPlayerName(networkPlayerInfoIn));
      }

   }
}
