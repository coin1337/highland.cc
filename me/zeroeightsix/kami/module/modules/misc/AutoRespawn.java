package me.zeroeightsix.kami.module.modules.misc;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.GuiScreenEvent.Displayed;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiScreen;

@Module$Info(
   name = "AutoRespawn",
   description = "Automatically respawns upon death and tells you where you died",
   category = Module$Category.MISC
)
public class AutoRespawn extends Module {
   private Setting<Boolean> deathCoords = this.register(Settings.b("DeathCoords", false));
   private Setting<Boolean> respawn = this.register(Settings.b("Respawn", true));
   @EventHandler
   public Listener<Displayed> listener = new Listener((event) -> {
      if (event.getScreen() instanceof GuiGameOver) {
         if ((Boolean)this.deathCoords.getValue()) {
            Command.sendChatMessage(String.format("You died at x %d y %d z %d", (int)mc.field_71439_g.field_70165_t, (int)mc.field_71439_g.field_70163_u, (int)mc.field_71439_g.field_70161_v));
         }

         if ((Boolean)this.respawn.getValue()) {
            mc.field_71439_g.func_71004_bE();
            mc.func_147108_a((GuiScreen)null);
         }
      }

   }, new Predicate[0]);
}
