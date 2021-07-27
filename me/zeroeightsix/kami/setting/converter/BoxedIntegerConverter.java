package me.zeroeightsix.kami.setting.converter;

import com.google.gson.JsonElement;

public class BoxedIntegerConverter extends AbstractBoxedNumberConverter<Integer> {
   protected Integer doBackward(JsonElement s) {
      return s.getAsInt();
   }
}
