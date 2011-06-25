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
      String repoName = repository.getAnchorName();
      String repoID = "repo_" + repoName;
      List<BuildInfo> buildInfos = null;
      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;
        buildInfos = new ArrayList<BuildInfo>(drops.getBuildInfos());
        Collections.sort(buildInfos);
      }

      out.println(prefix(level) + "<li><a href=\"javascript:toggle('" + repoID + "')\" class=\"repo-label"
          + repository.getPathLevel() + "\">" + repository.getWebLabel() + "</a> <a name=\"" + repoName + "\" href=\"#"
          + repoName + "\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/link_obj.gif\"/></a>");

      out.println(prefix(level++)
          + "<div class=\"repo"
          + repository.getPathLevel()
          + "\" id=\"repo_"
          + repoName
          + "\""
          + (repository.isWebCollapsed() || buildInfos != null && buildInfos.isEmpty() ? " style=\"display: none\""
              : "") + ">");
      out.println(prefix(level)
          + "<p class=\"repo-info\"><b><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/internet-web-browser.png\"/> <a href=\""
          + http
          + "updates/"
          + repository.getPath()
          + "\">Composite&nbsp;Update&nbsp;Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
          + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can <b>not</b> be used with a web browser.</p>");

      if (buildInfos != null)
      {
        if (buildInfos.isEmpty())
        {
          out.println(prefix(level)
              + "<p class=\"repo-info\"><i>This composite update site is currently empty.</i></p>");
        }
        else
        {
          out.println(prefix(level++) + "<ul>");

          boolean firstDrop = true;
          for (BuildInfo buildInfo : buildInfos)
          {
            String dropName = buildInfo.getQualifier().replace('-', '_');
            String dropID = "drop_" + dropName;
            out.println(prefix(level) + "<li><b><a href=\"javascript:toggle('" + dropID + "')\" class=\"drop-label\">"
                + buildInfo.getQualifier() + "</a></b> <a name=\"" + dropName + "\" href=\"#" + dropName
                + "\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/link_obj.gif\"/></a>");
            out.println(prefix(level++) + "<div class=\"drop\" id=\"" + dropID + "\""
                + (firstDrop ? "" : " style=\"display: none\"") + ">");

            out.println(prefix(level)
                + "<div class=\"drop-info\"><b><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/text-html.png\"/> <a href=\""
                + http
                + "drops/"
                + buildInfo.getQualifier()
                + "\">Update&nbsp;Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
                + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>. Can also be used with a web browser.</div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/go-down.png\"/> <a href=\""
                + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                    + buildInfo.getQualifier() + "-Site.zip") + "\">emf-cdo-" + buildInfo.getQualifier()
                + "-Site.zip</a> for offline installations.<div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/go-down.png\"/> <a href=\""
                + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-"
                    + buildInfo.getQualifier() + "-All.zip") + "\">emf-cdo-" + buildInfo.getQualifier()
                + "-All.zip</a> for file system deployments.<div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/text-x-generic.png\"/> <a href=\""
                + http
                + "drops/"
                + buildInfo.getQualifier()
                + "/bookmarks.xml\">bookmarks.xml</a> for the <a href=\"http://help.eclipse.org/indigo/"
                + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-128.htm\">import</a> of the build dependencies.<div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/text-x-generic.png\"/> <a href=\""
                + http + "drops/" + buildInfo.getQualifier()
                + "/build-info.xml\">build-info.xml</a> for the parameters that produced this build.<div>");
            out.println(prefix(level)
                + "<div class=\"drop-info\"><img align=\"bottom\" src=\"http://www.eclipse.org/cdo/images/tango/text-x-generic.png\"/> <a href=\""
                + http + "drops/" + buildInfo.getQualifier()
                + "/testReport.xml\">test-report.xml</a> for the test results of this build.<div>");

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
