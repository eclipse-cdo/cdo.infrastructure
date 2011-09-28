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

  private static final Pattern TITLE_PATTERN = Pattern.compile("\\s*<title>Bug ([0-9]*) &ndash; (.*)</title>");

  private static final Pattern SEVERITY_PATTERN = Pattern.compile("<option value=\"(.+?)\"");

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
    final String[] title = { null };
    final String[] severity = { null };

    for (int i = 0; i < RETRIES; i++)
    {
      try
      {
        IO.readURL(SERVER + id, new IO.InputHandler()
        {
          public void handleInput(InputStream in) throws IOException
          {
            boolean inSeverity = false;
            String option = null;

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
            {
              if (title[0] == null)
              {
                Matcher matcher = TITLE_PATTERN.matcher(line);
                if (matcher.matches())
                {
                  title[0] = matcher.group(2);
                }
              }

              if (severity[0] == null)
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
                      severity[0] = option;
                    }
                  }
                  else
                  {
                    Matcher matcher = SEVERITY_PATTERN.matcher(line);
                    if (matcher.matches())
                    {
                      option = matcher.group(1);
                    }
                  }

                  if (line.equals("</select>"))
                  {
                    inSeverity = false;
                    option = null;
                  }
                }
              }

              if (title[0] != null && severity[0] != null)
              {
                return;
              }
            }
          }
        });

        if (title[0] != null && severity[0] != null)
        {
          return new Issue(id, title[0], severity[0]);
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
}
