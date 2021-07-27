package org.reflections.scanners;

import com.google.common.base.Predicate;
import com.google.common.collect.Multimap;
import javax.annotation.Nullable;
import org.reflections.Configuration;
import org.reflections.vfs.Vfs;

public interface Scanner {
   void setConfiguration(Configuration var1);

   Multimap<String, String> getStore();

   void setStore(Multimap<String, String> var1);

   Scanner filterResultsBy(Predicate<String> var1);

   boolean acceptsInput(String var1);

   Object scan(Vfs.File var1, @Nullable Object var2);

   boolean acceptResult(String var1);
}
