package me.zeroeightsix.kami.module.modules.sdashb.render;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

@Module$Info(
   name = "InvPreview",
   category = Module$Category.RENDER,
   description = "View Inventory"
)
public class InventoryViewer extends Module {
   private static final ResourceLocation box = new ResourceLocation("textures/gui/container/shulker_box.png");
   private Setting<Integer> optionX = this.register(Settings.i("X", 198));
   private Setting<Integer> optionY = this.register(Settings.i("Y", 469));

   private static void preboxrender() {
      GL11.glPushMatrix();
      GlStateManager.func_179094_E();
      GlStateManager.func_179118_c();
      GlStateManager.func_179086_m(256);
      GlStateManager.func_179147_l();
   }

   private static void postboxrender() {
      GlStateManager.func_179084_k();
      GlStateManager.func_179097_i();
      GlStateManager.func_179140_f();
      GlStateManager.func_179126_j();
      GlStateManager.func_179141_d();
      GlStateManager.func_179121_F();
      GL11.glPopMatrix();
   }

   private static void preitemrender() {
      GL11.glPushMatrix();
      GL11.glDepthMask(true);
      GlStateManager.func_179086_m(256);
      GlStateManager.func_179097_i();
      GlStateManager.func_179126_j();
      RenderHelper.func_74519_b();
      GlStateManager.func_179152_a(1.0F, 1.0F, 0.01F);
   }

   private static void postitemrender() {
      GlStateManager.func_179152_a(1.0F, 1.0F, 1.0F);
      RenderHelper.func_74518_a();
      GlStateManager.func_179141_d();
      GlStateManager.func_179084_k();
      GlStateManager.func_179140_f();
      GlStateManager.func_179139_a(0.5D, 0.5D, 0.5D);
      GlStateManager.func_179097_i();
      GlStateManager.func_179126_j();
      GlStateManager.func_179152_a(2.0F, 2.0F, 2.0F);
      GL11.glPopMatrix();
   }

   public void onEnable() {
      if (mc.field_71439_g != null) {
         Command.sendChatMessage("[InvPreview] Right click the module to move it around");
      } else if (mc.field_71439_g == null) {
         return;
      }

   }

   public void onRender() {
      NonNullList<ItemStack> items = mc.field_71439_g.field_71071_by.field_70462_a;
      this.boxrender((Integer)this.optionX.getValue(), (Integer)this.optionY.getValue());
      this.itemrender(items, (Integer)this.optionX.getValue(), (Integer)this.optionY.getValue());
   }

   private void boxrender(int x, int y) {
      preboxrender();
      mc.field_71446_o.func_110577_a(box);
      mc.field_71456_v.func_73729_b(x, y, 7, 17, 162, 54);
      postboxrender();
   }

   private void itemrender(NonNullList<ItemStack> items, int x, int y) {
      int size = items.size();

      for(int item = 9; item < size; ++item) {
         int slotx = x + 1 + item % 9 * 18;
         int sloty = y + 1 + (item / 9 - 1) * 18;
         preitemrender();
         mc.func_175599_af().func_180450_b((ItemStack)items.get(item), slotx, sloty);
         mc.func_175599_af().func_175030_a(mc.field_71466_p, (ItemStack)items.get(item), slotx, sloty);
         postitemrender();
      }

   }
}
