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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Eike Stepper
 */
public abstract class SourceCodeManager extends PromoterComponent
{
  public SourceCodeManager()
  {
  }

  public abstract void setTag(String branch, String tag);

  public abstract void handleLogEntries(String branch, String fromRevision, String toRevision, boolean withPaths,
      LogEntryHandler handler);

  public abstract void commit(String comment, File... checkouts);

  /**
   * @author Eike Stepper
   */
  public static class LogEntry
  {
    private String revision;

    private String author;

    private String date;

    private String message;

    private List<String> paths = new ArrayList<String>();

    public LogEntry(String revision)
    {
      this.revision = revision;
    }

    public String getRevision()
    {
      return revision;
    }

    public String getAuthor()
    {
      return author;
    }

    public void setAuthor(String author)
    {
      this.author = author;
    }

    public String getDate()
    {
      return date;
    }

    public void setDate(String date)
    {
      this.date = date;
    }

    public String getMessage()
    {
      return message;
    }

    public void setMessage(String message)
    {
      this.message = message;
    }

    public List<String> getPaths()
    {
      return paths;
    }

    @Override
    public String toString()
    {
      return "LogEntry [revision=" + revision + ", author=" + author + ", date=" + date + ", message=" + message + "]";
    }
  }

  /**
   * @author Eike Stepper
   */
  public interface LogEntryHandler
  {
    public void handleLogEntry(LogEntry logEntry);
  }
}
