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


/**
 * @author Eike Stepper
 */
public class Bugzilla extends IssueManager
{
  public Bugzilla()
  {
  }

  @Override
  public String parseID(String message)
  {
    StringBuilder builder = new StringBuilder();
    boolean inDigits = false;
    for (int i = 0; i < message.length(); i++)
    {
      char c = message.charAt(i);
      if (Character.isDigit(c))
      {
        builder.append(c);
        inDigits = true;
      }
      else if (inDigits)
      {
        break;
      }
    }

    return builder.toString();
  }

  @Override
  public Issue getIssue(String id)
  {
    return new Issue(id, "This is a bug report");
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
