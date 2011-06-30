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

import promoter.util.IO;
import promoter.util.IO.InputHandler;

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
  public static final String SERVER = "https://bugs.eclipse.org";

  private static final Pattern ID_PATTERN = Pattern.compile("\\[([0-9]+)].*");

  private static final Pattern TITLE_PATTERN = Pattern.compile("<title>Bug ([0-9]*) &ndash; (.*)</title>");

  public Bugzilla()
  {
  }

  @Override
  public String parseID(String message)
  {
    Matcher matcher = ID_PATTERN.matcher(message);
    if (matcher.matches())
    {
      try
      {
        return String.valueOf(Integer.parseInt(matcher.group(1)));
      }
      catch (NumberFormatException ex)
      {
        // fall-through
      }
    }

    return "";
  }

  @Override
  public Issue getIssue(String id)
  {
    final String[] title = { null };

    IO.readURL(SERVER + "/" + id, new InputHandler()
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

    return title[0] == null ? null : new Issue(id, title[0]);
  }

  @Override
  public String getURL(Issue issue)
  {
    return "https://bugs.eclipse.org/" + issue.getID();
  }

  public int compare(Issue i1, Issue i2)
  {
    return new Integer(i1.getID()).compareTo(new Integer(i2.getID()));
  }
}
