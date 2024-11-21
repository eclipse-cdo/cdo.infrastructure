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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import promoter.util.IO;
import promoter.util.XMLOutput;

/**
 * @author Eike Stepper
 */
public class ReleaseNotesGenerator extends PromoterComponent
{
  private SourceCodeManager scm;

  private List<IssueManager<?>> issueManagers;

  public ReleaseNotesGenerator()
  {
  }

  public synchronized void generateReleaseNotes(List<BuildInfo> buildInfos)
  {
    scm = getPromoter().getSourceCodeManager();
    issueManagers = getPromoter().createIssueManagers();

    for (ReleaseNotesStream stream : getStreams(buildInfos))
    {
      if (!stream.isDisabled())
      {
        generateReleaseNotes(stream);
      }
    }

    issueManagers = null;
    scm = null;
  }

  protected void generateReleaseNotes(ReleaseNotesStream stream)
  {
    BuildInfo[] buildInfos = getBuildInfos(stream);
    for (int i = 0; i < buildInfos.length; i++)
    {
      generateReleaseNotes(stream, buildInfos, i);
    }
  }

  protected void generateReleaseNotes(ReleaseNotesStream stream, BuildInfo[] buildInfos, int i)
  {
    BuildInfo buildInfo = buildInfos[i];
    String qualifier = buildInfo.getQualifier();

    File drop = buildInfo.getDrop();
    File relnotesXML = new File(drop, "relnotes.xml");
    File relnotesHTML = new File(drop, "relnotes.html");
    if (!relnotesXML.exists() || !relnotesHTML.exists())
    {
      System.out.println();
      System.out.println("Generating release notes for " + qualifier);

      BuildInfo previousBuildInfo = getPreviousBuildInfo(buildInfos, i);
      String fromRevision = previousBuildInfo == null ? stream.getFirstRevision() : previousBuildInfo.getRelnotesRevision();
      String toRevision = buildInfo.getRevision();

      List<Issue> issues = new ArrayList<>(getIssues(buildInfo, fromRevision, toRevision));
      sortIssues(issues);

      generateReleaseNotesXML(buildInfo, previousBuildInfo, fromRevision, toRevision, issues, relnotesXML);
      generateReleaseNotesHTML(buildInfo, previousBuildInfo, fromRevision, toRevision, issues, relnotesHTML);
    }
  }

  protected void generateReleaseNotesXML(BuildInfo buildInfo, BuildInfo previousBuildInfo, String fromRevision, String toRevision, List<Issue> issues,
      File relnotesXML)
  {
    OutputStream out = null;

    try
    {
      out = new FileOutputStream(relnotesXML);
      XMLOutput xml = new XMLOutput(out);

      xml.element("relnotes");
      xml.attribute("stream", buildInfo.getStream());
      xml.attribute("drop", buildInfo.getQualifier());
      xml.attribute("revision", toRevision);
      if (previousBuildInfo != null)
      {
        xml.attribute("previousDrop", previousBuildInfo.getQualifier());
      }

      xml.attribute("previousRevision", fromRevision);
      xml.push();

      for (Issue issue : issues)
      {
        xml.element("issue");
        xml.attribute("url", issue.getURL());
        xml.attribute("id", issue.getID());
        xml.attribute("label", issue.getLabel());
        xml.attribute("type", issue.getType());
        xml.attribute("title", issue.getTitle());
        xml.attribute("severity", issue.getSeverity());
        xml.attribute("component", issue.getComponent());
        xml.attribute("version", issue.getVersion());
      }

      xml.pop();
      xml.done();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }
  }

