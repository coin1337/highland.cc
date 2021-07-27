package org.reflections.scanners;

import java.util.Iterator;

public class MethodAnnotationsScanner extends AbstractScanner {
   public void scan(Object cls) {
      Iterator var2 = this.getMetadataAdapter().getMethods(cls).iterator();

      while(var2.hasNext()) {
         Object method = var2.next();
         Iterator var4 = this.getMetadataAdapter().getMethodAnnotationNames(method).iterator();

         while(var4.hasNext()) {
            String methodAnnotation = (String)var4.next();
            if (this.acceptResult(methodAnnotation)) {
               this.getStore().put(methodAnnotation, this.getMetadataAdapter().getMethodFullKey(cls, method));
            }
         }
      }

   }
}
