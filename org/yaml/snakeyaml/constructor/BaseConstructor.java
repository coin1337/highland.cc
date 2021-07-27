package org.yaml.snakeyaml.constructor;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.composer.Composer;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.nodes.CollectionNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;

public abstract class BaseConstructor {
   protected final Map<NodeId, Construct> yamlClassConstructors = new EnumMap(NodeId.class);
   protected final Map<Tag, Construct> yamlConstructors = new HashMap();
   protected final Map<String, Construct> yamlMultiConstructors = new HashMap();
   protected Composer composer;
   final Map<Node, Object> constructedObjects = new HashMap();
   private final Set<Node> recursiveObjects = new HashSet();
   private final ArrayList<BaseConstructor.RecursiveTuple<Map<Object, Object>, BaseConstructor.RecursiveTuple<Object, Object>>> maps2fill = new ArrayList();
   private final ArrayList<BaseConstructor.RecursiveTuple<Set<Object>, Object>> sets2fill = new ArrayList();
   protected Tag rootTag = null;
   private PropertyUtils propertyUtils;
   private boolean explicitPropertyUtils = false;
   private boolean allowDuplicateKeys = true;
   protected final Map<Class<? extends Object>, TypeDescription> typeDefinitions = new HashMap();
   protected final Map<Tag, Class<? extends Object>> typeTags = new HashMap();

   public BaseConstructor() {
      this.typeDefinitions.put(SortedMap.class, new TypeDescription(SortedMap.class, Tag.OMAP, TreeMap.class));
      this.typeDefinitions.put(SortedSet.class, new TypeDescription(SortedSet.class, Tag.SET, TreeSet.class));
   }

   public void setComposer(Composer composer) {
      this.composer = composer;
   }

   public boolean checkData() {
      return this.composer.checkNode();
   }

   public Object getData() {
      this.composer.checkNode();
      Node node = this.composer.getNode();
      if (this.rootTag != null) {
         node.setTag(this.rootTag);
      }

      return this.constructDocument(node);
   }

   public Object getSingleData(Class<?> type) {
      Node node = this.composer.getSingleNode();
      if (node != null && !Tag.NULL.equals(node.getTag())) {
         if (Object.class != type) {
            node.setTag(new Tag(type));
         } else if (this.rootTag != null) {
            node.setTag(this.rootTag);
         }

         return this.constructDocument(node);
      } else {
         return null;
      }
   }

   protected final Object constructDocument(Node node) {
      Object data = this.constructObject(node);
      this.fillRecursive();
      this.constructedObjects.clear();
      this.recursiveObjects.clear();
      return data;
   }

   private void fillRecursive() {
      Iterator var1;
      BaseConstructor.RecursiveTuple value;
      if (!this.maps2fill.isEmpty()) {
         var1 = this.maps2fill.iterator();

         while(var1.hasNext()) {
            value = (BaseConstructor.RecursiveTuple)var1.next();
            BaseConstructor.RecursiveTuple<Object, Object> key_value = (BaseConstructor.RecursiveTuple)value._2();
            ((Map)value._1()).put(key_value._1(), key_value._2());
         }

         this.maps2fill.clear();
      }

      if (!this.sets2fill.isEmpty()) {
         var1 = this.sets2fill.iterator();

         while(var1.hasNext()) {
            value = (BaseConstructor.RecursiveTuple)var1.next();
            ((Set)value._1()).add(value._2());
         }

         this.sets2fill.clear();
      }

   }

   protected Object constructObject(Node node) {
      return this.constructedObjects.containsKey(node) ? this.constructedObjects.get(node) : this.constructObjectNoCheck(node);
   }

   protected Object constructObjectNoCheck(Node node) {
      if (this.recursiveObjects.contains(node)) {
         throw new ConstructorException((String)null, (Mark)null, "found unconstructable recursive node", node.getStartMark());
      } else {
         this.recursiveObjects.add(node);
         Construct constructor = this.getConstructor(node);
         Object data = this.constructedObjects.containsKey(node) ? this.constructedObjects.get(node) : constructor.construct(node);
         this.finalizeConstruction(node, data);
         this.constructedObjects.put(node, data);
         this.recursiveObjects.remove(node);
         if (node.isTwoStepsConstruction()) {
            constructor.construct2ndStep(node, data);
         }

         return data;
      }
   }

   protected Construct getConstructor(Node node) {
      if (node.useClassConstructor()) {
         return (Construct)this.yamlClassConstructors.get(node.getNodeId());
      } else {
         Construct constructor = (Construct)this.yamlConstructors.get(node.getTag());
         if (constructor == null) {
            Iterator var3 = this.yamlMultiConstructors.keySet().iterator();

            String prefix;
            do {
               if (!var3.hasNext()) {
                  return (Construct)this.yamlConstructors.get((Object)null);
               }

               prefix = (String)var3.next();
            } while(!node.getTag().startsWith(prefix));

            return (Construct)this.yamlMultiConstructors.get(prefix);
         } else {
            return constructor;
         }
      }
   }

