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
    int n = level;

    String http = "http://download.eclipse.org/" + PromoterConfig.INSTANCE.getProperties().getProperty("downloadsPath")
        + "/";

    System.out.println(prefix(n) + "Generating HTML for " + folder.getName());

    if (repository != null)
    {
      String repoID = "repo_" + repository.getAnchorName();
      out.println(prefix(n) + "<li><a href=\"javascript:toggle('" + repoID + "')\">" + repository.getWebLabel()
          + "</a>");
      out.println(prefix(n) + "<a name=\"" + repository.getAnchorName() + "\"/>");

      out.println(prefix(n++) + "<div class=\"repo\" id=\"repo_" + repository.getAnchorName() + "\">");
      out.println(prefix(n)
          + "<p class=\"repo-info\"><b><a href=\""
          + http
          + "updates/"
          + repository.getPath()
          + "\">Composite Update Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
          + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can <b>not</b> be used with a web browser.</p>");

      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;

        List<BuildInfo> buildInfos = new ArrayList<BuildInfo>(drops.getBuildInfos());
        Collections.sort(buildInfos);

        boolean firstDrop = true;
        for (BuildInfo buildInfo : buildInfos)
        {
          String dropID = "drop_" + buildInfo.getQualifier().replace('-', '_');
          out.println(prefix(n) + "<li><b><a href=\"javascript:toggle('" + dropID + "')\">" + buildInfo.getQualifier()
              + "</a></b>");
          out.println(prefix(n) + "<a name=\"" + buildInfo.getQualifier().replace('-', '_') + "\"/>");
          out.println(prefix(n++) + "<div class=\"drop\" id=\"" + dropID + "\""
              + (firstDrop ? "" : " style=\"display: none\"") + ">");

          out.println(prefix(n)
              + "<a href=\""
              + http
              + "drops/"
              + buildInfo.getQualifier()
              + "\">Update&nbsp;Site</a> for use with <a href=\"http://help.eclipse.org/indigo/"
              + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can also be used with a web browser.<br>");
          out.println(prefix(n)
              + "<a href=\""
              + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                  + buildInfo.getQualifier() + "-Site.zip")
              + "\">Update&nbsp;Site&nbsp;Archive</a> for offline installation.<br>");
          out.println(prefix(n)
              + "<a href=\""
              + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                  + buildInfo.getQualifier() + "-All.zip")
              + "\">Dropins&nbsp;Archive</a> for file system deployments.<br>");
          out.println(prefix(n)
              + "<a href=\""
              + http
              + "drops/"
              + buildInfo.getQualifier()
              + "/bookmarks.xml\">Bookmarks</a> for the <a href=\"http://help.eclipse.org/indigo/"
              + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-128.htm\">import</a> of the build dependencies.<br>");
          out.println(prefix(n) + "<a href=\"" + http + "drops/" + buildInfo.getQualifier()
              + "/build-info.xml\">Build&nbsp;Infos</a> for the parameters that produced this build.<br>");

          out.println(prefix(--n) + "</div>");
          firstDrop = false;
        }
      }
    }

    out.println(prefix(n++) + "<ul>");
    for (WebNode child : children)
    {
      child.generate(out, n + 1);
    }

    out.println(prefix(--n) + "</ul>");
    out.println(prefix(--n) + "</div>");
  }

  private String prefix(int l)
  {
    String indent = "   ";
    String prefix = "";
    for (int i = 0; i < l; i++)
    {
      prefix += indent;
    }

    return prefix;
  }
}
