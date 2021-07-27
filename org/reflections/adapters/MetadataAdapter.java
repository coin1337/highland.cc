package org.reflections.adapters;

import java.util.List;
import org.reflections.vfs.Vfs;

public interface MetadataAdapter<C, F, M> {
   String getClassName(C var1);

   String getSuperclassName(C var1);

   List<String> getInterfacesNames(C var1);

   List<F> getFields(C var1);

   List<M> getMethods(C var1);

   String getMethodName(M var1);

   List<String> getParameterNames(M var1);

   List<String> getClassAnnotationNames(C var1);

   List<String> getFieldAnnotationNames(F var1);

   List<String> getMethodAnnotationNames(M var1);

   List<String> getParameterAnnotationNames(M var1, int var2);

   String getReturnTypeName(M var1);

   String getFieldName(F var1);

   C getOfCreateClassObject(Vfs.File var1) throws Exception;

   String getMethodModifier(M var1);

   String getMethodKey(C var1, M var2);

   String getMethodFullKey(C var1, M var2);

   boolean isPublic(Object var1);

   boolean acceptsInput(String var1);
}
