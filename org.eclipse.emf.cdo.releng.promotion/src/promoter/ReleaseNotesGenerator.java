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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eike Stepper
 */
public class ReleaseNotesGenerator
{
  private Promoter promoter;

  public ReleaseNotesGenerator()
  {
  }

  public final Promoter getPromoter()
  {
    return promoter;
  }

  void setPromoter(Promoter promoter)
  {
    this.promoter = promoter;
  }

  public void generateReleaseNotes(List<BuildInfo> buildInfos)
  {
    Map<String, Stream> streams = getStreams(buildInfos);

  }

  protected Map<String, Stream> getStreams(List<BuildInfo> buildInfos)
  {
    Map<String, Stream> streams = new HashMap<String, Stream>();
    for (BuildInfo buildInfo : buildInfos)
    {
      String name = buildInfo.getStream();

      Stream stream = streams.get(name);
      if (stream == null)
      {
        stream = new Stream(name);
        streams.put(name, stream);
      }

      stream.getBuildInfos().add(buildInfo);
    }

    return streams;
  }

  /**
   * @author Eike Stepper
   */
  public static class Stream
  {
    private String name;

    private List<BuildInfo> buildInfos = new ArrayList<BuildInfo>();

    public Stream(String name)
    {
      this.name = name;
    }

    public final String getName()
    {
      return name;
    }

    public final List<BuildInfo> getBuildInfos()
    {
      return buildInfos;
    }
  }
}
