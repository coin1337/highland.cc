package me.zeroeightsix.kami.module.modules.combat;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.Friends;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "Auto32k",
   category = Module$Category.COMBAT
)
public class Auto32k extends Module {
   private static final DecimalFormat df = new DecimalFormat("#.#");
   private static final List<Block> shulkerList;
   private Setting<Boolean> moveToHotbar = this.register(Settings.b("Move 32k to Hotbar", true));
   private Setting<Double> placeRange = this.register(Settings.d("Place Range", 4.0D));
   private Setting<Boolean> placeCloseToEnemy = this.register(Settings.b("Place close to enemy", false));
   private Setting<Boolean> placeObiOnTop = this.register(Settings.b("Place Obi on Top", true));
   private Setting<Boolean> spoofRotation = this.register(Settings.b("Spoof Rotation", true));
   private Setting<Boolean> raytraceCheck = this.register(Settings.b("Raytrace Check", false));
   private Setting<Boolean> debugMessages = this.register(Settings.b("Debug Messages", false));
   private int swordSlot;

   protected void onEnable() {
      if (!this.isDisabled() && mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         df.setRoundingMode(RoundingMode.CEILING);
         int hopperSlot = -1;
         int shulkerSlot = -1;
         int obiSlot = -1;
         this.swordSlot = -1;

         int i;
         for(i = 0; i < 9 && (hopperSlot == -1 || shulkerSlot == -1 || obiSlot == -1); ++i) {
            ItemStack stack = mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
               Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
               if (block == Blocks.field_150438_bZ) {
                  hopperSlot = i;
               } else if (shulkerList.contains(block)) {
                  shulkerSlot = i;
               } else if (block == Blocks.field_150343_Z) {
                  obiSlot = i;
               }
            }
         }

         if (hopperSlot == -1) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("Hopper missing, disabling.");
            }

