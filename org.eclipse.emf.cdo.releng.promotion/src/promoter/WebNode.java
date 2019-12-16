/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.StreamSupport;

import promoter.Repository.Drops;
import promoter.util.Config;
import promoter.util.FileSizeInserter;
import promoter.util.IO;

/**
 * @author Eike Stepper
 */
public class WebNode implements Comparable<WebNode>
{
  private final boolean EXPAND_ALL = Boolean.getBoolean("webExpandAll");

  private File relativePath;

  private int level;

  private Repository repository;

  private Repository latestRepository;

  private List<WebNode> children = new ArrayList<>();

  private String childRetention;

  private String targetInfo;

  private String targetVersions;

  private String apiBaselineURL;

  private String apiBaselineSize;

  private String webLabel;

  private int webPriority;

  private boolean webCollapsed;

  public WebNode(File relativePath, Properties properties)
  {
    this.relativePath = relativePath;
    level = (int)StreamSupport.stream(relativePath.toPath().spliterator(), false).count();

    childRetention = properties.getProperty("child.retention");
    targetInfo = properties.getProperty("target.info");
    targetVersions = properties.getProperty("target.versions", "");
    apiBaselineURL = properties.getProperty("api.baseline.url");
    apiBaselineSize = properties.getProperty("api.baseline.size", "");
    webLabel = properties.getProperty("web.label", getName());
    webPriority = Integer.parseInt(properties.getProperty("web.priority", "500"));
    webCollapsed = Boolean.parseBoolean(properties.getProperty("web.collapsed", "false"));
  }

  public final File getRelativePath()
  {
    return relativePath;
  }

  public final int getLevel()
  {
    return level;
  }

  public final String getName()
  {
    String name = relativePath.getName();
    return name.isEmpty() ? "<root>" : name;
  }

  public final Repository getRepository()
  {
    return repository;
  }

  public final void setRepository(Repository repository)
  {
    this.repository = repository;
  }

  public Repository getLatestRepository()
  {
    return latestRepository;
  }

  public void setLatestRepository(Repository latestRepository)
  {
    this.latestRepository = latestRepository;
  }

  public final List<WebNode> getChildren()
  {
    return children;
  }

  public final BuildInfo getLatestDrop(boolean includeInvisibles)
  {
    List<BuildInfo> drops = getDrops(true);

    BuildInfo latest = null;
    for (BuildInfo drop : drops)
    {
      if (!includeInvisibles)
      {
        if (new File(drop.getDrop(), DropProcessor.MARKER_INVISIBLE).isFile())
        {
          continue;
        }
      }

      if (drop.isLaterThan(latest))
      {
        latest = drop;
      }
    }

    return latest;
  }

  public final List<BuildInfo> getDrops(boolean recursive)
  {
    List<BuildInfo> drops = new ArrayList<>();
    collectAllDrops(recursive, drops);
    return drops;
  }

  private void collectAllDrops(boolean recursive, List<BuildInfo> drops)
  {
    if (repository instanceof Drops)
    {
      Drops dropsRepository = (Drops)repository;
      drops.addAll(dropsRepository.getBuildInfos());
    }

    if (recursive)
    {
      for (WebNode childNode : children)
      {
        childNode.collectAllDrops(true, drops);
      }
    }
  }

  public String getAnchorName()
  {
    StringBuilder builder = new StringBuilder();
    for (char c : relativePath.toString().toCharArray())
    {
      if (Character.isJavaIdentifierPart(c))
      {
        builder.append(c);
      }
      else
      {
        builder.append('_');
      }
    }

    return builder.toString();
  }

  @Override
  public int compareTo(WebNode o)
  {
    Integer p1 = repository == null ? -1 : webPriority;
    Integer p2 = o.repository == null ? -1 : o.webPriority;
    return p2.compareTo(p1);
  }

  @Override
  public String toString()
  {
    return getName();
  }

