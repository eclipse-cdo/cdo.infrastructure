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
package promoter;

import promoter.util.FileSizeInserter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    System.out.println(prefix(level) + "Generating HTML for " + folder.getName());

    if (repository != null)
    {
      List<BuildInfo> buildInfos = null;
      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;
        buildInfos = new ArrayList<BuildInfo>(drops.getBuildInfos());
        Collections.sort(buildInfos);
      }

      level = generateRepositoryStart(out, level, buildInfos != null && buildInfos.isEmpty());

      if (buildInfos != null && !buildInfos.isEmpty())
      {
        out.println(prefix(level++) + "<ul>");

        boolean firstDrop = true;
        for (BuildInfo buildInfo : buildInfos)
        {
          level = generateDrop(out, level, buildInfo, firstDrop);
          firstDrop = false;
        }

        out.println(prefix(--level) + "</ul>");
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
      generateRepositoryEnd(out, level);
    }
  }

  protected int generateRepositoryStart(PrintStream out, int level, boolean empty)
  {
    String repoName = repository.getAnchorName();
    String repoID = "repo_" + repoName;

    out.println(prefix(level)
        + "<li class=\"repo-item\"><a href=\"javascript:toggle('"
        + repoID
        + "')\" class=\"repo-label"
        + repository.getPathLevel()
        + "\">"
        + repository.getWebLabel()
        + "</a> <a name=\""
        + repoName
        + "\" href=\"#"
        + repoName
        + "\"><img src=\"http://www.eclipse.org/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a>");

    out.println(prefix(level++) + "<div class=\"repo" + repository.getPathLevel() + "\" id=\"repo_" + repoName + "\""
        + (repository.isWebCollapsed() || empty ? " style=\"display: none\"" : "") + ">");

    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    out.println(prefix(level)
        + "<tr class=\"repo-info\"><td><img src=\"http://www.eclipse.org/cdo/images/22x22/package-x-generic.png\"/></td>"
        + "<td><b><a href=\""
        + http()
        + "updates/"
        + repository.getPath()
        + "\">Composite&nbsp;Update&nbsp;Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
        + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a> but <b>not</b> with a web browser.</td><td class=\"file-size\"></td></tr>");

    if (empty)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td></td><td><i>Currently this composite update site is empty.<br>"
          + "This may change in the future when new builds are promoted.</i></td></tr>");
    }

    out.println(prefix(--level) + "</table>");
    return level;
  }

  protected int generateRepositoryEnd(PrintStream out, int level)
  {
    out.println(prefix(--level) + "</div>");
    return level;
  }

  protected int generateDrop(PrintStream out, int level, BuildInfo buildInfo, boolean firstDrop)
  {
    String dropName = buildInfo.getQualifier().replace('-', '_');
    String dropID = "drop_" + dropName;

    out.println(prefix(level)
        + "<li class=\"repo-item\"><b><a href=\"javascript:toggle('"
        + dropID
        + "')\" class=\"drop-label\">"
        + buildInfo.getQualifier()
        + "</a></b> <a name=\""
        + dropName
        + "\" href=\"#"
        + dropName
        + "\"><img src=\"http://www.eclipse.org/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a>");

    out.println(prefix(level++) + "<div class=\"drop\" id=\"" + dropID + "\""
        + (firstDrop ? "" : " style=\"display: none\"") + ">");

    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    out.println(prefix(level)
        + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/package-x-generic.png\"/></td><td><b><a href=\""
        + http()
        + "drops/"
        + buildInfo.getQualifier()
        + "\">Update&nbsp;Site</a></b> for use with <a href=\"http://help.eclipse.org/indigo/"
        + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.</td><td class=\"file-size\"></td></tr>");

    out.println(prefix(level)
        + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/internet-web-browser.png\"/></td><td><b><a href=\""
        + http() + "drops/" + buildInfo.getQualifier()
        + "/index.html\">Contents</a></b> for use with a web browser.</td><td class=\"file-size\"></td></tr>");

    generateDropSeparator(out, level);

    out.println(prefix(level)
        + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/go-down.png\"/></td><td><a href=\""
        + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-" + buildInfo.getQualifier()
            + "-Site.zip")
        + "\">emf-cdo-"
        + buildInfo.getQualifier()
        + "-Site.zip</a> for local use with <a href=\"http://help.eclipse.org/indigo/"
        + "index.jsp?topic=/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.</td><td class=\"file-size\">"
        + formatFileSize(PromoterConfig.INSTANCE.getDropsArea().getAbsolutePath() + "/" + buildInfo.getQualifier()
            + "/zips/emf-cdo-" + buildInfo.getQualifier() + "-Site.zip") + "</td></tr>");

    out.println(prefix(level)
        + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/go-down.png\"/></td><td><a href=\""
        + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/zips/emf-cdo-" + buildInfo.getQualifier()
            + "-All.zip")
        + "\">emf-cdo-"
        + buildInfo.getQualifier()
        + "-All.zip</a> for use with a <a href=\"http://help.eclipse.org/indigo/"
        + "index.jsp?topic=/org.eclipse.platform.doc.isv/reference/misc/p2_dropins_format.html\">dropins</a> folder.</td><td class=\"file-size\">"
        + formatFileSize(PromoterConfig.INSTANCE.getDropsArea().getAbsolutePath() + "/" + buildInfo.getQualifier()
            + "/zips/emf-cdo-" + buildInfo.getQualifier() + "-All.zip") + "</td></tr>");

    generateDropSeparator(out, level);
    generateDropFile(out, level, buildInfo, "index.xml");
    generateDropFile(out, level, buildInfo, "bookmarks.xml");
    generateDropFile(out, level, buildInfo, "build-info.xml");
    generateDropFile(out, level, buildInfo, "test-report.xml");
    generateDropSeparator(out, level);

    out.println(prefix(--level) + "</table>");
    out.println(prefix(--level) + "</div>");
    return level;
  }

  protected void generateDropSeparator(PrintStream out, int level)
  {
    out.println(prefix(level) + "<tr class=\"drop-info\"><td colspan=\"3\"><hr class=\"drop-separator\"></td></tr>");
  }

  protected void generateDropFile(PrintStream out, int level, BuildInfo buildInfo, String path)
  {
    String size = formatFileSize(PromoterConfig.INSTANCE.getDropsArea().getAbsolutePath() + "/"
        + buildInfo.getQualifier() + "/" + path);
    if (size.length() > 0)
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/text-x-generic.png\"/></td><td><a href=\""
          + http() + "drops/" + buildInfo.getQualifier() + "/" + path + "\">" + path
          + "</a> for detailed contents of this build.</td><td class=\"file-size\">" + size + "</td></tr>");
    }
  }

  private static String formatFileSize(String path)
  {
    String size = FileSizeInserter.formatFileSize(path);
    if (size.length() > 0)
    {
      size = "<i>" + size + "</i>";
    }

    return size;
  }

  private static String prefix(int level)
  {
    String indent = "   ";
    String prefix = "";
    for (int i = 0; i < level; i++)
    {
      prefix += indent;
    }

    return prefix;
  }

  private static String http()
  {
    String downloadsPath = PromoterConfig.INSTANCE.getProperty("downloadsPath");
    return "http://download.eclipse.org/" + downloadsPath + "/";
  }
}
