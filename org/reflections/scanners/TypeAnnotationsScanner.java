package org.reflections.scanners;

import java.lang.annotation.Inherited;
import java.util.Iterator;

public class TypeAnnotationsScanner extends AbstractScanner {
   public void scan(Object cls) {
      String className = this.getMetadataAdapter().getClassName(cls);
      Iterator var3 = this.getMetadataAdapter().getClassAnnotationNames(cls).iterator();

      while(true) {
         String annotationType;
         do {
            if (!var3.hasNext()) {
               return;
            }

            annotationType = (String)var3.next();
         } while(!this.acceptResult(annotationType) && !annotationType.equals(Inherited.class.getName()));

         this.getStore().put(annotationType, className);
      }
   }
}