  protected void generateReleaseNotesHTML(BuildInfo buildInfo, BuildInfo previousBuildInfo, String fromRevision, String toRevision, List<Issue> issues,
      File relnotesHTML)
  {
    PrintStream out = null;

    try
    {
      List<IssueComponent> components = new ArrayList<>();
      addIssueComponent(components, "cdo.core", "CDO Model Repository (Core)");
      addIssueComponent(components, "cdo.legacy", "CDO Model Repository (Legacy Mode)");
      addIssueComponent(components, "cdo.ui", "CDO Model Repository (User Interface)");
      addIssueComponent(components, "cdo.db", "CDO Model Repository (JDBC Support)");
      addIssueComponent(components, "cdo.dawn", "CDO Dawn");
      addIssueComponent(components, "cdo.net4j", "Net4j Signalling Platform and Utilities");
      addIssueComponent(components, "cdo.net4j.ui", "Net4j User Interface");
      addIssueComponent(components, "cdo.net4j.db", "Net4j DB Framework");
      addIssueComponent(components, "cdo.docs", "Documentation");
      addIssueComponent(components, "cdo.releng", "Release Engineering");
      IssueComponent other = addIssueComponent(components, "other", "Other");

      for (Issue issue : issues)
      {
        String name = issue.getComponent();
        IssueComponent component = getIssueComponent(components, name);
        if (component == null)
        {
          component = other;
        }

        component.addIssue(issue);
      }

      for (Iterator<IssueComponent> it = components.iterator(); it.hasNext();)
      {
        IssueComponent component = it.next();
        if (component.isEmpty())
        {
          it.remove();
        }
      }

      out = new PrintStream(relnotesHTML);

      String qualifier = buildInfo.getQualifier();
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("  <title>Release Notes for CDO " + qualifier + "</title>");
      out.println("</head>");
      out.println();
      out.println("<body style=\"font-family:Arial; font-size:small;\">");
      out.println(
          "<h1>Release Notes for CDO <a href=\"https://www.eclipse.org/cdo/downloads/#" + qualifier.replace('-', '_') + "\">" + qualifier + "</a></h1>");

      out.println("<p>");
      out.println("These release notes have been generated from the commit log of the <a href=\"https://www.eclipse.org/cdo/downloads/#releases_"
          + buildInfo.getStream().replace('.', '_') + "\">" + buildInfo.getStream() + "</a> stream and the associated "
          + "<a href=\"https://github.com/eclipse-cdo/cdo/issues\">issues</a> and "
          + "<a href=\"https://github.com/eclipse-cdo/cdo/pulls\">pull requests</a>.");
      out.println("<br/>The first commit is " + fromRevision + " in the " + branchLink(previousBuildInfo) + " branch.");
      out.println("<br/>The last commit is " + toRevision + " in the " + branchLink(buildInfo) + " branch.");

      previousBuildNote(out, buildInfo, previousBuildInfo);
      out.println("</p>");

      if (!components.isEmpty())
      {
        out.println("<h3>Table of Contents</h3>");
        out.println("<ul>");
        for (IssueComponent component : components)
        {
          component.renderTOC(out);
        }

        out.println("</ul>");

        for (IssueComponent component : components)
        {
          component.renderHTML(out);
        }

        previousBuildNote(out, buildInfo, previousBuildInfo);
      }
      else
      {
        out.println("<h3>This build does not contain any tracked enhancements or bug fixes.</h3>");
        out.println("It may contain other changes, though.");
      }

      out.println("</body>");
      out.println("</html>");
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }
  }

  protected void previousBuildNote(PrintStream out, BuildInfo buildInfo, BuildInfo previousBuildInfo)
  {
    out.print("<br/>");
    if (previousBuildInfo != null)
    {
      String q = previousBuildInfo.getQualifier();
      out.println("The previous build of the " + buildInfo.getStream() + " stream is <a href=\"https://www.eclipse.org/cdo/downloads/#" + q.replace('-', '_')
          + "\">" + q + "</a>.");
    }
    else
    {
      out.println("This is the first build of the " + buildInfo.getStream() + " stream.");
    }
  }

  protected IssueComponent addIssueComponent(List<IssueComponent> components, String name, String label)
  {
    IssueComponent component = new IssueComponent(name, label);
    components.add(component);
    return component;
  }

  protected IssueComponent getIssueComponent(List<IssueComponent> components, String name)
  {
    for (IssueComponent component : components)
    {
      String componentName = component.getName();
      if (componentName.equalsIgnoreCase(name))
      {
        return component;
      }
    }

    return null;
  }

  protected Collection<ReleaseNotesStream> getStreams(List<BuildInfo> buildInfos)
  {
    Map<String, ReleaseNotesStream> streams = new LinkedHashMap<>();
    for (BuildInfo buildInfo : buildInfos)
    {
      String name = buildInfo.getStream();
      if (name != null && name.length() != 0)
      {
        ReleaseNotesStream stream = streams.get(name);
        if (stream == null)
        {
          stream = new ReleaseNotesStream(name);
          streams.put(name, stream);
        }

        stream.getBuildInfos().add(buildInfo);
      }
    }

    return streams.values();
  }

  protected BuildInfo[] getBuildInfos(ReleaseNotesStream stream)
  {
    List<BuildInfo> buildInfos = stream.getBuildInfos();
    Collections.sort(buildInfos, (bi1, bi2) -> bi1.getTimestamp().compareTo(bi2.getTimestamp()));

    return buildInfos.toArray(new BuildInfo[buildInfos.size()]);
  }

  protected BuildInfo getPreviousBuildInfo(BuildInfo[] buildInfos, int current)
  {
    String currentBuildType = buildInfos[current].getType();
    String previousBuildTypes = getPreviousBuildTypes(currentBuildType);

    for (int i = current - 1; i >= 0; --i)
    {
      BuildInfo previousBuildInfo = buildInfos[i];
      String previousBuildType = previousBuildInfo.getType();
      if (previousBuildTypes.contains(previousBuildType))
      {
        return previousBuildInfo;
      }
    }

    // Use stream start
    return null;
  }

