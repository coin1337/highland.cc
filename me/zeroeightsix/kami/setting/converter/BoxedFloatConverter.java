package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;

public class BoxedFloatConverter extends AbstractBoxedNumberConverter<Float> {
   protected Float doBackward(JsonElement s) {
      return s.getAsFloat();
   }
}
