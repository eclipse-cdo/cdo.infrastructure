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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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
  public static final String ROOT = "<root>";

  private final boolean EXPAND_ALL = Boolean.getBoolean("webExpandAll");

  // The names of the trains that contain Eclipse 4.2 or higher such that the list index is the minor version.
  private final List<String> TRAINS_4_X = Arrays.asList("", "", "juno", "kepler", "luna", "mars", "neon", "oxygen", "photon");

  // The names of the trains that contain Eclipse 3.0 or higher such that the list index is the minor version.
  private final List<String> TRAINS_3_X = Arrays.asList("", "", "", "europa", "galileo", "ganymede", "helios", "indigo");

  private File relativePath;

  private int pathLevel;

  private Repository repository;

  private Repository latestRepository;

  private List<WebNode> children = new ArrayList<>();

  private String apiBaselineURL;

  private String apiBaselineSize;

  private String webLabel;

  private int webPriority;

  private String webRetention;

  private boolean webCollapsed;

  public WebNode(File relativePath, Properties properties)
  {
    this.relativePath = relativePath;
    pathLevel = (int)StreamSupport.stream(relativePath.toPath().spliterator(), false).count();

    apiBaselineURL = properties.getProperty("api.baseline.url");
    apiBaselineSize = properties.getProperty("api.baseline.size", "");

    webLabel = properties.getProperty("web.label", getName());
    webPriority = Integer.parseInt(properties.getProperty("web.priority", "500"));
    webRetention = properties.getProperty("web.retention");
    webCollapsed = Boolean.parseBoolean(properties.getProperty("web.collapsed", "false"));
  }

  public final File getRelativePath()
  {
    return relativePath;
  }

  public final int getPathLevel()
  {
    return pathLevel;
  }

  public final String getName()
  {
    String name = relativePath.getName();
    return name.isEmpty() ? ROOT : name;
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

  public final WebNode getChild(String name)
  {
    for (WebNode child : children)
    {
      if (Objects.equals(child.getName(), name))
      {
        return child;
      }
    }

    return null;
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
    System.out.println(prefix(level) + "Generating HTML for " + (level == 0 ? "https://download.eclipse.org/modeling/emf/cdo/updates/index.html" : getName()));

    if (repository != null)
    {
      List<BuildInfo> buildInfos = null;
      boolean empty = false;
      boolean surrogate = false;

      if (repository instanceof Repository.Drops)
      {
        Repository.Drops drops = (Repository.Drops)repository;
        buildInfos = new ArrayList<>(drops.getBuildInfos());
        empty = buildInfos.isEmpty();
        surrogate = drops.containsSurrogateDrop();
      }

      level = generateRepositoryStart(out, level, empty, surrogate);

      if (buildInfos != null && !empty)
      {
        boolean firstDrop = true;
        for (BuildInfo buildInfo : buildInfos)
        {
          level = generateDrop(out, level, buildInfo, firstDrop);
          firstDrop = false;
        }
      }
    }

    if (!children.isEmpty())
    {
      for (WebNode child : children)
      {
        child.generate(out, level);
      }
    }

    if (repository != null)
    {
      generateRepositoryEnd(out, level);
    }
  }

  protected int generateRepositoryStart(PrintStream out, int level, boolean empty, boolean surrogate)
  {
    String nodeName = getAnchorName();
    String nodeID = "repo_" + nodeName;
    boolean collapsed = !EXPAND_ALL && webCollapsed;

    // Heading
    out.println();
    out.println(prefix(level) + "<div class=\"repo-item\"><a href=\"javascript:toggle('" + nodeID + "')\">" + toggleImage(nodeID, collapsed)
        + "</a> <span class=\"repo-label" + pathLevel + "\">" + webLabel + "</span> <a name=\"" + nodeName + "\" href=\"#" + nodeName
        + "\"><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a></div>");

    out.println(
        prefix(level++) + "<div class=\"repo" + pathLevel + "\" id=\"repo_" + nodeName + "\"" + (collapsed || empty ? " style=\"display: none\"" : "") + ">");

    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    String boldStart = "<b>";
    String boldEnd = "</b>";

    // Latest Update Site
    if (latestRepository != null)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/22x22/package-x-generic.png\"/>&nbsp;&nbsp;<b><a href=\""
          + latestRepository.getURL(false) + "\">Latest&nbsp;Update&nbsp;Site</a></b> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
          + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.</td>" + "<td class=\"file-size level" + pathLevel + "\"></td></tr>");
      boldStart = "";
      boldEnd = "";
    }

    // Composite Update Site
    out.println(prefix(level)
        + "<tr class=\"repo-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/22x22/package-x-generic.png\"/>&nbsp;&nbsp;"
        + boldStart + "<a href=\"" + repository.getURL(false) + "\">Composite&nbsp;Update&nbsp;Site</a>" + boldEnd + " for use with <a href=\""
        + PromoterConfig.INSTANCE.getHelpTopicURL() + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.</td>" + "<td class=\"file-size level"
        + pathLevel + "\"></td></tr>");

    // API Baseline
    if (apiBaselineURL != null)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/22x22/go-down.png\"/>&nbsp;&nbsp;<a href=\""
          + apiBaselineURL + "\">" + new File(apiBaselineURL).getName() + "</a> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
          + "/org.eclipse.pde.doc.user/tasks/api_tooling_baseline.htm\">API Tools</a>.</td>" + "<td class=\"file-size level" + pathLevel + "\"><i>"
          + apiBaselineSize + "</i></td></tr>");
    }

    if (webRetention != null)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/22x22/dialog-information.png\"/>&nbsp;&nbsp;"
          + webRetention + "</td>" + "<td class=\"file-size level" + pathLevel + "\"></td></tr>");
    }

    if (empty)
    {
      out.println(prefix(level)
          + "<tr class=\"repo-info\"><td><img style=\"width:22px;height:22px;vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/empty.gif\"/>&nbsp;&nbsp;<i>Currently this composite update site is empty.<br>"
          + "<img style=\"width:22px;height:22px;vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/empty.gif\"/>&nbsp;&nbsp;This may change in the future when new builds are promoted.</i></td></tr>");
    }
    else if (surrogate)
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"width:22px;height:22px;vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/empty.gif\"/>&nbsp;&nbsp;<i>Currently this composite update site contains a surrogate build.<br>"
          + "<img style=\"width:22px;height:22px;vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/empty.gif\"/>&nbsp;&nbsp;It may disappear in the future when new builds are promoted.</i></td></tr>");
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
    boolean collapsed = !EXPAND_ALL && !firstDrop;

    String webLabel = buildInfo.getWebLabel();
    if (webLabel != null)
    {
      dropLabel = webLabel + " (" + dropLabel + ")";
    }

    out.println(prefix(level) + "<div class=\"repo-item\"><a href=\"javascript:toggle('" + dropID + "')\">" + toggleImage(dropID, collapsed)
        + "</a> <span class=\"drop-label\">" + dropLabel + "</span> <a name=\"" + dropName + "\" href=\"#" + dropName
        + "\"><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/link_obj.gif\" alt=\"Permalink\" width=\"12\" height=\"12\"/></a></div>");

    out.println(prefix(level++) + "<div class=\"drop\" id=\"" + dropID + "\"" + (collapsed ? " style=\"display: none\"" : "") + ">");
    out.println(prefix(level++) + "<table border=\"0\" width=\"100%\">");

    File drop = buildInfo.getDrop();
    String dropURL = buildInfo.getDropURL(null, false);
    int elements = 0;

    // Update Site
    if (IO.isRepository(drop))
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/package-x-generic.png\"/>&nbsp;&nbsp;<b><a href=\""
          + dropURL + "\">Update&nbsp;Site</a></b> for use with <a href=\"" + PromoterConfig.INSTANCE.getHelpTopicURL()
          + "/org.eclipse.platform.doc.user/tasks/tasks-127.htm\">p2</a>.</td>" + "<td class=\"file-size level" + (pathLevel + 1) + "\"></td></tr>");
      ++elements;
    }

    // Release Notes
    if (new File(drop, "relnotes.html").isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/edit-paste.png\"/>&nbsp;&nbsp;<a href=\""
          + dropURL + "/relnotes.html\">Release Notes</a> to see what's in this build.</td><td class=\"file-size level" + (pathLevel + 1) + "\"></td></tr>");
      ++elements;
    }

    // Documentation
    if (new File(new File(drop, "help"), "index.html").isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/help-browser.png\"/>&nbsp;&nbsp;<a href=\""
          + dropURL + "/help/index.html\">Documentation</a> to browse the online help center of this build.</td><td class=\"file-size level" + (pathLevel + 1)
          + "\"></td></tr>");
      ++elements;
    }

    // API Revolution Report
    if (new File(drop, "api.html").isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/api/report.gif\"/>&nbsp;&nbsp;<a href=\""
          + dropURL + "/api.html\">API Evolution Report</a> to see the API changes in this stream.</td><td class=\"file-size level" + (pathLevel + 1)
          + "\"></td></tr>");
      ++elements;
    }

    // Test Report
    if (new File(new File(drop, "tests"), "index.html").isFile())
    {
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/junit.png\"/>&nbsp;&nbsp;<a href=\""
          + dropURL + "/tests/index.html\">Test Report</a> to explore the quality of this build.</td><td class=\"file-size level" + (pathLevel + 1)
          + "\"></td></tr>");
      ++elements;
    }

    // Target Info
    String train = buildInfo.getTrain();
    if (train != null && !train.isEmpty())
    {
      String targetInfo = "<a href=\"https://www.eclipse.org/downloads/packages/release/" + train + "\">" + train.substring(0, 1).toUpperCase()
          + train.substring(1).toLowerCase() + "</a> is the target of this build.";

      String eclipse = buildInfo.getEclipse();
      String emf = buildInfo.getEMF();

      int minorVersion = TRAINS_4_X.indexOf(train);
      if (minorVersion != -1)
      {
        eclipse = "4." + minorVersion;
      }
      else
      {
        minorVersion = TRAINS_3_X.indexOf(train);
        if (minorVersion != -1)
        {
          eclipse = "3." + minorVersion;
          emf = "2." + minorVersion;
        }
      }

      String targetVersions = "";
      if (eclipse != null && !eclipse.isEmpty())
      {
        targetVersions += "Eclipse&nbsp;" + eclipse;
      }

      if (emf != null && !emf.isEmpty())
      {
        if (!targetVersions.isEmpty())
        {
          targetVersions += " + ";
        }

        targetVersions += "EMF&nbsp;" + emf;
      }

      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/dialog-information.png\"/>&nbsp;&nbsp;"
          + targetInfo + "</td>" + "<td class=\"file-size level" + (pathLevel + 1) + "\"><i>" + targetVersions + "</i></td></tr>");
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
    out.println(prefix(level) + "<tr class=\"drop-info\"><td colspan=\"2\"><hr class=\"drop-separator\"></td></tr>");
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

        out.println(prefix(level)
            + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/text-x-generic.png\"/>&nbsp;&nbsp;<a href=\""
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
      out.println(prefix(level)
          + "<tr class=\"drop-info\"><td><img style=\"vertical-align:middle\" src=\"https://eclipse.dev/cdo/images/16x16/go-down.png\"/>&nbsp;&nbsp;<a href=\""
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

  private static String toggleImage(String elementId, boolean expand)
  {
    return "<img id=\"img_" + elementId + "\" src=\"https://eclipse.dev/cdo/images/" + (expand ? "expand" : "collapse") + ".gif\"/>";
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
      int result = Integer.valueOf(o.getPriority()).compareTo(priority);
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