  protected String getPreviousBuildTypes(String currentBuildType)
  {
    if ("N".equals(currentBuildType))
    {
      return "NIMSR";
    }

    if ("I".equals(currentBuildType) || "M".equals(currentBuildType))
    {
      return "IMSR";
    }

    if ("S".equals(currentBuildType))
    {
      return "SR";
    }

    if ("R".equals(currentBuildType))
    {
      return "R";
    }

    throw new RuntimeException("Unrecognized build type: " + currentBuildType);
  }

  protected Set<Issue> getIssues(BuildInfo buildInfo, String fromRevision, String toRevision)
  {
    Set<Issue> issues = new LinkedHashSet<>();

    if (!fromRevision.equals(toRevision))
    {
      String branch = buildInfo.getBranch();

      scm.getCommits(branch, fromRevision, toRevision, (revision, message) -> {
        for (IssueManager<?> issueManager : issueManagers)
        {
          issueManager.getCommitIssues(revision, message, issue -> {
            if (issues.add(issue))
            {
              System.out.println("   Found " + issue.getType() + " issue " + issue.getLabel() + ": " + issue.getTitle());
            }
          });
        }
      });
    }

    return issues;
  }

  protected void sortIssues(List<Issue> issues)
  {
    Collections.sort(issues, (issue1, issue2) -> {
      int managerIndex1 = issueManagers.indexOf(issue1.getManager());
      int managerIndex2 = issueManagers.indexOf(issue2.getManager());

      int result = Integer.compare(managerIndex1, managerIndex2);
      if (result == 0)
      {
        result = issue1.getID().compareTo(issue2.getID());
      }

      return result;
    });
  }

  private static String branchLink(BuildInfo buildInfo)
  {
    String branch = buildInfo == null ? "master" : buildInfo.getBranch();
    return "<a href=\"" + branchHref(branch) + "\">" + branch + "</a>";
  }

  private static String branchHref(String branch)
  {
    if (branch == null || branch.equals("master"))
    {
      return "https://github.com/eclipse-cdo/cdo";
    }

    return "https://github.com/eclipse-cdo/cdo/tree/" + branch;
  }

  /**
   * @author Eike Stepper
   */
  private final class IssueComponent
  {
    private final String name;

    private final String label;

    private final List<Issue> enhancements = new ArrayList<>();

    private final List<Issue> fixes = new ArrayList<>();

    public IssueComponent(String name, String label)
    {
      this.name = name;
      this.label = label;
    }

    public String getName()
    {
      return name;
    }

    public boolean isEmpty()
    {
      return enhancements.isEmpty() && fixes.isEmpty();
    }

    public void addIssue(Issue issue)
    {
      (issue.isEnhancement() ? enhancements : fixes).add(issue);
    }

    public void renderTOC(PrintStream out)
    {
      if (isEmpty())
      {
        return;
      }

      out.println("<a name=\"toc." + name + "\"></a>");
      out.println("<li><a href=\"#" + name + "\">" + label + "</a>");
    }

    public void renderHTML(PrintStream out)
    {
      if (isEmpty())
      {
        return;
      }

      out.println("<a name=\"" + name + "\"></a>");
      out.println("<h2>" + label + "</h2>");
      out.println("<div style=\"margin-left:20px;\">");

      if (!enhancements.isEmpty())
      {
        out.println("<h3>Enhancements</h3>");
        out.println("<div style=\"margin-left:20px;\">");
        renderHTML(out, enhancements);
        out.println("</div>");
      }

      if (!fixes.isEmpty())
      {
        fixes.sort(null);

        out.println("<h3>Bug Fixes</h3>");
        out.println("<div style=\"margin-left:20px;\">");
        renderHTML(out, fixes);
        out.println("</div>");
      }

      out.println("</div>");
    }

    private void renderHTML(PrintStream out, List<Issue> issues)
    {
      for (Issue issue : issues)
      {
        String severity = issue.getSeverity();
        String url = issue.getURL();
        String title = issue.getTitle().replaceAll("<", "&lt;").replaceAll("\"", "&quot;");

        out.print("<img src=\"../../images/" + severity + ".gif\" alt=\"" + severity + "\">&nbsp;");
        out.print("[<a href=\"" + url + "\">" + issue.getLabel() + "</a>]&nbsp;" + title);
        out.print("&nbsp;&nbsp;&nbsp;&nbsp;<font color=\"#aaaaaa\"><i>" + issue.getStatus().toLowerCase() + " in " + issue.getVersion() + "</i></font>");
        out.println("<br/>");
      }
    }
  }
}
