package me.zeroeightsix.kami.module.modules.movement;

import java.util.function.Predicate;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.KamiEvent.Era;
import me.zeroeightsix.kami.event.events.AddCollisionBoxToListEvent;
import me.zeroeightsix.kami.event.events.PacketEvent.Send;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

@Module$Info(
   name = "Jesus",
   description = "Allows you to walk on water",
   category = Module$Category.MOVEMENT
)
public class Jesus extends Module {
   private static final AxisAlignedBB WATER_WALK_AA = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.99D, 1.0D);
   @EventHandler
   Listener<AddCollisionBoxToListEvent> addCollisionBoxToListEventListener = new Listener((event) -> {
      if (mc.field_71439_g != null && event.getBlock() instanceof BlockLiquid && (EntityUtil.isDrivenByPlayer(event.getEntity()) || event.getEntity() == mc.field_71439_g) && !(event.getEntity() instanceof EntityBoat) && !mc.field_71439_g.func_70093_af() && mc.field_71439_g.field_70143_R < 3.0F && !EntityUtil.isInWater(mc.field_71439_g) && (EntityUtil.isAboveWater(mc.field_71439_g, false) || EntityUtil.isAboveWater(mc.field_71439_g.func_184187_bx(), false)) && isAboveBlock(mc.field_71439_g, event.getPos())) {
         AxisAlignedBB axisalignedbb = WATER_WALK_AA.func_186670_a(event.getPos());
         if (event.getEntityBox().func_72326_a(axisalignedbb)) {
            event.getCollidingBoxes().add(axisalignedbb);
         }

         event.cancel();
      }

   }, new Predicate[0]);
   @EventHandler
   Listener<Send> packetEventSendListener = new Listener((event) -> {
      if (event.getEra() == Era.PRE && event.getPacket() instanceof CPacketPlayer && EntityUtil.isAboveWater(mc.field_71439_g, true) && !EntityUtil.isInWater(mc.field_71439_g) && !isAboveLand(mc.field_71439_g)) {
         int ticks = mc.field_71439_g.field_70173_aa % 2;
         if (ticks == 0) {
            CPacketPlayer var10000 = (CPacketPlayer)event.getPacket();
            var10000.field_149477_b += 0.02D;
         }
      }

   }, new Predicate[0]);

   public void onUpdate() {
      if (!ModuleManager.isModuleEnabled("Freecam") && EntityUtil.isInWater(mc.field_71439_g) && !mc.field_71439_g.func_70093_af()) {
         mc.field_71439_g.field_70181_x = 0.1D;
         if (mc.field_71439_g.func_184187_bx() != null && !(mc.field_71439_g.func_184187_bx() instanceof EntityBoat)) {
            mc.field_71439_g.func_184187_bx().field_70181_x = 0.3D;
         }
      }

   }

   private static boolean isAboveLand(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         double y = entity.field_70163_u - 0.01D;

         for(int x = MathHelper.func_76128_c(entity.field_70165_t); x < MathHelper.func_76143_f(entity.field_70165_t); ++x) {
            for(int z = MathHelper.func_76128_c(entity.field_70161_v); z < MathHelper.func_76143_f(entity.field_70161_v); ++z) {
               BlockPos pos = new BlockPos(x, MathHelper.func_76128_c(y), z);
               if (Wrapper.getWorld().func_180495_p(pos).func_177230_c().func_149730_j(Wrapper.getWorld().func_180495_p(pos))) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   private static boolean isAboveBlock(Entity entity, BlockPos pos) {
      return entity.field_70163_u >= (double)pos.func_177956_o();
   }
}
