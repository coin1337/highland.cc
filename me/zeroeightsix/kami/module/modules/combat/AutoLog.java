package me.zeroeightsix.kami.module.modules.combat;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

@Module$Info(
   name = "AutoLog",
   description = "Automatically log when in danger or on low health",
   category = Module$Category.COMBAT
)
public class AutoLog extends Module {
   private Setting<Integer> health = this.register(Settings.integerBuilder("Health").withRange(0, 36).withValue((int)6).build());
   private boolean shouldLog = false;
   long lastLog = System.currentTimeMillis();
   @EventHandler
   private Listener<LivingDamageEvent> livingDamageEventListener = new Listener((event) -> {
      if (mc.field_71439_g != null) {
         if (event.getEntity() == mc.field_71439_g && mc.field_71439_g.func_110143_aJ() - event.getAmount() < (float)(Integer)this.health.getValue()) {
            this.log();
         }

      }
   }, new Predicate[0]);
   @EventHandler
   private Listener<EntityJoinWorldEvent> entityJoinWorldEventListener = new Listener((event) -> {
      if (mc.field_71439_g != null) {
         if (event.getEntity() instanceof EntityEnderCrystal && mc.field_71439_g.func_110143_aJ() - CrystalAura.calculateDamage((EntityEnderCrystal)event.getEntity(), mc.field_71439_g) < (float)(Integer)this.health.getValue()) {
            this.log();
         }

      }
   }, new Predicate[0]);

   public void onUpdate() {
      if (this.shouldLog) {
         this.shouldLog = false;
         if (System.currentTimeMillis() - this.lastLog < 2000L) {
            return;
         }

         Minecraft.func_71410_x().func_147114_u().func_147253_a(new SPacketDisconnect(new TextComponentString("AutoLogged")));
      }

   }

   private void log() {
      ModuleManager.getModuleByName("AutoReconnect").disable();
      this.shouldLog = true;
      this.lastLog = System.currentTimeMillis();
   }
}
