package org.spongepowered.asm.launch.platform;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

final class MainAttributes {
   private static final Map<URI, MainAttributes> instances = new HashMap();
   protected final Attributes attributes;

   private MainAttributes() {
      this.attributes = new Attributes();
   }

   private MainAttributes(File jar) {
      this.attributes = getAttributes(jar);
   }

   public final String get(String name) {
      return this.attributes != null ? this.attributes.getValue(name) : null;
   }

   private static Attributes getAttributes(File jar) {
      if (jar == null) {
         return null;
      } else {
         JarFile jarFile = null;

         try {
            jarFile = new JarFile(jar);
            Manifest manifest = jarFile.getManifest();
            if (manifest != null) {
               Attributes var3 = manifest.getMainAttributes();
               return var3;
            }
         } catch (IOException var14) {
         } finally {
            try {
               if (jarFile != null) {
                  jarFile.close();
               }
            } catch (IOException var13) {
            }

         }

         return new Attributes();
      }
   }

   public static MainAttributes of(File jar) {
      return of(jar.toURI());
   }

   public static MainAttributes of(URI uri) {
      MainAttributes attributes = (MainAttributes)instances.get(uri);
      if (attributes == null) {
         attributes = new MainAttributes(new File(uri));
         instances.put(uri, attributes);
      }

      return attributes;
   }
}
