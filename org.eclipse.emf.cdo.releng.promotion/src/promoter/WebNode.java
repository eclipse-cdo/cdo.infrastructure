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
package promoter;

import promoter.util.Config;
import promoter.util.FileSizeInserter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public class WebNode implements Comparable<WebNode>
{
  public static final String HELP_TOPIC_URL = "http://help.eclipse.org/indigo/index.jsp?topic=";

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
        + "<td><b><a href=\"" + http() + "updates/" + repository.getPath()
        + "\">Composite&nbsp;Update&nbsp;Site</a></b> for use with <a href=\"" + HELP_TOPIC_URL
        + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a> or a web browser.</td>"
        + "<td class=\"file-size level" + repository.getPathLevel() + "\"></td></tr>");

    String apiBaselineURL = repository.getApiBaselineURL();
    if (apiBaselineURL != null)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img src=\"http://www.eclipse.org/cdo/images/22x22/go-down.png\"/></td>"
          + "<td><a href=\"" + apiBaselineURL + "\">" + new File(apiBaselineURL).getName()
          + "</a> for use with <a href=\"" + HELP_TOPIC_URL
          + "/org.eclipse.pde.doc.user/tasks/api_tooling_baseline.htm\">API Tools</a>.</td>"
          + "<td class=\"file-size level" + repository.getPathLevel() + "\"><i>" + repository.getApiBaselineSize()
          + "</i></td></tr>");
    }

    String targetInfo = repository.getTargetInfo();
    if (targetInfo != null)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img src=\"http://www.eclipse.org/cdo/images/22x22/dialog-information.png\"/></td>"
          + "<td>" + targetInfo + "</td>" + "<td class=\"file-size level" + repository.getPathLevel() + "\"><i>"
          + repository.getTargetVersions() + "</i></td></tr>");
    }

    String childRetention = repository.getChildRetention();
    if (childRetention != null)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img src=\"http://www.eclipse.org/cdo/images/22x22/dialog-information.png\"/></td>"
          + "<td>" + childRetention + "</td>" + "<td class=\"file-size level" + repository.getPathLevel()
          + "\"></td></tr>");
    }

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
    String dropLabel = buildInfo.getQualifier();

    Properties webProperties = Config.loadProperties(
        new File(PromoterConfig.INSTANCE.getDropsArea(), buildInfo.getQualifier() + "/web.properties"), false);
    String webLabel = webProperties.getProperty("web.label");
    if (webLabel != null)
    {
      dropLabel = webLabel + " (" + dropLabel + ")";
    }

    out.println(prefix(level)
        + "<li class=\"repo-item\"><b><a href=\"javascript:toggle('"
        + dropID
        + "')\" class=\"drop-label\">"
        + dropLabel
        + "</a></b> <a name=\""
        + dropName
        + "\" href=\"#"
        + dropName
        + "\"><img src=\"http://www.eclipse.org/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a>");

    out.println(prefix(level++) + "<div class=\"drop\" id=\"" + dropID + "\""
        + (firstDrop ? "" : " style=\"display: none\"") + ">");

    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    out.println(prefix(level)
        + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/package-x-generic.png\"/></td>"
        + "<td><b><a href=\"" + http() + "drops/" + buildInfo.getQualifier()
        + "\">Update&nbsp;Site</a></b> for use with <a href=\"" + HELP_TOPIC_URL
        + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a> or a web browser.</td>"
        + "<td class=\"file-size level" + (repository.getPathLevel() + 1) + "\"></td></tr>");

    out.println(prefix(level)
        + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/edit-paste.png\"/></td><td><b><a href=\""
        + http() + "drops/" + buildInfo.getQualifier()
        + "/relnotes.html\">Release Notes</a></b> to see what's in this build.</td><td class=\"file-size level"
        + (repository.getPathLevel() + 1) + "\"></td></tr>");

    File apiHTML = getDropFile(buildInfo, "api.html");
    if (apiHTML.isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/api/report.gif\"/></td><td><b><a href=\""
          + http()
          + "drops/"
          + buildInfo.getQualifier()
          + "/api.html\">API Evolution Report</a></b> to see the API changes in this stream.</td><td class=\"file-size level"
          + (repository.getPathLevel() + 1) + "\"></td></tr>");
    }

    generateDropHelp(out, level, buildInfo);

    generateDropSeparator(out, level);

    generateDropDownload(out, level, buildInfo, "zips/emf-cdo-" + buildInfo.getQualifier() + "-Site.zip",
        " for local use with <a href=\"" + HELP_TOPIC_URL
            + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.");

    generateDropZips(out, level, buildInfo);

    generateDropSeparator(out, level);

    generateDropFile(out, level, buildInfo, "index.xml", " for the contents of this build.");

    generateDropFile(out, level, buildInfo, "relnotes.xml", " for the change infos of this build.");

    generateDropFile(out, level, buildInfo, "api.xml", " for the API evolution report of this build.");

    generateDropFile(out, level, buildInfo, "bookmarks.xml", " for the <a href=\"" + HELP_TOPIC_URL
        + "/org.eclipse.platform.doc.user/tasks/tasks-128.htm\">import</a> of the build dependencies.");

    generateDropFile(out, level, buildInfo, "bom.xml",
        " for the <a href=\"http://www.eclipse.org/buckminster\">bill of materials</a> of this build.");

    generateDropFile(out, level, buildInfo, "build-info.xml", " for the parameters that produced this build.");

    generateDropFile(out, level, buildInfo, "test-report.xml", " for the test results of this build.");

    generateDropSeparator(out, level);

    out.println(prefix(--level) + "</table>");
    out.println(prefix(--level) + "</div>");
    return level;
  }

  protected void generateDropSeparator(PrintStream out, int level)
  {
    out.println(prefix(level) + "<tr class=\"drop-info\"><td colspan=\"3\"><hr class=\"drop-separator\"></td></tr>");
  }

  protected void generateDropHelp(PrintStream out, int level, BuildInfo buildInfo)
  {
    File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), buildInfo.getQualifier());
    File help = new File(drop, "help");
    File index = new File(help, "index.html");
    if (index.isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/help-browser.png\"/></td><td><b><a href=\""
          + http()
          + "drops/"
          + buildInfo.getQualifier()
          + "/help/index.html\">Documentation</a></b> to browse the online help center of this build.</td><td class=\"file-size level"
          + (repository.getPathLevel() + 1) + "\"></td></tr>");
    }
  }

  protected void generateDropDownload(PrintStream out, int level, BuildInfo buildInfo, String path, String description)
  {
    File download = getDropFile(buildInfo, path);
    if (download.isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/go-down.png\"/></td><td><a href=\""
          + PromoterConfig.INSTANCE.formatDropURL(buildInfo.getQualifier() + "/" + path) + "\">"
          + new File(path).getName() + "</a>" + description + "</td><td class=\"file-size level"
          + (repository.getPathLevel() + 1) + "\">" + formatFileSize(download.getAbsolutePath()) + "</td></tr>");
    }
  }

  private File getDropFile(BuildInfo buildInfo, String path)
  {
    File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), buildInfo.getQualifier());
    File download = new File(drop, path);
    return download;
  }

  protected void generateDropZips(PrintStream out, int level, BuildInfo buildInfo)
  {
    File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), buildInfo.getQualifier());
    List<DropZip> dropZips = new ArrayList<DropZip>();

    for (File file : new File(drop, "zips").listFiles())
    {
      String name = file.getName();
      if (file.isFile() && name.endsWith(".zip") && !name.endsWith("-Site.zip"))
      {
        DropZip dropZip = new DropZip(file);
        dropZips.add(dropZip);
      }
    }

    Collections.sort(dropZips);
    for (DropZip dropZip : dropZips)
    {
      generateDropDownload(out, level, buildInfo, "zips/" + dropZip.getName(), " " + dropZip.getDescription());
    }
  }

  protected void generateDropFile(PrintStream out, int level, BuildInfo buildInfo, String path, String description)
  {
    String size = formatFileSize(PromoterConfig.INSTANCE.getDropsArea().getAbsolutePath() + "/"
        + buildInfo.getQualifier() + "/" + path);
    if (size.length() > 0)
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img src=\"http://www.eclipse.org/cdo/images/16x16/text-x-generic.png\"/></td><td><a href=\""
          + http() + "drops/" + buildInfo.getQualifier() + "/" + path + "\">" + path + "</a>" + description
          + "</td><td class=\"file-size level" + (repository.getPathLevel() + 1) + "\">" + size + "</td></tr>");
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

  public static String http()
  {
    String downloadsPath = PromoterConfig.INSTANCE.getProperty("downloadsPath");
    return "http://download.eclipse.org/" + downloadsPath + "/";
  }

  /**
   * @author Eike Stepper
   */
  private static final class DropZip implements Comparable<DropZip>
  {
    private File file;

    private String description;

    private int priority;

    public DropZip(File file)
    {
      this.file = file;

      String name = file.getName();
      String propsFileName = name.substring(0, name.length() - ".zip".length()) + ".properties";
      File propsFile = new File(file.getParentFile(), propsFileName);

      Properties properties = Config.loadProperties(propsFile, false);
      description = properties.getProperty("description", "");
      priority = Integer.parseInt(properties.getProperty("priority", "500"));
    }

    public String getName()
    {
      return file.getName();
    }

    public String getDescription()
    {
      return description;
    }

    public int getPriority()
    {
      return priority;
    }

    public int compareTo(DropZip o)
    {
      int result = new Integer(o.getPriority()).compareTo(priority);
      if (result == 0)
      {
        result = getName().compareTo(o.getName());
      }

      return result;
    }

    @Override
    public String toString()
    {
      return "DropZip [file=" + file + ", description=" + description + ", priority=" + priority + "]";
    }
  }
}
