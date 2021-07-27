package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.BlockInteractionHelper;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "AutoNomadHut",
   category = Module$Category.MISC,
   description = "Builds a NomadHut. Stand still in the middle of a block!"
)
public class AutoNomadHut extends Module {
   private final Vec3d[] surroundTargets = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 0.0D, 1.0D), new Vec3d(1.0D, 0.0D, -1.0D), new Vec3d(-1.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, -1.0D), new Vec3d(2.0D, 0.0D, 0.0D), new Vec3d(2.0D, 0.0D, 1.0D), new Vec3d(2.0D, 0.0D, -1.0D), new Vec3d(-2.0D, 0.0D, 0.0D), new Vec3d(-2.0D, 0.0D, 1.0D), new Vec3d(-2.0D, 0.0D, -1.0D), new Vec3d(0.0D, 0.0D, 2.0D), new Vec3d(1.0D, 0.0D, 2.0D), new Vec3d(-1.0D, 0.0D, 2.0D), new Vec3d(0.0D, 0.0D, -2.0D), new Vec3d(-1.0D, 0.0D, -2.0D), new Vec3d(1.0D, 0.0D, -2.0D), new Vec3d(2.0D, 1.0D, -1.0D), new Vec3d(2.0D, 1.0D, 1.0D), new Vec3d(-2.0D, 1.0D, 0.0D), new Vec3d(-2.0D, 1.0D, 1.0D), new Vec3d(-2.0D, 1.0D, -1.0D), new Vec3d(0.0D, 1.0D, 2.0D), new Vec3d(1.0D, 1.0D, 2.0D), new Vec3d(-1.0D, 1.0D, 2.0D), new Vec3d(0.0D, 1.0D, -2.0D), new Vec3d(1.0D, 1.0D, -2.0D), new Vec3d(-1.0D, 1.0D, -2.0D), new Vec3d(2.0D, 2.0D, -1.0D), new Vec3d(2.0D, 2.0D, 1.0D), new Vec3d(-2.0D, 2.0D, 1.0D), new Vec3d(-2.0D, 2.0D, -1.0D), new Vec3d(1.0D, 2.0D, 2.0D), new Vec3d(-1.0D, 2.0D, 2.0D), new Vec3d(1.0D, 2.0D, -2.0D), new Vec3d(-1.0D, 2.0D, -2.0D), new Vec3d(2.0D, 3.0D, 0.0D), new Vec3d(2.0D, 3.0D, -1.0D), new Vec3d(2.0D, 3.0D, 1.0D), new Vec3d(-2.0D, 3.0D, 0.0D), new Vec3d(-2.0D, 3.0D, 1.0D), new Vec3d(-2.0D, 3.0D, -1.0D), new Vec3d(0.0D, 3.0D, 2.0D), new Vec3d(1.0D, 3.0D, 2.0D), new Vec3d(-1.0D, 3.0D, 2.0D), new Vec3d(0.0D, 3.0D, -2.0D), new Vec3d(1.0D, 3.0D, -2.0D), new Vec3d(-1.0D, 3.0D, -2.0D), new Vec3d(0.0D, 4.0D, 0.0D), new Vec3d(1.0D, 4.0D, 0.0D), new Vec3d(-1.0D, 4.0D, 0.0D), new Vec3d(0.0D, 4.0D, 1.0D), new Vec3d(0.0D, 4.0D, -1.0D), new Vec3d(1.0D, 4.0D, 1.0D), new Vec3d(-1.0D, 4.0D, 1.0D), new Vec3d(-1.0D, 4.0D, -1.0D), new Vec3d(1.0D, 4.0D, -1.0D), new Vec3d(2.0D, 4.0D, 0.0D), new Vec3d(2.0D, 4.0D, 1.0D), new Vec3d(2.0D, 4.0D, -1.0D)};
   private final Vec3d[] surroundTargetsCritical = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, 0.0D), new Vec3d(0.0D, 0.0D, -1.0D), new Vec3d(1.0D, 0.0D, 1.0D), new Vec3d(1.0D, 0.0D, -1.0D), new Vec3d(-1.0D, 0.0D, 1.0D), new Vec3d(-1.0D, 0.0D, -1.0D), new Vec3d(2.0D, 0.0D, 0.0D), new Vec3d(2.0D, 0.0D, 1.0D), new Vec3d(2.0D, 0.0D, -1.0D), new Vec3d(-2.0D, 0.0D, 0.0D), new Vec3d(-2.0D, 0.0D, 1.0D), new Vec3d(-2.0D, 0.0D, -1.0D), new Vec3d(0.0D, 0.0D, 2.0D), new Vec3d(1.0D, 0.0D, 2.0D), new Vec3d(-1.0D, 0.0D, 2.0D), new Vec3d(0.0D, 0.0D, -2.0D), new Vec3d(-1.0D, 0.0D, -2.0D), new Vec3d(1.0D, 0.0D, -2.0D), new Vec3d(2.0D, 1.0D, -1.0D), new Vec3d(2.0D, 1.0D, 1.0D), new Vec3d(-2.0D, 1.0D, 0.0D), new Vec3d(-2.0D, 1.0D, 1.0D), new Vec3d(-2.0D, 1.0D, -1.0D), new Vec3d(0.0D, 1.0D, 2.0D), new Vec3d(1.0D, 1.0D, 2.0D), new Vec3d(-1.0D, 1.0D, 2.0D), new Vec3d(0.0D, 1.0D, -2.0D), new Vec3d(1.0D, 1.0D, -2.0D), new Vec3d(-1.0D, 1.0D, -2.0D), new Vec3d(2.0D, 2.0D, -1.0D), new Vec3d(2.0D, 2.0D, 1.0D), new Vec3d(-2.0D, 2.0D, 1.0D), new Vec3d(-2.0D, 2.0D, -1.0D), new Vec3d(1.0D, 2.0D, 2.0D), new Vec3d(-1.0D, 2.0D, 2.0D), new Vec3d(1.0D, 2.0D, -2.0D), new Vec3d(-1.0D, 2.0D, -2.0D), new Vec3d(2.0D, 3.0D, 0.0D), new Vec3d(2.0D, 3.0D, -1.0D), new Vec3d(2.0D, 3.0D, 1.0D), new Vec3d(-2.0D, 3.0D, 0.0D), new Vec3d(-2.0D, 3.0D, 1.0D), new Vec3d(-2.0D, 3.0D, -1.0D), new Vec3d(0.0D, 3.0D, 2.0D), new Vec3d(1.0D, 3.0D, 2.0D), new Vec3d(-1.0D, 3.0D, 2.0D), new Vec3d(0.0D, 3.0D, -2.0D), new Vec3d(1.0D, 3.0D, -2.0D), new Vec3d(-1.0D, 3.0D, -2.0D), new Vec3d(0.0D, 4.0D, 0.0D), new Vec3d(1.0D, 4.0D, 0.0D), new Vec3d(-1.0D, 4.0D, 0.0D), new Vec3d(0.0D, 4.0D, 1.0D), new Vec3d(0.0D, 4.0D, -1.0D), new Vec3d(1.0D, 4.0D, 1.0D), new Vec3d(-1.0D, 4.0D, 1.0D), new Vec3d(-1.0D, 4.0D, -1.0D), new Vec3d(1.0D, 4.0D, -1.0D), new Vec3d(2.0D, 4.0D, 0.0D), new Vec3d(2.0D, 4.0D, 1.0D), new Vec3d(2.0D, 4.0D, -1.0D)};
   private Setting<Boolean> toggleable = this.register(Settings.b("Toggleable", true));
   private Setting<Boolean> spoofRotations = this.register(Settings.b("Spoof Rotations", false));
   private Setting<Boolean> spoofHotbar = this.register(Settings.b("Spoof Hotbar", false));
   private Setting<Double> blockPerTick = this.register(Settings.d("Blocks per Tick", 1.0D));
   private Setting<Boolean> debugMessages = this.register(Settings.b("Debug Messages", false));
   private BlockPos basePos;
   private int offsetStep = 0;
   private int playerHotbarSlot = -1;
   private int lastHotbarSlot = -1;

   public void onUpdate() {
      if (!this.isDisabled() && mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         if (this.offsetStep == 0) {
            this.basePos = (new BlockPos(mc.field_71439_g.func_174791_d())).func_177977_b();
            this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[AutoFeetPlace] Starting Loop, current Player Slot: " + this.playerHotbarSlot);
            }

            if (!(Boolean)this.spoofHotbar.getValue()) {
               this.lastHotbarSlot = mc.field_71439_g.field_71071_by.field_70461_c;
            }
         }

         for(int i = 0; i < (int)Math.floor((Double)this.blockPerTick.getValue()); ++i) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[AutoFeetPlace] Loop iteration: " + this.offsetStep);
            }

            if (this.offsetStep >= this.surroundTargets.length) {
               this.endLoop();
               return;
            }

            Vec3d offset = this.surroundTargets[this.offsetStep];
            this.placeBlock(new BlockPos(this.basePos.func_177963_a(offset.field_72450_a, offset.field_72448_b, offset.field_72449_c)));
            ++this.offsetStep;
         }

      }
   }

   protected void onEnable() {
      if (mc.field_71439_g == null) {
         this.disable();
      } else {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Enabling");
         }

         this.playerHotbarSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
         this.lastHotbarSlot = -1;
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Saving initial Slot  = " + this.playerHotbarSlot);
         }

      }
   }

   protected void onDisable() {
      if (mc.field_71439_g != null) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Disabling");
         }

         if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[AutoFeetPlace] Setting Slot to  = " + this.playerHotbarSlot);
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

   private void endLoop() {
      this.offsetStep = 0;
      if ((Boolean)this.debugMessages.getValue()) {
         Command.sendChatMessage("[AutoFeetPlace] Ending Loop");
      }

      if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Setting Slot back to  = " + this.playerHotbarSlot);
         }

         if ((Boolean)this.spoofHotbar.getValue()) {
            mc.field_71439_g.field_71174_a.func_147297_a(new CPacketHeldItemChange(this.playerHotbarSlot));
         } else {
            Wrapper.getPlayer().field_71071_by.field_70461_c = this.playerHotbarSlot;
         }

         this.lastHotbarSlot = this.playerHotbarSlot;
      }

      if (!(Boolean)this.toggleable.getValue()) {
         this.disable();
      }

   }

   private void placeBlock(BlockPos blockPos) {
      if (!Wrapper.getWorld().func_180495_p(blockPos).func_185904_a().func_76222_j()) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] Block is already placed, skipping");
         }

      } else if (!BlockInteractionHelper.checkForNeighbours(blockPos)) {
         if ((Boolean)this.debugMessages.getValue()) {
            Command.sendChatMessage("[AutoFeetPlace] !checkForNeighbours(blockPos), disabling! ");
         }

      } else {
         this.placeBlockExecute(blockPos);
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

   public void placeBlockExecute(BlockPos pos) {
      Vec3d eyesPos = new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + (double)Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
      EnumFacing[] var3 = EnumFacing.values();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         EnumFacing side = var3[var5];
         BlockPos neighbor = pos.func_177972_a(side);
         EnumFacing side2 = side.func_176734_d();
         if (!canBeClicked(neighbor)) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[AutoFeetPlace] No neighbor to click at!");
            }
         } else {
            Vec3d hitVec = (new Vec3d(neighbor)).func_72441_c(0.5D, 0.5D, 0.5D).func_178787_e((new Vec3d(side2.func_176730_m())).func_186678_a(0.5D));
            if (!(eyesPos.func_72436_e(hitVec) > 18.0625D)) {
               boolean needSneak = false;
               Block blockBelow = mc.field_71441_e.func_180495_p(neighbor).func_177230_c();
               if (BlockInteractionHelper.blackList.contains(blockBelow) || BlockInteractionHelper.shulkerList.contains(blockBelow)) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[AutoFeetPlace] Sneak enabled!");
                  }

                  needSneak = true;
               }

               if (needSneak) {
                  mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.START_SNEAKING));
               }

               int obiSlot = this.findObiInHotbar();
               if (obiSlot == -1) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[AutoFeetPlace] No Obi in Hotbar, disabling!");
                  }

                  this.disable();
                  return;
               }

               if (this.lastHotbarSlot != obiSlot) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("[AutoFeetPlace] Setting Slot to Obi at  = " + obiSlot);
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
                     Command.sendChatMessage("[AutoFeetPlace] Sneak disabled!");
                  }

                  mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.STOP_SNEAKING));
               }

               return;
            }

            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("[AutoFeetPlace] Distance > 4.25 blocks!");
            }
         }
      }

   }

   private static boolean canBeClicked(BlockPos pos) {
      return getBlock(pos).func_176209_a(getState(pos), false);
   }

   private static Block getBlock(BlockPos pos) {
      return getState(pos).func_177230_c();
   }

   private static IBlockState getState(BlockPos pos) {
      return Wrapper.getWorld().func_180495_p(pos);
   }

   private static void faceVectorPacketInstant(Vec3d vec) {
      float[] rotations = getLegitRotations(vec);
      Wrapper.getPlayer().field_71174_a.func_147297_a(new Rotation(rotations[0], rotations[1], Wrapper.getPlayer().field_70122_E));
   }

   private static float[] getLegitRotations(Vec3d vec) {
      Vec3d eyesPos = getEyesPos();
      double diffX = vec.field_72450_a - eyesPos.field_72450_a;
      double diffY = vec.field_72448_b - eyesPos.field_72448_b;
      double diffZ = vec.field_72449_c - eyesPos.field_72449_c;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{Wrapper.getPlayer().field_70177_z + MathHelper.func_76142_g(yaw - Wrapper.getPlayer().field_70177_z), Wrapper.getPlayer().field_70125_A + MathHelper.func_76142_g(pitch - Wrapper.getPlayer().field_70125_A)};
   }

   private static Vec3d getEyesPos() {
      return new Vec3d(Wrapper.getPlayer().field_70165_t, Wrapper.getPlayer().field_70163_u + (double)Wrapper.getPlayer().func_70047_e(), Wrapper.getPlayer().field_70161_v);
   }
}
