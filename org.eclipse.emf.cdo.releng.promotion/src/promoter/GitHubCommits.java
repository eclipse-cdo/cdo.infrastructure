/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.RepositoryCommit;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * @author Eike Stepper
 */
public class GitHubCommits extends SourceCodeManager
{
  private Map<String, RepositoryCommit> repositoryCommits;

  public GitHubCommits()
  {
  }

  @Override
  public void setTag(final String branch, final String revision, final String qualifier)
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public void getCommits(String branch, String fromRevision, String toRevision, BiConsumer<String, String> commitConsumer)
  {
    Deque<String> pending = new ArrayDeque<>();
    pending.add(toRevision);

    Set<String> visited = new HashSet<>();
    visited.add(fromRevision);

    while (!pending.isEmpty())
    {
      String sha = pending.remove();

      try
      {
        RepositoryCommit repositoryCommit = getCommit(sha);
        if (repositoryCommit != null)
        {
          String message = repositoryCommit.getCommit().getMessage();
          commitConsumer.accept(sha, message);

          if (visited.add(sha))
          {
            for (Commit parent : repositoryCommit.getParents())
            {
              pending.add(parent.getSha());
            }
          }
        }
      }
      catch (IOException ex)
      {
        throw new RuntimeException(ex);
      }
    }
  }

  private RepositoryCommit getCommit(String sha) throws IOException
  {
    if (repositoryCommits == null)
    {
      repositoryCommits = new HashMap<>();

      for (RepositoryCommit repositoryCommit : GitHub.getCommitService().getCommits(GitHub.REPO))
      {
        repositoryCommits.put(repositoryCommit.getSha(), repositoryCommit);
      }
    }

    return repositoryCommits.get(sha);
  }
}
