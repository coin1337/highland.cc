package me.zeroeightsix.kami.mixin.client;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Closed;
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class MixinMinecraft {
   @Shadow
   WorldClient field_71441_e;
   @Shadow
   EntityPlayerSP field_71439_g;
   @Shadow
   GuiScreen field_71462_r;
   @Shadow
   GameSettings field_71474_y;
   @Shadow
   GuiIngame field_71456_v;
   @Shadow
   boolean field_71454_w;
   @Shadow
   SoundHandler field_147127_av;

   @Inject(
      method = {"displayGuiScreen"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void displayGuiScreen(GuiScreen guiScreenIn, CallbackInfo info) {
      Closed screenEvent = new Closed(Wrapper.getMinecraft().field_71462_r);
      KamiMod.EVENT_BUS.post(screenEvent);
      Displayed screenEvent1 = new Displayed(guiScreenIn);
      KamiMod.EVENT_BUS.post(screenEvent1);
      GuiScreen guiScreenIn = screenEvent1.getScreen();
      if (guiScreenIn == null && this.field_71441_e == null) {
         guiScreenIn = new GuiMainMenu();
      } else if (guiScreenIn == null && this.field_71439_g.func_110143_aJ() <= 0.0F) {
         guiScreenIn = new GuiGameOver((ITextComponent)null);
      }

      GuiScreen old = this.field_71462_r;
      GuiOpenEvent event = new GuiOpenEvent((GuiScreen)guiScreenIn);
      if (!MinecraftForge.EVENT_BUS.post(event)) {
         guiScreenIn = event.getGui();
         if (old != null && guiScreenIn != old) {
            old.func_146281_b();
         }

         if (guiScreenIn instanceof GuiMainMenu || guiScreenIn instanceof GuiMultiplayer) {
            this.field_71474_y.field_74330_P = false;
            this.field_71456_v.func_146158_b().func_146231_a(true);
         }

         this.field_71462_r = guiScreenIn;
         if (guiScreenIn != null) {
            Minecraft.func_71410_x().func_71364_i();
            KeyBinding.func_74506_a();

            while(true) {
               if (!Mouse.next()) {
                  while(Keyboard.next()) {
                  }

                  ScaledResolution scaledresolution = new ScaledResolution(Minecraft.func_71410_x());
                  int i = scaledresolution.func_78326_a();
                  int j = scaledresolution.func_78328_b();
                  guiScreenIn.func_146280_a(Minecraft.func_71410_x(), i, j);
                  this.field_71454_w = false;
                  break;
               }
            }
         } else {
            this.field_147127_av.func_147687_e();
            Minecraft.func_71410_x().func_71381_h();
         }

         info.cancel();
      }
   }

   @Redirect(
      method = {"run"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/Minecraft;displayCrashReport(Lnet/minecraft/crash/CrashReport;)V"
)
   )
   public void displayCrashReport(Minecraft minecraft, CrashReport crashReport) {
      this.save();
   }

   @Inject(
      method = {"shutdown"},
      at = {@At("HEAD")}
   )
   public void shutdown(CallbackInfo info) {
      this.save();
   }

   private void save() {
      System.out.println("Shutting down: saving KAMI configuration");
      KamiMod.saveConfiguration();
      System.out.println("Configuration saved.");
   }
}
