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

import promoter.util.IO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Eike Stepper
 */
public class Bugzilla extends IssueManager
{
  public static final String SERVER = "https://bugs.eclipse.org/";

  public static final String XML = SERVER + "bugs/show_bug.cgi?ctype=xml&id=";

  private static final int RETRIES = 3;

  public Bugzilla()
  {
  }

  @Override
  public String parseID(String message)
  {
    if (message.length() >= 3 && message.charAt(0) == '[')
    {
      int end = message.indexOf(']');
      if (end != -1)
      {
        String id = message.substring(1, end);

        try
        {
          Integer.parseInt(id); // Valid integer?
          return id;
        }
        catch (NumberFormatException ex)
        {
          //$FALL-THROUGH$
        }
      }
    }

    return "";
  }

  @Override
  protected Issue doGetIssue(String id)
  {
    for (int i = 0; i < RETRIES; i++)
    {
      try
      {
        BugHandler handler = new BugHandler();
        IO.readURL(XML + id, handler);
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
  public String getURL(Issue issue)
  {
    return SERVER + issue.getID();
  }

  @Override
  public Integer getSeverity(Issue issue)
  {
    String severity = issue.getSeverity();
    if ("trivial".equals(severity))
    {
      return 1;
    }

    if ("minor".equals(severity))
    {
      return 2;
    }

    if ("normal".equals(severity))
    {
      return 3;
    }

    if ("major".equals(severity))
    {
      return 4;
    }

    if ("critical".equals(severity))
    {
      return 5;
    }

    if ("blocker".equals(severity))
    {
      return 6;
    }

    return 0;
  }

  public int compare(Issue i1, Issue i2)
  {
    return new Integer(i1.getID()).compareTo(new Integer(i2.getID()));
  }

  public static void main(String[] args)
  {
    Issue issue = new Bugzilla().doGetIssue("355921");
    System.out.println(issue.getTitle());
    System.out.println(issue.getSeverity());
  }

  /**
   * @author Eike Stepper
   */
  private static final class BugHandler implements IO.InputHandler
  {
    private Issue issue;

    public BugHandler()
    {
    }

    public final Issue getIssue()
    {
      return issue;
    }

    public void handleInput(InputStream in) throws IOException
    {
      String id = null;
      String title = null;
      String severity = null;
      String component = null;
      String version = null;

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

        if (id != null && title != null && severity != null && component != null && version != null)
        {
          issue = new Issue(id, title, severity, component, version);
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
  }
}
