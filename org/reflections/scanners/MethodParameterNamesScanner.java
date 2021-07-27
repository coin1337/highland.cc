package org.reflections.scanners;

import com.google.common.base.Joiner;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.reflections.adapters.MetadataAdapter;

public class MethodParameterNamesScanner extends AbstractScanner {
   public void scan(Object cls) {
      MetadataAdapter md = this.getMetadataAdapter();
      Iterator var3 = md.getMethods(cls).iterator();

      while(true) {
         Object method;
         String key;
         LocalVariableAttribute table;
         int length;
         int i;
         do {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               method = var3.next();
               key = md.getMethodFullKey(cls, method);
            } while(!this.acceptResult(key));

            table = (LocalVariableAttribute)((MethodInfo)method).getCodeAttribute().getAttribute("LocalVariableTable");
            length = table.tableLength();
            i = Modifier.isStatic(((MethodInfo)method).getAccessFlags()) ? 0 : 1;
         } while(i >= length);

         ArrayList names = new ArrayList(length - i);

         while(i < length) {
            names.add(((MethodInfo)method).getConstPool().getUtf8Info(table.nameIndex(i++)));
         }

         this.getStore().put(key, Joiner.on(", ").join(names));
      }
   }
}
