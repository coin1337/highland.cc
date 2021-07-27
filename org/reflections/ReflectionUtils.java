package org.reflections;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.Utils;

public abstract class ReflectionUtils {
   public static boolean includeObject = false;
   private static List<String> primitiveNames;
   private static List<Class> primitiveTypes;
   private static List<String> primitiveDescriptors;

   public static Set<Class<?>> getAllSuperTypes(Class<?> type, Predicate<? super Class<?>>... predicates) {
      Set<Class<?>> result = Sets.newLinkedHashSet();
      if (type != null && (includeObject || !type.equals(Object.class))) {
         result.add(type);
         Iterator var3 = getSuperTypes(type).iterator();

         while(var3.hasNext()) {
            Class<?> supertype = (Class)var3.next();
            result.addAll(getAllSuperTypes(supertype));
         }
      }

      return filter((Iterable)result, predicates);
   }

   public static Set<Class<?>> getSuperTypes(Class<?> type) {
      Set<Class<?>> result = new LinkedHashSet();
      Class<?> superclass = type.getSuperclass();
      Class<?>[] interfaces = type.getInterfaces();
      if (superclass != null && (includeObject || !superclass.equals(Object.class))) {
         result.add(superclass);
      }

      if (interfaces != null && interfaces.length > 0) {
         result.addAll(Arrays.asList(interfaces));
      }

      return result;
   }

   public static Set<Method> getAllMethods(Class<?> type, Predicate<? super Method>... predicates) {
      Set<Method> result = Sets.newHashSet();
      Iterator var3 = getAllSuperTypes(type).iterator();

      while(var3.hasNext()) {
         Class<?> t = (Class)var3.next();
         result.addAll(getMethods(t, predicates));
      }

      return result;
   }

   public static Set<Method> getMethods(Class<?> t, Predicate<? super Method>... predicates) {
      return filter((Object[])(t.isInterface() ? t.getMethods() : t.getDeclaredMethods()), predicates);
   }

   public static Set<Constructor> getAllConstructors(Class<?> type, Predicate<? super Constructor>... predicates) {
      Set<Constructor> result = Sets.newHashSet();
      Iterator var3 = getAllSuperTypes(type).iterator();

      while(var3.hasNext()) {
         Class<?> t = (Class)var3.next();
         result.addAll(getConstructors(t, predicates));
      }

      return result;
   }

   public static Set<Constructor> getConstructors(Class<?> t, Predicate<? super Constructor>... predicates) {
      return filter((Object[])t.getDeclaredConstructors(), predicates);
   }

   public static Set<Field> getAllFields(Class<?> type, Predicate<? super Field>... predicates) {
      Set<Field> result = Sets.newHashSet();
      Iterator var3 = getAllSuperTypes(type).iterator();

      while(var3.hasNext()) {
         Class<?> t = (Class)var3.next();
         result.addAll(getFields(t, predicates));
      }

      return result;
   }

   public static Set<Field> getFields(Class<?> type, Predicate<? super Field>... predicates) {
      return filter((Object[])type.getDeclaredFields(), predicates);
   }

   public static <T extends AnnotatedElement> Set<Annotation> getAllAnnotations(T type, Predicate<Annotation>... predicates) {
      Set<Annotation> result = Sets.newHashSet();
      if (type instanceof Class) {
         Iterator var3 = getAllSuperTypes((Class)type).iterator();

         while(var3.hasNext()) {
            Class<?> t = (Class)var3.next();
            result.addAll(getAnnotations(t, predicates));
         }
      } else {
         result.addAll(getAnnotations(type, predicates));
      }

      return result;
   }

   public static <T extends AnnotatedElement> Set<Annotation> getAnnotations(T type, Predicate<Annotation>... predicates) {
      return filter((Object[])type.getDeclaredAnnotations(), predicates);
   }

   public static <T extends AnnotatedElement> Set<T> getAll(Set<T> elements, Predicate<? super T>... predicates) {
      return (Set)(Utils.isEmpty((Object[])predicates) ? elements : Sets.newHashSet(Iterables.filter(elements, Predicates.and(predicates))));
   }

