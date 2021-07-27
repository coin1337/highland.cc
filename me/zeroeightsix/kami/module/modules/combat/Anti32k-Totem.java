package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;

@Module$Info(
   name = "AutoTotem",
   category = Module$Category.COMBAT
)
public class AutoTotem extends Module {
   int totems;
   boolean moving = false;
   boolean returnI = false;
   private Setting<Boolean> soft = this.register(Settings.b("Soft"));

   public void onUpdate() {
      if (!(mc.field_71462_r instanceof GuiContainer)) {
         int t;
         int i;
         if (this.returnI) {
            t = -1;

            for(i = 0; i < 45; ++i) {
               if (mc.field_71439_g.field_71071_by.func_70301_a(i).field_190928_g) {
                  t = i;
                  break;
               }
            }

            if (t == -1) {
               return;
            }

            mc.field_71442_b.func_187098_a(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.field_71439_g);
            this.returnI = false;
         }

         this.totems = mc.field_71439_g.field_71071_by.field_70462_a.stream().filter((itemStack) -> {
            return itemStack.func_77973_b() == Items.field_190929_cY;
         }).mapToInt(ItemStack::func_190916_E).sum();
         if (mc.field_71439_g.func_184592_cb().func_77973_b() == Items.field_190929_cY) {
            ++this.totems;
         } else {
            if ((Boolean)this.soft.getValue() && !mc.field_71439_g.func_184592_cb().field_190928_g) {
               return;
            }

            if (this.moving) {
               mc.field_71442_b.func_187098_a(0, 45, 0, ClickType.PICKUP, mc.field_71439_g);
               this.moving = false;
               if (!mc.field_71439_g.field_71071_by.field_70457_g.func_190926_b()) {
                  this.returnI = true;
               }

               return;
            }

            if (mc.field_71439_g.field_71071_by.field_70457_g.func_190926_b()) {
               if (this.totems == 0) {
                  return;
               }

               t = -1;

               for(i = 0; i < 45; ++i) {
                  if (mc.field_71439_g.field_71071_by.func_70301_a(i).func_77973_b() == Items.field_190929_cY) {
                     t = i;
                     break;
                  }
               }

               if (t == -1) {
                  return;
               }

               mc.field_71442_b.func_187098_a(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.field_71439_g);
               this.moving = true;
            } else if (!(Boolean)this.soft.getValue()) {
               t = -1;

               for(i = 0; i < 45; ++i) {
                  if (mc.field_71439_g.field_71071_by.func_70301_a(i).field_190928_g) {
                     t = i;
                     break;
                  }
               }

               if (t == -1) {
                  return;
               }

               mc.field_71442_b.func_187098_a(0, t < 9 ? t + 36 : t, 0, ClickType.PICKUP, mc.field_71439_g);
            }
         }

      }
   }

   public String getHudInfo() {
      return String.valueOf(this.totems);
   }
}
