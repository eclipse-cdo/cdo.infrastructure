/*
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

import promoter.util.IO;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eike Stepper
 */
public class Bugzilla extends IssueManager
{
  public static final String SERVER = "https://bugs.eclipse.org/";

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
        IO.readURL(SERVER + id, handler);

        String title = handler.getTitle();
        String severity = handler.getSeverity();
        if (severity == null)
        {
          severity = "";
        }

        if (title != null)
        {
          return new Issue(id, title, severity);
        }
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

  public int compare(Issue i1, Issue i2)
  {
    return new Integer(i1.getID()).compareTo(new Integer(i2.getID()));
  }

  public static void main(String[] args)
  {
    BugHandler handler = new BugHandler();
    IO.readFile(new File("bug.html"), handler);

    String title = handler.getTitle();
    System.out.println(title);

    String severity = handler.getSeverity();
    System.out.println(severity);
  }

  /**
   * @author Eike Stepper
   */
  private static final class BugHandler implements IO.InputHandler
  {
    private static final Pattern TITLE_PATTERN = Pattern.compile("\\s*<title>Bug ([0-9]*) &ndash; (.*)</title>");

    private static final Pattern SEVERITY_PATTERN = Pattern.compile("<option value=\"(.+?)\"");

    private String title;

    private String severity;

    public BugHandler()
    {
    }

    public final String getTitle()
    {
      return title;
    }

    public final String getSeverity()
    {
      return severity;
    }

    public void handleInput(InputStream in) throws IOException
    {
      boolean inSeverity = false;
      String option = null;

      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;
      while ((line = reader.readLine()) != null)
      {
        if (title == null)
        {
          Matcher matcher = TITLE_PATTERN.matcher(line);
          if (matcher.matches())
          {
            title = matcher.group(2);
          }
        }

        if (severity == null)
        {
          line = line.trim();
          if (line.startsWith("<select id=\"bug_severity\""))
          {
            inSeverity = true;
          }
          else if (inSeverity)
          {
            if (option != null)
            {
              if (line.startsWith("selected=\"selected\""))
              {
                severity = option;
              }
            }

            Matcher matcher = SEVERITY_PATTERN.matcher(line);
            if (matcher.matches())
            {
              option = matcher.group(1);
            }

            if (line.equals("</select>"))
            {
              inSeverity = false;
              option = null;
            }
          }
        }

        if (title != null && severity != null)
        {
          return;
        }
      }
    }
  }
}