  public void generate(PrintStream out, int level) throws IOException
  {
    System.out.println(prefix(level) + "Generating HTML for " + this);

    if (repository != null)
    {
      List<BuildInfo> buildInfos = null;
      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;
        buildInfos = new ArrayList<>(drops.getBuildInfos());
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
    String nodeName = getAnchorName();
    String nodeID = "repo_" + nodeName;

    // Heading
    out.println(prefix(level) + "<li class=\"repo-item\"><a href=\"javascript:toggle('" + nodeID + "')\" class=\"repo-label" + level + "\">" + webLabel
        + "</a> <a name=\"" + nodeName + "\" href=\"#" + nodeName
        + "\"><img src=\"https://www.eclipse.org/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a>");

    out.println(prefix(level++) + "<div class=\"repo" + level + "\" id=\"repo_" + nodeName + "\""
        + (!EXPAND_ALL && webCollapsed || empty ? " style=\"display: none\"" : "") + ">");

    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    // Latest Update Site
    if (latestRepository != null)
    {
      out.println(
          prefix(level) + "<tr class=\"repo-info\"><td><img src=\"https://www.eclipse.org/cdo/images/22x22/package-x-generic.png\"/></td>" + "<td><b><a href=\""
              + latestRepository.getURL(false) + "\">Latest&nbsp;Update&nbsp;Site</a></b> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
              + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a> or a web browser.</td>" + "<td class=\"file-size level" + level + "\"></td></tr>");
    }

    // Composite Update Site
    out.println(
        prefix(level) + "<tr class=\"repo-info\"><td><img src=\"https://www.eclipse.org/cdo/images/22x22/package-x-generic.png\"/></td>" + "<td><b><a href=\""
            + repository.getURL(false) + "\">Composite&nbsp;Update&nbsp;Site</a></b> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
            + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a> or a web browser.</td>" + "<td class=\"file-size level" + level + "\"></td></tr>");

    // API Baseline
    if (apiBaselineURL != null)
    {
      out.println(prefix(level) + "<tr class=\"repo-info\"><td><img src=\"https://www.eclipse.org/cdo/images/22x22/go-down.png\"/></td>" + "<td><a href=\""
          + apiBaselineURL + "\">" + new File(apiBaselineURL).getName() + "</a> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
          + "/org.eclipse.pde.doc.user/tasks/api_tooling_baseline.htm\">API Tools</a>.</td>" + "<td class=\"file-size level" + level + "\"><i>"
          + apiBaselineSize + "</i></td></tr>");
    }

    // Target Info
    if (targetInfo != null)
    {
      out.println(prefix(level) + "<tr class=\"repo-info\"><td><img src=\"https://www.eclipse.org/cdo/images/22x22/dialog-information.png\"/></td>" + "<td>"
          + targetInfo + "</td>" + "<td class=\"file-size level" + level + "\"><i>" + targetVersions + "</i></td></tr>");
    }

    if (childRetention != null)
    {
      out.println(prefix(level) + "<tr class=\"repo-info\"><td><img src=\"https://www.eclipse.org/cdo/images/22x22/dialog-information.png\"/></td>" + "<td>"
          + childRetention + "</td>" + "<td class=\"file-size level" + level + "\"></td></tr>");
    }

    if (empty)
    {
      out.println(prefix(level) + "<tr class=\"repo-info\"><td></td><td><i>Currently this composite update site is empty.<br>"
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

    Properties webProperties = Config.loadProperties(new File(buildInfo.getDrop(), "web.properties"), false);
    String webLabel = webProperties.getProperty("web.label");
    if (webLabel != null)
    {
      dropLabel = webLabel + " (" + dropLabel + ")";
    }

    out.println(prefix(level) + "<li class=\"repo-item\"><b><a href=\"javascript:toggle('" + dropID + "')\" class=\"drop-label\">" + dropLabel
        + "</a></b> <a name=\"" + dropName + "\" href=\"#" + dropName
        + "\"><img src=\"https://www.eclipse.org/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a>");

    out.println(prefix(level++) + "<div class=\"drop\" id=\"" + dropID + "\"" + (EXPAND_ALL || firstDrop ? "" : " style=\"display: none\"") + ">");
    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    File drop = buildInfo.getDrop();
    String dropURL = buildInfo.getDropURL(null, false);
    int elements = 0;

    // Update Site
    if (IO.isRepository(drop))
    {
      out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/16x16/package-x-generic.png\"/></td>"
          + "<td><b><a href=\"" + dropURL + "\">Update&nbsp;Site</a></b> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
          + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a> or a web browser.</td>" + "<td class=\"file-size level" + (level + 1)
          + "\"></td></tr>");
      ++elements;
    }

    // Release Notes
    if (new File(drop, "relnotes.html").isFile())
    {
      out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/16x16/edit-paste.png\"/></td><td><b><a href=\""
          + dropURL + "/relnotes.html\">Release Notes</a></b> to see what's in this build.</td><td class=\"file-size level" + (level + 1) + "\"></td></tr>");
      ++elements;
    }

    // Documentation
    if (new File(new File(drop, "help"), "index.html").isFile())
    {
      out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/16x16/help-browser.png\"/></td><td><b><a href=\""
          + dropURL + "/help/index.html\">Documentation</a></b> to browse the online help center of this build.</td><td class=\"file-size level" + (level + 1)
          + "\"></td></tr>");
      ++elements;
    }

    // API Revolution Report
    if (new File(drop, "api.html").isFile())
    {
      out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/api/report.gif\"/></td><td><b><a href=\"" + dropURL
          + "/api.html\">API Evolution Report</a></b> to see the API changes in this stream.</td><td class=\"file-size level" + (level + 1) + "\"></td></tr>");
      ++elements;
    }

