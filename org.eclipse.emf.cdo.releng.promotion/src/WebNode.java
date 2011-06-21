import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Copyright (c) 2004 - 2011 Eike Stepper (Berlin, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */

/**
 * @author Eike Stepper
 */
public class WebNode implements Comparable<WebNode>
{
  private File folder;

  private Repository repository;

  private List<WebNode> children = new ArrayList<WebNode>();

  public WebNode(File folder)
  {
    this.folder = folder;
  }

  public final File getFolder()
  {
    return folder;
  }

  public final Repository getRepository()
  {
    return repository;
  }

  public final void setRepository(Repository repository)
  {
    this.repository = repository;
  }

  public final List<WebNode> getChildren()
  {
    return children;
  }

  public int compareTo(WebNode o)
  {
    Integer p1 = repository == null ? -1 : repository.getWebPriority();
    Integer p2 = o.repository == null ? -1 : o.repository.getWebPriority();
    return p2.compareTo(p1);
  }

  public void generate(PrintStream out, int level) throws IOException
  {
    for (int i = 0; i < level; i++)
    {
      System.out.print("   ");
    }

    System.out.println("Generating PHP for " + folder.getName());

    if (repository != null)
    {
      out.println("<h" + level + ">" + repository.getWebLabel() + "</h" + level + ">");
      out.println("<ul>");
      out.println("<li><b><a href=\"" + repository.getPath() + "\">Composite Update Site</a></b></li>");

      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;
        String path = getPath();

        for (BuildInfo buildInfo : drops.getBuildInfos())
        {
          out.print("<li>" + buildInfo.getQualifier() + ":");
          out.print("&nbsp;&nbsp;<a href=\"" + path + buildInfo.getQualifier() + "\">Contents Listing</a>");
          out.print("&nbsp;&nbsp;<a href=\"" + path + buildInfo.getQualifier() + "\">Update Site</a>");
          out.print("&nbsp;-&nbsp;<a href=\"" + path + buildInfo.getQualifier() + "/zips/emf-cdo-"
              + buildInfo.getQualifier() + "-Site.zip\">Update Site Archive</a>");
          out.print("&nbsp;-&nbsp;<a href=\"" + path + buildInfo.getQualifier() + "/zips/emf-cdo-"
              + buildInfo.getQualifier() + "-All.zip\">Dropins Archive</a>");
          out.println("</li>");
        }

      }

      out.println("</ul>");
    }

    for (WebNode child : children)
    {
      child.generate(out, level + 1);
    }
  }

  private String getPath()
  {
    String path = "";
    StringTokenizer tokenizer = new StringTokenizer(PromoterConfig.INSTANCE.getProperties().getProperty(
        "compositionPath"), "/");
    while (tokenizer.hasMoreTokens())
    {
      tokenizer.nextToken();
      path += "../";
    }

    return path + "drops/";
  }
}
