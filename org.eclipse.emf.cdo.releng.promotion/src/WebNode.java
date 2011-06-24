import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

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

    String http = "http://download.eclipse.org/" + PromoterConfig.INSTANCE.getProperties().getProperty("downloadsPath")
        + "/";
    System.out.println("Generating HTML for " + folder.getName());

    if (repository != null)
    {
      int header = level == 1 ? 2 : 4;
      out.println("<h" + header + ">" + repository.getWebLabel() + "</h" + header + ">");
      out.println("<a name=\"" + repository.getAnchorName() + "\"/>");

      out.println("<ul>");
      out.println("<li><b><a href=\"" + http + "updates/" + repository.getPath()
          + "\">Composite Update Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
          + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.</li>");

      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;

        for (BuildInfo buildInfo : drops.getBuildInfos())
        {
          out.print("<li><b>" + buildInfo.getQualifier() + "</b>");
          out.print(" <a href=\"" + http + "drops/" + buildInfo.getQualifier() + "\">Contents</a>");
          out.print(", <a href=\"" + http + "drops/" + buildInfo.getQualifier() + "\">Update&nbsp;Site</a>");
          out.print(", <a href=\""
              + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                  + buildInfo.getQualifier() + "-Site.zip") + "\">Archive</a>");
          out.print(", <a href=\""
              + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                  + buildInfo.getQualifier() + "-All.zip") + "\">Dropins</a>");
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
}
