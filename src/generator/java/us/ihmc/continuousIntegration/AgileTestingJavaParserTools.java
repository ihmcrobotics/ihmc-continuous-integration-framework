package us.ihmc.continuousIntegration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import us.ihmc.continuousIntegration.model.AgileTestingClassPath;

public class AgileTestingJavaParserTools
{
   public static Pair<CompilationUnit, ClassOrInterfaceDeclaration> parseForTestAnnotations(final AgileTestingClassPath classPath, final Map<String, MutablePair<MethodDeclaration, HashMap<String, AnnotationExpr>>> methodAnnotationMap)
   {
      try
      {
         CompilationUnit compilationUnit = JavaParser.parse(classPath.getPath().toFile());
         final Pair<CompilationUnit, ClassOrInterfaceDeclaration> pair = MutablePair.of(compilationUnit, null);
         
         VoidVisitorAdapter<Object> visitor = new VoidVisitorAdapter<Object>()
         {
            @Override
            public void visit(MethodDeclaration methodDeclaration, Object arg)
            {
               super.visit(methodDeclaration, arg);
               
               boolean isATestMethod = false;
               for(AnnotationExpr expr : methodDeclaration.getAnnotations())
               {
                  if (expr.getNameAsString().equals(Test.class.getSimpleName()) || expr.getNameAsString().equals(Test.class.getName()))
                  {
                     isATestMethod = true;
                     break;
                  }
               }
               
               if (!isATestMethod)
               {
                  return;
               }
               
               methodAnnotationMap.put(methodDeclaration.getNameAsString(), MutablePair.of(methodDeclaration, new HashMap<String, AnnotationExpr>()));
               
               for(AnnotationExpr expr : methodDeclaration.getAnnotations())
               {
                  String[] split = expr.getNameAsString().split("\\.");
                  methodAnnotationMap.get(methodDeclaration.getName()).getRight().put(split[split.length - 1], expr);
               }
            }
            
            @Override
            public void visit(ClassOrInterfaceDeclaration n, Object arg)
            {
               super.visit(n, arg);
               if (n.getName().equals(classPath.getSimpleName()))
               {
                  pair.setValue(n);
               }
            }
         };
         
         visitor.visit(compilationUnit, null);
         
         return pair;
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return null;
      }
   }
   
   public static Map<String, MemberValuePair> mapAnnotationFields(AnnotationExpr annotationExpr)
   {
      Map<String, MemberValuePair> nameToFieldMap = new HashMap<>();
      
      for (Node node : annotationExpr.getChildNodes())
      {
         if (node instanceof MemberValuePair)
         {
            MemberValuePair memberValuePair = (MemberValuePair) node;
            nameToFieldMap.put(memberValuePair.getNameAsString(), memberValuePair);
         }
      }
      
      return nameToFieldMap;
   }
   
   public static boolean classOrInterfaceExtends(ClassOrInterfaceDeclaration classOrInterfaceDeclaration)
   {
      return !(classOrInterfaceDeclaration.getExtendedTypes() == null) && !classOrInterfaceDeclaration.getExtendedTypes().isEmpty();
   }
}
