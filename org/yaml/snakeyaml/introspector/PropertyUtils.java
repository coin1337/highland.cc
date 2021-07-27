package org.yaml.snakeyaml.introspector;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.util.PlatformFeatureDetector;

public class PropertyUtils {
   private static final Logger log = Logger.getLogger(PropertyUtils.class.getPackage().getName());
   private final Map<Class<?>, Map<String, Property>> propertiesCache;
   private final Map<Class<?>, Set<Property>> readableProperties;
   private BeanAccess beanAccess;
   private boolean allowReadOnlyProperties;
   private boolean skipMissingProperties;
   private PlatformFeatureDetector platformFeatureDetector;
   private boolean transientMethodChecked;
   private Method isTransientMethod;

   public PropertyUtils() {
      this(new PlatformFeatureDetector());
   }

   PropertyUtils(PlatformFeatureDetector platformFeatureDetector) {
      this.propertiesCache = new HashMap();
      this.readableProperties = new HashMap();
      this.beanAccess = BeanAccess.DEFAULT;
      this.allowReadOnlyProperties = false;
      this.skipMissingProperties = false;
      this.platformFeatureDetector = platformFeatureDetector;
      if (platformFeatureDetector.isRunningOnAndroid()) {
         this.beanAccess = BeanAccess.FIELD;
      }

   }

   protected Map<String, Property> getPropertiesMap(Class<?> type, BeanAccess bAccess) {
      if (this.propertiesCache.containsKey(type)) {
         return (Map)this.propertiesCache.get(type);
      } else {
         LinkedHashMap properties;
         boolean inaccessableFieldsExist;
         properties = new LinkedHashMap();
         inaccessableFieldsExist = false;
         Class c;
         Field[] var6;
         int var7;
         int var8;
         Field field;
         int modifiers;
         label91:
         switch(bAccess) {
         case FIELD:
            c = type;

            while(true) {
               if (c == null) {
                  break label91;
               }

               var6 = c.getDeclaredFields();
               var7 = var6.length;

               for(var8 = 0; var8 < var7; ++var8) {
                  field = var6[var8];
                  modifiers = field.getModifiers();
                  if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers) && !properties.containsKey(field.getName())) {
                     properties.put(field.getName(), new FieldProperty(field));
                  }
               }

               c = c.getSuperclass();
            }
         default:
            try {
               PropertyDescriptor[] var12 = Introspector.getBeanInfo(type).getPropertyDescriptors();
               int var13 = var12.length;

               for(var7 = 0; var7 < var13; ++var7) {
                  PropertyDescriptor property = var12[var7];
                  Method readMethod = property.getReadMethod();
                  if ((readMethod == null || !readMethod.getName().equals("getClass")) && !this.isTransient(property)) {
                     properties.put(property.getName(), new MethodProperty(property));
                  }
               }
            } catch (IntrospectionException var11) {
               throw new YAMLException(var11);
            }

            for(c = type; c != null; c = c.getSuperclass()) {
               var6 = c.getDeclaredFields();
               var7 = var6.length;

               for(var8 = 0; var8 < var7; ++var8) {
                  field = var6[var8];
                  modifiers = field.getModifiers();
                  if (!Modifier.isStatic(modifiers) && !Modifier.isTransient(modifiers)) {
                     if (Modifier.isPublic(modifiers)) {
                        properties.put(field.getName(), new FieldProperty(field));
                     } else {
                        inaccessableFieldsExist = true;
                     }
                  }
               }
            }
         }

         if (properties.isEmpty() && inaccessableFieldsExist) {
            throw new YAMLException("No JavaBean properties found in " + type.getName());
         } else {
            this.propertiesCache.put(type, properties);
            return properties;
         }
      }
   }

   private boolean isTransient(FeatureDescriptor fd) {
      if (!this.transientMethodChecked) {
         this.transientMethodChecked = true;

         try {
            this.isTransientMethod = FeatureDescriptor.class.getDeclaredMethod("isTransient");
            this.isTransientMethod.setAccessible(true);
         } catch (NoSuchMethodException var6) {
            log.fine("NoSuchMethod: FeatureDescriptor.isTransient(). Don't check it anymore.");
         } catch (SecurityException var7) {
            var7.printStackTrace();
            this.isTransientMethod = null;
         }
      }

      if (this.isTransientMethod != null) {
         try {
            return Boolean.TRUE.equals(this.isTransientMethod.invoke(fd));
         } catch (IllegalAccessException var3) {
            var3.printStackTrace();
         } catch (IllegalArgumentException var4) {
            var4.printStackTrace();
         } catch (InvocationTargetException var5) {
            var5.printStackTrace();
         }

         this.isTransientMethod = null;
      }

      return false;
   }

   public Set<Property> getProperties(Class<? extends Object> type) {
      return this.getProperties(type, this.beanAccess);
   }

   public Set<Property> getProperties(Class<? extends Object> type, BeanAccess bAccess) {
      if (this.readableProperties.containsKey(type)) {
         return (Set)this.readableProperties.get(type);
      } else {
         Set<Property> properties = this.createPropertySet(type, bAccess);
         this.readableProperties.put(type, properties);
         return properties;
      }
   }

   protected Set<Property> createPropertySet(Class<? extends Object> type, BeanAccess bAccess) {
      Set<Property> properties = new TreeSet();
      Collection<Property> props = this.getPropertiesMap(type, bAccess).values();
      Iterator var5 = props.iterator();

      while(true) {
         Property property;
         do {
            do {
               if (!var5.hasNext()) {
                  return properties;
               }

               property = (Property)var5.next();
            } while(!property.isReadable());
         } while(!this.allowReadOnlyProperties && !property.isWritable());

         properties.add(property);
      }
   }

   public Property getProperty(Class<? extends Object> type, String name) {
      return this.getProperty(type, name, this.beanAccess);
   }

   public Property getProperty(Class<? extends Object> type, String name, BeanAccess bAccess) {
      Map<String, Property> properties = this.getPropertiesMap(type, bAccess);
      Property property = (Property)properties.get(name);
      if (property == null && this.skipMissingProperties) {
         property = new MissingProperty(name);
      }

      if (property == null) {
         throw new YAMLException("Unable to find property '" + name + "' on class: " + type.getName());
      } else {
         return (Property)property;
      }
   }

   public void setBeanAccess(BeanAccess beanAccess) {
      if (this.platformFeatureDetector.isRunningOnAndroid() && beanAccess != BeanAccess.FIELD) {
         throw new IllegalArgumentException("JVM is Android - only BeanAccess.FIELD is available");
      } else {
         if (this.beanAccess != beanAccess) {
            this.beanAccess = beanAccess;
            this.propertiesCache.clear();
            this.readableProperties.clear();
         }

      }
   }

   public void setAllowReadOnlyProperties(boolean allowReadOnlyProperties) {
      if (this.allowReadOnlyProperties != allowReadOnlyProperties) {
         this.allowReadOnlyProperties = allowReadOnlyProperties;
         this.readableProperties.clear();
      }

   }

   public boolean isAllowReadOnlyProperties() {
      return this.allowReadOnlyProperties;
   }

   public void setSkipMissingProperties(boolean skipMissingProperties) {
      if (this.skipMissingProperties != skipMissingProperties) {
         this.skipMissingProperties = skipMissingProperties;
         this.readableProperties.clear();
      }

   }

   public boolean isSkipMissingProperties() {
      return this.skipMissingProperties;
   }
}
