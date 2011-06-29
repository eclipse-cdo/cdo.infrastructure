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

import promoter.issues.IssueManager;
import promoter.issues.IssueManager.Issue;
import promoter.scm.SCM;
import promoter.scm.SCM.LogEntry;
import promoter.scm.SCM.LogEntryHandler;
import promoter.util.Config;
import promoter.util.IO;
import promoter.util.XMLOutput;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Eike Stepper
 */
public class ReleaseNotesGenerator
{
  private Promoter promoter;

  private SCM scm;

  private IssueManager issueManager;

  public ReleaseNotesGenerator()
  {
  }

  public final Promoter getPromoter()
  {
    return promoter;
  }

  void setPromoter(Promoter promoter)
  {
    this.promoter = promoter;
    scm = promoter.createSCM();
    issueManager = promoter.createIssueManager();
  }

  public final SCM getSCM()
  {
    return scm;
  }

  public final IssueManager getIssueManager()
  {
    return issueManager;
  }

  public void generateReleaseNotes(List<BuildInfo> buildInfos)
  {
    for (Stream stream : getStreams(buildInfos))
    {
      generateReleaseNotes(stream);
    }
  }

  protected void generateReleaseNotes(Stream stream)
  {
    List<BuildInfo> buildInfos = stream.getBuildInfos();
    Collections.sort(buildInfos, new Comparator<BuildInfo>()
    {
      public int compare(BuildInfo bi1, BuildInfo bi2)
      {
        return new Integer(bi1.getRevision()).compareTo(new Integer(bi2.getRevision()));
      }
    });

    String fromRevision = stream.getFirstRevision();
    for (BuildInfo buildInfo : buildInfos)
    {
      String toRevision = buildInfo.getRevision();
      generateReleaseNotes(buildInfo, fromRevision, toRevision);
      fromRevision = toRevision + 1;
    }
  }

  protected void generateReleaseNotes(BuildInfo buildInfo, String fromRevision, String toRevision)
  {
    File drop = new File(PromoterConfig.INSTANCE.getDropsArea(), buildInfo.getQualifier());
    File relnotes = new File(drop, "relnotes.xml");
    if (!relnotes.exists())
    {
      System.out.println();
      System.out.println("Generating " + relnotes);

      OutputStream out = null;

      try
      {
        out = new FileOutputStream(relnotes);
        XMLOutput xml = new XMLOutput(out);

        xml.element("relnotes");
        xml.attribute("drop", buildInfo.getQualifier());
        xml.attribute("from", fromRevision);
        xml.attribute("to", toRevision);
        xml.push();

        List<Issue> issues = getIssues(buildInfo, fromRevision, toRevision);
        sortIssues(issues);

        for (Issue issue : issues)
        {
          System.out.println(" Adding " + issue);

          xml.element("issue");
          xml.attribute("id", issue.getID());
          xml.attribute("title", issue.getTitle());
          xml.attribute("url", issueManager.getURL(issue));
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
  }

  protected Collection<Stream> getStreams(List<BuildInfo> buildInfos)
  {
    Map<String, Stream> streams = new HashMap<String, Stream>();
    for (BuildInfo buildInfo : buildInfos)
    {
      String name = buildInfo.getStream();

      Stream stream = streams.get(name);
      if (stream == null)
      {
        stream = new Stream(name);
        streams.put(name, stream);
      }

      stream.getBuildInfos().add(buildInfo);
    }

    return streams.values();
  }

  protected List<Issue> getIssues(BuildInfo buildInfo, String fromRevision, String toRevision)
  {
    final List<Issue> issues = new ArrayList<Issue>();
    scm.handleLogEntries(buildInfo.getBranch(), fromRevision, toRevision, false, new LogEntryHandler()
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
            issues.add(issue);
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
  public static class Stream
  {
    private String name;

    private String firstRevision;

    private List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();

    public Stream(String name)
    {
      this.name = name;

      Properties streamProperties = Config.loadProperties(new File("streams", name + ".properties"), true);
      firstRevision = streamProperties.getProperty("first.revision");
      if (firstRevision == null)
      {
        throw new IllegalStateException("First revision of stream " + name + "is not specified");
      }
    }

    public final String getName()
    {
      return name;
    }

    public String getFirstRevision()
    {
      return firstRevision;
    }

    public final List<BuildInfo> getBuildInfos()
    {
      return buildInfos;
    }
  }
}
