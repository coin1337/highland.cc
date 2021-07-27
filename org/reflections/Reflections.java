package org.reflections;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.collect.Sets.SetView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.Scanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;

public class Reflections {
   @Nullable
   public static Logger log = Utils.findLogger(Reflections.class);
   protected final transient Configuration configuration;
   protected Store store;

   public Reflections(Configuration configuration) {
      this.configuration = configuration;
      this.store = new Store(configuration);
      if (configuration.getScanners() != null && !configuration.getScanners().isEmpty()) {
         Iterator var2 = configuration.getScanners().iterator();

         while(var2.hasNext()) {
            Scanner scanner = (Scanner)var2.next();
            scanner.setConfiguration(configuration);
            scanner.setStore(this.store.getOrCreate(scanner.getClass().getSimpleName()));
         }

         this.scan();
         if (configuration.shouldExpandSuperTypes()) {
            this.expandSuperTypes();
         }
      }

   }

   public Reflections(String prefix, @Nullable Scanner... scanners) {
      this(prefix, scanners);
   }

   public Reflections(Object... params) {
      this((Configuration)ConfigurationBuilder.build(params));
   }

   protected Reflections() {
      this.configuration = new ConfigurationBuilder();
      this.store = new Store(this.configuration);
   }

   protected void scan() {
      if (this.configuration.getUrls() != null && !this.configuration.getUrls().isEmpty()) {
         if (log != null && log.isDebugEnabled()) {
            log.debug("going to scan these urls:\n" + Joiner.on("\n").join(this.configuration.getUrls()));
         }

         long time = System.currentTimeMillis();
         int scannedUrls = 0;
         ExecutorService executorService = this.configuration.getExecutorService();
         List<Future<?>> futures = Lists.newArrayList();
         Iterator var6 = this.configuration.getUrls().iterator();

         while(var6.hasNext()) {
            final URL url = (URL)var6.next();

            try {
               if (executorService != null) {
                  futures.add(executorService.submit(new Runnable() {
                     public void run() {
                        if (Reflections.log != null && Reflections.log.isDebugEnabled()) {
                           Reflections.log.debug("[" + Thread.currentThread().toString() + "] scanning " + url);
                        }

                        Reflections.this.scan(url);
                     }
                  }));
               } else {
                  this.scan(url);
               }

               ++scannedUrls;
            } catch (ReflectionsException var11) {
               if (log != null && log.isWarnEnabled()) {
                  log.warn("could not create Vfs.Dir from url. ignoring the exception and continuing", var11);
               }
            }
         }

         if (executorService != null) {
            var6 = futures.iterator();

            while(var6.hasNext()) {
               Future future = (Future)var6.next();

               try {
                  future.get();
               } catch (Exception var10) {
                  throw new RuntimeException(var10);
               }
            }
         }

         time = System.currentTimeMillis() - time;
         if (executorService != null) {
            executorService.shutdown();
         }

         if (log != null) {
            int keys = 0;
            int values = 0;

            String index;
            for(Iterator var8 = this.store.keySet().iterator(); var8.hasNext(); values += this.store.get(index).size()) {
               index = (String)var8.next();
               keys += this.store.get(index).keySet().size();
            }

            log.info(String.format("Reflections took %d ms to scan %d urls, producing %d keys and %d values %s", time, scannedUrls, keys, values, executorService != null && executorService instanceof ThreadPoolExecutor ? String.format("[using %d cores]", ((ThreadPoolExecutor)executorService).getMaximumPoolSize()) : ""));
         }

      } else {
         if (log != null) {
            log.warn("given scan urls are empty. set urls in the configuration");
         }

      }
   }

   protected void scan(URL url) {
      Vfs.Dir dir = Vfs.fromURL(url);

      try {
         Iterator var3 = dir.getFiles().iterator();

         while(var3.hasNext()) {
            Vfs.File file = (Vfs.File)var3.next();
            Predicate<String> inputsFilter = this.configuration.getInputsFilter();
            String path = file.getRelativePath();
            String fqn = path.replace('/', '.');
            if (inputsFilter == null || inputsFilter.apply(path) || inputsFilter.apply(fqn)) {
               Object classObject = null;
               Iterator var9 = this.configuration.getScanners().iterator();

               while(var9.hasNext()) {
                  Scanner scanner = (Scanner)var9.next();

                  try {
                     if (scanner.acceptsInput(path) || scanner.acceptResult(fqn)) {
                        classObject = scanner.scan(file, classObject);
                     }
                  } catch (Exception var15) {
                     if (log != null && log.isDebugEnabled()) {
                        log.debug("could not scan file " + file.getRelativePath() + " in url " + url.toExternalForm() + " with scanner " + scanner.getClass().getSimpleName(), var15);
                     }
                  }
               }
            }
         }
      } finally {
         dir.close();
      }

   }

