package org.reflections.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.adapters.JavaReflectionAdapter;
import org.reflections.adapters.JavassistAdapter;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;

public class ConfigurationBuilder implements Configuration {
   @Nonnull
   private Set<Scanner> scanners = Sets.newHashSet(new Scanner[]{new TypeAnnotationsScanner(), new SubTypesScanner()});
   @Nonnull
   private Set<URL> urls = Sets.newHashSet();
   protected MetadataAdapter metadataAdapter;
   @Nullable
   private Predicate<String> inputsFilter;
   private Serializer serializer;
   @Nullable
   private ExecutorService executorService;
   @Nullable
   private ClassLoader[] classLoaders;
   private boolean expandSuperTypes = true;

   public static ConfigurationBuilder build(@Nullable Object... params) {
      ConfigurationBuilder builder = new ConfigurationBuilder();
      List<Object> parameters = Lists.newArrayList();
      Iterator var7;
      Object param;
      if (params != null) {
         Object[] var3 = params;
         int var4 = params.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Object param = var3[var5];
            if (param != null) {
               if (param.getClass().isArray()) {
                  Object[] var18 = (Object[])((Object[])param);
                  int var19 = var18.length;

                  for(int var9 = 0; var9 < var19; ++var9) {
                     Object p = var18[var9];
                     if (p != null) {
                        parameters.add(p);
                     }
                  }
               } else if (param instanceof Iterable) {
                  var7 = ((Iterable)param).iterator();

                  while(var7.hasNext()) {
                     param = var7.next();
                     if (param != null) {
                        parameters.add(param);
                     }
                  }
               } else {
                  parameters.add(param);
               }
            }
         }
      }

      List<ClassLoader> loaders = Lists.newArrayList();
      Iterator var13 = parameters.iterator();

      while(var13.hasNext()) {
         Object param = var13.next();
         if (param instanceof ClassLoader) {
            loaders.add((ClassLoader)param);
         }
      }

      ClassLoader[] classLoaders = loaders.isEmpty() ? null : (ClassLoader[])loaders.toArray(new ClassLoader[loaders.size()]);
      FilterBuilder filter = new FilterBuilder();
      List<Scanner> scanners = Lists.newArrayList();
      var7 = parameters.iterator();

      while(var7.hasNext()) {
         param = var7.next();
         if (param instanceof String) {
            builder.addUrls(ClasspathHelper.forPackage((String)param, classLoaders));
            filter.includePackage((String)param);
         } else if (param instanceof Class) {
            if (Scanner.class.isAssignableFrom((Class)param)) {
               try {
                  builder.addScanners((Scanner)((Class)param).newInstance());
               } catch (Exception var11) {
               }
            }

            builder.addUrls(ClasspathHelper.forClass((Class)param, classLoaders));
            filter.includePackage((Class)param);
         } else if (param instanceof Scanner) {
            scanners.add((Scanner)param);
         } else if (param instanceof URL) {
            builder.addUrls((URL)param);
         } else if (!(param instanceof ClassLoader)) {
            if (param instanceof Predicate) {
               filter.add((Predicate)param);
            } else if (param instanceof ExecutorService) {
               builder.setExecutorService((ExecutorService)param);
            } else if (Reflections.log != null) {
               throw new ReflectionsException("could not use param " + param);
            }
         }
      }

      if (builder.getUrls().isEmpty()) {
         if (classLoaders != null) {
            builder.addUrls(ClasspathHelper.forClassLoader(classLoaders));
         } else {
            builder.addUrls(ClasspathHelper.forClassLoader());
         }
      }

      builder.filterInputsBy(filter);
      if (!scanners.isEmpty()) {
         builder.setScanners((Scanner[])scanners.toArray(new Scanner[scanners.size()]));
      }

      if (!loaders.isEmpty()) {
         builder.addClassLoaders((Collection)loaders);
      }

