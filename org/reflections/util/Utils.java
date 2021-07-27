package org.reflections.util;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Utils {
   public static String repeat(String string, int times) {
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < times; ++i) {
         sb.append(string);
      }

      return sb.toString();
   }

   public static boolean isEmpty(String s) {
      return s == null || s.length() == 0;
   }

   public static boolean isEmpty(Object[] objects) {
      return objects == null || objects.length == 0;
   }

   public static File prepareFile(String filename) {
      File file = new File(filename);
      File parent = file.getAbsoluteFile().getParentFile();
      if (!parent.exists()) {
         parent.mkdirs();
      }

      return file;
   }

   public static Member getMemberFromDescriptor(String descriptor, ClassLoader... classLoaders) throws ReflectionsException {
      int p0 = descriptor.lastIndexOf(40);
      String memberKey = p0 != -1 ? descriptor.substring(0, p0) : descriptor;
      String methodParameters = p0 != -1 ? descriptor.substring(p0 + 1, descriptor.lastIndexOf(41)) : "";
      int p1 = Math.max(memberKey.lastIndexOf(46), memberKey.lastIndexOf("$"));
      String className = memberKey.substring(memberKey.lastIndexOf(32) + 1, p1);
      String memberName = memberKey.substring(p1 + 1);
      Class<?>[] parameterTypes = null;
      if (!isEmpty(methodParameters)) {
         String[] parameterNames = methodParameters.split(",");
         List<Class<?>> result = new ArrayList(parameterNames.length);
         String[] var11 = parameterNames;
         int var12 = parameterNames.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            String name = var11[var13];
            result.add(ReflectionUtils.forName(name.trim(), classLoaders));
         }

         parameterTypes = (Class[])result.toArray(new Class[result.size()]);
      }

      Class aClass = ReflectionUtils.forName(className, classLoaders);

      while(aClass != null) {
         try {
            if (descriptor.contains("(")) {
               if (!isConstructor(descriptor)) {
                  return aClass.isInterface() ? aClass.getMethod(memberName, parameterTypes) : aClass.getDeclaredMethod(memberName, parameterTypes);
               }

               return aClass.isInterface() ? aClass.getConstructor(parameterTypes) : aClass.getDeclaredConstructor(parameterTypes);
            }

            return aClass.isInterface() ? aClass.getField(memberName) : aClass.getDeclaredField(memberName);
         } catch (Exception var15) {
            aClass = aClass.getSuperclass();
         }
      }

      throw new ReflectionsException("Can't resolve member named " + memberName + " for class " + className);
   }

   public static Set<Method> getMethodsFromDescriptors(Iterable<String> annotatedWith, ClassLoader... classLoaders) {
      Set<Method> result = Sets.newHashSet();
      Iterator var3 = annotatedWith.iterator();

      while(var3.hasNext()) {
         String annotated = (String)var3.next();
         if (!isConstructor(annotated)) {
            Method member = (Method)getMemberFromDescriptor(annotated, classLoaders);
            if (member != null) {
               result.add(member);
            }
         }
      }

      return result;
   }

   public static Set<Constructor> getConstructorsFromDescriptors(Iterable<String> annotatedWith, ClassLoader... classLoaders) {
      Set<Constructor> result = Sets.newHashSet();
      Iterator var3 = annotatedWith.iterator();

      while(var3.hasNext()) {
         String annotated = (String)var3.next();
         if (isConstructor(annotated)) {
            Constructor member = (Constructor)getMemberFromDescriptor(annotated, classLoaders);
            if (member != null) {
               result.add(member);
            }
         }
      }

      return result;
   }

   public static Set<Member> getMembersFromDescriptors(Iterable<String> values, ClassLoader... classLoaders) {
      Set<Member> result = Sets.newHashSet();
      Iterator var3 = values.iterator();

      while(var3.hasNext()) {
         String value = (String)var3.next();

         try {
            result.add(getMemberFromDescriptor(value, classLoaders));
         } catch (ReflectionsException var6) {
            throw new ReflectionsException("Can't resolve member named " + value, var6);
         }
      }

      return result;
   }

   public static Field getFieldFromString(String field, ClassLoader... classLoaders) {
      String className = field.substring(0, field.lastIndexOf(46));
      String fieldName = field.substring(field.lastIndexOf(46) + 1);

      try {
         return ReflectionUtils.forName(className, classLoaders).getDeclaredField(fieldName);
      } catch (NoSuchFieldException var5) {
         throw new ReflectionsException("Can't resolve field named " + fieldName, var5);
      }
   }

   public static void close(InputStream closeable) {
      try {
         if (closeable != null) {
            closeable.close();
         }
      } catch (IOException var2) {
         if (Reflections.log != null) {
            Reflections.log.warn("Could not close InputStream", var2);
         }
      }

   }

   @Nullable
   public static Logger findLogger(Class<?> aClass) {
      try {
         Class.forName("org.slf4j.impl.StaticLoggerBinder");
         return LoggerFactory.getLogger(aClass);
      } catch (Throwable var2) {
         return null;
      }
   }

   public static boolean isConstructor(String fqn) {
      return fqn.contains("init>");
   }

   public static String name(Class type) {
      if (!type.isArray()) {
         return type.getName();
      } else {
         int dim;
         for(dim = 0; type.isArray(); type = type.getComponentType()) {
            ++dim;
         }

         return type.getName() + repeat("[]", dim);
      }
   }

   public static List<String> names(Iterable<Class<?>> types) {
      List<String> result = new ArrayList();
      Iterator var2 = types.iterator();

      while(var2.hasNext()) {
         Class<?> type = (Class)var2.next();
         result.add(name(type));
      }

      return result;
   }

   public static List<String> names(Class<?>... types) {
      return names((Iterable)Arrays.asList(types));
   }

   public static String name(Constructor constructor) {
      return constructor.getName() + ".<init>(" + Joiner.on(", ").join(names(constructor.getParameterTypes())) + ")";
   }

   public static String name(Method method) {
      return method.getDeclaringClass().getName() + "." + method.getName() + "(" + Joiner.on(", ").join(names(method.getParameterTypes())) + ")";
   }

   public static String name(Field field) {
      return field.getDeclaringClass().getName() + "." + field.getName();
   }
}
