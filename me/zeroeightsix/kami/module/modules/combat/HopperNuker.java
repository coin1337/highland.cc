package me.zeroeightsix.kami.module.modules.combat;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "HopperNuker",
   category = Module$Category.COMBAT
)
public class HopperNuker extends Module {
   private Setting<Double> range = this.register(Settings.d("Range", 5.5D));
   private Setting<Boolean> pickswitch = this.register(Settings.b("Auto Switch", false));
   private int oldSlot = -1;
   private boolean isMining = false;

   public void onUpdate() {
      BlockPos pos = this.getNearestHopper();
      if (pos != null) {
         if (!this.isMining) {
            this.oldSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
            this.isMining = true;
         }

         float[] angle = calcAngle(Wrapper.getPlayer().func_174824_e(Wrapper.getMinecraft().func_184121_ak()), new Vec3d((double)((float)pos.func_177958_n() + 0.5F), (double)((float)pos.func_177956_o() + 0.5F), (double)((float)pos.func_177952_p() + 0.5F)));
         Wrapper.getPlayer().field_70177_z = angle[0];
         Wrapper.getPlayer().field_70759_as = angle[0];
         Wrapper.getPlayer().field_70125_A = angle[1];
         if (this.canBreak(pos)) {
            if ((Boolean)this.pickswitch.getValue()) {
               int newSlot = -1;

               for(int i = 0; i < 9; ++i) {
                  ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
                  if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemPickaxe) {
                     newSlot = i;
                     break;
                  }
               }

               if (newSlot != -1) {
                  Wrapper.getPlayer().field_71071_by.field_70461_c = newSlot;
               }
            }

            Wrapper.getMinecraft().field_71442_b.func_180512_c(pos, Wrapper.getPlayer().func_174811_aO());
            Wrapper.getPlayer().func_184609_a(EnumHand.MAIN_HAND);
         }
      } else if ((Boolean)this.pickswitch.getValue() && this.oldSlot != -1) {
         Wrapper.getPlayer().field_71071_by.field_70461_c = this.oldSlot;
         this.oldSlot = -1;
         this.isMining = false;
      }

   }

   private boolean canBreak(BlockPos pos) {
      IBlockState blockState = Wrapper.getWorld().func_180495_p(pos);
      Block block = blockState.func_177230_c();
      return block.func_176195_g(blockState, Wrapper.getWorld(), pos) != -1.0F;
   }

   private BlockPos getNearestHopper() {
      Double maxDist = (Double)this.range.getValue();
      BlockPos ret = null;

      for(Double x = maxDist; x >= -maxDist; x = x - 1.0D) {
         for(Double y = maxDist; y >= -maxDist; y = y - 1.0D) {
            for(Double z = maxDist; z >= -maxDist; z = z - 1.0D) {
               BlockPos pos = new BlockPos(Wrapper.getPlayer().field_70165_t + x, Wrapper.getPlayer().field_70163_u + y, Wrapper.getPlayer().field_70161_v + z);
               double dist = Wrapper.getPlayer().func_70011_f((double)pos.func_177958_n(), (double)pos.func_177956_o(), (double)pos.func_177952_p());
               if (dist <= maxDist && Wrapper.getWorld().func_180495_p(pos).func_177230_c() == Blocks.field_150438_bZ && this.canBreak(pos) && (double)pos.func_177956_o() >= Wrapper.getPlayer().field_70163_u) {
                  maxDist = dist;
                  ret = pos;
               }
            }
         }
      }

      return ret;
   }

   private static float[] calcAngle(Vec3d from, Vec3d to) {
      double difX = to.field_72450_a - from.field_72450_a;
      double difY = (to.field_72448_b - from.field_72448_b) * -1.0D;
      double difZ = to.field_72449_c - from.field_72449_c;
      double dist = (double)MathHelper.func_76133_a(difX * difX + difZ * difZ);
      return new float[]{(float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0D), (float)MathHelper.func_76138_g(Math.toDegrees(Math.atan2(difY, dist)))};
   }
}
