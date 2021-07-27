package me.zeroeightsix.kami.setting.builder.numerical;

import java.util.function.BiConsumer;
import me.zeroeightsix.kami.setting.builder.SettingBuilder;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;

public abstract class NumericalSettingBuilder<T extends Number> extends SettingBuilder<T> {
   protected T min;
   protected T max;

   public NumericalSettingBuilder<T> withMinimum(T minimum) {
      this.predicateList.add((t) -> {
         return t.doubleValue() >= minimum.doubleValue();
      });
      if (this.min == null || minimum.doubleValue() > this.min.doubleValue()) {
         this.min = minimum;
      }

      return this;
   }

   public NumericalSettingBuilder<T> withMaximum(T maximum) {
      this.predicateList.add((t) -> {
         return t.doubleValue() <= maximum.doubleValue();
      });
      if (this.max == null || maximum.doubleValue() < this.max.doubleValue()) {
         this.max = maximum;
      }

      return this;
   }

   public NumericalSettingBuilder<T> withRange(T minimum, T maximum) {
      this.predicateList.add((t) -> {
         double doubleValue = t.doubleValue();
         return doubleValue >= minimum.doubleValue() && doubleValue <= maximum.doubleValue();
      });
      if (this.min == null || minimum.doubleValue() > this.min.doubleValue()) {
         this.min = minimum;
      }

      if (this.max == null || maximum.doubleValue() < this.max.doubleValue()) {
         this.max = maximum;
      }

      return this;
   }

   public NumericalSettingBuilder<T> withListener(BiConsumer<T, T> consumer) {
      this.consumer = consumer;
      return this;
   }

   public NumericalSettingBuilder<T> withValue(T value) {
      return (NumericalSettingBuilder)super.withValue(value);
   }

   public NumericalSettingBuilder withName(String name) {
      return (NumericalSettingBuilder)super.withName(name);
   }

   public abstract NumberSetting build();
}
