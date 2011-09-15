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
  public static final String SERVER = "https://bugs.eclipse.org/bugs/show_bug.cgi?id=";

  private static final int RETRIES = 3;

  private static final Pattern TITLE_PATTERN = Pattern.compile("\\s*<title>Bug ([0-9]*) &ndash; (.*)</title>");

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
    // return new Issue(id, "This is a bugzilla");

    final String[] title = { null };

    for (int i = 0; i < RETRIES; i++)
    {
      try
      {
        IO.readURL(SERVER + id, new IO.InputHandler()
        {
          public void handleInput(InputStream in) throws IOException
          {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null)
            {
              Matcher matcher = TITLE_PATTERN.matcher(line);
              if (matcher.matches())
              {
                title[0] = matcher.group(2);
                return;
              }
            }
          }
        });

        if (title[0] != null)
        {
          return new Issue(id, title[0]);
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
