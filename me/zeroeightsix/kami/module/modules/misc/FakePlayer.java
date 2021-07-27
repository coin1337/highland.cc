package me.zeroeightsix.kami.module.modules.misc;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import net.minecraft.client.entity.EntityOtherPlayerMP;

@Module$Info(
   name = "FakePlayer",
   category = Module$Category.MISC,
   description = "Spawns a fake Player"
)
public class FakePlayer extends Module {
   protected void onEnable() {
      if (mc.field_71441_e != null) {
         EntityOtherPlayerMP fakePlayer = new EntityOtherPlayerMP(mc.field_71441_e, new GameProfile(UUID.fromString("e195d3d7-e6dc-456e-8393-e2f90816a1af"), "Hausemaster"));
         fakePlayer.func_82149_j(mc.field_71439_g);
         fakePlayer.field_70759_as = mc.field_71439_g.field_70759_as;
         mc.field_71441_e.func_73027_a(-100, fakePlayer);
         this.disable();
      }
   }
}
