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

import promoter.SourceCodeManager.LogEntry;
import promoter.SourceCodeManager.LogEntryHandler;
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
      fromRevision = scm.getNextRevision(toRevision);
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

        List<Issue> issues = new ArrayList<Issue>(getIssues(buildInfo, fromRevision, toRevision));
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
}
