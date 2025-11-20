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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import promoter.Repository.Drops;
import promoter.util.IO;
import promoter.util.IO.PrintHandler;

/**
 * @author Eike Stepper
 */
public class CDOWebGenerator extends WebGenerator
{
  private static final String GITHUB = "https://raw.githubusercontent.com/eclipse-cdo/cdo.www/refs/heads/master";

  private static final String DOWNLOADS_TEMPLATE = "/downloads/index.html";

  private static final String RELNOTES_TEMPLATE = "/downloads/all-relnotes.html";

  private static final String DOCUMENTATION_TEMPLATE = "/documentation/index.html";

  private static final Pattern OVERVIEW_PATTERN = Pattern.compile("<p class=\"author\">Author: Eike Stepper</p>(.+)<p align=\"right\">", Pattern.DOTALL);

  private static final Pattern BREADCRUMB_PATTERN = pattern("BREADCRUMB");

  private static final Pattern GENERATED_BODY_PATTERN = pattern("GENERATED_BODY");

  private static final Pattern RELNOTES_PATTERN = Pattern.compile("<body style=\"[^\"]*\">(.*)</body>", Pattern.DOTALL);

  public CDOWebGenerator()
  {
  }

  @Override
  protected void generateWeb(WebNode root, PrintStream out) throws IOException
  {
    // All downloads (index.html)
    generateTemplate(out, DOWNLOADS_TEMPLATE, "<a href=\"downloads.html\">Downloads</a><span>All</span>", IO.print(ps -> super.generateWeb(root, ps)));

    // Simple downloads (downloads.html)
    System.out.println();
    System.out.println("Generating HTML --> https://download.eclipse.org/modeling/emf/cdo/updates/downloads.html");
    printFile("downloads.html", html -> {
      generateTemplate(html, DOWNLOADS_TEMPLATE, "<span>Downloads</span>", IO.print(ps -> generateDownloads(root, ps)));
    });

    // All release notes (all-relnotes.html)
    System.out.println();
    System.out.println("Generating HTML --> https://download.eclipse.org/modeling/emf/cdo/updates/all-relnotes.html");
    printFile("all-relnotes.html", html -> {
      generateTemplate(html, RELNOTES_TEMPLATE, "<span>Release Notes</span>", IO.print(ps -> generateRelnotes(root, ps)));
    });

    // Documentation (documentation.html)
    System.out.println();
    System.out.println("Generating HTML --> https://download.eclipse.org/modeling/emf/cdo/updates/documentation.html");
    printFile("documentation.html", html -> {
      generateTemplate(html, DOCUMENTATION_TEMPLATE, "<span>Documentation</span>", IO.print(ps -> generateDocumentation(root, ps)));
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
    out.println("<a class=\"button button-download\" href=\"index.html\">All Promoted Builds</a>");
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

    System.out.println("   Generating HTML for " + qualifier);

    Repository repository = webNode.getRepository();
    if (repository instanceof Drops)
    {
      Drops drops = (Drops)repository;
      if (drops.containsSurrogateDrop())
      {
        dropLabel += " is replaced by the " + heading.toLowerCase() + " when available.";
      }
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

  private void generateRelnotes(WebNode root, PrintStream out)
  {
    Set<BuildInfo> releases = new HashSet<>();
    root.getChild("releases").getDrops(true).stream().filter(BuildInfo::isRelease).forEach(releases::add);

    List<BuildInfo> sortedReleases = new ArrayList<>(releases);
    sortedReleases.sort((a, b) -> b.getQualifier().compareTo(a.getQualifier()));

    for (BuildInfo release : sortedReleases)
    {
      File drop = release.getDrop();

      File relnotesHTML = new File(drop, "relnotes.html");
      if (relnotesHTML.isFile())
      {
        String html = IO.readTextFile(relnotesHTML);

        Matcher matcher = RELNOTES_PATTERN.matcher(html);
        if (matcher.find())
        {
          System.out.println("   Generating HTML for " + release.getQualifier());
          String body = matcher.group(1).trim();
          body = body.replaceAll("<h3>Table of Contents</h3>.*</ul>", ""); // Remove TOC
          body = body.replace("../../images/", "../images/"); // Fix image paths

          String webLabel = release.getWebLabel();
          if (webLabel != null)
          {
            body = body.replaceAll(">([A-Z0-9-_)+</a></h1>", ">" + webLabel + "($1)</a></h1>");
          }

          out.println();
          out.println(body);
          out.println();
        }
      }
    }
  }

  private void generateDocumentation(WebNode root, PrintStream out)
  {
    WebNode releases = root.getChild("releases");
    BuildInfo buildInfo = releases.getLatestDrop(false);
    String qualifier = buildInfo.getQualifier();
    String webLabel = buildInfo.getWebLabel();

    System.out.println("   Generating HTML for " + qualifier);

    String drop = "https://download.eclipse.org/modeling/emf/cdo/drops/" + qualifier;
    String help = drop + "/help";
    String cdodoc = help + "/org.eclipse.emf.cdo.doc/html/";
    String overview = IO.readURL(cdodoc + "Overview.html");

    Matcher matcher = OVERVIEW_PATTERN.matcher(overview);
    if (matcher.find())
    {
      String body = matcher.group(1).trim();

      // 1) Protect absolute URLs against rewrite in 2) below.
      body = body.replace("<a href=\"http", "<a HREF=\"http");
      body = body.replace("<img src=\"http", "<img SRC=\"http");

      // 2) Rewrite relative URLs.
      body = body.replace("<a href=\"", "<a href=\"" + cdodoc);
      body = body.replace("<img src=\"", "<img src=\"" + cdodoc);

      String others = "<p class=\"others\"><i>This overview is an extract from the <a href=\"" + help + "\">Help Center</a> of CDO " + webLabel + " (<a href=\""
          + drop + "\">" + qualifier + "</a>).<br>\n"
          + "The help centers of other CDO versions are available on the <a href=\"https://download.eclipse.org/modeling/emf/cdo/updates/index.html\">Downloads</a> page.</i></p>\n\n";

      out.println(others);
      out.println(body);
      out.println(others);
    }
  }

  protected static void generateTemplate(PrintStream out, String templatePath, String breadcrumb, String body)
  {
    String template;

    boolean localTemplates = Boolean.getBoolean("localTemplates");
    if (localTemplates)
    {
      template = IO.readTextFile(new File(PromoterConfig.INSTANCE.getTemplatesDirectory(), templatePath));
    }
    else
    {
      template = IO.readURL(GITHUB + templatePath);
    }

    template = replacePlaceholder(template, BREADCRUMB_PATTERN, breadcrumb);
    template = replacePlaceholder(template, GENERATED_BODY_PATTERN, body);
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
