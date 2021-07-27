package org.reflections.vfs;

import com.google.common.base.Predicate;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;

public class UrlTypeVFS implements Vfs.UrlType {
   public static final String[] REPLACE_EXTENSION = new String[]{".ear/", ".jar/", ".war/", ".sar/", ".har/", ".par/"};
   final String VFSZIP = "vfszip";
   final String VFSFILE = "vfsfile";
   Predicate<File> realFile = new Predicate<File>() {
      public boolean apply(File file) {
         return file.exists() && file.isFile();
      }
   };

   public boolean matches(URL url) {
      return "vfszip".equals(url.getProtocol()) || "vfsfile".equals(url.getProtocol());
   }

   public Vfs.Dir createDir(URL url) {
      try {
         URL adaptedUrl = this.adaptURL(url);
         return new ZipDir(new JarFile(adaptedUrl.getFile()));
      } catch (Exception var5) {
         try {
            return new ZipDir(new JarFile(url.getFile()));
         } catch (IOException var4) {
            if (Reflections.log != null) {
               Reflections.log.warn("Could not get URL", var5);
               Reflections.log.warn("Could not get URL", var4);
            }

            return null;
         }
      }
   }

   public URL adaptURL(URL url) throws MalformedURLException {
      if ("vfszip".equals(url.getProtocol())) {
         return this.replaceZipSeparators(url.getPath(), this.realFile);
      } else {
         return "vfsfile".equals(url.getProtocol()) ? new URL(url.toString().replace("vfsfile", "file")) : url;
      }
   }

   URL replaceZipSeparators(String path, Predicate<File> acceptFile) throws MalformedURLException {
      int pos = 0;

      while(pos != -1) {
         pos = this.findFirstMatchOfDeployableExtention(path, pos);
         if (pos > 0) {
            File file = new File(path.substring(0, pos - 1));
            if (acceptFile.apply(file)) {
               return this.replaceZipSeparatorStartingFrom(path, pos);
            }
         }
      }

      throw new ReflectionsException("Unable to identify the real zip file in path '" + path + "'.");
   }

   int findFirstMatchOfDeployableExtention(String path, int pos) {
      Pattern p = Pattern.compile("\\.[ejprw]ar/");
      Matcher m = p.matcher(path);
      return m.find(pos) ? m.end() : -1;
   }

   URL replaceZipSeparatorStartingFrom(String path, int pos) throws MalformedURLException {
      String zipFile = path.substring(0, pos - 1);
      String zipPath = path.substring(pos);
      int numSubs = 1;
      String[] var6 = REPLACE_EXTENSION;
      int i = var6.length;

      for(int var8 = 0; var8 < i; ++var8) {
         for(String ext = var6[var8]; zipPath.contains(ext); ++numSubs) {
            zipPath = zipPath.replace(ext, ext.substring(0, 4) + "!");
         }
      }

      String prefix = "";

      for(i = 0; i < numSubs; ++i) {
         prefix = prefix + "zip:";
      }

      if (zipPath.trim().length() == 0) {
         return new URL(prefix + "/" + zipFile);
      } else {
         return new URL(prefix + "/" + zipFile + "!" + zipPath);
      }
   }
}
