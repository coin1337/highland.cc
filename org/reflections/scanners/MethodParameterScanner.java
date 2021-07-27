package org.reflections.scanners;

import java.util.Iterator;
import java.util.List;
import org.reflections.adapters.MetadataAdapter;

public class MethodParameterScanner extends AbstractScanner {
   public void scan(Object cls) {
      MetadataAdapter md = this.getMetadataAdapter();
      Iterator var3 = md.getMethods(cls).iterator();

      while(var3.hasNext()) {
         Object method = var3.next();
         String signature = md.getParameterNames(method).toString();
         if (this.acceptResult(signature)) {
            this.getStore().put(signature, md.getMethodFullKey(cls, method));
         }

         String returnTypeName = md.getReturnTypeName(method);
         if (this.acceptResult(returnTypeName)) {
            this.getStore().put(returnTypeName, md.getMethodFullKey(cls, method));
         }

         List<String> parameterNames = md.getParameterNames(method);

         for(int i = 0; i < parameterNames.size(); ++i) {
            Iterator var9 = md.getParameterAnnotationNames(method, i).iterator();

            while(var9.hasNext()) {
               Object paramAnnotation = var9.next();
               if (this.acceptResult((String)paramAnnotation)) {
                  this.getStore().put((String)paramAnnotation, md.getMethodFullKey(cls, method));
               }
            }
         }
      }

   }
}
