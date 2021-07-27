package org.reflections.scanners;

import java.util.Iterator;
import java.util.List;

public class FieldAnnotationsScanner extends AbstractScanner {
   public void scan(Object cls) {
      String className = this.getMetadataAdapter().getClassName(cls);
      List<Object> fields = this.getMetadataAdapter().getFields(cls);
      Iterator var4 = fields.iterator();

      while(var4.hasNext()) {
         Object field = var4.next();
         List<String> fieldAnnotations = this.getMetadataAdapter().getFieldAnnotationNames(field);
         Iterator var7 = fieldAnnotations.iterator();

         while(var7.hasNext()) {
            String fieldAnnotation = (String)var7.next();
            if (this.acceptResult(fieldAnnotation)) {
               String fieldName = this.getMetadataAdapter().getFieldName(field);
               this.getStore().put(fieldAnnotation, String.format("%s.%s", className, fieldName));
            }
         }
      }

   }
}
