package org.reflections.scanners;

import java.util.Iterator;
import org.reflections.util.FilterBuilder;

public class SubTypesScanner extends AbstractScanner {
   public SubTypesScanner() {
      this(true);
   }

   public SubTypesScanner(boolean excludeObjectClass) {
      if (excludeObjectClass) {
         this.filterResultsBy((new FilterBuilder()).exclude(Object.class.getName()));
      }

   }

   public void scan(Object cls) {
      String className = this.getMetadataAdapter().getClassName(cls);
      String superclass = this.getMetadataAdapter().getSuperclassName(cls);
      if (this.acceptResult(superclass)) {
         this.getStore().put(superclass, className);
      }

      Iterator var4 = this.getMetadataAdapter().getInterfacesNames(cls).iterator();

      while(var4.hasNext()) {
         String anInterface = (String)var4.next();
         if (this.acceptResult(anInterface)) {
            this.getStore().put(anInterface, className);
         }
      }

   }
}
