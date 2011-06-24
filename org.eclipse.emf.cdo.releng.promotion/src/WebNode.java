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
      out.println("<a name=\"" + repository.getAnchorName() + "\"></a>");

      out.println("<ul class=\"repo\" id=\"repo_" + repository.getAnchorName() + "\">");
      out.println("<li class=\"repo-info\"><b><a href=\""
          + http
          + "updates/"
          + repository.getPath()
          + "\">Composite Update Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
          + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can <b>not</b> be used with a web browser.");

      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;

        for (BuildInfo buildInfo : drops.getBuildInfos())
        {
          String dropID = "drop_" + buildInfo.getQualifier().replace('-', '_');
          out.print("<li><a href=\"javascript:toggle('" + dropID + "')><b><i>" + buildInfo.getQualifier()
              + "</i></b></a>");

          out.println("<ul class=\"drop\" id=\"" + dropID + "\">");
          out.print("<li class=\"drop-info\"><a href=\""
              + http
              + "drops/"
              + buildInfo.getQualifier()
              + "\">Update&nbsp;Site</a> for use with <a href=\"http://help.eclipse.org/indigo/"
              + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can also be used with a web browser.");
          out.print("<li class=\"drop-info\"><a href=\""
              + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                  + buildInfo.getQualifier() + "-Site.zip")
              + "\">Update&nbsp;Site&nbsp;Archive</a> for offline installation.");
          out.print("<li class=\"drop-info\"><a href=\""
              + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                  + buildInfo.getQualifier() + "-All.zip") + "\">Dropins&nbsp;Archive</a> for file system deployments.");
          out.print("<li class=\"drop-info\"><a href=\""
              + http
              + "drops/"
              + buildInfo.getQualifier()
              + "/bookmarks.xml\">Bookmarks</a> for the <a href=\"http://help.eclipse.org/indigo/"
              + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-128.htm\">import</a> of the build dependencies.");
          out.print("<li class=\"drop-info\"><a href=\"" + http + "drops/" + buildInfo.getQualifier()
              + "/build-info.xml\">Build&nbsp;Infos</a> for the parameters that produced this build.");
          out.println("</ul>");
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