   public static Reflections collect() {
      return collect("META-INF/reflections/", (new FilterBuilder()).include(".*-reflections.xml"));
   }

   public static Reflections collect(String packagePrefix, Predicate<String> resourceNameFilter, @Nullable Serializer... optionalSerializer) {
      Serializer serializer = optionalSerializer != null && optionalSerializer.length == 1 ? optionalSerializer[0] : new XmlSerializer();
      Collection<URL> urls = ClasspathHelper.forPackage(packagePrefix);
      if (urls.isEmpty()) {
         return null;
      } else {
         long start = System.currentTimeMillis();
         Reflections reflections = new Reflections();
         Iterable<Vfs.File> files = Vfs.findFiles(urls, packagePrefix, resourceNameFilter);
         Iterator var9 = files.iterator();

         while(var9.hasNext()) {
            Vfs.File file = (Vfs.File)var9.next();
            InputStream inputStream = null;

            try {
               inputStream = file.openInputStream();
               reflections.merge(((Serializer)serializer).read(inputStream));
            } catch (IOException var16) {
               throw new ReflectionsException("could not merge " + file, var16);
            } finally {
               Utils.close(inputStream);
            }
         }

         if (log != null) {
            Store store = reflections.getStore();
            int keys = 0;
            int values = 0;

            String index;
            for(Iterator var12 = store.keySet().iterator(); var12.hasNext(); values += store.get(index).size()) {
               index = (String)var12.next();
               keys += store.get(index).keySet().size();
            }

            log.info(String.format("Reflections took %d ms to collect %d url%s, producing %d keys and %d values [%s]", System.currentTimeMillis() - start, urls.size(), urls.size() > 1 ? "s" : "", keys, values, Joiner.on(", ").join(urls)));
         }

         return reflections;
      }
   }

   public Reflections collect(InputStream inputStream) {
      try {
         this.merge(this.configuration.getSerializer().read(inputStream));
         if (log != null) {
            log.info("Reflections collected metadata from input stream using serializer " + this.configuration.getSerializer().getClass().getName());
         }

         return this;
      } catch (Exception var3) {
         throw new ReflectionsException("could not merge input stream", var3);
      }
   }

   public Reflections collect(File file) {
      FileInputStream inputStream = null;

      Reflections var3;
      try {
         inputStream = new FileInputStream(file);
         var3 = this.collect((InputStream)inputStream);
      } catch (FileNotFoundException var7) {
         throw new ReflectionsException("could not obtain input stream from file " + file, var7);
      } finally {
         Utils.close(inputStream);
      }

      return var3;
   }

   public Reflections merge(Reflections reflections) {
      if (reflections.store != null) {
         Iterator var2 = reflections.store.keySet().iterator();

         while(var2.hasNext()) {
            String indexName = (String)var2.next();
            Multimap<String, String> index = reflections.store.get(indexName);
            Iterator var5 = index.keySet().iterator();

            while(var5.hasNext()) {
               String key = (String)var5.next();
               Iterator var7 = index.get(key).iterator();

               while(var7.hasNext()) {
                  String string = (String)var7.next();
                  this.store.getOrCreate(indexName).put(key, string);
               }
            }
         }
      }

      return this;
   }

   public void expandSuperTypes() {
      if (this.store.keySet().contains(index(SubTypesScanner.class))) {
         Multimap<String, String> mmap = this.store.get(index(SubTypesScanner.class));
         SetView<String> keys = Sets.difference(mmap.keySet(), Sets.newHashSet(mmap.values()));
         Multimap<String, String> expand = HashMultimap.create();
         UnmodifiableIterator var4 = keys.iterator();

         while(var4.hasNext()) {
            String key = (String)var4.next();
            Class<?> type = ReflectionUtils.forName(key);
            if (type != null) {
               this.expandSupertypes(expand, key, type);
            }
         }

         mmap.putAll(expand);
      }

   }

