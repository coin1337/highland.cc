package me.zeroeightsix.kami.mixin.client;

import io.netty.channel.ChannelHandlerContext;
import java.io.IOException;
import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.event.events.PacketEvent.Receive;
import me.zeroeightsix.kami.event.events.PacketEvent.Send;
import me.zeroeightsix.kami.module.modules.misc.NoPacketKick;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({NetworkManager.class})
public class MixinNetworkManager {
   @Inject(
      method = {"sendPacket(Lnet/minecraft/network/Packet;)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onSendPacket(Packet<?> packet, CallbackInfo callbackInfo) {
      PacketEvent event = new Send(packet);
      KamiMod.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         callbackInfo.cancel();
      }

   }

   @Inject(
      method = {"channelRead0"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onChannelRead(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callbackInfo) {
      PacketEvent event = new Receive(packet);
      KamiMod.EVENT_BUS.post(event);
      if (event.isCancelled()) {
         callbackInfo.cancel();
      }

   }

   @Inject(
      method = {"exceptionCaught"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_, CallbackInfo info) {
      if (p_exceptionCaught_2_ instanceof IOException && NoPacketKick.isEnabled()) {
         info.cancel();
      }

   }
}
