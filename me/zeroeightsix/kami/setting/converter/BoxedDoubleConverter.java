package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;

public class BoxedDoubleConverter extends AbstractBoxedNumberConverter<Double> {
   protected Double doBackward(JsonElement s) {
      return s.getAsDouble();
   }
}
