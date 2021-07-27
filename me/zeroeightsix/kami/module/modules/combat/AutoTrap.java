package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import java.util.List;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "AutoTrap",
   category = Module$Category.COMBAT
)
public class AutoTrap extends Module {
   BlockPos abovehead;
   BlockPos aboveheadpartner;
   BlockPos side1;
   BlockPos side2;
   BlockPos side3;
   BlockPos side4;
   BlockPos side11;
   BlockPos side22;
   BlockPos side33;
   BlockPos side44;
   int test;
   int delay;
   public static EntityPlayer target;
   public static List<EntityPlayer> targets;
   public static float yaw;
   public static float pitch;

   public boolean isInBlockRange(Entity target) {
      return target.func_70032_d(mc.field_71439_g) <= 4.0F;
   }

   public static boolean canBeClicked(BlockPos pos) {
      return mc.field_71441_e.func_180495_p(pos).func_177230_c().func_176209_a(mc.field_71441_e.func_180495_p(pos), false);
   }

   private static void faceVectorPacket(Vec3d vec) {
      double diffX = vec.field_72450_a - mc.field_71439_g.field_70165_t;
      double diffY = vec.field_72448_b - (mc.field_71439_g.field_70163_u + (double)mc.field_71439_g.func_70047_e());
      double diffZ = vec.field_72449_c - mc.field_71439_g.field_70161_v;
      double dist = (double)MathHelper.func_76133_a(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, dist)));
      mc.func_147114_u().func_147297_a(new Rotation(mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - mc.field_71439_g.field_70177_z), mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - mc.field_71439_g.field_70125_A), mc.field_71439_g.field_70122_E));
   }

   public boolean isValid(EntityPlayer entity) {
      return entity instanceof EntityPlayer && this.isInBlockRange(entity) && entity.func_110143_aJ() > 0.0F && !entity.field_70128_L && !entity.func_70005_c_().startsWith("Body #") && !Friends.isFriend(entity.func_70005_c_());
   }

   public void loadTargets() {
      Iterator var1 = mc.field_71441_e.field_73010_i.iterator();

      while(var1.hasNext()) {
         EntityPlayer player = (EntityPlayer)var1.next();
         if (!(player instanceof EntityPlayerSP)) {
            if (this.isValid(player)) {
               targets.add(player);
            } else if (targets.contains(player)) {
               targets.remove(player);
            }
         }
      }

   }

   private boolean isStackObby(ItemStack stack) {
      return stack != null && stack.func_77973_b() == Item.func_150899_d(49);
   }

   private boolean doesHotbarHaveObby() {
      for(int i = 36; i < 45; ++i) {
         ItemStack stack = mc.field_71439_g.field_71069_bz.func_75139_a(i).func_75211_c();
         if (stack != null && this.isStackObby(stack)) {
            return true;
         }
      }

      return false;
   }

   public static Block getBlock(BlockPos pos) {
      return getState(pos).func_177230_c();
   }

   public static IBlockState getState(BlockPos pos) {
      return mc.field_71441_e.func_180495_p(pos);
   }

   public static boolean placeBlockLegit(BlockPos pos) {
      Vec3d eyesPos = new Vec3d(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + (double)mc.field_71439_g.func_70047_e(), mc.field_71439_g.field_70161_v);
      EnumFacing[] var2 = EnumFacing.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EnumFacing side = var2[var4];
         BlockPos neighbor = pos.func_177972_a(side);
         if (canBeClicked(neighbor)) {
            Vec3d hitVec = eyesPos.func_178787_e((new Vec3d(side.func_176730_m())).func_186678_a(0.5D));
            if (!(eyesPos.func_72436_e(hitVec) > 36.0D)) {
               mc.field_71442_b.func_187099_a(mc.field_71439_g, mc.field_71441_e, neighbor, side.func_176734_d(), hitVec, EnumHand.MAIN_HAND);
               mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
               return true;
            }
         }
      }

      return false;
   }

   public void onUpdate() {
      ++this.test;
      if (this.test == 20) {
         super.toggle();
      }

      if (!mc.field_71439_g.func_184587_cr()) {
         if (!this.isValid(target) || target == null) {
            this.updateTarget();
         }

         Iterator var1 = mc.field_71441_e.field_73010_i.iterator();

         while(var1.hasNext()) {
            EntityPlayer player = (EntityPlayer)var1.next();
            if (!(player instanceof EntityPlayerSP) && this.isValid(player) && player.func_70032_d(mc.field_71439_g) < target.func_70032_d(mc.field_71439_g)) {
               target = player;
               return;
            }
         }

         if (this.isValid(target) && mc.field_71439_g.func_70032_d(target) < 4.0F) {
            this.trap(target);
         } else {
            this.delay = 0;
         }

      }
   }

   public static double roundToHalf(double d) {
      return (double)Math.round(d * 2.0D) / 2.0D;
   }

   private void trap(EntityPlayer player) {
      this.delay = 11;
      if ((double)player.field_191988_bg != 0.0D && (double)player.field_70702_br != 0.0D && (double)player.field_70701_bs != 0.0D) {
         this.delay = 0;
      }

      if (!this.doesHotbarHaveObby()) {
         this.delay = 0;
      }

      if (this.doesHotbarHaveObby()) {
         this.abovehead = new BlockPos(player.field_70165_t, player.field_70163_u + 2.0D, player.field_70161_v);
         this.aboveheadpartner = new BlockPos(player.field_70165_t + 1.0D, player.field_70163_u + 2.0D, player.field_70161_v);
         this.side1 = new BlockPos(player.field_70165_t + 1.0D, player.field_70163_u, player.field_70161_v);
         this.side2 = new BlockPos(player.field_70165_t, player.field_70163_u, player.field_70161_v + 1.0D);
         this.side3 = new BlockPos(player.field_70165_t - 1.0D, player.field_70163_u, player.field_70161_v);
         this.side4 = new BlockPos(player.field_70165_t, player.field_70163_u, player.field_70161_v - 1.0D);
         this.side11 = new BlockPos(player.field_70165_t + 1.0D, player.field_70163_u + 1.0D, player.field_70161_v);
         this.side22 = new BlockPos(player.field_70165_t, player.field_70163_u + 1.0D, player.field_70161_v + 1.0D);
         this.side33 = new BlockPos(player.field_70165_t - 1.0D, player.field_70163_u + 1.0D, player.field_70161_v);
         this.side44 = new BlockPos(player.field_70165_t, player.field_70163_u + 1.0D, player.field_70161_v - 1.0D);

         for(int i = 36; i < 45; ++i) {
            ItemStack stack = mc.field_71439_g.field_71069_bz.func_75139_a(i).func_75211_c();
            if (stack != null && this.isStackObby(stack)) {
               int oldSlot = mc.field_71439_g.field_71071_by.field_70461_c;
               if (mc.field_71441_e.func_180495_p(this.abovehead).func_185904_a().func_76222_j() || mc.field_71441_e.func_180495_p(this.side1).func_185904_a().func_76222_j() || mc.field_71441_e.func_180495_p(this.side2).func_185904_a().func_76222_j() || mc.field_71441_e.func_180495_p(this.side3).func_185904_a().func_76222_j() || mc.field_71441_e.func_180495_p(this.side4).func_185904_a().func_76222_j()) {
                  mc.field_71439_g.field_71071_by.field_70461_c = i - 36;
                  if (mc.field_71441_e.func_180495_p(this.side1).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side1);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side2).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side2);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side3).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side3);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side4).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side4);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side11).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side11);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side22).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side22);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side33).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side33);
                  }

                  if (mc.field_71441_e.func_180495_p(this.side44).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.side44);
                  }

                  if (mc.field_71441_e.func_180495_p(this.aboveheadpartner).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.aboveheadpartner);
                  }

                  if (mc.field_71441_e.func_180495_p(this.abovehead).func_185904_a().func_76222_j()) {
                     placeBlockLegit(this.abovehead);
                  }

                  mc.field_71439_g.field_71071_by.field_70461_c = oldSlot;
                  this.delay = 0;
                  break;
               }

               this.delay = 0;
            }

            this.delay = 0;
         }
      }

   }

   public void onDisable() {
      this.delay = 0;
      yaw = mc.field_71439_g.field_70177_z;
      pitch = mc.field_71439_g.field_70125_A;
      target = null;
      this.test = 0;
   }

   public void updateTarget() {
      Iterator var1 = mc.field_71441_e.field_73010_i.iterator();

      while(var1.hasNext()) {
         EntityPlayer player = (EntityPlayer)var1.next();
         if (!(player instanceof EntityPlayerSP) && !(player instanceof EntityPlayerSP) && this.isValid(player)) {
            target = player;
         }
      }

   }

   public EnumFacing getEnumFacing(float posX, float posY, float posZ) {
      return EnumFacing.func_176737_a(posX, posY, posZ);
   }

   public BlockPos getBlockPos(double x, double y, double z) {
      BlockPos pos = new BlockPos(x, y, z);
      return pos;
   }
}
