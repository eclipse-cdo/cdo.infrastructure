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

import promoter.SourceCodeManager.LogEntry;
import promoter.SourceCodeManager.LogEntryHandler;
import promoter.util.IO;
import promoter.util.XMLOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Eike Stepper
 */
public class ReleaseNotesGenerator extends PromoterComponent
{
  private SourceCodeManager scm;

  private IssueManager issueManager;

  public ReleaseNotesGenerator()
  {
  }

  public synchronized void generateReleaseNotes(List<BuildInfo> buildInfos)
  {
    scm = getPromoter().createSourceCodeManager();
    issueManager = getPromoter().createIssueManager();

    for (ReleaseNotesStream stream : getStreams(buildInfos))
    {
      generateReleaseNotes(stream);
    }

    issueManager = null;
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

    File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), qualifier);
    File relnotesXML = new File(drop, "relnotes.xml");
    File relnotesHTML = new File(drop, "relnotes.html");
    // if (!relnotesXML.exists() || !relnotesHTML.exists()) // XXX
    {
      System.out.println();
      System.out.println("Generating release notes for " + qualifier);

      BuildInfo previousBuildInfo = getPreviousBuildInfo(buildInfos, i);
      String fromRevision = previousBuildInfo == null ? stream.getFirstRevision() : previousBuildInfo.getRevision();
      String toRevision = buildInfo.getRevision();

      List<Issue> issues = new ArrayList<Issue>(getIssues(buildInfo, fromRevision, toRevision));
      sortIssues(issues);

      generateReleaseNotesXML(buildInfo, previousBuildInfo, fromRevision, toRevision, issues, relnotesXML);
      generateReleaseNotesHTML(buildInfo, previousBuildInfo, fromRevision, toRevision, issues, relnotesHTML);
    }
  }

  protected void generateReleaseNotesXML(BuildInfo buildInfo, BuildInfo previousBuildInfo, String fromRevision,
      String toRevision, List<Issue> issues, File relnotesXML)
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
        xml.attribute("url", issueManager.getURL(issue));
        xml.attribute("id", issue.getID());
        xml.attribute("title", issue.getTitle());
        xml.attribute("severity", issue.getSeverity());
        xml.attribute("component", issue.getComponent());
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

  protected void generateReleaseNotesHTML(BuildInfo buildInfo, BuildInfo previousBuildInfo, String fromRevision,
      String toRevision, List<Issue> issues, File relnotesHTML)
  {
    PrintStream out = null;

    try
    {
      List<IssueComponent> components = new ArrayList<IssueComponent>();
      addIssueComponent(components, "cdo.core", "CDO Model Repository (Core)");
      addIssueComponent(components, "cdo.legacy", "CDO Model Repository (Legacy Mode)");
      addIssueComponent(components, "cdo.ui", "CDO Model Repository (User Interface)");
      addIssueComponent(components, "cdo.db", "CDO Model Repository (JDB Support)");
      addIssueComponent(components, "cdo.hibernate", "CDO Model Repository (Hibernate Support)");
      addIssueComponent(components, "cdo.objy", "CDO Model Repository (Objectivity Support)");
      addIssueComponent(components, "cdo.dawn", "CDO Dawn");
      addIssueComponent(components, "cdo.net4j", "Net4j Signalling Platform and Utilities");
      addIssueComponent(components, "cdo.net4j.ui", "Net4j User Interface");
      addIssueComponent(components, "cdo.net4j.db", "Net4j DB Framework");
      addIssueComponent(components, "cdo.docs", "Documentation");
      addIssueComponent(components, "cdo.releng", "Release Engineering");
      IssueComponent other = addIssueComponent(components, "", "Other");

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

      out = new PrintStream(relnotesHTML);

      String qualifier = buildInfo.getQualifier();
      String title = "Release Notes for CDO " + qualifier + " (" + buildInfo.getStream() + ")";
      String branch = buildInfo.getBranch();
      String branchURL = "http://git.eclipse.org/c/cdo/cdo.git/?h=" + branch.replaceAll("/", "%2F");

      out.println("<html>");
      out.println("<head>");
      out.println("<title>" + title + "</title>");
      out.println("</head>");
      out.println();
      out.println("<body>");
      out.println("<<basefont face=\"arial\">");
      out.println("<h1>" + title + "</h1>");

      out.println("<p>");
      out.println("These release notes have been generated from commits to the <a href=\"" + branchURL + "\">" + branch
          + "</a> branch.");
      out.println("<br/>The relevant commits are between " + fromRevision + " and " + toRevision + ".");

      if (previousBuildInfo != null)
      {
        String q = previousBuildInfo.getQualifier();
        out.println("<br/>The previous build of the " + buildInfo.getStream()
            + " stream is <a href=\"http://www.eclipse.org/cdo/downloads/#" + q.replace('-', '_') + "\">" + q + "</a>.");
      }
      else
      {
        out.println("<br/>This is the first build of the " + buildInfo.getStream() + " stream.");
      }

      out.println("</p>");

      out.println("<p>");
      out.print("&nbsp;&nbsp;&nbsp;&nbsp;<img src=\"http://www.eclipse.org/cdo/images/16x16/go-down.png\">&nbsp;");
      out.println("<a href=\"http://www.eclipse.org/cdo/downloads/#" + qualifier.replace('-', '_')
          + "\">Downloads for " + qualifier + "</a>");
      out.println("</p>");

      for (IssueComponent component : components)
      {
        component.renderHTML(out);
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
      if (component.getName().equals(name))
      {
        return component;
      }
    }

    return null;
  }

  protected Collection<ReleaseNotesStream> getStreams(List<BuildInfo> buildInfos)
  {
    Map<String, ReleaseNotesStream> streams = new HashMap<String, ReleaseNotesStream>();
    for (BuildInfo buildInfo : buildInfos)
    {
      String name = buildInfo.getStream();

      ReleaseNotesStream stream = streams.get(name);
      if (stream == null)
      {
        stream = new ReleaseNotesStream(name);
        streams.put(name, stream);
      }

      stream.getBuildInfos().add(buildInfo);
    }

    return streams.values();
  }

  protected BuildInfo[] getBuildInfos(ReleaseNotesStream stream)
  {
    List<BuildInfo> buildInfos = stream.getBuildInfos();
    Collections.sort(buildInfos/*
                                * , new Comparator<BuildInfo>() { public int compare(BuildInfo bi1, BuildInfo bi2) {
                                * return new Integer(bi1.getRevision()).compareTo(new Integer(bi2.getRevision())); } }
                                */);

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
    String branch = buildInfo.getBranch();

    final Set<Issue> issues = new HashSet<Issue>();
    scm.handleLogEntries(branch, fromRevision, toRevision, false, new LogEntryHandler()
    {
      public void handleLogEntry(LogEntry logEntry)
      {
        String message = logEntry.getMessage();
        String id = issueManager.parseID(message);
        if (id != null && id.length() != 0)
        {
          Issue issue = issueManager.getIssue(id);
          if (issue != null)
          {
            if (issues.add(issue))
            {
              System.out.println("   " + issue.getID() + ": " + issue.getTitle() + " --> " + issue.getSeverity());
            }
          }
        }
      }
    });

    return issues;
  }

  protected void sortIssues(List<Issue> issues)
  {
    Collections.sort(issues, issueManager);
  }

  /**
   * @author Eike Stepper
   */
  private final class IssueComponent
  {
    private final String name;

    private final String label;

    private final List<Issue> enhancements = new ArrayList<Issue>();

    private final List<Issue> fixes = new ArrayList<Issue>();

    public IssueComponent(String name, String label)
    {
      this.name = name;
      this.label = label;
    }

    public String getName()
    {
      return name;
    }

    public void addIssue(Issue issue)
    {
      if (issueManager.getSeverity(issue) == 0)
      {
        enhancements.add(issue);
      }
      else
      {
        fixes.add(issue);
      }
    }

    public void renderHTML(PrintStream out)
    {
      if (enhancements.isEmpty() && fixes.isEmpty())
      {
        return;
      }

      out.println("<a name=\"" + name + "\"></a>");
      out.println("<h2>" + label + "</h2>");

      if (!enhancements.isEmpty())
      {
        out.println("<h3>Enhancements</h3>");
        renderHTML(out, enhancements);
      }

      if (!fixes.isEmpty())
      {
        Collections.sort(fixes, new Comparator<Issue>()
        {
          public int compare(Issue i1, Issue i2)
          {
            Integer s1 = issueManager.getSeverity(i1);
            Integer s2 = issueManager.getSeverity(i2);
            return -s1.compareTo(s2);
          }
        });

        out.println("<h3>Bug Fixes</h3>");
        renderHTML(out, fixes);
      }
    }

    private void renderHTML(PrintStream out, List<Issue> issues)
    {
      for (Issue issue : issues)
      {
        String severity = issue.getSeverity();
        String url = issueManager.getURL(issue);
        String title = issue.getTitle().replaceAll("<", "&lt;").replaceAll("\"", "&quot;");

        out.print("&nbsp;&nbsp;&nbsp;&nbsp;");
        out.print("<img=\"" + severity + ".gif\" alt=\"" + severity + "\">&nbsp;");
        out.print("[<a href=\"" + url + "\">" + issue.getID() + "</a>]&nbsp;" + title);
        out.println("<br/>");
      }
    }
  }
}
