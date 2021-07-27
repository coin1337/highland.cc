package me.zeroeightsix.kami.module.modules.combat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "AutoTrap2",
   category = Module$Category.COMBAT
)
public class AutoTrap2 extends Module {
   private final Vec3d[] offsetsDefault = new Vec3d[]{new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 1.0D, -1.0D), new Vec3d(1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 1.0D, 1.0D), new Vec3d(-1.0D, 1.0D, 0.0D), new Vec3d(0.0D, 2.0D, -1.0D), new Vec3d(1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 2.0D, 1.0D), new Vec3d(-1.0D, 2.0D, 0.0D), new Vec3d(0.0D, 3.0D, -1.0D), new Vec3d(0.0D, 3.0D, 0.0D)};
   private Setting<Double> range = this.register(Settings.d("Range", 5.5D));
   private Setting<Integer> blockPerTick = this.register(Settings.i("Blocks per Tick", 4));
   private Setting<Boolean> rotate = this.register(Settings.b("Rotate", true));
   private Setting<Boolean> announceUsage = this.register(Settings.b("Announce Usage", false));
   private EntityPlayer closestTarget;
   private String lastTickTargetName;
   private int playerHotbarSlot = -1;
   private int lastHotbarSlot = -1;
   private boolean isSneaking = false;
   private int offsetStep = 0;
   private boolean firstRun;

   protected void onEnable() {
      if (mc.field_71439_g == null) {
         this.disable();
      } else {
         this.firstRun = true;
         this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
         this.lastHotbarSlot = -1;
      }
   }

   protected void onDisable() {
      if (mc.field_71439_g != null) {
         if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
         }

         if (this.isSneaking) {
            mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.STOP_SNEAKING));
            this.isSneaking = false;
         }

         this.playerHotbarSlot = -1;
         this.lastHotbarSlot = -1;
         if ((Boolean)this.announceUsage.getValue()) {
            Command.sendChatMessage("[AutoTrap2] Disabled!");
         }

      }
   }

   public void onUpdate() {
      if (mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         this.findClosestTarget();
         if (this.closestTarget == null) {
            if (this.firstRun) {
               this.firstRun = false;
               if ((Boolean)this.announceUsage.getValue()) {
                  Command.sendChatMessage("[AutoTrap2] Enabled, waiting for target.");
               }
            }

         } else {
            if (this.firstRun) {
               this.firstRun = false;
               this.lastTickTargetName = this.closestTarget.func_70005_c_();
               if ((Boolean)this.announceUsage.getValue()) {
                  Command.sendChatMessage("[AutoTrap2] Enabled, target: " + this.lastTickTargetName);
               }
            } else if (!this.lastTickTargetName.equals(this.closestTarget.func_70005_c_())) {
               this.lastTickTargetName = this.closestTarget.func_70005_c_();
               this.offsetStep = 0;
               if ((Boolean)this.announceUsage.getValue()) {
                  Command.sendChatMessage("[AutoTrap2] New target: " + this.lastTickTargetName);
               }
            }

            List<Vec3d> placeTargets = new ArrayList();
            Collections.addAll(placeTargets, this.offsetsDefault);

            int blocksPlaced;
            for(blocksPlaced = 0; blocksPlaced < (Integer)this.blockPerTick.getValue(); ++this.offsetStep) {
               if (this.offsetStep >= placeTargets.size()) {
                  this.offsetStep = 0;
                  break;
               }

               BlockPos offsetPos = new BlockPos((Vec3d)placeTargets.get(this.offsetStep));
               BlockPos targetPos = (new BlockPos(this.closestTarget.func_174791_d())).func_177977_b().func_177982_a(offsetPos.field_177962_a, offsetPos.field_177960_b, offsetPos.field_177961_c);
               boolean shouldTryToPlace = true;
               if (!Wrapper.getWorld().func_180495_p(targetPos).func_185904_a().func_76222_j()) {
                  shouldTryToPlace = false;
               }

               Iterator var6 = mc.field_71441_e.func_72839_b((Entity)null, new AxisAlignedBB(targetPos)).iterator();

               while(var6.hasNext()) {
                  Entity entity = (Entity)var6.next();
                  if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                     shouldTryToPlace = false;
                     break;
                  }
               }

               if (shouldTryToPlace && this.placeBlock(targetPos)) {
                  ++blocksPlaced;
               }
            }

            if (blocksPlaced > 0) {
               if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
                  Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
                  this.lastHotbarSlot = this.playerHotbarSlot;
               }

               if (this.isSneaking) {
                  mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.STOP_SNEAKING));
                  this.isSneaking = false;
               }
            }

         }
      }
   }

   private boolean placeBlock(BlockPos pos) {
      if (!mc.field_71441_e.func_180495_p(pos).func_185904_a().func_76222_j()) {
         return false;
      } else if (!BlockInteractionHelper.checkForNeighbours(pos)) {
         return false;
      } else {
         Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + (double)Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
         EnumFacing[] var3 = EnumFacing.values();
         int var4 = var3.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            EnumFacing side = var3[var5];
            BlockPos neighbor = pos.func_177972_a(side);
            EnumFacing side2 = side.func_176734_d();
            if (mc.field_71441_e.func_180495_p(neighbor).func_177230_c().func_176209_a(mc.field_71441_e.func_180495_p(neighbor), false)) {
               Vec3d hitVec = (new Vec3d(neighbor)).func_72441_c(0.5D, 0.5D, 0.5D).func_178787_e((new Vec3d(side2.func_176730_m())).func_186678_a(0.5D));
               if (eyesPos.func_72438_d(hitVec) <= (Double)this.range.getValue()) {
                  int obiSlot = this.findObiInHotbar();
                  if (obiSlot == -1) {
                     this.disable();
                     return false;
                  }

                  if (this.lastHotbarSlot != obiSlot) {
                     Wrapper.getPlayer().field_71071_by.field_70461_c = obiSlot;
                     this.lastHotbarSlot = obiSlot;
                  }

                  Block neighborPos = mc.field_71441_e.func_180495_p(neighbor).func_177230_c();
                  if (BlockInteractionHelper.blackList.contains(neighborPos) || BlockInteractionHelper.shulkerList.contains(neighborPos)) {
                     mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.START_SNEAKING));
                     this.isSneaking = true;
                  }

                  if ((Boolean)this.rotate.getValue()) {
                     BlockInteractionHelper.faceVectorPacketInstant(hitVec);
                  }

                  mc.field_71442_b.func_187099_a(mc.field_71439_g, mc.field_71441_e, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
                  mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
                  return true;
               }
            }
         }

         return false;
      }
   }

   private int findObiInHotbar() {
      int slot = -1;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
         if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
            if (block instanceof BlockObsidian) {
               slot = i;
               break;
            }
         }
      }

      return slot;
   }

   private void findClosestTarget() {
      List<EntityPlayer> playerList = Wrapper.getWorld().field_73010_i;
      this.closestTarget = null;
      Iterator var2 = playerList.iterator();

      while(var2.hasNext()) {
         EntityPlayer target = (EntityPlayer)var2.next();
         if (target != mc.field_71439_g && !Friends.isFriend(target.func_70005_c_()) && EntityUtil.isLiving(target) && !(target.func_110143_aJ() <= 0.0F)) {
            if (this.closestTarget == null) {
               this.closestTarget = target;
            } else if (!(Wrapper.getPlayer().func_70032_d(target) >= Wrapper.getPlayer().func_70032_d(this.closestTarget))) {
               this.closestTarget = target;
            }
         }
      }

   }
}
