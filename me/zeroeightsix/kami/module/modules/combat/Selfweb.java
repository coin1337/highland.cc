package me.zeroeightsix.kami.module.modules.combat;

import java.util.Collections;
import java.util.List;
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
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFalling;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Module$Info(
   name = "Selfweb-test",
   category = Module$Category.COMBAT
)
public class Selfweb extends Module {
   private final Vec3d[] surroundList = new Vec3d[]{new Vec3d(0.0D, 0.0D, 0.0D), new Vec3d(0.0D, 1.0D, 0.0D)};
   private final List obsidian;
   private Setting toggleable;
   private Setting slowmode;
   private Setting full;
   private Vec3d[] surroundTargets;
   private int blocksPerTick;
   private BlockPos basePos;
   private boolean slowModeSwitch;
   private int offsetStep;
   private int oldSlot;

   public Selfweb() {
      this.obsidian = Collections.singletonList(Blocks.field_150321_G);
      this.toggleable = this.register(Settings.b("Toggleable", true));
      this.slowmode = this.register(Settings.b("Slower version", false));
      this.full = this.register(Settings.b("Adds extra corners", false));
      this.blocksPerTick = 3;
      this.slowModeSwitch = false;
      this.offsetStep = 0;
      this.oldSlot = 0;
   }

   public void onUpdate() {
      if (!this.isDisabled() && mc.field_71439_g != null && !ModuleManager.isModuleEnabled("Freecam")) {
         if (this.slowModeSwitch) {
            this.slowModeSwitch = false;
         } else {
            if (this.offsetStep == 0) {
               this.init();
            }

            for(int i = 0; i < this.blocksPerTick; ++i) {
               if (this.offsetStep >= this.surroundTargets.length) {
                  this.end();
                  return;
               }

               Vec3d offset = this.surroundTargets[this.offsetStep];
               this.placeBlock(new BlockPos(this.basePos.func_177963_a(offset.field_72450_a, offset.field_72448_b, offset.field_72449_c)));
               ++this.offsetStep;
            }

            this.slowModeSwitch = true;
         }
      }

   }

   private void placeBlock(BlockPos blockPos) {
      if (Wrapper.getWorld().func_180495_p(blockPos).func_185904_a().func_76222_j()) {
         int newSlot = -1;

         for(int i = 0; i < 9; ++i) {
            ItemStack stack = Wrapper.getPlayer().field_71071_by.func_70301_a(i);
            if (stack != ItemStack.field_190927_a && stack.func_77973_b() instanceof ItemBlock) {
               Block block = ((ItemBlock)stack.func_77973_b()).func_179223_d();
               if (!BlockInteractionHelper.blackList.contains(block) && !(block instanceof BlockContainer) && Block.func_149634_a(stack.func_77973_b()).func_176223_P().func_185913_b() && (!(((ItemBlock)stack.func_77973_b()).func_179223_d() instanceof BlockFalling) || !Wrapper.getWorld().func_180495_p(blockPos.func_177977_b()).func_185904_a().func_76222_j()) && this.obsidian.contains(block)) {
                  newSlot = i;
                  break;
               }
            }
         }

         if (newSlot == -1) {
            if (!(Boolean)this.toggleable.getValue()) {
               Command.sendChatMessage("Surround: Please Put Obsidian in Hotbar");
            }

            this.end();
         } else {
            Wrapper.getPlayer().field_71071_by.field_70461_c = newSlot;
            if (BlockInteractionHelper.checkForNeighbours(blockPos)) {
               BlockInteractionHelper.placeBlockScaffold(blockPos);
            }
         }
      }

   }

   private void init() {
      this.basePos = (new BlockPos(mc.field_71439_g.func_174791_d())).func_177977_b();
      if ((Boolean)this.slowmode.getValue()) {
         this.blocksPerTick = 1;
      }

      if ((Boolean)this.full.getValue()) {
         this.surroundTargets = this.surroundList;
      } else {
         this.surroundTargets = this.surroundList;
      }

   }

   private void end() {
      this.offsetStep = 0;
      if (!(Boolean)this.toggleable.getValue()) {
         this.disable();
      }

   }

   protected void onEnable() {
      mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.START_SNEAKING));
      this.oldSlot = Wrapper.getPlayer().field_71071_by.field_70461_c;
   }

   protected void onDisable() {
      mc.field_71439_g.field_71174_a.func_147297_a(new CPacketEntityAction(mc.field_71439_g, Action.STOP_SNEAKING));
      Wrapper.getPlayer().field_71071_by.field_70461_c = this.oldSlot;
   }
}
