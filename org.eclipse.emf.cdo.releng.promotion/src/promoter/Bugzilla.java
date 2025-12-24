/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import promoter.util.IO;

/**
 * @author Eike Stepper
 */
public class Bugzilla extends IssueManager<Object>
{
  public static final String SERVER = "https://bugs.eclipse.org/";

  public static final String XML = SERVER + "bugs/show_bug.cgi?ctype=xml&id=";

  private static final int RETRIES = 3;

  private static final Integer SEVERITY_ENHANCEMENT = 0;

  private static final Integer SEVERITY_TRIVIAL = 1;

  private static final Integer SEVERITY_MINOR = 2;

  private static final Integer SEVERITY_NORMAL = 3;

  private static final Integer SEVERITY_MAJOR = 4;

  private static final Integer SEVERITY_CRITICAL = 5;

  private static final Integer SEVERITY_BLOCKER = 6;

  public Bugzilla()
  {
  }

  @Override
  protected void getIssueIDs(String commitID, String commitMessage, IssueIDConsumer<Object> issueIDConsumer)
  {
    if (commitMessage.length() >= 3 && commitMessage.charAt(0) == '[')
    {
      int end = commitMessage.indexOf(']');
      if (end != -1)
      {
        String id = commitMessage.substring(1, end);

        try
        {
          Integer.parseInt(id); // Valid integer?
          issueIDConsumer.accept(id, null);
        }
        catch (NumberFormatException ex)
        {
          //$FALL-THROUGH$
        }
      }
    }
  }

  @Override
  protected Issue createIssue(String issueID, Object issueInfo)
  {
    for (int i = 0; i < RETRIES; i++)
    {
      try
      {
        BugHandler handler = new BugHandler();
        IO.readURL(XML + issueID, handler);
        return handler.getIssue();
      }
      catch (Exception ex)
      {
        if (i == RETRIES - 1)
        {
          throw new RuntimeException(ex);
        }
      }
    }

    return null;
  }

  @Override
  public int compare(Issue i1, Issue i2)
  {
    return Integer.valueOf(i1.getID()).compareTo(Integer.valueOf(i2.getID()));
  }

  /**
   * @author Eike Stepper
   */
  private final class BugHandler implements IO.InputHandler
  {
    private Issue issue;

    public BugHandler()
    {
    }

    public final Issue getIssue()
    {
      return issue;
    }

    @Override
    public void handleInput(InputStream in) throws IOException
    {
      String id = null;
      String title = null;
      String severity = null;
      String component = null;
      String version = null;
      String status = null;
      String resolution = null;

      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;
      while ((line = reader.readLine()) != null)
      {
        line = line.trim();

        if (id == null)
        {
          id = match(line, "bug_id");
        }

        if (title == null)
        {
          title = match(line, "short_desc");
        }

        if (severity == null)
        {
          severity = match(line, "bug_severity");
        }

        if (component == null)
        {
          component = match(line, "component");
        }

        if (version == null)
        {
          version = match(line, "version");
        }

        if (status == null)
        {
          status = match(line, "bug_status");
        }

        if (resolution == null)
        {
          if ("<resolution/>".equals(line))
          {
            resolution = "";
          }
          else
          {
            resolution = match(line, "resolution");
          }
        }

        if (id != null && title != null && severity != null && component != null && version != null && status != null && resolution != null)
        {
          if (resolution.length() != 0)
          {
            status += "-" + resolution.toLowerCase();
          }

          String url = SERVER + id;
          Integer severityIndex = getSeverityIndex(severity);
          boolean enhancement = severityIndex == SEVERITY_ENHANCEMENT;

          issue = new Issue(Bugzilla.this, url, id, null, title, enhancement, severity, severityIndex, component, version, status);
          break;
        }
      }
    }

    private String match(String line, String element)
    {
      String start = "<" + element + ">";
      if (line.startsWith(start))
      {
        line = line.substring(start.length());

        String end = "</" + element + ">";
        if (line.endsWith(end))
        {
          return line.substring(0, line.length() - end.length());
        }
      }

      return null;
    }

    private Integer getSeverityIndex(String severity)
    {
      if ("trivial".equals(severity))
      {
        return SEVERITY_TRIVIAL;
      }

      if ("minor".equals(severity))
      {
        return SEVERITY_MINOR;
      }

      if ("normal".equals(severity))
      {
        return SEVERITY_NORMAL;
      }

      if ("major".equals(severity))
      {
        return SEVERITY_MAJOR;
      }

      if ("critical".equals(severity))
      {
        return SEVERITY_CRITICAL;
      }

      if ("blocker".equals(severity))
      {
        return SEVERITY_BLOCKER;
      }

      return SEVERITY_ENHANCEMENT;
    }
  }
}
