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
package promoter.tasks;

import promoter.BuildInfo;
import promoter.PromoterConfig;
import promoter.util.Ant;
import promoter.util.XMLOutput;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eike Stepper
 */
public class DuplicateTask extends AbstractDropTask
{
  private File antHome;

  public DuplicateTask()
  {
    this(PromoterConfig.INSTANCE.getDirectory("ANT_HOME"));
  }

  private DuplicateTask(File antHome)
  {
    this.antHome = antHome;
  }

  @Override
  protected boolean execute(File drop, List<String> args, List<BuildInfo> builds)
  {
    final String qualifier = drop.getName();
    final char type = qualifier.charAt(0);

    final String newQualifier = args.remove(0);
    final char newType = newQualifier.charAt(0);

    if ("IMSR".indexOf(newType) == -1)
    {
      throw new IllegalArgumentException("Unsupported build type: " + newType);
    }

    System.out.println("   New Qualifier = " + newQualifier);

    try
    {
      File script = File.createTempFile("duplicateDrop-", ".ant");
      File basedir = drop.getParentFile();
      new Ant<Object>(antHome, script, basedir)
      {
        @Override
        protected Object create(XMLOutput xml) throws Exception
        {
          xml.element("copy").attribute("todir", newQualifier).attribute("includeemptydirs", true);
          xml.push();
          xml.element("fileset").attribute("dir", qualifier);
          xml.push();
          xml.element("include").attribute("name", "**");
          xml.pop();
          xml.pop();

          xml.element("move").attribute("todir", newQualifier + "/zips");
          xml.push();
          xml.element("fileset").attribute("dir", newQualifier + "/zips");
          xml.element("mapper").attribute("type", "regexp").attribute("from", "(.*)" + qualifier + "(.*)")
              .attribute("to", "\\1" + newQualifier + "\\2");
          xml.pop();

          xml.element("delete").attribute("includeemptydirs", true).attribute("failonerror", false);
          xml.push();
          xml.element("fileset").attribute("dir", newQualifier);
          xml.push();
          xml.element("include").attribute("name", "categories/");
          xml.element("include").attribute("name", "web.properties");
          xml.element("include").attribute("name", "relnotes.*");
          xml.element("include").attribute("name", ".promoted");
          xml.element("include").attribute("name", ".mirrored");
          xml.element("include").attribute("name", ".staged");
          xml.pop();
          xml.pop();

          xml.element("touch").attribute("file", newQualifier + "/.invisible");

          xml.element("replaceregexp").attribute("match", qualifier).attribute("replace", newQualifier)
              .attribute("byline", false).attribute("flags", "sg");
          xml.push();
          xml.element("fileset").attribute("dir", newQualifier);
          xml.push();
          xml.element("include").attribute("name", "build-info.xml");
          xml.element("include").attribute("name", "relnotes.*");
          xml.pop();
          xml.pop();

          xml.element("replaceregexp").attribute("match", "type=\"" + type + "\"")
              .attribute("replace", "type=\"" + newType + "\"").attribute("byline", false).attribute("flags", "sg");
          xml.push();
          xml.element("fileset").attribute("dir", newQualifier);
          xml.push();
          xml.element("include").attribute("name", "build-info.xml");
          xml.pop();
          xml.pop();

          return null;
        }
      }.run();

      File newDrop = new File(drop.getParent(), newQualifier);
      BuildInfo newBuildInfo = BuildInfo.read(new File(newDrop, "build-info.xml"));
      builds.add(newBuildInfo);
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }

    return true; // Order recomposition
  }

  public static void main(String[] _args)
  {
    File antHome = new File("C:\\develop\\bin\\apache-ant-1.8.2");
    File drop = new File("C:\\Users\\Stepper\\Desktop\\drops\\R20120612-1449");
    List<String> args = new ArrayList<String>();
    args.add("S20120621-1100");
    List<BuildInfo> builds = new ArrayList<BuildInfo>();

    DuplicateTask task = new DuplicateTask(antHome);
    task.execute(drop, args, builds);
  }
}
