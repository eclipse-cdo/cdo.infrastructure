package tasks.hudson;

import util.BuildInfo;

/**
 * @author Eike Stepper
 */
public class PromoterNightly extends Promoter
{
  public PromoterNightly(BuildInfo buildInfo)
  {
    super(buildInfo);
  }

  @Override
  public void promoteBuild()
  {
    String buildQualifier = getBuildInfo().getQualifier();
    out("Ignoring " + buildQualifier);
  }
}
