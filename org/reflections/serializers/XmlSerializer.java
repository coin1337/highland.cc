package org.reflections.serializers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.Store;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.Utils;

public class XmlSerializer implements Serializer {
   public Reflections read(InputStream inputStream) {
      Reflections reflections;
      try {
         Constructor<Reflections> constructor = Reflections.class.getDeclaredConstructor();
         constructor.setAccessible(true);
         reflections = (Reflections)constructor.newInstance();
      } catch (Exception var15) {
         reflections = new Reflections(new ConfigurationBuilder());
      }

      try {
         Document document = (new SAXReader()).read(inputStream);
         Iterator var4 = document.getRootElement().elements().iterator();

         while(var4.hasNext()) {
            Object e1 = var4.next();
            Element index = (Element)e1;
            Iterator var7 = index.elements().iterator();

            while(var7.hasNext()) {
               Object e2 = var7.next();
               Element entry = (Element)e2;
               Element key = entry.element("key");
               Element values = entry.element("values");
               Iterator var12 = values.elements().iterator();

               while(var12.hasNext()) {
                  Object o3 = var12.next();
                  Element value = (Element)o3;
                  reflections.getStore().getOrCreate(index.getName()).put(key.getText(), value.getText());
               }
            }
         }

         return reflections;
      } catch (DocumentException var16) {
         throw new ReflectionsException("could not read.", var16);
      } catch (Throwable var17) {
         throw new RuntimeException("Could not read. Make sure relevant dependencies exist on classpath.", var17);
      }
   }

   public File save(Reflections reflections, String filename) {
      File file = Utils.prepareFile(filename);

      try {
         Document document = this.createDocument(reflections);
         XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(file), OutputFormat.createPrettyPrint());
         xmlWriter.write(document);
         xmlWriter.close();
         return file;
      } catch (IOException var6) {
         throw new ReflectionsException("could not save to file " + filename, var6);
      } catch (Throwable var7) {
         throw new RuntimeException("Could not save to file " + filename + ". Make sure relevant dependencies exist on classpath.", var7);
      }
   }

   public String toString(Reflections reflections) {
      Document document = this.createDocument(reflections);

      try {
         StringWriter writer = new StringWriter();
         XMLWriter xmlWriter = new XMLWriter(writer, OutputFormat.createPrettyPrint());
         xmlWriter.write(document);
         xmlWriter.close();
         return writer.toString();
      } catch (IOException var5) {
         throw new RuntimeException();
      }
   }

   private Document createDocument(Reflections reflections) {
      Store map = reflections.getStore();
      Document document = DocumentFactory.getInstance().createDocument();
      Element root = document.addElement("Reflections");
      Iterator var5 = map.keySet().iterator();

      while(var5.hasNext()) {
         String indexName = (String)var5.next();
         Element indexElement = root.addElement(indexName);
         Iterator var8 = map.get(indexName).keySet().iterator();

         while(var8.hasNext()) {
            String key = (String)var8.next();
            Element entryElement = indexElement.addElement("entry");
            entryElement.addElement("key").setText(key);
            Element valuesElement = entryElement.addElement("values");
            Iterator var12 = map.get(indexName).get(key).iterator();

            while(var12.hasNext()) {
               String value = (String)var12.next();
               valuesElement.addElement("value").setText(value);
            }
         }
      }

      return document;
   }
}
