package me.zeroeightsix.kami.module.modules.combat;

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
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "WebAuraMin",
   category = Module$Category.COMBAT
)
public class WebAuraMin extends Module {
   private Setting<Double> range = this.register(Settings.d("Range", 5.5D));
   private Setting<Double> blockPerTick = this.register(Settings.d("Blocks per Tick", 8.0D));
   private Setting<Boolean> spoofRotations = this.register(Settings.b("Spoof Rotations", false));
   private Setting<Boolean> spoofHotbar = this.register(Settings.b("Spoof Hotbar", false));
   private Setting<Boolean> debugMessages = this.register(Settings.b("Debug Messages", false));
   private final Vec3d[] offsetList = new Vec3d[]{new Vec3d(0.0D, 2.0D, 0.0D), new Vec3d(0.0D, 1.0D, 0.0D), new Vec3d(0.0D, 0.0D, 0.0D)};
   private boolean slowModeSwitch = false;
   private EntityPlayer closestTarget;
   private int playerHotbarSlot = -1;
   private int lastHotbarSlot = -1;
   private int offsetStep = 0;

   public void onUpdate() {
      if (!this.isDisabled() && mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         if (this.closestTarget != null) {
            if (this.slowModeSwitch) {
               this.slowModeSwitch = false;
            } else {
               for(int i = 0; i < (int)Math.floor((Double)this.blockPerTick.getValue()); ++i) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[WebAuraMin] Loop iteration: " + this.offsetStep);
                  }

                  if (this.offsetStep >= this.offsetList.length) {
                     this.endLoop();
                     return;
                  }

                  Vec3d offset = this.offsetList[this.offsetStep];
                  this.placeBlock((new BlockPos(this.closestTarget.func_174791_d())).func_177977_b().func_177963_a(offset.field_72450_a, offset.field_72448_b, offset.field_72449_c));
                  ++this.offsetStep;
               }

               this.slowModeSwitch = true;
            }
         }
      }
   }

   private void placeBlock(BlockPos blockPos) {
      if (!Wrapper.getWorld().func_180495_p(blockPos).func_185904_a().func_76222_j()) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[WebAuraMin] Block is already placed, skipping");
         }

      } else if (BlockInteractionHelper.checkForNeighbours(blockPos)) {
         this.placeBlockExecute(blockPos);
      }
   }

   public void placeBlockExecute(BlockPos pos) {
      Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + (double)Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
      EnumFacing[] var3 = EnumFacing.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         EnumFacing side = var3[var5];
         BlockPos neighbor = pos.func_177972_a(side);
         EnumFacing side2 = side.func_176734_d();
         if (!BlockInteractionHelper.canBeClicked(neighbor)) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[WebAuraMin] No neighbor to click at!");
            }
         } else {
            Vec3d hitVec = (new Vec3d(neighbor)).func_72441_c(0.5D, 0.5D, 0.5D).func_178787_e((new Vec3d(side2.func_176730_m())).func_186678_a(0.5D));
            if (!(eyesPos.func_72436_e(hitVec) > 18.0625D)) {
               if ((Boolean)this.spoofRotations.getValue()) {
                  BlockInteractionHelper.faceVectorPacketInstant(hitVec);
               }

               boolean needSneak = false;
               Block blockBelow = mc.field_71441_e.func_180495_p(neighbor).func_177230_c();
               if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(blockBelow)) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[WebAuraMin] Sneak enabled!");
                  }

                  needSneak = true;
               }

               if (needSneak) {
                  mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.START_SNEAKING));
               }

               int obiSlot = this.findObiInHotbar();
               if (obiSlot == -1) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[WebAuraMin] No Obi in Hotbar, disabling!");
                  }

                  this.disable();
                  return;
               }

               if (this.lastHotbarSlot != obiSlot) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[WebAuraMin Setting Slot to Obi at  = " + obiSlot);
                  }

                  if ((Boolean)this.spoofHotbar.getValue()) {
                     mc.field_71439_g.field_71174_a.func_147297_a(new CPacketHeldItemChange(obiSlot));
                  } else {
                     Wrapper.getPlayer().field_71071_by.field_70461_c = obiSlot;
                  }

                  this.lastHotbarSlot = obiSlot;
               }

               mc.field_71442_b.func_187099_a(Wrapper.getPlayer(), mc.field_71441_e, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
               mc.field_71439_g.field_71174_a.func_147297_a(new CPacketAnimation(EnumHand.MAIN_HAND));
               if (needSneak) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[WebAurav] Sneak disabled!");
                  }

                  mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.STOP_SNEAKING));
               }

               return;
            }

            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[WebAuraMin] Distance > 4.25 blocks!");
            }
         }
      }

   }

   private int findObiInHotbar() {
      int slot = -1;

      for(int i = 0; i < 9; ++i) {
         ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
         if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
            Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
            if (block instanceof BlockWeb) {
               slot = i;
               break;
            }
         }
      }

      return slot;
   }

   private void findTarget() {
      List<EntityPlayer> playerList = Wrapper.getWorld().field_73010_i;
      Iterator var2 = playerList.iterator();

      while(var2.hasNext()) {
         EntityPlayer target = (EntityPlayer)var2.next();
         if (target != mc.field_71439_g && !Friends.isFriend(target.func_70005_c_()) && EntityUtil.isLiving(target) && !(target.func_110143_aJ() <= 0.0F)) {
            double currentDistance = (double)Wrapper.getPlayer().func_70032_d(target);
            if (!(currentDistance > (Double)this.range.getValue())) {
               if (this.closestTarget == null) {
                  this.closestTarget = target;
               } else if (!(currentDistance >= (double)Wrapper.getPlayer().func_70032_d(this.closestTarget))) {
                  this.closestTarget = target;
               }
            }
         }
      }

   }

   private void endLoop() {
      this.offsetStep = 0;
      if ((Boolean)this.debugMessages.getValue()) {
         Command.sendChatMessage("[WebAuraMin] Ending Loop");
      }

      if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[WebAuraMin] Setting Slot back to  = " + this.playerHotbarSlot);
         }

         if ((Boolean)this.spoofHotbar.getValue()) {
            mc.field_71439_g.field_71174_a.func_147297_a(new CPacketHeldItemChange(this.playerHotbarSlot));
         } else {
            Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
         }

         this.lastHotbarSlot = this.playerHotbarSlot;
      }

      this.findTarget();
   }

   protected void onEnable() {
      if (mc.field_71439_g == null) {
         this.disable();
      } else {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[WebAuraMin] Enabling");
         }

         this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
         this.lastHotbarSlot = -1;
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[WebAuraMin] Saving initial Slot  = " + this.playerHotbarSlot);
         }

         this.findTarget();
      }
   }

   protected void onDisable() {
      if (mc.field_71439_g != null) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[WebAuraMin] Disabling");
         }

         if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[WebAuraMin] Setting Slot to  = " + this.playerHotbarSlot);
            }

            if ((Boolean)this.spoofHotbar.getValue()) {
               mc.field_71439_g.field_71174_a.func_147297_a(new CPacketHeldItemChange(this.playerHotbarSlot));
            } else {
               Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
            }
         }

         this.playerHotbarSlot = -1;
         this.lastHotbarSlot = -1;
      }
   }
}
