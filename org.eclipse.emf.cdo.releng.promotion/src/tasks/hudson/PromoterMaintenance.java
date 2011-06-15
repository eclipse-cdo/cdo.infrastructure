package tasks.hudson;

import util.BuildInfo;

/**
 * @author Eike Stepper
 */
public class PromoterMaintenance extends Promoter
{
  public PromoterMaintenance(BuildInfo buildInfo)
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
