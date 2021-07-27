package me.zeroeightsix.kami.module.modules.chat;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent.Receive;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.Module$Category;
import me.zeroeightsix.kami.module.Module$Info;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.network.play.server.SPacketChat;

@Module$Info(
   name = "AntiSpam",
   category = Module$Category.CHAT
)
public class AntiSpam extends Module {
   private Setting<Boolean> greenText = this.register(Settings.b("Green Text", true));
   private Setting<Boolean> discordLinks = this.register(Settings.b("Discord Links", true));
   private Setting<Boolean> webLinks = this.register(Settings.b("Web Links", true));
   private Setting<Boolean> announcers = this.register(Settings.b("Announcers", true));
   private Setting<Boolean> tradeChat = this.register(Settings.b("Trade Chat", true));
   private Setting<Boolean> duplicates = this.register(Settings.b("Duplicates", true));
   private Setting<Integer> duplicatesTimeout = this.register(Settings.integerBuilder("Duplicates Timeout").withMinimum(1).withValue((int)10).withMaximum(600).build());
   private Setting<Boolean> skipOwn = this.register(Settings.b("Skip Own", true));
   private Setting<Boolean> debug = this.register(Settings.b("Debug Messages", false));
   private ConcurrentHashMap<String, Long> messageHistory;
   @EventHandler
   public Listener<Receive> listener = new Listener((event) -> {
      if (mc.field_71439_g != null && !this.isDisabled()) {
         if (event.getPacket() instanceof SPacketChat) {
            SPacketChat sPacketChat = (SPacketChat)event.getPacket();
            if (this.detectSpam(sPacketChat.func_148915_c().func_150260_c())) {
               event.cancel();
            }

         }
      }
   }, new Predicate[0]);

   public void onEnable() {
      this.messageHistory = new ConcurrentHashMap();
   }

   public void onDisable() {
      this.messageHistory = null;
   }

   private boolean detectSpam(String message) {
      if ((Boolean)this.skipOwn.getValue() && this.findPatterns(AntiSpam.FilterPatterns.ownMessage, message)) {
         return false;
      } else if ((Boolean)this.greenText.getValue() && this.findPatterns(AntiSpam.FilterPatterns.greenText, message)) {
         if ((Boolean)this.debug.getValue()) {
            Command.sendChatMessage("[AntiSpam] Green Text: " + message);
         }

         return true;
      } else if ((Boolean)this.discordLinks.getValue() && this.findPatterns(AntiSpam.FilterPatterns.discord, message)) {
         if ((Boolean)this.debug.getValue()) {
            Command.sendChatMessage("[AntiSpam] Discord Link: " + message);
         }

         return true;
      } else if ((Boolean)this.webLinks.getValue() && this.findPatterns(AntiSpam.FilterPatterns.webLink, message)) {
         if ((Boolean)this.debug.getValue()) {
            Command.sendChatMessage("[AntiSpam] Web Link: " + message);
         }

         return true;
      } else if ((Boolean)this.tradeChat.getValue() && this.findPatterns(AntiSpam.FilterPatterns.tradeChat, message)) {
         if ((Boolean)this.debug.getValue()) {
            Command.sendChatMessage("[AntiSpam] Trade Chat: " + message);
         }

         return true;
      } else if ((Boolean)this.announcers.getValue() && this.findPatterns(AntiSpam.FilterPatterns.announcer, message)) {
         if ((Boolean)this.debug.getValue()) {
            Command.sendChatMessage("[AntiSpam] Announcer: " + message);
         }

         return true;
      } else {
         if ((Boolean)this.duplicates.getValue()) {
            if (this.messageHistory == null) {
               this.messageHistory = new ConcurrentHashMap();
            }

            boolean isDuplicate = false;
            if (this.messageHistory.containsKey(message) && (System.currentTimeMillis() - (Long)this.messageHistory.get(message)) / 1000L < (long)(Integer)this.duplicatesTimeout.getValue()) {
               isDuplicate = true;
            }

            this.messageHistory.put(message, System.currentTimeMillis());
            if (isDuplicate) {
               if ((Boolean)this.debug.getValue()) {
                  Command.sendChatMessage("[AntiSpam] Duplicate: " + message);
               }

               return true;
            }
         }

         return false;
      }
   }

   private boolean findPatterns(String[] patterns, String string) {
      String[] var3 = patterns;
      int var4 = patterns.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         String pattern = var3[var5];
         if (Pattern.compile(pattern).matcher(string).find()) {
            return true;
         }
      }

      return false;
   }

   private static class FilterPatterns {
      private static final String[] announcer = new String[]{"I just walked .+ feet!", "I just placed a .+!", "I just attacked .+ with a .+!", "I just dropped a .+!", "I just opened chat!", "I just opened my console!", "I just opened my GUI!", "I just went into full screen mode!", "I just paused my game!", "I just opened my inventory!", "I just looked at the player list!", "I just took a screen shot!", "I just swaped hands!", "I just ducked!", "I just changed perspectives!", "I just jumped!", "I just ate a .+!", "I just crafted .+ .+!", "I just picked up a .+!", "I just smelted .+ .+!", "I just respawned!", "I just attacked .+ with my hands", "I just broke a .+!"};
      private static final String[] discord = new String[]{"discord.gg"};
      private static final String[] greenText = new String[]{"<.+> >"};
      private static final String[] ownMessage;
      private static final String[] tradeChat;
      private static final String[] webLink;

      static {
         ownMessage = new String[]{"<" + AntiSpam.mc.field_71439_g.func_70005_c_() + ">"};
         tradeChat = new String[]{"buy", "sell"};
         webLink = new String[]{"http:\\/\\/", "https:\\/\\/"};
      }
   }
}
