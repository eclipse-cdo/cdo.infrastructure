/*
 * Copyright (c) 2004 - 2012 Eike Stepper (Loehne, Germany) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package promoter;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

import promoter.util.IO;

/**
 * Focused regression tests for the promotion service's explicit UTF-8 text handling.
 */
final class CharsetTest
{
  private CharsetTest()
  {
  }

  /**
   * Runs the focused charset regression tests.
   *
   * @param args ignored
   * @throws Exception if a test cannot access its temporary file
   */
  public static void main(String[] args) throws Exception
  {
    File file = File.createTempFile("promoter-charset-", ".txt");

    try
    {
      String text = "UTF-8: Gr\u00fc\u00dfe \u2014 (Java\u2122 ARchive)";
      IO.writeTextFile(file, text);
      assertEquals(text, IO.readTextFile(file));

      String replacement = "(Java\u2122 ARchive)";
      IO.writeTextFile(file, "header: MARKER");
      new DropProcessor().replaceRegex(file, Pattern.compile("MARKER"), replacement);
      assertEquals("header: " + replacement, IO.readTextFile(file));

      assertEquals(replacement, IO.print(out -> out.print(replacement)));
      assertEquals(text, new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8)
          .replace("header: " + replacement, text));
    }
    finally
    {
      Files.deleteIfExists(file.toPath());
    }
  }

  private static void assertEquals(String expected, String actual)
  {
    if (!expected.equals(actual))
    {
      throw new AssertionError("Expected <" + expected + "> but was <" + actual + ">");
    }
  }
}
