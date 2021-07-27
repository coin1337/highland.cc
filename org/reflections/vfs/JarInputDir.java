package org.reflections.vfs;

import com.google.common.collect.AbstractIterator;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import org.reflections.ReflectionsException;
import org.reflections.util.Utils;

public class JarInputDir implements Vfs.Dir {
   private final URL url;
   JarInputStream jarInputStream;
   long cursor = 0L;
   long nextCursor = 0L;

   public JarInputDir(URL url) {
      this.url = url;
   }

   public String getPath() {
      return this.url.getPath();
   }

   public Iterable<Vfs.File> getFiles() {
      return new Iterable<Vfs.File>() {
         public Iterator<Vfs.File> iterator() {
            return new AbstractIterator<Vfs.File>() {
               {
                  try {
                     JarInputDir.this.jarInputStream = new JarInputStream(JarInputDir.this.url.openConnection().getInputStream());
                  } catch (Exception var3) {
                     throw new ReflectionsException("Could not open url connection", var3);
                  }
               }

               protected Vfs.File computeNext() {
                  while(true) {
                     try {
                        ZipEntry entry = JarInputDir.this.jarInputStream.getNextJarEntry();
                        if (entry == null) {
                           return (Vfs.File)this.endOfData();
                        }

                        long size = entry.getSize();
                        if (size < 0L) {
                           size += 4294967295L;
                        }

                        JarInputDir var10000 = JarInputDir.this;
                        var10000.nextCursor += size;
                        if (!entry.isDirectory()) {
                           return new JarInputFile(entry, JarInputDir.this, JarInputDir.this.cursor, JarInputDir.this.nextCursor);
                        }
                     } catch (IOException var4) {
                        throw new ReflectionsException("could not get next zip entry", var4);
                     }
                  }
               }
            };
         }
      };
   }

   public void close() {
      Utils.close(this.jarInputStream);
   }
}
