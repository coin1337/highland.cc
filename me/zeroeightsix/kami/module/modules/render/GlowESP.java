package me.zeroeightsix.kami.module.modules.render;

import java.util.Iterator;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@Module$Info(
   name = "Glow ESP",
   category = Module$Category.RENDER,
   description = "Gives players glowing effect"
)
public class GlowESP extends Module {
   public void onUpdate() {
      if (Wrapper.getMinecraft().func_175598_ae().field_78733_k != null) {
         Iterator var1 = mc.field_71441_e.field_72996_f.iterator();

         while(var1.hasNext()) {
            Entity entity = (Entity)var1.next();
            if (entity instanceof EntityPlayer && !entity.func_184202_aL()) {
               entity.func_184195_f(true);
            }
         }

      }
   }

   public void onDisable() {
      Iterator var1 = mc.field_71441_e.field_72996_f.iterator();

      while(var1.hasNext()) {
         Entity entity = (Entity)var1.next();
         if (entity instanceof EntityPlayer && entity.func_184202_aL()) {
            entity.func_184195_f(false);
         }
      }

      super.onDisable();
   }
}
