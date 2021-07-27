package org.reflections;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Store {
   private transient boolean concurrent;
   private final Map<String, Multimap<String, String>> storeMap = new HashMap();

   protected Store() {
      this.concurrent = false;
   }

   public Store(Configuration configuration) {
      this.concurrent = configuration.getExecutorService() != null;
   }

   public Set<String> keySet() {
      return this.storeMap.keySet();
   }

   public Multimap<String, String> getOrCreate(String index) {
      Multimap<String, String> mmap = (Multimap)this.storeMap.get(index);
      if (mmap == null) {
         SetMultimap<String, String> multimap = Multimaps.newSetMultimap(new HashMap(), new Supplier<Set<String>>() {
            public Set<String> get() {
               return Sets.newSetFromMap(new ConcurrentHashMap());
            }
         });
         mmap = this.concurrent ? Multimaps.synchronizedSetMultimap(multimap) : multimap;
         this.storeMap.put(index, mmap);
      }

      return (Multimap)mmap;
   }

   public Multimap<String, String> get(String index) {
      Multimap<String, String> mmap = (Multimap)this.storeMap.get(index);
      if (mmap == null) {
         throw new ReflectionsException("Scanner " + index + " was not configured");
      } else {
         return mmap;
      }
   }

   public Iterable<String> get(String index, String... keys) {
      return this.get(index, (Iterable)Arrays.asList(keys));
   }

   public Iterable<String> get(String index, Iterable<String> keys) {
      Multimap<String, String> mmap = this.get(index);
      Store.IterableChain<String> result = new Store.IterableChain();
      Iterator var5 = keys.iterator();

      while(var5.hasNext()) {
         String key = (String)var5.next();
         result.addAll(mmap.get(key));
      }

      return result;
   }

   private Iterable<String> getAllIncluding(String index, Iterable<String> keys, Store.IterableChain<String> result) {
      result.addAll(keys);
      Iterator var4 = keys.iterator();

      while(var4.hasNext()) {
         String key = (String)var4.next();
         Iterable<String> values = this.get(index, key);
         if (values.iterator().hasNext()) {
            this.getAllIncluding(index, values, result);
         }
      }

      return result;
   }

   public Iterable<String> getAll(String index, String key) {
      return this.getAllIncluding(index, this.get(index, key), new Store.IterableChain());
   }

   public Iterable<String> getAll(String index, Iterable<String> keys) {
      return this.getAllIncluding(index, this.get(index, keys), new Store.IterableChain());
   }

   private static class IterableChain<T> implements Iterable<T> {
      private final List<Iterable<T>> chain;

      private IterableChain() {
         this.chain = Lists.newArrayList();
      }

      private void addAll(Iterable<T> iterable) {
         this.chain.add(iterable);
      }

      public Iterator<T> iterator() {
         return Iterables.concat(this.chain).iterator();
      }

      // $FF: synthetic method
      IterableChain(Object x0) {
         this();
      }
   }
}
