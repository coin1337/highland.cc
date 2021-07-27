package me.zeroeightsix.kami.module.modules.combat;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;

@Module$Info(
   name = "StrengthDetect",
   category = Module$Category.COMBAT,
   description = "Detects when players have Strength 2"
)
public class StrengthDetect extends Module {
   private Setting<Boolean> watermark = this.register(Settings.b("Watermark", true));
   private Setting<Boolean> color = this.register(Settings.b("Color", false));
   private Set<EntityPlayer> str = Collections.newSetFromMap(new WeakHashMap());
   public static final Minecraft mc = Minecraft.func_71410_x();

   public void onUpdate() {
      Iterator var1 = mc.field_71441_e.field_73010_i.iterator();

      while(var1.hasNext()) {
         EntityPlayer player = (EntityPlayer)var1.next();
         if (!player.equals(mc.field_71439_g)) {
            if (player.func_70644_a(MobEffects.field_76420_g) && !this.str.contains(player)) {
               if ((Boolean)this.watermark.getValue()) {
                  if ((Boolean)this.color.getValue()) {
                     Command.sendChatMessage("&a" + player.getDisplayNameString() + " has drank strength");
                  } else {
                     Command.sendChatMessage(player.getDisplayNameString() + " has drank strength");
                  }
               } else if ((Boolean)this.color.getValue()) {
                  Command.sendRawChatMessage("&a" + player.getDisplayNameString() + " has drank strength");
               } else {
                  Command.sendRawChatMessage(player.getDisplayNameString() + " has drank strength");
               }

               this.str.add(player);
            }

            if (this.str.contains(player) && !player.func_70644_a(MobEffects.field_76420_g)) {
               if ((Boolean)this.watermark.getValue()) {
                  if ((Boolean)this.color.getValue()) {
                     Command.sendChatMessage("&c" + player.getDisplayNameString() + " has ran out of strength");
                  } else {
                     Command.sendChatMessage(player.getDisplayNameString() + " has ran out of strength");
                  }
               } else if ((Boolean)this.color.getValue()) {
                  Command.sendRawChatMessage("&c" + player.getDisplayNameString() + " has ran out of strength");
               } else {
                  Command.sendRawChatMessage(player.getDisplayNameString() + " has ran out of strength");
               }

               this.str.remove(player);
            }
         }
      }

   }
}
