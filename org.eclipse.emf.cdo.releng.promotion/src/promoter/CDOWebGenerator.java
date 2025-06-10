/*
 * Copyright (c) 2004-2013 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eike Stepper - initial API and implementation
 */
package promoter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import promoter.util.IO;
import promoter.util.IO.PrintHandler;

/**
 * @author Eike Stepper
 */
public class CDOWebGenerator extends WebGenerator
{
  private static final String TEMPLATE = "https://raw.githubusercontent.com/eclipse-cdo/cdo.www/refs/heads/master/downloads/index.html";

  private static final Pattern BREADCRUMB = pattern("BREADCRUMB");

  private static final Pattern GENERATED_BODY = pattern("GENERATED_BODY");

  public CDOWebGenerator()
  {
  }

  @Override
  protected void generateWeb(WebNode root, PrintStream out) throws IOException
  {
    // All downloads (index.html)
    generateTemplate(out, TEMPLATE, "<a href=\"downloads.html\">Downloads</a><span>All</span>", IO.print(ps -> super.generateWeb(root, ps)));

    // Simple downloads (downloads.html)
    printFile("downloads.html", html -> {
      generateTemplate(html, TEMPLATE, "<span>Downloads</span>", IO.print(ps -> generateDownloads(root, ps)));
    });
  }

  private void generateDownloads(WebNode root, PrintStream out)
  {
    WebNode releases = root.getChild("releases");
    WebNode integration = root.getChild("integration");

    generateDownload("Latest Release", releases, out);
    generateDownload("Latest Stable Build", integration.getChild("stable"), out);
    generateDownload("Latest Weekly Build", integration.getChild("weekly"), out);

    out.println();
    out.println("<h3>Other Builds</h3>");
    out.println("<div class=\"indent\">");
    out.println("<a class=\"button button-neutral\" href=\"index.html\">All Promoted Builds</a>");
    out.println("<br>");
    out.println("<a class=\"button button-neutral\" href=\"https://ci.eclipse.org/cdo/job/emf-cdo-build/job/master\">Continuous Integration</a>");
    out.println("</div>");
  }

  private void generateDownload(String heading, WebNode webNode, PrintStream out)
  {
    BuildInfo buildInfo = webNode.getLatestDrop(false);
    String qualifier = buildInfo.getQualifier();
    String dropLabel = qualifier;

    String webLabel = buildInfo.getWebLabel();
    if (webLabel != null)
    {
      dropLabel += " (CDO " + webLabel + ")";
    }

    out.println();
    out.println("<h3>" + heading + "</h3>");
    out.println("<div class=\"indent\">");
    out.println("<h4>" + dropLabel + "</h4>");

    out.println("<a class=\"button button-download\" href=\"https://www.eclipse.org/downloads/download.php?file=/modeling/emf/cdo/drops/" + qualifier
        + "/zips/emf-cdo-" + qualifier + "-Dropins.zip&protocol=http\" title=\"An archive file with the features and bundles of this build\">Download</a>");

    out.println("<a class=\"button button-download\" href=\"https://download.eclipse.org/modeling/emf/cdo/drops/" + qualifier
        + "\" title=\"A p2 repository to install this build\">Update&nbsp;Site</a>");

    out.println("<a class=\"button button-download\" href=\"" + webNode.getLatestRepository().getURL(false)
        + "\" title=\"A p2 composite repository to always install the " + heading.toLowerCase() + "\">Floating&nbsp;Update&nbsp;Site</a>");

    out.println("<br>");

    out.println("<a class=\"button button-neutral\" href=\"https://download.eclipse.org/modeling/emf/cdo/drops/" + qualifier
        + "/relnotes.html\" title=\"The enhancements and fixes in this build\">Release&nbsp;Notes</a>");

    out.println("<a class=\"button button-neutral\" href=\"https://download.eclipse.org/modeling/emf/cdo/drops/" + qualifier
        + "/api.html\" title=\"The API changes in this build compared to the previous release\">API&nbsp;Report</a>");

    out.println("<a class=\"button button-neutral\" href=\"https://download.eclipse.org/modeling/emf/cdo/drops/" + qualifier
        + "/tests/index.html\" title=\"The test results of this build\">Test&nbsp;Report</a>");

    out.println("<a class=\"button button-neutral\" href=\"https://download.eclipse.org/modeling/emf/cdo/drops/" + qualifier
        + "/help/index.html\" title=\"The online help center for this build\">Help Center</a>");

    out.println("<a href=\"https://download.eclipse.org/modeling/emf/cdo/updates/index.html#" + qualifier + "\">More&nbsp;infos</a>");
    out.println("</div>");
  }

  protected static void generateTemplate(PrintStream out, String templateURL, String breadcrumb, String body)
  {
    String template = IO.readURL(templateURL);
    template = replacePlaceholder(template, BREADCRUMB, breadcrumb);
    template = replacePlaceholder(template, GENERATED_BODY, body);
    out.print(template);
  }

  protected static void generateTemplate(PrintStream out, String templateURL, String breadcrumb, PrintHandler bodyHandler)
  {
    String body = IO.print(bodyHandler);
    generateTemplate(out, templateURL, breadcrumb, body);
  }

  private static String replacePlaceholder(String template, Pattern pattern, String replacement)
  {
    replacement = Matcher.quoteReplacement(replacement);
    return pattern.matcher(template).replaceFirst(replacement);
  }

  private static Pattern pattern(String placeholder)
  {
    return Pattern.compile("<!-- <" + placeholder + ">.*</" + placeholder + "> -->", Pattern.DOTALL);
  }
}
