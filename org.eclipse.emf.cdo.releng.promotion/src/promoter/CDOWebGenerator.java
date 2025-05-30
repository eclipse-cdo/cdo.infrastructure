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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import promoter.util.IO;

/**
 * @author Eike Stepper
 */
public class CDOWebGenerator extends WebGenerator
{
  private static final String TEMPLATE = "https://eclipse.dev/cdo/downloads.html";

  private static final Pattern BREADCRUMB = pattern("BREADCRUMB");

  private static final Pattern GENERATED_BODY = pattern("GENERATED_BODY");

  public CDOWebGenerator()
  {
  }

  @Override
  protected void generateWeb(WebNode webNode, PrintStream out) throws IOException
  {
    String template = IO.readURL(TEMPLATE);
    String body = generateWebBody(webNode);

    template = replacePlaceholder(template, BREADCRUMB, "CDO Downloads");
    template = replacePlaceholder(template, GENERATED_BODY, body);
    out.print(template);
  }

  private String generateWebBody(WebNode webNode)
  {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream out = null;

    try
    {
      out = new PrintStream(baos);
      super.generateWeb(webNode, out);
      out.flush();
    }
    catch (Exception ex)
    {
      throw new RuntimeException(ex);
    }
    finally
    {
      IO.close(out);
    }

    return new String(baos.toByteArray(), StandardCharsets.UTF_8);
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
