package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.event.events.RenderEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.util.EntityUtil;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@Module$Info(
   name = "PlayerAnnouncer",
   category = Module$Category.MISC,
   description = "Announces When A Player Enters Your Visual Range"
)
public class PlayerAnnouncer extends Module {
   public void onWorldRender(RenderEvent event) {
      Minecraft.func_71410_x().field_71441_e.field_72996_f.stream().filter(EntityUtil::isLiving).filter((entity) -> {
         return !EntityUtil.isFakeLocalPlayer(entity);
      }).filter((entity) -> {
         return entity instanceof EntityPlayer;
      }).filter((entity) -> {
         return !(entity instanceof EntityPlayerSP);
      }).forEach(this::sendMessage);
   }

   private void sendMessage(Entity entityIn) {
      Wrapper.getPlayer().func_71165_d(entityIn.func_70005_c_() + " entered visual range");
      this.disable();
   }
}
