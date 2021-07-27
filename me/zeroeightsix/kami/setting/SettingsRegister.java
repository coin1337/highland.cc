package me.zeroeightsix.kami.setting;

import java.util.HashMap;
import java.util.StringTokenizer;
import me.zeroeightsix.kami.util.Pair;

public class SettingsRegister {
   public static final SettingsRegister ROOT = new SettingsRegister();
   public HashMap<String, SettingsRegister> registerHashMap = new HashMap();
   public HashMap<String, Setting> settingHashMap = new HashMap();

   public SettingsRegister subregister(String name) {
      if (this.registerHashMap.containsKey(name)) {
         return (SettingsRegister)this.registerHashMap.get(name);
      } else {
         SettingsRegister register = new SettingsRegister();
         this.registerHashMap.put(name, register);
         return register;
      }
   }

   private void put(String name, Setting setting) {
      this.settingHashMap.put(name, setting);
   }

   public static void register(String name, Setting setting) {
      Pair<String, SettingsRegister> pair = dig(name);
      ((SettingsRegister)pair.getValue()).put((String)pair.getKey(), setting);
   }

   public Setting getSetting(String group) {
      return (Setting)this.settingHashMap.get(group);
   }

   public static Setting get(String group) {
      Pair<String, SettingsRegister> pair = dig(group);
      return ((SettingsRegister)pair.getValue()).getSetting((String)pair.getKey());
   }

   private static Pair<String, SettingsRegister> dig(String group) {
      SettingsRegister current = ROOT;
      StringTokenizer tokenizer = new StringTokenizer(group, ".");
      String previousToken = null;

      while(tokenizer.hasMoreTokens()) {
         if (previousToken == null) {
            previousToken = tokenizer.nextToken();
         } else {
            String token = tokenizer.nextToken();
            current = current.subregister(previousToken);
            previousToken = token;
         }
      }

      return new Pair(previousToken == null ? "" : previousToken, current);
   }
}
