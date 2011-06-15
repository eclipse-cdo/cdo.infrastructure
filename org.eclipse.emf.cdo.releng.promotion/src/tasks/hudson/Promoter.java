package tasks.hudson;

import util.BuildInfo;

/**
 * @author Eike Stepper
 */
public abstract class Promoter implements Runnable
{
  private BuildInfo buildInfo;

  public Promoter(BuildInfo buildInfo)
  {
    this.buildInfo = buildInfo;
  }

  public final BuildInfo getBuildInfo()
  {
    return buildInfo;
  }

  public final void run()
  {
    promoteBuild();
  }

  protected void promoteBuild()
  {
    String buildQualifier = buildInfo.getQualifier();
    out("Promoting " + buildQualifier);
    buildInfo.getDrop().mkdirs();
  }

  protected final void out(Object msg)
  {
    System.out.println(formatMessage(msg));
  }

  protected final void err(Object msg)
  {
    System.err.println(formatMessage(msg));
  }

  private String formatMessage(Object msg)
  {
    return buildInfo.getNumber() + ": " + msg;
  }
}