   protected Object constructScalar(ScalarNode node) {
      return node.getValue();
   }

   protected List<Object> createDefaultList(int initSize) {
      return new ArrayList(initSize);
   }

   protected Set<Object> createDefaultSet(int initSize) {
      return new LinkedHashSet(initSize);
   }

   protected Map<Object, Object> createDefaultMap() {
      return new LinkedHashMap();
   }

   protected Set<Object> createDefaultSet() {
      return new LinkedHashSet();
   }

   protected Object createArray(Class<?> type, int size) {
      return Array.newInstance(type.getComponentType(), size);
   }

   protected Object finalizeConstruction(Node node, Object data) {
      Class<? extends Object> type = node.getType();
      return this.typeDefinitions.containsKey(type) ? ((TypeDescription)this.typeDefinitions.get(type)).finalizeConstruction(data) : data;
   }

   protected Object newInstance(Node node) {
      try {
         return this.newInstance(Object.class, node);
      } catch (InstantiationException var3) {
         throw new YAMLException(var3);
      }
   }

   protected final Object newInstance(Class<?> ancestor, Node node) throws InstantiationException {
      return this.newInstance(ancestor, node, true);
   }

   protected Object newInstance(Class<?> ancestor, Node node, boolean tryDefault) throws InstantiationException {
      Class<? extends Object> type = node.getType();
      if (this.typeDefinitions.containsKey(type)) {
         TypeDescription td = (TypeDescription)this.typeDefinitions.get(type);
         Object instance = td.newInstance(node);
         if (instance != null) {
            return instance;
         }
      }

      if (tryDefault && ancestor.isAssignableFrom(type) && !Modifier.isAbstract(type.getModifiers())) {
         try {
            java.lang.reflect.Constructor<?> c = type.getDeclaredConstructor();
            c.setAccessible(true);
            return c.newInstance();
         } catch (NoSuchMethodException var7) {
            throw new InstantiationException("NoSuchMethodException:" + var7.getLocalizedMessage());
         } catch (Exception var8) {
            throw new YAMLException(var8);
         }
      } else {
         throw new InstantiationException();
      }
   }

   protected Set<Object> newSet(CollectionNode<?> node) {
      try {
         return (Set)this.newInstance(Set.class, node);
      } catch (InstantiationException var3) {
         return this.createDefaultSet(node.getValue().size());
      }
   }

   protected List<Object> newList(SequenceNode node) {
      try {
         return (List)this.newInstance(List.class, node);
      } catch (InstantiationException var3) {
         return this.createDefaultList(node.getValue().size());
      }
   }

   protected Map<Object, Object> newMap(MappingNode node) {
      try {
         return (Map)this.newInstance(Map.class, node);
      } catch (InstantiationException var3) {
         return this.createDefaultMap();
      }
   }

   protected List<? extends Object> constructSequence(SequenceNode node) {
      List<Object> result = this.newList(node);
      this.constructSequenceStep2(node, result);
      return result;
   }

   protected Set<? extends Object> constructSet(SequenceNode node) {
      Set<Object> result = this.newSet(node);
      this.constructSequenceStep2(node, result);
      return result;
   }

   protected Object constructArray(SequenceNode node) {
      return this.constructArrayStep2(node, this.createArray(node.getType(), node.getValue().size()));
   }

   protected void constructSequenceStep2(SequenceNode node, Collection<Object> collection) {
      Iterator var3 = node.getValue().iterator();

      while(var3.hasNext()) {
         Node child = (Node)var3.next();
         collection.add(this.constructObject(child));
      }

   }

