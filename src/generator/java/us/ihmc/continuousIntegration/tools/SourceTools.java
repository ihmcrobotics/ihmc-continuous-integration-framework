package us.ihmc.continuousIntegration.tools;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import us.ihmc.log.LogTools;
import us.ihmc.commons.nio.BasicPathVisitor;
import us.ihmc.commons.nio.PathTools;

public class SourceTools
{
   private static final Path WORKSPACE_PATH = Paths.get("..", "..");
   private static final Path PROJECT_PATH = Paths.get("..");
   public static final String NOT_COMMENTED_REGEX = "^\\s*";
   public static final String ANY_STRING_REGEX = ".*";
   public static final String ALL_JAVA_FILES_REGEX = ".*\\.java$";

   public static final String SOURCE_SETS_DIRECTORY_NAME = "src";
   public static final String JAVA_SOURCE_DIRECTORY_NAME = "java";
   public static final SourceFolder MAIN_SOURCE_FOLDER = new SourceFolder("main");
   public static final SourceFolder TEST_SOURCE_FOLDER = new SourceFolder("test");

   public static Map<String, Path> mapAllClassNamesToPaths(Path workspacePath)
   {
      LogTools.info("Mapping all class names to Paths in " + workspacePath.toAbsolutePath());

      Map<String, Path> javaNameToPathMap = new LinkedHashMap<>();

      for (Path path : PathTools.findAllPathsRecursivelyThatMatchRegex(workspacePath, ALL_JAVA_FILES_REGEX))
      {
         String className = SourceTools.deriveClassNameFromPath(path);

         if (!className.isEmpty())
         {
            javaNameToPathMap.put(className, path);
         }
      }

      LogTools.info("Map size: " + javaNameToPathMap.values().size() + " paths.");

      return javaNameToPathMap;
   }

   public static List<Path> findAllProjectPaths(Path workspacePath)
   {
      final List<Path> paths = new ArrayList<>();

      PathTools.walkDepth(workspacePath, 2, new BasicPathVisitor()
      {
         @Override
         public FileVisitResult visitPath(Path file, PathType pathType)
         {
            if (Files.exists(file.resolve("build.gradle")))
            {
               paths.add(file);
            }

            return FileVisitResult.CONTINUE;
         }
      });

      return paths;
   }

   public static String derivePackageFromPath(Path path)
   {
      String className = "";

      boolean gotAJava = false;
      boolean gotASrc = false;

      for (int i = 0; i < path.getNameCount(); i++)
      {
         String subPathString = path.getName(i).toString();

         if (gotAJava == false || gotASrc == false)
         {
            if (subPathString.equals(SourceTools.SOURCE_SETS_DIRECTORY_NAME))
            {
               gotASrc = true;
            }
            if (subPathString.equals(SourceTools.JAVA_SOURCE_DIRECTORY_NAME))
            {
               gotAJava = true;
            }
         }
         else
         {
            if (isLastNameInPath(i, path))
            {
               if (!FilenameUtils.getExtension(subPathString).isEmpty())
                  subPathString = "";
            }

            className += subPathString;

            if (!isLastNameInPath(i, path) && FilenameUtils.getExtension(path.getName(i + 1).toString()).isEmpty())
            {
               className += ".";
            }
         }
      }

      return className;
   }

   public static String deriveClassNameFromPath(Path path)
   {
      String className = "";

      boolean gotAJava = false;
      boolean gotASrc = false;

      for (int i = 0; i < path.getNameCount(); i++)
      {
         String subPathString = path.getName(i).toString();

         if (gotAJava == false || gotASrc == false)
         {
            if (subPathString.equals(SourceTools.SOURCE_SETS_DIRECTORY_NAME))
            {
               gotASrc = true;
            }
            if (subPathString.equals(SourceTools.JAVA_SOURCE_DIRECTORY_NAME))
            {
               gotAJava = true;
            }
         }
         else
         {
            if (isLastNameInPath(i, path))
            {
               subPathString = FilenameUtils.getBaseName(subPathString);
            }

            className += subPathString;

            if (!isLastNameInPath(i, path))
            {
               className += ".";
            }
         }
      }

      return className;
   }

   public static SourceFolder deriveSourceFolderFromPath(Path path)
   {
      for (int i = 0; i < path.getNameCount(); i++)
      {
         String subPathString = path.getName(i).toString();

         if (subPathString.equals(SOURCE_SETS_DIRECTORY_NAME))
         {
            return new SourceFolder(path.getName(i + 1).toString());
         }
      }

      return null;
   }

   public static String deriveProjectNameFromPath(Path path)
   {
      for (int i = 0; i < path.getNameCount(); i++)
      {
         String subPathString = path.getName(i).toString();

         if (subPathString.equals(SOURCE_SETS_DIRECTORY_NAME))
         {
            return path.getName(i - 1).toString();
         }
      }

      return null;
   }

   public static Path derivePathFromClass(Path sourceFolder, Class<?> clazz)
   {
      Path packagePath = sourceFolder.resolve(derivePathFromPackage(clazz.getPackage()));

      Path classPath = packagePath.resolve(clazz.getSimpleName() + ".java");

      return classPath;
   }

   public static Path derivePathFromPackage(Package parcel)
   {
      String[] names = parcel.getName().split("\\.");

      return Paths.get(names[0], Arrays.copyOfRange(names, 1, names.length));
   }

   private static boolean isLastNameInPath(int i, Path path)
   {
      return i >= (path.getNameCount() - 1);
   }

   public static String extractSuperClassSimpleName(List<String> linesInFile, String testClassSimpleName)
   {
      int classDeclarationLineIndex = -1;

      try
      {
         classDeclarationLineIndex = SourceTools.getLineNumbersThatMatchRegex(linesInFile, ".*" + testClassSimpleName + "\\s+extends\\s+.*").get(0);
      }
      catch (IndexOutOfBoundsException e)
      {
         throw new RuntimeException("Super class not found in " + testClassSimpleName + ". Try formatting the class.");
      }

      String classDeclarationLine = linesInFile.get(classDeclarationLineIndex);

      String[] splitClassDeclarationLine = classDeclarationLine.split("extends\\s+");

      String secondHalfOfSplitDeclaration = splitClassDeclarationLine[1];

      return secondHalfOfSplitDeclaration.split("[<\\s]")[0];
   }

   public static List<Path> findAllJavaPathsInWorkspace()
   {
      return SourceTools.findAllPathsInWorkspaceThatMatchRegex(SourceTools.ALL_JAVA_FILES_REGEX);
   }

   public static List<Path> findAllPathsInWorkspaceThatMatchRegex(String regex)
   {
      return PathTools.findAllPathsRecursivelyThatMatchRegex(getWorkspacePath(), regex);
   }

   public static List<Integer> getLineNumbersThatMatchRegex(List<String> linesInFile, String regex)
   {
      List<Integer> lineNumbersThatMatchRegex = new ArrayList<>();

      for (int i = 0; i < linesInFile.size(); i++)
      {
         if (linesInFile.get(i).matches(regex))
         {
            lineNumbersThatMatchRegex.add(i);
         }
      }

      return lineNumbersThatMatchRegex;
   }

   public static Path getWorkspacePath()
   {
      return WORKSPACE_PATH;
   }

   public static Path getProjectPath()
   {
      return PROJECT_PATH;
   }
}
