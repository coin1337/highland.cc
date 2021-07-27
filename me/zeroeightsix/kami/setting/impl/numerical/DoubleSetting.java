package me.zeroeightsix.kami.setting.impl.numerical;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import me.zeroeightsix.kami.setting.converter.BoxedDoubleConverter;

public class DoubleSetting extends NumberSetting<Double> {
   private static final BoxedDoubleConverter converter = new BoxedDoubleConverter();

   public DoubleSetting(Double value, Predicate<Double> restriction, BiConsumer<Double, Double> consumer, String name, Predicate<Double> visibilityPredicate, Double min, Double max) {
      super(value, restriction, consumer, name, visibilityPredicate, min, max);
   }

   public AbstractBoxedNumberConverter converter() {
      return converter;
   }
}
