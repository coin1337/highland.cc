package org.reflections.scanners;

import com.google.common.base.Joiner;
import javassist.CannotCompileException;
import javassist.ClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.MethodInfo;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;
import org.reflections.ReflectionsException;
import org.reflections.util.ClasspathHelper;

public class MemberUsageScanner extends AbstractScanner {
   private ClassPool classPool;

   public void scan(Object cls) {
      try {
         CtClass ctClass = this.getClassPool().get(this.getMetadataAdapter().getClassName(cls));
         CtConstructor[] var3 = ctClass.getDeclaredConstructors();
         int var4 = var3.length;

         int var5;
         for(var5 = 0; var5 < var4; ++var5) {
            CtBehavior member = var3[var5];
            this.scanMember(member);
         }

         CtMethod[] var8 = ctClass.getDeclaredMethods();
         var4 = var8.length;

         for(var5 = 0; var5 < var4; ++var5) {
            CtBehavior member = var8[var5];
            this.scanMember(member);
         }

         ctClass.detach();
      } catch (Exception var7) {
         throw new ReflectionsException("Could not scan method usage for " + this.getMetadataAdapter().getClassName(cls), var7);
      }
   }

   void scanMember(CtBehavior member) throws CannotCompileException {
      final String key = member.getDeclaringClass().getName() + "." + member.getMethodInfo().getName() + "(" + this.parameterNames(member.getMethodInfo()) + ")";
      member.instrument(new ExprEditor() {
         public void edit(NewExpr e) throws CannotCompileException {
            try {
               MemberUsageScanner.this.put(e.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.this.parameterNames(e.getConstructor().getMethodInfo()) + ")", e.getLineNumber(), key);
            } catch (NotFoundException var3) {
               throw new ReflectionsException("Could not find new instance usage in " + key, var3);
            }
         }

         public void edit(MethodCall m) throws CannotCompileException {
            try {
               MemberUsageScanner.this.put(m.getMethod().getDeclaringClass().getName() + "." + m.getMethodName() + "(" + MemberUsageScanner.this.parameterNames(m.getMethod().getMethodInfo()) + ")", m.getLineNumber(), key);
            } catch (NotFoundException var3) {
               throw new ReflectionsException("Could not find member " + m.getClassName() + " in " + key, var3);
            }
         }

         public void edit(ConstructorCall c) throws CannotCompileException {
            try {
               MemberUsageScanner.this.put(c.getConstructor().getDeclaringClass().getName() + ".<init>(" + MemberUsageScanner.this.parameterNames(c.getConstructor().getMethodInfo()) + ")", c.getLineNumber(), key);
            } catch (NotFoundException var3) {
               throw new ReflectionsException("Could not find member " + c.getClassName() + " in " + key, var3);
            }
         }

         public void edit(FieldAccess f) throws CannotCompileException {
            try {
               MemberUsageScanner.this.put(f.getField().getDeclaringClass().getName() + "." + f.getFieldName(), f.getLineNumber(), key);
            } catch (NotFoundException var3) {
               throw new ReflectionsException("Could not find member " + f.getFieldName() + " in " + key, var3);
            }
         }
      });
   }

   private void put(String key, int lineNumber, String value) {
      if (this.acceptResult(key)) {
         this.getStore().put(key, value + " #" + lineNumber);
      }

   }

   String parameterNames(MethodInfo info) {
      return Joiner.on(", ").join(this.getMetadataAdapter().getParameterNames(info));
   }

   private ClassPool getClassPool() {
      if (this.classPool == null) {
         synchronized(this) {
            this.classPool = new ClassPool();
            ClassLoader[] classLoaders = this.getConfiguration().getClassLoaders();
            if (classLoaders == null) {
               classLoaders = ClasspathHelper.classLoaders();
            }

            ClassLoader[] var3 = classLoaders;
            int var4 = classLoaders.length;

            for(int var5 = 0; var5 < var4; ++var5) {
               ClassLoader classLoader = var3[var5];
               this.classPool.appendClassPath((ClassPath)(new LoaderClassPath(classLoader)));
            }
         }
      }

      return this.classPool;
   }
}