    // Test Report
    if (new File(new File(drop, "tests"), "index.html").isFile())
    {
      out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/16x16/junit.png\"/></td><td><b><a href=\""
          + dropURL + "/tests/index.html\">Test Report</a></b> to explore the quality of this build.</td><td class=\"file-size level" + (level + 1)
          + "\"></td></tr>");
      ++elements;
    }

    if (elements > 0)
    {
      generateDropSeparator(out, level);
      elements = 0;
    }

    // Downloads
    if (generateDropDownload(out, level, buildInfo, "zips/emf-cdo-" + buildInfo.getQualifier() + "-Site.zip",
        " for local use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL() + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>."))
    {
      ++elements;
    }

    // Zips
    List<DropZip> dropZips = new ArrayList<>();

    File zipsFolder = new File(drop, "zips");
    if (zipsFolder.isDirectory())
    {
      for (File file : zipsFolder.listFiles())
      {
        String name = file.getName();
        if (file.isFile() && name.endsWith(".zip") && !name.endsWith("-Site.zip"))
        {
          DropZip dropZip = new DropZip(file);
          dropZips.add(dropZip);
        }
      }
    }

    Collections.sort(dropZips);
    for (DropZip dropZip : dropZips)
    {
      generateDropDownload(out, level, buildInfo, "zips/" + dropZip.getName(), " " + dropZip.getDescription());
      ++elements;
    }

    if (elements > 0)
    {
      generateDropSeparator(out, level);
      elements = 0;
    }

    generateDropFile(out, level, buildInfo, "index.xml", " for the contents of this build.");

    generateDropFile(out, level, buildInfo, "relnotes.xml", " for the change infos of this build.");

    generateDropFile(out, level, buildInfo, "api.xml", " for the API evolution report of this build.");

    generateDropFile(out, level, buildInfo, "bookmarks.xml", " for the <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
        + "/org.eclipse.platform.doc.user/tasks/tasks-128.htm\">import</a> of the build dependencies.");

    generateDropFile(out, level, buildInfo, "bom.xml", " for the <a href=\"https://www.eclipse.org/buckminster\">bill of materials</a> of this build.");

    generateDropFile(out, level, buildInfo, "build-info.xml", " for the parameters that produced this build.");

    generateDropFile(out, level, buildInfo, "test-report.xml", " for the test results of this build.");
    generateDropFile(out, level, buildInfo, "tests/test-report.xml", " for the test results of this build.");

    generateDropSeparator(out, level);

    out.println(prefix(--level) + "</table>");
    out.println(prefix(--level) + "</div>");
    return level;
  }

  protected void generateDropSeparator(PrintStream out, int level)
  {
    out.println(prefix(level) + "<tr class=\"drop-info\"><td colspan=\"3\"><hr class=\"drop-separator\"></td></tr>");
  }

  protected boolean generateDropFile(PrintStream out, int level, BuildInfo buildInfo, String path, String description)
  {
    File file = new File(buildInfo.getDrop(), path);
    if (file.isFile())
    {
      String size = formatFileSize(file.getAbsolutePath());
      if (size.length() > 0)
      {
        int lastSlash = path.lastIndexOf('/');
        String label = lastSlash == -1 ? path : path.substring(lastSlash + 1);

        out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/16x16/text-x-generic.png\"/></td><td><a href=\""
            + buildInfo.getDropURL(path, false) + "\">" + label + "</a>" + description + "</td><td class=\"file-size level" + (level + 1) + "\">" + size
            + "</td></tr>");
        return true;
      }
    }

    return false;
  }

  protected boolean generateDropDownload(PrintStream out, int level, BuildInfo buildInfo, String path, String description)
  {
    File download = new File(buildInfo.getDrop(), path);
    if (download.isFile())
    {
      out.println(prefix(level) + "<tr class=\"drop-info\"><td><img src=\"https://www.eclipse.org/cdo/images/16x16/go-down.png\"/></td><td><a href=\""
          + buildInfo.getDropURL(path, true) + "\">" + new File(path).getName() + "</a>" + description + "</td><td class=\"file-size level" + (level + 1)
          + "\">" + formatFileSize(download.getAbsolutePath()) + "</td></tr>");
      return true;
    }

    return false;
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

    @Override
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