   public static <T extends Member> Predicate<T> withName(final String name) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return input != null && input.getName().equals(name);
         }
      };
   }

   public static <T extends Member> Predicate<T> withPrefix(final String prefix) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return input != null && input.getName().startsWith(prefix);
         }
      };
   }

   public static <T extends AnnotatedElement> Predicate<T> withPattern(final String regex) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return Pattern.matches(regex, input.toString());
         }
      };
   }

   public static <T extends AnnotatedElement> Predicate<T> withAnnotation(final Class<? extends Annotation> annotation) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return input != null && input.isAnnotationPresent(annotation);
         }
      };
   }

   public static <T extends AnnotatedElement> Predicate<T> withAnnotations(final Class<? extends Annotation>... annotations) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return input != null && Arrays.equals(annotations, ReflectionUtils.annotationTypes(input.getAnnotations()));
         }
      };
   }

   public static <T extends AnnotatedElement> Predicate<T> withAnnotation(final Annotation annotation) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return input != null && input.isAnnotationPresent(annotation.annotationType()) && ReflectionUtils.areAnnotationMembersMatching(input.getAnnotation(annotation.annotationType()), annotation);
         }
      };
   }

   public static <T extends AnnotatedElement> Predicate<T> withAnnotations(final Annotation... annotations) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            if (input != null) {
               Annotation[] inputAnnotations = input.getAnnotations();
               if (inputAnnotations.length == annotations.length) {
                  for(int i = 0; i < inputAnnotations.length; ++i) {
                     if (!ReflectionUtils.areAnnotationMembersMatching(inputAnnotations[i], annotations[i])) {
                        return false;
                     }
                  }
               }
            }

            return true;
         }
      };
   }

   public static Predicate<Member> withParameters(final Class<?>... types) {
      return new Predicate<Member>() {
         public boolean apply(@Nullable Member input) {
            return Arrays.equals(ReflectionUtils.parameterTypes(input), types);
         }
      };
   }

   public static Predicate<Member> withParametersAssignableTo(final Class... types) {
      return new Predicate<Member>() {
         public boolean apply(@Nullable Member input) {
            if (input != null) {
               Class<?>[] parameterTypes = ReflectionUtils.parameterTypes(input);
               if (parameterTypes.length == types.length) {
                  for(int i = 0; i < parameterTypes.length; ++i) {
                     if (!parameterTypes[i].isAssignableFrom(types[i]) || parameterTypes[i] == Object.class && types[i] != Object.class) {
                        return false;
                     }
                  }

                  return true;
               }
            }

            return false;
         }
      };
   }

   public static Predicate<Member> withParametersCount(final int count) {
      return new Predicate<Member>() {
         public boolean apply(@Nullable Member input) {
            return input != null && ReflectionUtils.parameterTypes(input).length == count;
         }
      };
   }

   public static Predicate<Member> withAnyParameterAnnotation(final Class<? extends Annotation> annotationClass) {
      return new Predicate<Member>() {
         public boolean apply(@Nullable Member input) {
            return input != null && Iterables.any(ReflectionUtils.annotationTypes((Iterable)ReflectionUtils.parameterAnnotations(input)), new Predicate<Class<? extends Annotation>>() {
               public boolean apply(@Nullable Class<? extends Annotation> input) {
                  return input.equals(annotationClass);
               }
            });
         }
      };
   }

   public static Predicate<Member> withAnyParameterAnnotation(final Annotation annotation) {
      return new Predicate<Member>() {
         public boolean apply(@Nullable Member input) {
            return input != null && Iterables.any(ReflectionUtils.parameterAnnotations(input), new Predicate<Annotation>() {
               public boolean apply(@Nullable Annotation input) {
                  return ReflectionUtils.areAnnotationMembersMatching(annotation, input);
               }
            });
         }
      };
   }

   public static <T> Predicate<Field> withType(final Class<T> type) {
      return new Predicate<Field>() {
         public boolean apply(@Nullable Field input) {
            return input != null && input.getType().equals(type);
         }
      };
   }

   public static <T> Predicate<Field> withTypeAssignableTo(final Class<T> type) {
      return new Predicate<Field>() {
         public boolean apply(@Nullable Field input) {
            return input != null && type.isAssignableFrom(input.getType());
         }
      };
   }

   public static <T> Predicate<Method> withReturnType(final Class<T> type) {
      return new Predicate<Method>() {
         public boolean apply(@Nullable Method input) {
            return input != null && input.getReturnType().equals(type);
         }
      };
   }

   public static <T> Predicate<Method> withReturnTypeAssignableTo(final Class<T> type) {
      return new Predicate<Method>() {
         public boolean apply(@Nullable Method input) {
            return input != null && type.isAssignableFrom(input.getReturnType());
         }
      };
   }

   public static <T extends Member> Predicate<T> withModifier(final int mod) {
      return new Predicate<T>() {
         public boolean apply(@Nullable T input) {
            return input != null && (input.getModifiers() & mod) != 0;
         }
      };
   }

   public static Predicate<Class<?>> withClassModifier(final int mod) {
      return new Predicate<Class<?>>() {
         public boolean apply(@Nullable Class<?> input) {
            return input != null && (input.getModifiers() & mod) != 0;
         }
      };
   }

   public static Class<?> forName(String typeName, ClassLoader... classLoaders) {
      if (getPrimitiveNames().contains(typeName)) {
         return (Class)getPrimitiveTypes().get(getPrimitiveNames().indexOf(typeName));
      } else {
         String type;
         if (typeName.contains("[")) {
            int i = typeName.indexOf("[");
            type = typeName.substring(0, i);
            String array = typeName.substring(i).replace("]", "");
            if (getPrimitiveNames().contains(type)) {
               type = (String)getPrimitiveDescriptors().get(getPrimitiveNames().indexOf(type));
            } else {
               type = "L" + type + ";";
            }

            type = array + type;
         } else {
            type = typeName;
         }

         List<ReflectionsException> reflectionsExceptions = Lists.newArrayList();
         ClassLoader[] var12 = ClasspathHelper.classLoaders(classLoaders);
         int var5 = var12.length;
         int var6 = 0;

         while(var6 < var5) {
            ClassLoader classLoader = var12[var6];
            if (type.contains("[")) {
               try {
                  return Class.forName(type, false, classLoader);
               } catch (Throwable var10) {
                  reflectionsExceptions.add(new ReflectionsException("could not get type for name " + typeName, var10));
               }
            }

            try {
               return classLoader.loadClass(type);
            } catch (Throwable var9) {
               reflectionsExceptions.add(new ReflectionsException("could not get type for name " + typeName, var9));
               ++var6;
            }
         }

         if (Reflections.log != null) {
            Iterator var13 = reflectionsExceptions.iterator();

            while(var13.hasNext()) {
               ReflectionsException reflectionsException = (ReflectionsException)var13.next();
               Reflections.log.warn("could not get type for name " + typeName + " from any class loader", reflectionsException);
            }
         }

         return null;
      }
   }

   public static <T> List<Class<? extends T>> forNames(Iterable<String> classes, ClassLoader... classLoaders) {
      List<Class<? extends T>> result = new ArrayList();
      Iterator var3 = classes.iterator();

      while(var3.hasNext()) {
         String className = (String)var3.next();
         Class<?> type = forName(className, classLoaders);
         if (type != null) {
            result.add(type);
         }
      }

      return result;
   }

   private static Class[] parameterTypes(Member member) {
      return member != null ? (member.getClass() == Method.class ? ((Method)member).getParameterTypes() : (member.getClass() == Constructor.class ? ((Constructor)member).getParameterTypes() : null)) : null;
   }

   private static Set<Annotation> parameterAnnotations(Member member) {
      Set<Annotation> result = Sets.newHashSet();
      Annotation[][] annotations = member instanceof Method ? ((Method)member).getParameterAnnotations() : (member instanceof Constructor ? ((Constructor)member).getParameterAnnotations() : (Annotation[][])null);
      Annotation[][] var3 = annotations;
      int var4 = annotations.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Annotation[] annotation = var3[var5];
         Collections.addAll(result, annotation);
      }

      return result;
   }

   private static Set<Class<? extends Annotation>> annotationTypes(Iterable<Annotation> annotations) {
      Set<Class<? extends Annotation>> result = Sets.newHashSet();
      Iterator var2 = annotations.iterator();

      while(var2.hasNext()) {
         Annotation annotation = (Annotation)var2.next();
         result.add(annotation.annotationType());
      }

      return result;
   }

   private static Class<? extends Annotation>[] annotationTypes(Annotation[] annotations) {
      Class<? extends Annotation>[] result = new Class[annotations.length];

      for(int i = 0; i < annotations.length; ++i) {
         result[i] = annotations[i].annotationType();
      }

      return result;
   }

   private static void initPrimitives() {
      if (primitiveNames == null) {
         primitiveNames = Lists.newArrayList(new String[]{"boolean", "char", "byte", "short", "int", "long", "float", "double", "void"});
         primitiveTypes = Lists.newArrayList(new Class[]{Boolean.TYPE, Character.TYPE, Byte.TYPE, Short.TYPE, Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE, Void.TYPE});
         primitiveDescriptors = Lists.newArrayList(new String[]{"Z", "C", "B", "S", "I", "J", "F", "D", "V"});
      }

   }

   private static List<String> getPrimitiveNames() {
      initPrimitives();
      return primitiveNames;
   }

   private static List<Class> getPrimitiveTypes() {
      initPrimitives();
      return primitiveTypes;
   }

   private static List<String> getPrimitiveDescriptors() {
      initPrimitives();
      return primitiveDescriptors;
   }

   static <T> Set<T> filter(T[] elements, Predicate<? super T>... predicates) {
      return Utils.isEmpty((Object[])predicates) ? Sets.newHashSet(elements) : Sets.newHashSet(Iterables.filter(Arrays.asList(elements), Predicates.and(predicates)));
   }

   static <T> Set<T> filter(Iterable<T> elements, Predicate<? super T>... predicates) {
      return Utils.isEmpty((Object[])predicates) ? Sets.newHashSet(elements) : Sets.newHashSet(Iterables.filter(elements, Predicates.and(predicates)));
   }

   private static boolean areAnnotationMembersMatching(Annotation annotation1, Annotation annotation2) {
      if (annotation2 != null && annotation1.annotationType() == annotation2.annotationType()) {
         Method[] var2 = annotation1.annotationType().getDeclaredMethods();
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            Method method = var2[var4];

            try {
               if (!method.invoke(annotation1).equals(method.invoke(annotation2))) {
                  return false;
               }
            } catch (Exception var7) {
               throw new ReflectionsException(String.format("could not invoke method %s on annotation %s", method.getName(), annotation1.annotationType()), var7);
            }
         }

         return true;
      } else {
         return false;
      }
   }
}
