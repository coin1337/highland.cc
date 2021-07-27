package me.zeroeightsix.kami.module.modules.misc;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Closed;
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

@Module$Info(
   name = "AutoReconnect",
   description = "Automatically reconnects after being disconnected",
   category = Module$Category.MISC,
   alwaysListening = true
)
public class AutoReconnect extends Module {
   private Setting<Integer> seconds = this.register(Settings.integerBuilder("Seconds").withValue((int)5).withMinimum(0).build());
   private static ServerData cServer;
   @EventHandler
   public Listener<Closed> closedListener = new Listener((event) -> {
      if (event.getScreen() instanceof GuiConnecting) {
         cServer = mc.field_71422_O;
      }

   }, new Predicate[0]);
   @EventHandler
   public Listener<Displayed> displayedListener = new Listener((event) -> {
      if (this.isEnabled() && event.getScreen() instanceof GuiDisconnected && (cServer != null || mc.field_71422_O != null)) {
         event.setScreen(new AutoReconnect.KamiGuiDisconnected((GuiDisconnected)event.getScreen()));
      }

   }, new Predicate[0]);

   private class KamiGuiDisconnected extends GuiDisconnected {
      int millis;
      long cTime;

      public KamiGuiDisconnected(GuiDisconnected disconnected) {
         super(disconnected.field_146307_h, disconnected.field_146306_a, disconnected.field_146304_f);
         this.millis = (Integer)AutoReconnect.this.seconds.getValue() * 1000;
         this.cTime = System.currentTimeMillis();
      }

      public void func_73876_c() {
         if (this.millis <= 0) {
            this.field_146297_k.func_147108_a(new GuiConnecting(this.field_146307_h, this.field_146297_k, AutoReconnect.cServer == null ? this.field_146297_k.field_71422_O : AutoReconnect.cServer));
         }

      }

      public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
         super.func_73863_a(mouseX, mouseY, partialTicks);
         long a = System.currentTimeMillis();
         this.millis = (int)((long)this.millis - (a - this.cTime));
         this.cTime = a;
         String s = "Reconnecting in " + Math.max(0.0D, Math.floor((double)this.millis / 100.0D) / 10.0D) + "s";
         this.field_146289_q.func_175065_a(s, (float)(this.field_146294_l / 2 - this.field_146289_q.func_78256_a(s) / 2), (float)(this.field_146295_m - 16), 16777215, true);
      }
   }
}
