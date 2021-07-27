package me.zeroeightsix.kami.module.modules.misc;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

@Module$Info(
   name = "AutoTool",
   description = "Automatically switch to the best tools when mining or attacking",
   category = Module$Category.MISC
)
public class AutoTool extends Module {
   @EventHandler
   private Listener<LeftClickBlock> leftClickListener = new Listener((event) -> {
      this.equipBestTool(mc.field_71441_e.func_180495_p(event.getPos()));
   }, new Predicate[0]);
   @EventHandler
   private Listener<AttackEntityEvent> attackListener = new Listener((event) -> {
      equipBestWeapon();
   }, new Predicate[0]);

   private void equipBestTool(IBlockState blockState) {
      int bestSlot = -1;
      double max = 0.0D;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.field_71439_g.field_71071_by.func_70301_a(i);
         if (!stack.field_190928_g) {
            float speed = stack.func_150997_a(blockState);
            if (speed > 1.0F) {
               int eff;
               speed = (float)((double)speed + ((eff = EnchantmentHelper.func_77506_a(Enchantments.field_185305_q, stack)) > 0 ? Math.pow((double)eff, 2.0D) + 1.0D : 0.0D));
               if ((double)speed > max) {
                  max = (double)speed;
                  bestSlot = i;
               }
            }
         }
      }

      if (bestSlot != -1) {
         equip(bestSlot);
      }

   }

   public static void equipBestWeapon() {
      int bestSlot = -1;
      double maxDamage = 0.0D;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = mc.field_71439_g.field_71071_by.func_70301_a(i);
         if (!stack.field_190928_g) {
            double damage;
            if (stack.func_77973_b() instanceof ItemTool) {
               damage = (double)((ItemTool)stack.func_77973_b()).field_77865_bY + (double)EnchantmentHelper.func_152377_a(stack, EnumCreatureAttribute.UNDEFINED);
               if (damage > maxDamage) {
                  maxDamage = damage;
                  bestSlot = i;
               }
            } else if (stack.func_77973_b() instanceof ItemSword) {
               damage = (double)((ItemSword)stack.func_77973_b()).func_150931_i() + (double)EnchantmentHelper.func_152377_a(stack, EnumCreatureAttribute.UNDEFINED);
               if (damage > maxDamage) {
                  maxDamage = damage;
                  bestSlot = i;
               }
            }
         }
      }

      if (bestSlot != -1) {
         equip(bestSlot);
      }

   }

   private static void equip(int slot) {
      mc.field_71439_g.field_71071_by.field_70461_c = slot;
      mc.field_71442_b.func_78750_j();
   }
}
