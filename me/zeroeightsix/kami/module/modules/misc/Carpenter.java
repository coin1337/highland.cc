package me.zeroeightsix.kami.module.modules.misc;

import java.util.List;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.ColourUtils;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class Carpenter extends Module {
   private int displayList = -1;

   public class Shape {
      final BlockPos[] blocks;
      private final int colour;

      Shape(List<BlockPos> blocks) {
         this.blocks = (BlockPos[])blocks.toArray(new BlockPos[0]);
         this.colour = ColourUtils.toRGBA(0.5D + Math.random() * 0.5D, 0.5D + Math.random() * 0.5D, 0.5D + Math.random() * 0.5D, 1.0D);
      }

      public BlockPos[] getBlocks() {
         return this.blocks;
      }

      public int getColour() {
         return this.colour;
      }
   }

   public static class Selection {
      private BlockPos first;
      private BlockPos second;

      public Selection(BlockPos pos1, BlockPos pos2) {
         this.first = pos1;
         this.second = pos2;
      }

      public BlockPos getFirst() {
         return this.first;
      }

      public BlockPos getSecond() {
         return this.second;
      }

      public void setFirst(BlockPos first) {
         this.first = first;
      }

      public void setSecond(BlockPos second) {
         this.second = second;
      }

      public boolean isInvalid() {
         return this.first == null || this.second == null;
      }

      public int getWidth() {
         int x1 = Math.min(this.first.func_177958_n(), this.second.func_177958_n());
         int x2 = Math.max(this.first.func_177958_n(), this.second.func_177958_n()) + 1;
         return Math.abs(x1 - x2);
      }

      public int getLength() {
         int z1 = Math.min(this.first.func_177952_p(), this.second.func_177952_p());
         int z2 = Math.max(this.first.func_177952_p(), this.second.func_177952_p()) + 1;
         return Math.abs(z1 - z2);
      }

      public int getHeight() {
         int y1 = Math.min(this.first.func_177956_o(), this.second.func_177956_o()) + 1;
         int y2 = Math.max(this.first.func_177956_o(), this.second.func_177956_o());
         return Math.abs(y1 - y2);
      }

      public int getSize() {
         return this.getWidth() * this.getLength() * this.getHeight();
      }

      public BlockPos getMinimum() {
         int x1 = Math.min(this.first.func_177958_n(), this.second.func_177958_n());
         int y1 = Math.min(this.first.func_177956_o(), this.second.func_177956_o());
         int z1 = Math.min(this.first.func_177952_p(), this.second.func_177952_p());
         return new BlockPos(x1, y1, z1);
      }

      public BlockPos getMaximum() {
         int x1 = Math.min(this.first.func_177958_n(), this.second.func_177958_n()) + 1;
         int y1 = Math.min(this.first.func_177956_o(), this.second.func_177956_o());
         int z1 = Math.min(this.first.func_177952_p(), this.second.func_177952_p()) + 1;
         return new BlockPos(x1, y1, z1);
      }

      public BlockPos getFurthest(int x, int y, int z) {
         if (x > 0) {
            return this.first.func_177958_n() > this.second.func_177958_n() ? this.first : this.second;
         } else if (x < 0) {
            return this.first.func_177958_n() < this.second.func_177958_n() ? this.first : this.second;
         } else if (y > 0) {
            return this.first.func_177958_n() > this.second.func_177958_n() ? this.first : this.second;
         } else if (y < 0) {
            return this.first.func_177956_o() < this.second.func_177956_o() ? this.first : this.second;
         } else if (z > 0) {
            return this.first.func_177952_p() > this.second.func_177952_p() ? this.first : this.second;
         } else if (z < 0) {
            return this.first.func_177952_p() < this.second.func_177952_p() ? this.first : this.second;
         } else {
            return null;
         }
      }

      public void moveSelection(int x, int y, int z) {
         this.first = this.first.func_177982_a(x, y, z);
         this.second = this.second.func_177982_a(x, y, z);
      }

      public void expand(int amount, EnumFacing facing) {
         BlockPos affect = this.second;
         switch(facing) {
         case DOWN:
            if (this.second.func_177956_o() < this.first.func_177956_o()) {
               this.second = this.second.func_177982_a(0, -amount, 0);
            } else {
               this.first = this.first.func_177982_a(0, -amount, 0);
            }
            break;
         case UP:
            if (this.second.func_177956_o() > this.first.func_177956_o()) {
               this.second = this.second.func_177982_a(0, amount, 0);
            } else {
               this.first = this.first.func_177982_a(0, amount, 0);
            }
            break;
         case NORTH:
            if (this.second.func_177952_p() < this.first.func_177952_p()) {
               this.second = this.second.func_177982_a(0, 0, -amount);
            } else {
               this.first = this.first.func_177982_a(0, 0, -amount);
            }
            break;
         case SOUTH:
            if (this.second.func_177952_p() > this.first.func_177952_p()) {
               this.second = this.second.func_177982_a(0, 0, amount);
            } else {
               this.first = this.first.func_177982_a(0, 0, amount);
            }
            break;
         case WEST:
            if (this.second.func_177958_n() < this.first.func_177958_n()) {
               this.second = this.second.func_177982_a(-amount, 0, 0);
            } else {
               this.first = this.first.func_177982_a(-amount, 0, 0);
            }
            break;
         case EAST:
            if (this.second.func_177958_n() > this.first.func_177958_n()) {
               this.second = this.second.func_177982_a(amount, 0, 0);
            } else {
               this.first = this.first.func_177982_a(amount, 0, 0);
            }
         }

      }
   }

   public static class ShapeBuilder {
      private static BlockPos from(double x, double y, double z) {
         return new BlockPos(Math.floor(x), Math.floor(y), Math.floor(z));
      }

      public static Carpenter.Shape oval(BlockPos origin, double width, double length) {
         return null;
      }
   }
}