   private void expandSupertypes(Multimap<String, String> mmap, String key, Class<?> type) {
      Iterator var4 = ReflectionUtils.getSuperTypes(type).iterator();

      while(var4.hasNext()) {
         Class<?> supertype = (Class)var4.next();
         if (mmap.put(supertype.getName(), key)) {
            if (log != null) {
               log.debug("expanded subtype {} -> {}", supertype.getName(), key);
            }

            this.expandSupertypes(mmap, supertype.getName(), supertype);
         }
      }

   }

   public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {
      return Sets.newHashSet(ReflectionUtils.forNames(this.store.getAll(index(SubTypesScanner.class), (Iterable)Arrays.asList(type.getName())), this.loaders()));
   }

   public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
      return this.getTypesAnnotatedWith(annotation, false);
   }

   public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation, boolean honorInherited) {
      Iterable<String> annotated = this.store.get(index(TypeAnnotationsScanner.class), annotation.getName());
      Iterable<String> classes = this.getAllAnnotated(annotated, annotation.isAnnotationPresent(Inherited.class), honorInherited);
      return Sets.newHashSet(Iterables.concat(ReflectionUtils.forNames(annotated, this.loaders()), ReflectionUtils.forNames(classes, this.loaders())));
   }

   public Set<Class<?>> getTypesAnnotatedWith(Annotation annotation) {
      return this.getTypesAnnotatedWith(annotation, false);
   }

   public Set<Class<?>> getTypesAnnotatedWith(Annotation annotation, boolean honorInherited) {
      Iterable<String> annotated = this.store.get(index(TypeAnnotationsScanner.class), annotation.annotationType().getName());
      Iterable<Class<?>> filter = ReflectionUtils.filter((Iterable)ReflectionUtils.forNames(annotated, this.loaders()), ReflectionUtils.withAnnotation(annotation));
      Iterable<String> classes = this.getAllAnnotated(Utils.names((Iterable)filter), annotation.annotationType().isAnnotationPresent(Inherited.class), honorInherited);
      return Sets.newHashSet(Iterables.concat(filter, ReflectionUtils.forNames(ReflectionUtils.filter(classes, Predicates.not(Predicates.in(Sets.newHashSet(annotated)))), this.loaders())));
   }

   protected Iterable<String> getAllAnnotated(Iterable<String> annotated, boolean inherited, boolean honorInherited) {
      Iterable subTypes;
      if (honorInherited) {
         if (inherited) {
            subTypes = this.store.get(index(SubTypesScanner.class), (Iterable)ReflectionUtils.filter(annotated, new Predicate<String>() {
               public boolean apply(@Nullable String input) {
                  Class<?> type = ReflectionUtils.forName(input, Reflections.this.loaders());
                  return type != null && !type.isInterface();
               }
            }));
            return Iterables.concat(subTypes, this.store.getAll(index(SubTypesScanner.class), subTypes));
         } else {
            return annotated;
         }
      } else {
         subTypes = Iterables.concat(annotated, this.store.getAll(index(TypeAnnotationsScanner.class), annotated));
         return Iterables.concat(subTypes, this.store.getAll(index(SubTypesScanner.class), subTypes));
      }
   }

   public Set<Method> getMethodsAnnotatedWith(Class<? extends Annotation> annotation) {
      Iterable<String> methods = this.store.get(index(MethodAnnotationsScanner.class), annotation.getName());
      return Utils.getMethodsFromDescriptors(methods, this.loaders());
   }

   public Set<Method> getMethodsAnnotatedWith(Annotation annotation) {
      return ReflectionUtils.filter((Iterable)this.getMethodsAnnotatedWith(annotation.annotationType()), ReflectionUtils.withAnnotation(annotation));
   }

   public Set<Method> getMethodsMatchParams(Class<?>... types) {
      return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), Utils.names(types).toString()), this.loaders());
   }

   public Set<Method> getMethodsReturn(Class returnType) {
      return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), (Iterable)Utils.names(returnType)), this.loaders());
   }

   public Set<Method> getMethodsWithAnyParamAnnotated(Class<? extends Annotation> annotation) {
      return Utils.getMethodsFromDescriptors(this.store.get(index(MethodParameterScanner.class), annotation.getName()), this.loaders());
   }

   public Set<Method> getMethodsWithAnyParamAnnotated(Annotation annotation) {
      return ReflectionUtils.filter((Iterable)this.getMethodsWithAnyParamAnnotated(annotation.annotationType()), ReflectionUtils.withAnyParameterAnnotation(annotation));
   }

   public Set<Constructor> getConstructorsAnnotatedWith(Class<? extends Annotation> annotation) {
      Iterable<String> methods = this.store.get(index(MethodAnnotationsScanner.class), annotation.getName());
      return Utils.getConstructorsFromDescriptors(methods, this.loaders());
   }

   public Set<Constructor> getConstructorsAnnotatedWith(Annotation annotation) {
      return ReflectionUtils.filter((Iterable)this.getConstructorsAnnotatedWith(annotation.annotationType()), ReflectionUtils.withAnnotation(annotation));
   }

   public Set<Constructor> getConstructorsMatchParams(Class<?>... types) {
      return Utils.getConstructorsFromDescriptors(this.store.get(index(MethodParameterScanner.class), Utils.names(types).toString()), this.loaders());
   }

   public Set<Constructor> getConstructorsWithAnyParamAnnotated(Class<? extends Annotation> annotation) {
      return Utils.getConstructorsFromDescriptors(this.store.get(index(MethodParameterScanner.class), annotation.getName()), this.loaders());
   }

   public Set<Constructor> getConstructorsWithAnyParamAnnotated(Annotation annotation) {
      return ReflectionUtils.filter((Iterable)this.getConstructorsWithAnyParamAnnotated(annotation.annotationType()), ReflectionUtils.withAnyParameterAnnotation(annotation));
   }

   public Set<Field> getFieldsAnnotatedWith(Class<? extends Annotation> annotation) {
      Set<Field> result = Sets.newHashSet();
      Iterator var3 = this.store.get(index(FieldAnnotationsScanner.class), annotation.getName()).iterator();

      while(var3.hasNext()) {
         String annotated = (String)var3.next();
         result.add(Utils.getFieldFromString(annotated, this.loaders()));
      }

      return result;
   }

   public Set<Field> getFieldsAnnotatedWith(Annotation annotation) {
      return ReflectionUtils.filter((Iterable)this.getFieldsAnnotatedWith(annotation.annotationType()), ReflectionUtils.withAnnotation(annotation));
   }

   public Set<String> getResources(Predicate<String> namePredicate) {
      Iterable<String> resources = Iterables.filter(this.store.get(index(ResourcesScanner.class)).keySet(), namePredicate);
      return Sets.newHashSet(this.store.get(index(ResourcesScanner.class), resources));
   }

   public Set<String> getResources(final Pattern pattern) {
      return this.getResources(new Predicate<String>() {
         public boolean apply(String input) {
            return pattern.matcher(input).matches();
         }
      });
   }

   public List<String> getMethodParamNames(Method method) {
      Iterable<String> names = this.store.get(index(MethodParameterNamesScanner.class), Utils.name(method));
      return !Iterables.isEmpty(names) ? Arrays.asList(((String)Iterables.getOnlyElement(names)).split(", ")) : Arrays.asList();
   }

   public List<String> getConstructorParamNames(Constructor constructor) {
      Iterable<String> names = this.store.get(index(MethodParameterNamesScanner.class), Utils.name(constructor));
      return !Iterables.isEmpty(names) ? Arrays.asList(((String)Iterables.getOnlyElement(names)).split(", ")) : Arrays.asList();
   }

   public Set<Member> getFieldUsage(Field field) {
      return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), Utils.name(field)));
   }

   public Set<Member> getMethodUsage(Method method) {
      return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), Utils.name(method)));
   }

   public Set<Member> getConstructorUsage(Constructor constructor) {
      return Utils.getMembersFromDescriptors(this.store.get(index(MemberUsageScanner.class), Utils.name(constructor)));
   }

   public Set<String> getAllTypes() {
      Set<String> allTypes = Sets.newHashSet(this.store.getAll(index(SubTypesScanner.class), Object.class.getName()));
      if (allTypes.isEmpty()) {
         throw new ReflectionsException("Couldn't find subtypes of Object. Make sure SubTypesScanner initialized to include Object class - new SubTypesScanner(false)");
      } else {
         return allTypes;
      }
   }

   public Store getStore() {
      return this.store;
   }

   public Configuration getConfiguration() {
      return this.configuration;
   }

   public File save(String filename) {
      return this.save(filename, this.configuration.getSerializer());
   }

   public File save(String filename, Serializer serializer) {
      File file = serializer.save(this, filename);
      if (log != null) {
         log.info("Reflections successfully saved in " + file.getAbsolutePath() + " using " + serializer.getClass().getSimpleName());
      }

      return file;
   }

   private static String index(Class<? extends Scanner> scannerClass) {
      return scannerClass.getSimpleName();
   }

   private ClassLoader[] loaders() {
      return this.configuration.getClassLoaders();
   }
}
