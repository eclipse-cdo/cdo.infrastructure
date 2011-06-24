import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
    String http = "http://download.eclipse.org/" + PromoterConfig.INSTANCE.getProperties().getProperty("downloadsPath")
        + "/";

    System.out.println(prefix(level) + "Generating HTML for " + folder.getName());

    if (repository != null)
    {
      String repoID = "repo_" + repository.getAnchorName();

      out.println(prefix(level) + "<li><a href=\"javascript:toggle('" + repoID + "')\" class=\"repo-label"
          + repository.getPathLevel() + "\">" + repository.getWebLabel() + "</a>");
      out.println(prefix(level) + "<a name=\"" + repository.getAnchorName() + "\"/>");

      out.println(prefix(level++) + "<div class=\"repo" + repository.getPathLevel() + "\" id=\"repo_"
          + repository.getAnchorName() + "\"" + (repository.isWebCollapsed() ? " style=\"display: none\"" : "") + ">");
      out.println(prefix(level)
          + "<p class=\"repo-info\"><b><a href=\""
          + http
          + "updates/"
          + repository.getPath()
          + "\">Composite Update Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
          + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can <b>not</b> be used with a web browser.</p>");

      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;

        if (drops.getBuildInfos().isEmpty())
        {
          out.println(prefix(level) + "<p class=\"repo-info\">This composite update site is currently empty.</p>");
        }
        else
        {
          out.println(prefix(level++) + "<ul>");
          List<BuildInfo> buildInfos = new ArrayList<BuildInfo>(drops.getBuildInfos());
          Collections.sort(buildInfos);

          boolean firstDrop = true;
          for (BuildInfo buildInfo : buildInfos)
          {
            String dropID = "drop_" + buildInfo.getQualifier().replace('-', '_');
            out.println(prefix(level) + "<li><b><a href=\"javascript:toggle('" + dropID + "')\" class=\"drop-label\">"
                + buildInfo.getQualifier() + "</a></b>");
            out.println(prefix(level) + "<a name=\"" + buildInfo.getQualifier().replace('-', '_') + "\"/>");
            out.println(prefix(level++) + "<div class=\"drop\" id=\"" + dropID + "\""
                + (firstDrop ? "" : " style=\"display: none\"") + ">");

            out.println(prefix(level)
                + "<div class=\"drop-info\"><a href=\""
                + http
                + "drops/"
                + buildInfo.getQualifier()
                + "\">Update&nbsp;Site</a> for use with <a href=\"http://help.eclipse.org/indigo/"
                + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can also be used with a web browser.</div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><a href=\""
                + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                    + buildInfo.getQualifier() + "-Site.zip")
                + "\">Update&nbsp;Site&nbsp;Archive</a> for offline installation.<div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><a href=\""
                + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                    + buildInfo.getQualifier() + "-All.zip")
                + "\">Dropins&nbsp;Archive</a> for file system deployments.<div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><a href=\""
                + http
                + "drops/"
                + buildInfo.getQualifier()
                + "/bookmarks.xml\">Bookmarks</a> for the <a href=\"http://help.eclipse.org/indigo/"
                + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-128.htm\">import</a> of the build dependencies.<div>");
            out.println(prefix(level) + "<div class=\"drop-info\"><a href=\"" + http + "drops/"
                + buildInfo.getQualifier()
                + "/build-info.xml\">Build&nbsp;Infos</a> for the parameters that produced this build.<div>");

            out.println(prefix(--level) + "</div>");
            firstDrop = false;
          }

          out.println(prefix(--level) + "</ul>");
        }
      }
    }

    if (!children.isEmpty())
    {
      out.println(prefix(level++) + "<ul>");
      for (WebNode child : children)
      {
        child.generate(out, level);
      }

      out.println(prefix(--level) + "</ul>");
    }

    if (repository != null)
    {
      out.println(prefix(--level) + "</div>");
    }
  }

  private String prefix(int level)
  {
    String indent = "   ";
    String prefix = "";
    for (int i = 0; i < level; i++)
    {
      prefix += indent;
    }

    return prefix;
  }
}
