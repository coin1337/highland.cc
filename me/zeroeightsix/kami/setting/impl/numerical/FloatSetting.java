package me.zeroeightsix.kami.setting.impl.numerical;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import me.zeroeightsix.kami.setting.converter.AbstractBoxedNumberConverter;
import me.zeroeightsix.kami.setting.converter.BoxedFloatConverter;

public class FloatSetting extends NumberSetting<Float> {
   private static final BoxedFloatConverter converter = new BoxedFloatConverter();

   public FloatSetting(Float value, Predicate<Float> restriction, BiConsumer<Float, Float> consumer, String name, Predicate<Float> visibilityPredicate, Float min, Float max) {
      super(value, restriction, consumer, name, visibilityPredicate, min, max);
   }

   public AbstractBoxedNumberConverter converter() {
      return converter;
   }
}
