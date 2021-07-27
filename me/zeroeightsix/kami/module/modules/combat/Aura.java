package me.zeroeightsix.kami.module.modules.combat;

import java.util.Iterator;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.misc.AutoTool;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Friends;
import me.zeroeightsix.kami.util.LagCompensator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "Aura",
   category = Module$Category.COMBAT,
   description = "Hits entities around you"
)
public class Aura extends Module {
   private Setting<Boolean> attackPlayers = this.register(Settings.b("Players", true));
   private Setting<Boolean> attackMobs = this.register(Settings.b("Mobs", false));
   private Setting<Boolean> attackAnimals = this.register(Settings.b("Animals", false));
   private Setting<Double> hitRange = this.register(Settings.d("Hit Range", 5.5D));
   private Setting<Boolean> ignoreWalls = this.register(Settings.b("Ignore Walls", true));
   private Setting<Aura.WaitMode> waitMode;
   private Setting<Integer> waitTick;
   private Setting<Boolean> switchTo32k;
   private Setting<Boolean> onlyUse32k;
   private int waitCounter;

   public Aura() {
      this.waitMode = this.register(Settings.e("Mode", Aura.WaitMode.DYNAMIC));
      this.waitTick = this.register(Settings.integerBuilder("Tick Delay").withMinimum(0).withValue((int)3).withVisibility((o) -> {
         return ((Aura.WaitMode)this.waitMode.getValue()).equals(Aura.WaitMode.STATIC);
      }).build());
      this.switchTo32k = this.register(Settings.b("32k Switch", true));
      this.onlyUse32k = this.register(Settings.b("32k Only", false));
   }

   public void onUpdate() {
      if (!mc.field_71439_g.field_70128_L) {
         boolean shield = mc.field_71439_g.func_184592_cb().func_77973_b().equals(Items.field_185159_cQ) && mc.field_71439_g.func_184600_cs() == EnumHand.OFF_HAND;
         if (!mc.field_71439_g.func_184587_cr() || shield) {
            if (((Aura.WaitMode)this.waitMode.getValue()).equals(Aura.WaitMode.DYNAMIC)) {
               if (mc.field_71439_g.func_184825_o(this.getLagComp()) < 1.0F) {
                  return;
               }

               if (mc.field_71439_g.field_70173_aa % 2 != 0) {
                  return;
               }
            }

            if (((Aura.WaitMode)this.waitMode.getValue()).equals(Aura.WaitMode.STATIC) && (Integer)this.waitTick.getValue() > 0) {
               if (this.waitCounter < (Integer)this.waitTick.getValue()) {
                  ++this.waitCounter;
                  return;
               }

               this.waitCounter = 0;
            }

            Iterator entityIterator = Minecraft.func_71410_x().field_71441_e.field_72996_f.iterator();

            Entity target;
            while(true) {
               do {
                  do {
                     do {
                        do {
                           do {
                              do {
                                 if (!entityIterator.hasNext()) {
                                    return;
                                 }

                                 target = (Entity)entityIterator.next();
                              } while(!EntityUtil.isLiving(target));
                           } while(target == mc.field_71439_g);
                        } while((double)mc.field_71439_g.func_70032_d(target) > (Double)this.hitRange.getValue());
                     } while(((EntityLivingBase)target).func_110143_aJ() <= 0.0F);
                  } while(((Aura.WaitMode)this.waitMode.getValue()).equals(Aura.WaitMode.DYNAMIC) && ((EntityLivingBase)target).field_70737_aN != 0);
               } while(!(Boolean)this.ignoreWalls.getValue() && !mc.field_71439_g.func_70685_l(target) && !this.canEntityFeetBeSeen(target));

               if ((Boolean)this.attackPlayers.getValue() && target instanceof EntityPlayer && !Friends.isFriend(target.func_70005_c_())) {
                  this.attack(target);
                  return;
               }

               if (EntityUtil.isPassive(target)) {
                  if ((Boolean)this.attackAnimals.getValue()) {
                     break;
                  }
               } else if (EntityUtil.isMobAggressive(target) && (Boolean)this.attackMobs.getValue()) {
                  break;
               }
            }

            if (!(Boolean)this.switchTo32k.getValue() && ModuleManager.isModuleEnabled("AutoTool")) {
               AutoTool.equipBestWeapon();
            }

            this.attack(target);
         }
      }
   }

   private boolean checkSharpness(ItemStack stack) {
      if (stack.func_77978_p() == null) {
         return false;
      } else {
         NBTTagList enchants = (NBTTagList)stack.func_77978_p().func_74781_a("ench");
         if (enchants == null) {
            return false;
         } else {
            for(int i = 0; i < enchants.func_74745_c(); ++i) {
               NBTTagCompound enchant = enchants.func_150305_b(i);
               if (enchant.func_74762_e("id") == 16) {
                  int lvl = enchant.func_74762_e("lvl");
                  if (lvl >= 42) {
                     return true;
                  }
                  break;
               }
            }

            return false;
         }
      }
   }

   private void attack(Entity e) {
      boolean holding32k = false;
      if (this.checkSharpness(mc.field_71439_g.func_184614_ca())) {
         holding32k = true;
      }

      if ((Boolean)this.switchTo32k.getValue() && !holding32k) {
         int newSlot = -1;

         for(int i = 0; i < 9; ++i) {
            ItemStack stack = mc.field_71439_g.field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a && this.checkSharpness(stack)) {
               newSlot = i;
               break;
            }
         }

         if (newSlot != -1) {
            mc.field_71439_g.field_71071_by.field_70461_c = newSlot;
            holding32k = true;
         }
      }

      if (!(Boolean)this.onlyUse32k.getValue() || holding32k) {
         mc.field_71442_b.func_78764_a(mc.field_71439_g, e);
         mc.field_71439_g.func_184609_a(EnumHand.MAIN_HAND);
      }
   }

   private float getLagComp() {
      return ((Aura.WaitMode)this.waitMode.getValue()).equals(Aura.WaitMode.DYNAMIC) ? -(20.0F - LagCompensator.INSTANCE.getTickRate()) : 0.0F;
   }

   private boolean canEntityFeetBeSeen(Entity entityIn) {
      return mc.field_71441_e.func_147447_a(new Vec3d(mc.field_71439_g.field_70165_t, mc.field_71439_g.field_70163_u + (double)mc.field_71439_g.func_70047_e(), mc.field_71439_g.field_70161_v), new Vec3d(entityIn.field_70165_t, entityIn.field_70163_u, entityIn.field_70161_v), false, true, false) == null;
   }

   private static enum WaitMode {
      DYNAMIC,
      STATIC;
   }
}