   protected Object constructArrayStep2(SequenceNode node, Object array) {
      Class<?> componentType = node.getType().getComponentType();
      int index = 0;

      for(Iterator var5 = node.getValue().iterator(); var5.hasNext(); ++index) {
         Node child = (Node)var5.next();
         if (child.getType() == Object.class) {
            child.setType(componentType);
         }

         Object value = this.constructObject(child);
         if (componentType.isPrimitive()) {
            if (value == null) {
               throw new NullPointerException("Unable to construct element value for " + child);
            }

            if (Byte.TYPE.equals(componentType)) {
               Array.setByte(array, index, ((Number)value).byteValue());
            } else if (Short.TYPE.equals(componentType)) {
               Array.setShort(array, index, ((Number)value).shortValue());
            } else if (Integer.TYPE.equals(componentType)) {
               Array.setInt(array, index, ((Number)value).intValue());
            } else if (Long.TYPE.equals(componentType)) {
               Array.setLong(array, index, ((Number)value).longValue());
            } else if (Float.TYPE.equals(componentType)) {
               Array.setFloat(array, index, ((Number)value).floatValue());
            } else if (Double.TYPE.equals(componentType)) {
               Array.setDouble(array, index, ((Number)value).doubleValue());
            } else if (Character.TYPE.equals(componentType)) {
               Array.setChar(array, index, (Character)value);
            } else {
               if (!Boolean.TYPE.equals(componentType)) {
                  throw new YAMLException("unexpected primitive type");
               }

               Array.setBoolean(array, index, (Boolean)value);
            }
         } else {
            Array.set(array, index, value);
         }
      }

      return array;
   }

   protected Set<Object> constructSet(MappingNode node) {
      Set<Object> set = this.newSet(node);
      this.constructSet2ndStep(node, set);
      return set;
   }

   protected Map<Object, Object> constructMapping(MappingNode node) {
      Map<Object, Object> mapping = this.newMap(node);
      this.constructMapping2ndStep(node, mapping);
      return mapping;
   }

   protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
      List<NodeTuple> nodeValue = node.getValue();
      Iterator var4 = nodeValue.iterator();

      while(var4.hasNext()) {
         NodeTuple tuple = (NodeTuple)var4.next();
         Node keyNode = tuple.getKeyNode();
         Node valueNode = tuple.getValueNode();
         Object key = this.constructObject(keyNode);
         if (key != null) {
            try {
               key.hashCode();
            } catch (Exception var10) {
               throw new ConstructorException("while constructing a mapping", node.getStartMark(), "found unacceptable key " + key, tuple.getKeyNode().getStartMark(), var10);
            }
         }

         Object value = this.constructObject(valueNode);
         if (keyNode.isTwoStepsConstruction()) {
            this.maps2fill.add(0, new BaseConstructor.RecursiveTuple(mapping, new BaseConstructor.RecursiveTuple(key, value)));
         } else {
            mapping.put(key, value);
         }
      }

   }

   protected void constructSet2ndStep(MappingNode node, Set<Object> set) {
      List<NodeTuple> nodeValue = node.getValue();
      Iterator var4 = nodeValue.iterator();

      while(var4.hasNext()) {
         NodeTuple tuple = (NodeTuple)var4.next();
         Node keyNode = tuple.getKeyNode();
         Object key = this.constructObject(keyNode);
         if (key != null) {
            try {
               key.hashCode();
            } catch (Exception var9) {
               throw new ConstructorException("while constructing a Set", node.getStartMark(), "found unacceptable key " + key, tuple.getKeyNode().getStartMark(), var9);
            }
         }

         if (keyNode.isTwoStepsConstruction()) {
            this.sets2fill.add(0, new BaseConstructor.RecursiveTuple(set, key));
         } else {
            set.add(key);
         }
      }

   }

   public void setPropertyUtils(PropertyUtils propertyUtils) {
      this.propertyUtils = propertyUtils;
      this.explicitPropertyUtils = true;
      Collection<TypeDescription> tds = this.typeDefinitions.values();
      Iterator var3 = tds.iterator();

      while(var3.hasNext()) {
         TypeDescription typeDescription = (TypeDescription)var3.next();
         typeDescription.setPropertyUtils(propertyUtils);
      }

   }

   public final PropertyUtils getPropertyUtils() {
      if (this.propertyUtils == null) {
         this.propertyUtils = new PropertyUtils();
      }

      return this.propertyUtils;
   }

   public TypeDescription addTypeDescription(TypeDescription definition) {
      if (definition == null) {
         throw new NullPointerException("TypeDescription is required.");
      } else {
         Tag tag = definition.getTag();
         this.typeTags.put(tag, definition.getType());
         definition.setPropertyUtils(this.getPropertyUtils());
         return (TypeDescription)this.typeDefinitions.put(definition.getType(), definition);
      }
   }

   public final boolean isExplicitPropertyUtils() {
      return this.explicitPropertyUtils;
   }

   public boolean isAllowDuplicateKeys() {
      return this.allowDuplicateKeys;
   }

   public void setAllowDuplicateKeys(boolean allowDuplicateKeys) {
      this.allowDuplicateKeys = allowDuplicateKeys;
   }

   private static class RecursiveTuple<T, K> {
      private final T _1;
      private final K _2;

      public RecursiveTuple(T _1, K _2) {
         this._1 = _1;
         this._2 = _2;
      }

      public K _2() {
         return this._2;
      }

      public T _1() {
         return this._1;
      }
   }
}