      return builder;
   }

   public ConfigurationBuilder forPackages(String... packages) {
      String[] var2 = packages;
      int var3 = packages.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String pkg = var2[var4];
         this.addUrls(ClasspathHelper.forPackage(pkg));
      }

      return this;
   }

   @Nonnull
   public Set<Scanner> getScanners() {
      return this.scanners;
   }

   public ConfigurationBuilder setScanners(@Nonnull Scanner... scanners) {
      this.scanners.clear();
      return this.addScanners(scanners);
   }

   public ConfigurationBuilder addScanners(Scanner... scanners) {
      this.scanners.addAll(Sets.newHashSet(scanners));
      return this;
   }

   @Nonnull
   public Set<URL> getUrls() {
      return this.urls;
   }

   public ConfigurationBuilder setUrls(@Nonnull Collection<URL> urls) {
      this.urls = Sets.newHashSet(urls);
      return this;
   }

   public ConfigurationBuilder setUrls(URL... urls) {
      this.urls = Sets.newHashSet(urls);
      return this;
   }

   public ConfigurationBuilder addUrls(Collection<URL> urls) {
      this.urls.addAll(urls);
      return this;
   }

   public ConfigurationBuilder addUrls(URL... urls) {
      this.urls.addAll(Sets.newHashSet(urls));
      return this;
   }

   public MetadataAdapter getMetadataAdapter() {
      if (this.metadataAdapter != null) {
         return this.metadataAdapter;
      } else {
         try {
            return this.metadataAdapter = new JavassistAdapter();
         } catch (Throwable var2) {
            if (Reflections.log != null) {
               Reflections.log.warn("could not create JavassistAdapter, using JavaReflectionAdapter", var2);
            }

            return this.metadataAdapter = new JavaReflectionAdapter();
         }
      }
   }

   public ConfigurationBuilder setMetadataAdapter(MetadataAdapter metadataAdapter) {
      this.metadataAdapter = metadataAdapter;
      return this;
   }

   @Nullable
   public Predicate<String> getInputsFilter() {
      return this.inputsFilter;
   }

   public void setInputsFilter(@Nullable Predicate<String> inputsFilter) {
      this.inputsFilter = inputsFilter;
   }

   public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
      this.inputsFilter = inputsFilter;
      return this;
   }

   @Nullable
   public ExecutorService getExecutorService() {
      return this.executorService;
   }

   public ConfigurationBuilder setExecutorService(@Nullable ExecutorService executorService) {
      this.executorService = executorService;
      return this;
   }

   public ConfigurationBuilder useParallelExecutor() {
      return this.useParallelExecutor(Runtime.getRuntime().availableProcessors());
   }

   public ConfigurationBuilder useParallelExecutor(int availableProcessors) {
      ThreadFactory factory = (new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("org.reflections-scanner-%d").build();
      this.setExecutorService(Executors.newFixedThreadPool(availableProcessors, factory));
      return this;
   }

   public Serializer getSerializer() {
      return this.serializer != null ? this.serializer : (this.serializer = new XmlSerializer());
   }

   public ConfigurationBuilder setSerializer(Serializer serializer) {
      this.serializer = serializer;
      return this;
   }

   @Nullable
   public ClassLoader[] getClassLoaders() {
      return this.classLoaders;
   }

   public boolean shouldExpandSuperTypes() {
      return this.expandSuperTypes;
   }

   public ConfigurationBuilder setExpandSuperTypes(boolean expandSuperTypes) {
      this.expandSuperTypes = expandSuperTypes;
      return this;
   }

   public void setClassLoaders(@Nullable ClassLoader[] classLoaders) {
      this.classLoaders = classLoaders;
   }

   public ConfigurationBuilder addClassLoader(ClassLoader classLoader) {
      return this.addClassLoaders(classLoader);
   }

   public ConfigurationBuilder addClassLoaders(ClassLoader... classLoaders) {
      this.classLoaders = this.classLoaders == null ? classLoaders : (ClassLoader[])ObjectArrays.concat(this.classLoaders, classLoaders, ClassLoader.class);
      return this;
   }

   public ConfigurationBuilder addClassLoaders(Collection<ClassLoader> classLoaders) {
      return this.addClassLoaders((ClassLoader[])classLoaders.toArray(new ClassLoader[classLoaders.size()]));
   }
}
