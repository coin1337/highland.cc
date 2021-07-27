package org.reflections.scanners;

import com.google.common.base.Joiner;
import java.util.Iterator;

public class TypeElementsScanner extends AbstractScanner {
   private boolean includeFields = true;
   private boolean includeMethods = true;
   private boolean includeAnnotations = true;
   private boolean publicOnly = true;

   public void scan(Object cls) {
      String className = this.getMetadataAdapter().getClassName(cls);
      if (this.acceptResult(className)) {
         this.getStore().put(className, "");
         Iterator var3;
         Object method;
         String methodKey;
         if (this.includeFields) {
            var3 = this.getMetadataAdapter().getFields(cls).iterator();

            while(var3.hasNext()) {
               method = var3.next();
               methodKey = this.getMetadataAdapter().getFieldName(method);
               this.getStore().put(className, methodKey);
            }
         }

         if (this.includeMethods) {
            var3 = this.getMetadataAdapter().getMethods(cls).iterator();

            label39:
            while(true) {
               do {
                  if (!var3.hasNext()) {
                     break label39;
                  }

                  method = var3.next();
               } while(this.publicOnly && !this.getMetadataAdapter().isPublic(method));

               methodKey = this.getMetadataAdapter().getMethodName(method) + "(" + Joiner.on(", ").join(this.getMetadataAdapter().getParameterNames(method)) + ")";
               this.getStore().put(className, methodKey);
            }
         }

         if (this.includeAnnotations) {
            var3 = this.getMetadataAdapter().getClassAnnotationNames(cls).iterator();

            while(var3.hasNext()) {
               method = var3.next();
               this.getStore().put(className, "@" + method);
            }
         }

      }
   }

   public TypeElementsScanner includeFields() {
      return this.includeFields(true);
   }

   public TypeElementsScanner includeFields(boolean include) {
      this.includeFields = include;
      return this;
   }

   public TypeElementsScanner includeMethods() {
      return this.includeMethods(true);
   }

   public TypeElementsScanner includeMethods(boolean include) {
      this.includeMethods = include;
      return this;
   }

   public TypeElementsScanner includeAnnotations() {
      return this.includeAnnotations(true);
   }

   public TypeElementsScanner includeAnnotations(boolean include) {
      this.includeAnnotations = include;
      return this;
   }

   public TypeElementsScanner publicOnly(boolean only) {
      this.publicOnly = only;
      return this;
   }

   public TypeElementsScanner publicOnly() {
      return this.publicOnly(true);
   }
}
