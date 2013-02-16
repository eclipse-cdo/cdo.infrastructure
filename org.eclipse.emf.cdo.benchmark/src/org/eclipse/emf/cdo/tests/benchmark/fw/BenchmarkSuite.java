/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package org.eclipse.emf.cdo.tests.benchmark.fw;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class BenchmarkSuite
{
  private static final File JAVA = new File("C:/Program Files/Java/jdk1.6.0_37/bin/javaw.exe");

  private static final File TP = new File("C:/develop/ws/cdo/.buckminster/tp/plugins");

  private static final File WS = new File("C:/develop/git/cdo/plugins");

  private static final String CLASS_PATH = buildClassPath();

  public static void main(String[] args) throws Exception
  {
    for (String benchmark : getBenchmarks())
    {
      Process server = null;
      Process client = null;

      try
      {
        server = startProcess(BenchmarkServer.class.getName(), "4g");
        client = startProcess(benchmark, "4g");

        InputStream stream = client.getErrorStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        String line;
        while ((line = reader.readLine()) != null)
        {
          System.out.println(line);
        }
      }
      finally
      {
        if (client != null)
        {
          client.destroy();
          client.waitFor();
        }

        if (server != null)
        {
          server.destroy();
          server.waitFor();
        }
      }
    }
  }

  private static List<String> getBenchmarks()
  {
    List<String> result = new ArrayList<String>();

    String packageName = BenchmarkSuite.class.getPackage().getName();
    int lastDot = packageName.lastIndexOf('.');
    packageName = packageName.substring(0, lastDot);

    for (File file : new File("src/" + packageName.replace('.', '/')).listFiles())
    {
      if (file.isFile())
      {
        String name = file.getName();
        if (name.endsWith(".java"))
        {
          result.add(packageName + "." + name.substring(0, name.length() - ".java".length()));
        }
      }
    }

    Collections.sort(result);
    return result;
  }

  private static Process startProcess(String mainClass, String heapSize) throws IOException
  {
    ProcessBuilder processBuilder = new ProcessBuilder();
    processBuilder.command().add(JAVA.getCanonicalFile().toString());
    if (BenchmarkServer.class.getName().equals(mainClass))
    {
      processBuilder.command().add("-Dbenchmark=SERVER");
    }
    else
    {
      processBuilder.command().add("-Dbenchmark=CLIENT");
    }

    processBuilder.command().add("-Xms" + heapSize);
    processBuilder.command().add("-Xmx" + heapSize);
    processBuilder.command().add("-classpath");
    processBuilder.command().add(CLASS_PATH);
    processBuilder.command().add(mainClass);
    return processBuilder.start();
  }

  private static String buildClassPath()
  {
    try
    {
      StringBuilder classPath = new StringBuilder("bin");
      addFromTP(classPath, "org.apache.commons.collections");
      addFromTP(classPath, "org.eclipse.core.contenttype");
      addFromTP(classPath, "org.eclipse.core.expressions");
      addFromTP(classPath, "org.eclipse.core.jobs");
      addFromTP(classPath, "org.eclipse.core.runtime");
      addFromTP(classPath, "org.eclipse.emf.common");
      addFromTP(classPath, "org.eclipse.emf.ecore.change");
      addFromTP(classPath, "org.eclipse.emf.ecore.xmi");
      addFromTP(classPath, "org.eclipse.emf.ecore");
      addFromTP(classPath, "org.eclipse.equinox.app");
      addFromTP(classPath, "org.eclipse.equinox.common");
      addFromTP(classPath, "org.eclipse.equinox.preferences");
      addFromTP(classPath, "org.eclipse.equinox.registry");
      addFromTP(classPath, "org.eclipse.osgi");
      addFromWS(classPath, "org.eclipse.emf.cdo.common");
      addFromWS(classPath, "org.eclipse.emf.cdo.net4j");
      addFromWS(classPath, "org.eclipse.emf.cdo.server.db");
      addFromWS(classPath, "org.eclipse.emf.cdo.server.net4j");
      addFromWS(classPath, "org.eclipse.emf.cdo.server");
      addFromWS(classPath, "org.eclipse.emf.cdo.tests.model1");
      addFromWS(classPath, "org.eclipse.emf.cdo");
      addFromWS(classPath, "org.eclipse.net4j.db.h2");
      addFromWS(classPath, "org.eclipse.net4j.db");
      addFromWS(classPath, "org.eclipse.net4j.tcp");
      addFromWS(classPath, "org.eclipse.net4j.util");
      addFromWS(classPath, "org.eclipse.net4j");
      return classPath.toString();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      throw new Error(ex);
    }
  }

  private static void addFromTP(StringBuilder classPath, String bundle) throws IOException
  {
    final String prefix = bundle + "_";
    File[] files = TP.listFiles(new FilenameFilter()
    {
      public boolean accept(File dir, String name)
      {
        return name.startsWith(prefix) && name.endsWith(".jar");
      }
    });

    if (files == null || files.length == 0)
    {
      throw new IOException(bundle + " not found in " + TP);
    }

    classPath.append(";");
    classPath.append(files[0].getCanonicalPath());
  }

  private static void addFromWS(StringBuilder classPath, String project) throws IOException
  {
    File projectFolder = new File(WS, project);
    File binFolder = new File(projectFolder, "bin");

    if (!binFolder.isDirectory())
    {
      throw new IOException(project + " not found in " + WS);
    }

    classPath.append(";");
    classPath.append(binFolder.getCanonicalPath());
  }
}
