package me.zeroeightsix.kami.module.modules.combat;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;

@Module$Info(
   name = "AntiDeathScreen",
   description = "Fixs random death screen glitches",
   category = Module$Category.COMBAT
)
public class AntiDeathScreen extends Module {
   private Setting<Boolean> respawn = this.register(Settings.b("Respawn", true));
   @EventHandler
   public Listener<Displayed> listener = new Listener((event) -> {
      if (event.getScreen() instanceof GuiGameOver && (Boolean)this.respawn.getValue()) {
         mc.field_71439_g.func_71004_bE();
         if (!mc.field_71439_g.field_70128_L) {
            mc.func_147108_a((GuiScreen)null);
         }
      }

   }, new Predicate[0]);
}
