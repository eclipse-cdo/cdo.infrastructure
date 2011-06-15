package tasks.hudson;

import util.BuildInfo;

/**
 * @author Eike Stepper
 */
public class PromoterStable extends Promoter
{
  public PromoterStable(BuildInfo buildInfo)
  {
    super(buildInfo);
  }

  @Override
  public void promoteBuild()
  {
    String buildQualifier = getBuildInfo().getQualifier();
    System.out.println("Promoting " + buildQualifier);
    getBuildInfo().getDrop().mkdirs();
  }
}
