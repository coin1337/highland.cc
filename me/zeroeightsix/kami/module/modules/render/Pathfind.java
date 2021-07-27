package me.zeroeightsix.kami.module.modules.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

@Module$Info(
   name = "Pathfind",
   category = Module$Category.MISC
)
public class Pathfind extends Module {
   public static ArrayList<PathPoint> points = new ArrayList();
   static PathPoint to = null;

   public static boolean createPath(PathPoint end) {
      to = end;
      WalkNodeProcessor walkNodeProcessor = new me.zeroeightsix.kami.module.modules.render.Pathfind.AnchoredWalkNodeProcessor(new PathPoint((int)mc.field_71439_g.field_70165_t, (int)mc.field_71439_g.field_70163_u, (int)mc.field_71439_g.field_70161_v));
      EntityZombie zombie = new EntityZombie(mc.field_71441_e);
      zombie.func_184644_a(PathNodeType.WATER, 16.0F);
      zombie.field_70165_t = mc.field_71439_g.field_70165_t;
      zombie.field_70163_u = mc.field_71439_g.field_70163_u;
      zombie.field_70161_v = mc.field_71439_g.field_70161_v;
      PathFinder finder = new PathFinder(walkNodeProcessor);
      Path path = finder.func_186336_a(mc.field_71441_e, zombie, new BlockPos(end.field_75839_a, end.field_75837_b, end.field_75838_c), Float.MAX_VALUE);
      zombie.func_184644_a(PathNodeType.WATER, 0.0F);
      if (path == null) {
         Command.sendChatMessage("Failed to create path!");
         return false;
      } else {
         points = new ArrayList(Arrays.asList(path.field_75884_a));
         return ((PathPoint)points.get(points.size() - 1)).func_75829_a(end) <= 1.0F;
      }
   }

   public void onWorldRender(RenderEvent event) {
      if (!points.isEmpty()) {
         GL11.glDisable(3042);
         GL11.glDisable(3553);
         GL11.glDisable(2896);
         GL11.glLineWidth(1.5F);
         GL11.glColor3f(1.0F, 1.0F, 1.0F);
         GlStateManager.func_179097_i();
         GL11.glBegin(1);
         PathPoint first = (PathPoint)points.get(0);
         GL11.glVertex3d((double)first.field_75839_a - mc.func_175598_ae().field_78725_b + 0.5D, (double)first.field_75837_b - mc.func_175598_ae().field_78726_c, (double)first.field_75838_c - mc.func_175598_ae().field_78723_d + 0.5D);

         for(int i = 0; i < points.size() - 1; ++i) {
            PathPoint pathPoint = (PathPoint)points.get(i);
            GL11.glVertex3d((double)pathPoint.field_75839_a - mc.func_175598_ae().field_78725_b + 0.5D, (double)pathPoint.field_75837_b - mc.func_175598_ae().field_78726_c, (double)pathPoint.field_75838_c - mc.func_175598_ae().field_78723_d + 0.5D);
            if (i != points.size() - 1) {
               GL11.glVertex3d((double)pathPoint.field_75839_a - mc.func_175598_ae().field_78725_b + 0.5D, (double)pathPoint.field_75837_b - mc.func_175598_ae().field_78726_c, (double)pathPoint.field_75838_c - mc.func_175598_ae().field_78723_d + 0.5D);
            }
         }

         GL11.glEnd();
         GlStateManager.func_179126_j();
      }
   }

   public void onUpdate() {
      PathPoint closest = (PathPoint)points.stream().min(Comparator.comparing((pathPoint) -> {
         return mc.field_71439_g.func_70011_f((double)pathPoint.field_75839_a, (double)pathPoint.field_75837_b, (double)pathPoint.field_75838_c);
      })).orElse((Object)null);
      if (closest != null) {
         if (!(mc.field_71439_g.func_70011_f((double)closest.field_75839_a, (double)closest.field_75837_b, (double)closest.field_75838_c) > 0.8D)) {
            Iterator iterator = points.iterator();

            while(iterator.hasNext()) {
               if (iterator.next() == closest) {
                  iterator.remove();
                  break;
               }

               iterator.remove();
            }

            if (points.size() <= 1 && to != null) {
               boolean b = createPath(to);
               boolean flag = points.size() <= 4;
               if (b && flag || flag) {
                  points.clear();
                  to = null;
                  if (b) {
                     Command.sendChatMessage("Arrived!");
                  } else {
                     Command.sendChatMessage("Can't go on: pathfinder has hit dead end");
                  }
               }
            }

         }
      }
   }
}
