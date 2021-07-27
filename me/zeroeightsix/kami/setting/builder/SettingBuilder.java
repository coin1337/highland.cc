package me.zeroeightsix.kami.setting.builder;

import com.google.common.base.MoreObjects;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.SettingsRegister;

public abstract class SettingBuilder<T> {
   protected String name;
   protected T initialValue;
   protected BiConsumer<T, T> consumer;
   protected List<Predicate<T>> predicateList = new ArrayList();
   private Predicate<T> visibilityPredicate;

   public SettingBuilder<T> withValue(T value) {
      this.initialValue = value;
      return this;
   }

   protected Predicate<T> predicate() {
      return this.predicateList.isEmpty() ? (t) -> {
         return true;
      } : (t) -> {
         return this.predicateList.stream().allMatch((tPredicate) -> {
            return tPredicate.test(t);
         });
      };
   }

   protected Predicate<T> visibilityPredicate() {
      return (Predicate)MoreObjects.firstNonNull(this.visibilityPredicate, (t) -> {
         return true;
      });
   }

   protected BiConsumer<T, T> consumer() {
      return (BiConsumer)MoreObjects.firstNonNull(this.consumer, (a, b) -> {
      });
   }

   public SettingBuilder<T> withConsumer(BiConsumer<T, T> consumer) {
      this.consumer = consumer;
      return this;
   }

   public SettingBuilder<T> withVisibility(Predicate<T> predicate) {
      this.visibilityPredicate = predicate;
      return this;
   }

   public SettingBuilder<T> withName(String name) {
      this.name = name;
      return this;
   }

   public SettingBuilder<T> withRestriction(Predicate<T> predicate) {
      this.predicateList.add(predicate);
      return this;
   }

   public abstract Setting<T> build();

   public final Setting<T> buildAndRegister(String group) {
      return register(this.build(), group);
   }

   public static <T> Setting<T> register(Setting<T> setting, String group) {
      String name = setting.getName();
      if (name != null && !name.isEmpty()) {
         SettingsRegister.register(group + "." + name, setting);
         return setting;
      } else {
         throw new RuntimeException("Can't register nameless setting");
      }
   }
}
