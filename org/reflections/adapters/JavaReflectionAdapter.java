package org.reflections.adapters;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import org.reflections.ReflectionUtils;
import org.reflections.util.Utils;
import org.reflections.vfs.Vfs;

public class JavaReflectionAdapter implements MetadataAdapter<Class, Field, Member> {
   public List<Field> getFields(Class cls) {
      return Lists.newArrayList(cls.getDeclaredFields());
   }

   public List<Member> getMethods(Class cls) {
      List<Member> methods = Lists.newArrayList();
      methods.addAll(Arrays.asList(cls.getDeclaredMethods()));
      methods.addAll(Arrays.asList(cls.getDeclaredConstructors()));
      return methods;
   }

   public String getMethodName(Member method) {
      return method instanceof Method ? method.getName() : (method instanceof Constructor ? "<init>" : null);
   }

   public List<String> getParameterNames(Member member) {
      List<String> result = Lists.newArrayList();
      Class<?>[] parameterTypes = member instanceof Method ? ((Method)member).getParameterTypes() : (member instanceof Constructor ? ((Constructor)member).getParameterTypes() : null);
      if (parameterTypes != null) {
         Class[] var4 = parameterTypes;
         int var5 = parameterTypes.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Class<?> paramType = var4[var6];
            String name = getName(paramType);
            result.add(name);
         }
      }

      return result;
   }

   public List<String> getClassAnnotationNames(Class aClass) {
      return this.getAnnotationNames(aClass.getDeclaredAnnotations());
   }

   public List<String> getFieldAnnotationNames(Field field) {
      return this.getAnnotationNames(field.getDeclaredAnnotations());
   }

   public List<String> getMethodAnnotationNames(Member method) {
      Annotation[] annotations = method instanceof Method ? ((Method)method).getDeclaredAnnotations() : (method instanceof Constructor ? ((Constructor)method).getDeclaredAnnotations() : null);
      return this.getAnnotationNames(annotations);
   }

   public List<String> getParameterAnnotationNames(Member method, int parameterIndex) {
      Annotation[][] annotations = method instanceof Method ? ((Method)method).getParameterAnnotations() : (method instanceof Constructor ? ((Constructor)method).getParameterAnnotations() : (Annotation[][])null);
      return this.getAnnotationNames(annotations != null ? annotations[parameterIndex] : null);
   }

   public String getReturnTypeName(Member method) {
      return ((Method)method).getReturnType().getName();
   }

   public String getFieldName(Field field) {
      return field.getName();
   }

   public Class getOfCreateClassObject(Vfs.File file) throws Exception {
      return this.getOfCreateClassObject(file, (ClassLoader[])null);
   }

   public Class getOfCreateClassObject(Vfs.File file, @Nullable ClassLoader... loaders) throws Exception {
      String name = file.getRelativePath().replace("/", ".").replace(".class", "");
      return ReflectionUtils.forName(name, loaders);
   }

   public String getMethodModifier(Member method) {
      return Modifier.toString(method.getModifiers());
   }

   public String getMethodKey(Class cls, Member method) {
      return this.getMethodName(method) + "(" + Joiner.on(", ").join(this.getParameterNames(method)) + ")";
   }

   public String getMethodFullKey(Class cls, Member method) {
      return this.getClassName(cls) + "." + this.getMethodKey(cls, method);
   }

   public boolean isPublic(Object o) {
      Integer mod = o instanceof Class ? ((Class)o).getModifiers() : o instanceof Member ? ((Member)o).getModifiers() : null;
      return mod != null && Modifier.isPublic(mod);
   }

   public String getClassName(Class cls) {
      return cls.getName();
   }

   public String getSuperclassName(Class cls) {
      Class superclass = cls.getSuperclass();
      return superclass != null ? superclass.getName() : "";
   }

   public List<String> getInterfacesNames(Class cls) {
      Class[] classes = cls.getInterfaces();
      List<String> names = new ArrayList(classes != null ? classes.length : 0);
      if (classes != null) {
         Class[] var4 = classes;
         int var5 = classes.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            Class cls1 = var4[var6];
            names.add(cls1.getName());
         }
      }

      return names;
   }

   public boolean acceptsInput(String file) {
      return file.endsWith(".class");
   }

   private List<String> getAnnotationNames(Annotation[] annotations) {
      List<String> names = new ArrayList(annotations.length);
      Annotation[] var3 = annotations;
      int var4 = annotations.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Annotation annotation = var3[var5];
         names.add(annotation.annotationType().getName());
      }

      return names;
   }

   public static String getName(Class type) {
      if (type.isArray()) {
         try {
            Class cl = type;

            int dim;
            for(dim = 0; cl.isArray(); cl = cl.getComponentType()) {
               ++dim;
            }

            return cl.getName() + Utils.repeat("[]", dim);
         } catch (Throwable var3) {
         }
      }

      return type.getName();
   }
}