            this.disable();
         } else if (shulkerSlot == -1) {
            if ((Boolean)this.debugMessages.getValue()) {
               Command.sendChatMessage("Shulker missing, disabling.");
            }

            this.disable();
         } else {
            i = (int)Math.ceil((Double)this.placeRange.getValue());
            CrystalAura crystalAura = (CrystalAura)ModuleManager.getModuleByName("CrystalAura");
            List<BlockPos> placeTargetList = crystalAura.getSphere(CrystalAura.getPlayerPos(), (float)i, i, false, true, 0);
            Map<BlockPos, Double> placeTargetMap = new HashMap();
            BlockPos placeTarget = null;
            boolean useRangeSorting = false;
            Iterator var10 = placeTargetList.iterator();

            BlockPos pos;
            while(var10.hasNext()) {
               pos = (BlockPos)var10.next();
               Iterator var12 = mc.field_71441_e.field_72996_f.iterator();

               while(var12.hasNext()) {
                  Entity entity = (Entity)var12.next();
                  if (entity instanceof EntityPlayer && entity != mc.field_71439_g && !Friends.isFriend(entity.func_70005_c_())) {
                     useRangeSorting = true;
                     if (this.isAreaPlaceable(pos)) {
                        double distanceToEntity = entity.func_70011_f((double)pos.field_177962_a, (double)pos.field_177960_b, (double)pos.field_177961_c);
                        placeTargetMap.put(pos, placeTargetMap.containsKey(pos) ? (Double)placeTargetMap.get(pos) + distanceToEntity : distanceToEntity);
                     }
                  }
               }
            }

            if (placeTargetMap.size() > 0) {
               placeTargetMap.forEach((k, v) -> {
                  if (!this.isAreaPlaceable(k)) {
                     placeTargetMap.remove(k);
                  }

               });
               if (placeTargetMap.size() == 0) {
                  useRangeSorting = false;
               }
            }

            if (useRangeSorting) {
               if ((Boolean)this.placeCloseToEnemy.getValue()) {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("Placing close to Enemy");
                  }

                  placeTarget = (BlockPos)((Entry)Collections.min(placeTargetMap.entrySet(), Entry.comparingByValue())).getKey();
               } else {
                  if ((Boolean)this.debugMessages.getValue()) {
                     Command.sendChatMessage("Placing far from Enemy");
                  }

                  placeTarget = (BlockPos)((Entry)Collections.max(placeTargetMap.entrySet(), Entry.comparingByValue())).getKey();
               }
            } else {
               if ((Boolean)this.debugMessages.getValue()) {
                  Command.sendChatMessage("No enemy nearby, placing at first found");
               }

               var10 = placeTargetList.iterator();

               while(var10.hasNext()) {
                  pos = (BlockPos)var10.next();
                  if (this.isAreaPlaceable(pos)) {
                     placeTarget = pos;
                     break;
                  }
               }
            }

            if (placeTarget == null) {
               if ((Boolean)this.debugMessages.getValue()) {
                  Command.sendChatMessage("No valid position in range to place.");
               }

               this.disable();
            } else {
               if ((Boolean)this.debugMessages.getValue()) {
                  Command.sendChatMessage("Place Target: " + placeTarget.toString() + " Distance: " + df.format(mc.field_71439_g.func_174791_d().func_72438_d(new Vec3d(placeTarget))));
               }

               mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.START_SNEAKING));
               mc.field_71439_g.field_71071_by.field_70461_c = hopperSlot;
               placeBlock(new BlockPos(placeTarget), (Boolean)this.spoofRotation.getValue());
               mc.field_71439_g.field_71071_by.field_70461_c = shulkerSlot;
               placeBlock(new BlockPos(placeTarget.func_177982_a(0, 1, 0)), (Boolean)this.spoofRotation.getValue());
               if ((Boolean)this.placeObiOnTop.getValue() && obiSlot != -1) {
                  mc.field_71439_g.field_71071_by.field_70461_c = obiSlot;
                  placeBlock(new BlockPos(placeTarget.func_177982_a(0, 2, 0)), (Boolean)this.spoofRotation.getValue());
               }

               mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.STOP_SNEAKING));
               mc.field_71439_g.field_71071_by.field_70461_c = shulkerSlot;
               BlockPos hopperPos = new BlockPos(placeTarget);
               mc.field_71439_g.field_71174_a.func_147297_a(new CPacketPlayerTryUseItemOnBlock(hopperPos, EnumFacing.DOWN, EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
               this.swordSlot = shulkerSlot + 32;
            }
         }
      } else {
         this.disable();
      }
   }

   public void onUpdate() {
      if (!this.isDisabled() && mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         if (mc.field_71462_r instanceof GuiContainer) {
            if (!(Boolean)this.moveToHotbar.getValue()) {
               this.disable();
            } else if (this.swordSlot != -1) {
               boolean swapReady = true;
               if (((GuiContainer)mc.field_71462_r).field_147002_h.func_75139_a(0).func_75211_c().field_190928_g) {
                  swapReady = false;
               }

               if (!((GuiContainer)mc.field_71462_r).field_147002_h.func_75139_a(this.swordSlot).func_75211_c().field_190928_g) {
                  swapReady = false;
               }

               if (swapReady) {
                  mc.field_71442_b.func_187098_a(((GuiContainer)mc.field_71462_r).field_147002_h.field_75152_c, 0, this.swordSlot - 32, ClickType.SWAP, mc.field_71439_g);
                  this.disable();
               }

            }
         }
      }
   }

   private boolean isAreaPlaceable(BlockPos blockPos) {
      Iterator var2 = mc.field_71441_e.func_72839_b((Entity)null, new AxisAlignedBB(blockPos)).iterator();

      while(var2.hasNext()) {
         Entity entity = (Entity)var2.next();
         if (entity instanceof EntityLivingBase) {
            return false;
         }
      }

      if (!mc.field_71441_e.func_180495_p(blockPos).func_185904_a().func_76222_j()) {
         return false;
      } else if (!mc.field_71441_e.func_180495_p(blockPos.func_177982_a(0, 1, 0)).func_185904_a().func_76222_j()) {
         return false;
      } else if (mc.field_71441_e.func_180495_p(blockPos.func_177982_a(0, -1, 0)).func_177230_c() instanceof BlockAir) {
         return false;
      } else if (mc.field_71441_e.func_180495_p(blockPos.func_177982_a(0, -1, 0)).func_177230_c() instanceof BlockLiquid) {
         return false;
      } else if (mc.field_71439_g.func_174791_d().func_72438_d(new Vec3d(blockPos)) > (Double)this.placeRange.getValue()) {
         return false;
      } else if (!(Boolean)this.raytraceCheck.getValue()) {
         return true;
      } else {
         RayTraceResult result = mc.field_71441_e.func_147447_a(new Vec3d(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + (double)mc.field_71439_g.func_70047_e(), mc.field_71439_g.field_70161_v), new Vec3d(blockPos), false, true, false);
         return result == null || result.func_178782_a().equals(blockPos);
      }
   }

   private static void placeBlock(BlockPos pos, boolean spoofRotation) {
      if (mc.field_71441_e.func_180495_p(pos).func_185904_a().func_76222_j()) {
         EnumFacing[] var2 = EnumFacing.values();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            EnumFacing side = var2[var4];
            BlockPos neighbor = pos.func_177972_a(side);
            EnumFacing side2 = side.func_176734_d();
            if (mc.field_71441_e.func_180495_p(neighbor).func_177230_c().func_176209_a(mc.field_71441_e.func_180495_p(neighbor), false)) {
               Vec3d hitVec = (new Vec3d(neighbor)).func_72441_c(0.5D, 0.5D, 0.5D).func_178787_e((new Vec3d(side2.func_176730_m())).func_186678_a(0.5D));
               if (spoofRotation) {
                  Vec3d eyesPos = new Vec3d(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + (double)mc.field_71439_g.func_70047_e(), mc.field_71439_g.field_70161_v);
                  double diffX = hitVec.field_72450_a - eyesPos.field_72450_a;
                  double diffY = hitVec.field_72448_b - eyesPos.field_72448_b;
                  double diffZ = hitVec.field_72449_c - eyesPos.field_72449_c;
                  double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
                  float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
                  float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
                  mc.field_71439_g.field_71174_a.func_147297_a(new Rotation(mc.field_71439_g.field_70177_z + MathHelper.func_76142_g(yaw - mc.field_71439_g.field_70177_z), mc.field_71439_g.field_70125_A + MathHelper.func_76142_g(pitch - mc.field_71439_g.field_70125_A), mc.field_71439_g.field_70122_E));
               }

               mc.field_71442_b.func_187099_a(mc.field_71439_g, mc.field_71441_e, neighbor, side2, hitVec, EnumHand.MAIN_HAND);
               mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
               mc.field_71467_ac = 4;
               return;
            }
         }

      }
   }

   static {
      shulkerList = Arrays.asList(Blocks.field_190977_dl, Blocks.field_190978_dm, Blocks.field_190979_dn, Blocks.field_190980_do, Blocks.field_190981_dp, Blocks.field_190982_dq, Blocks.field_190983_dr, Blocks.field_190984_ds, Blocks.field_190985_dt, Blocks.field_190986_du, Blocks.field_190987_dv, Blocks.field_190988_dw, Blocks.field_190989_dx, Blocks.field_190990_dy, Blocks.field_190991_dz, Blocks.field_190975_dA);
   }
}
