package me.zeroeightsix.kami.setting.converter;

import com.google.common.base.Converter;
import com.google.gson.JsonElement;

public interface Convertable<T> {
   Converter<T, JsonElement> converter();
}
