package me.zeroeightsix.kami.module;

public enum Module$Category {
   COMBAT("Combat", false),
   EXPLOITS("Exploits", false),
   RENDER("Render", false),
   MISC("Misc", false),
   PLAYER("Player", false),
   MOVEMENT("Movement", false),
   CHAT("Chat", false),
   DEV("Dev", false),
   HIDDEN("Hidden", true);

   boolean hidden;
   String name;

   private Module$Category(String name, boolean hidden) {
      this.name = name;
      this.hidden = hidden;
   }

   public boolean isHidden() {
      return this.hidden;
   }

   public String getName() {
      return this.name;
   }
}
