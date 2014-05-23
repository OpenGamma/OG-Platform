/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.errorreport;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.testng.annotations.Test;

import com.opengamma.integration.tool.GUIFeedback;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link BundleErrorReportInfo} class.
 */
@Test(groups = TestGroup.UNIT)
public class BundleErrorReportInfoTest {

  private static File tmpdir() {
    return new File(System.getProperty("java.io.tmpdir"));
  }

  private static String name() {
    return "test" + System.nanoTime();
  }

  private static void delete(final File file) {
    final File[] subfiles = file.listFiles();
    if (subfiles != null) {
      for (File subfile : subfiles) {
        if (!subfile.getName().startsWith(".")) {
          delete(subfile);
        }
      }
    }
    file.delete();
  }

  private static void writeTestProperties(final String basePath, final String path) throws IOException {
    try (final PrintStream ps = new PrintStream(new FileOutputStream(path))) {
      ps.println("# Example configuration file");
      ps.println("");
      ps.println("Invalid line=ignored");
      ps.println(" AttachFiles=" + basePath + File.separator + "test.log");
      ps.println("AttachFiles=%TEMP%" + File.separator + "this-will-not-exist-but-will-try-the-expansion.log");
    }
  }

  private static void writeTestFile(final String path, final int chunks) throws IOException {
    System.out.println("Writing " + path);
    final File file = new File(path);
    file.getParentFile().mkdirs();
    try (final OutputStream out = new FileOutputStream(file)) {
      final byte[] buffer = new byte[1000];
      for (int i = 0; i < chunks; i++) {
        out.write(buffer);
      }
    }
  }

  public void testCreateZIP() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      writeTestFile(file.getAbsolutePath() + File.separator + "test.log", 7);
      BundleErrorReportInfo.setUserHome(file.getAbsolutePath());
      final String props = file.getAbsolutePath() + File.separator + "Test.properties";
      writeTestProperties(file.getAbsolutePath(), props);
      assertEquals(BundleErrorReportInfo.mainImpl(new String[] {props }), 0);
      for (String zip : file.list()) {
        if (zip.endsWith(".zip")) {
          final File zipFile = new File(file, zip);
          assertEquals(zipFile.length(), 158L);
        }
      }
    } finally {
      delete(file);
      BundleErrorReportInfo.setUserHome(null);
    }
  }

  public void testAttachFiles() throws IOException {
    final File file = new File(tmpdir(), name());
    final AtomicReference<File> zipFile = new AtomicReference<File>();
    try {
      writeTestFile(file.getAbsolutePath() + File.separator + "Foo1" + File.separator + "test.log", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "Foo2x" + File.separator + "test.log", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "Foo3", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "testXY.log", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "test.log", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "testA.log", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "Foo2" + File.separator + "test.log", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "test.txt", 1);
      writeTestFile(file.getAbsolutePath() + File.separator + "testB.log" + File.separator + "foo.txt", 1);
      final AtomicInteger count = new AtomicInteger();
      final BundleErrorReportInfo beri = new BundleErrorReportInfo(new GUIFeedback("Test"), new String[] {
          "AttachFiles=" + file.getAbsolutePath() + File.separator + File.separator + "Foo?" + File.separator + "*.log",
          "AttachFiles=" + file.getAbsolutePath() + File.separator + "test*.log", "AttachFiles=" + File.separatorChar + "path" + File.separatorChar + "doesnt" + File.separatorChar + "exist" }) {

        @Override
        protected String openReportOutput() {
          final String path = super.openReportOutput();
          zipFile.set(new File(path));
          return path;
        }

        @Override
        protected void attachFile(final File source, final String name) {
          switch (count.incrementAndGet()) {
            case 1:
              assertEquals(source.getAbsolutePath().substring(file.getAbsolutePath().length()), File.separatorChar + "Foo1" + File.separatorChar + "test.log");
              assertEquals(name, "1-test.log");
              break;
            case 2:
              assertEquals(source.getAbsolutePath().substring(file.getAbsolutePath().length()), File.separatorChar + "Foo2" + File.separatorChar + "test.log");
              assertEquals(name, "2-test.log");
              break;
            case 3:
              assertEquals(source.getAbsolutePath().substring(file.getAbsolutePath().length()), File.separatorChar + "test.log");
              assertEquals(name, "3-test.log");
              break;
            case 4:
              assertEquals(source.getAbsolutePath().substring(file.getAbsolutePath().length()), File.separatorChar + "testA.log");
              assertEquals(name, "4-testA.log");
              break;
            case 5:
              assertEquals(source.getAbsolutePath().substring(file.getAbsolutePath().length()), File.separatorChar + "testXY.log");
              assertEquals(name, "5-testXY.log");
              break;
          }
        }

      };
      beri.run();
      assertEquals(count.get(), 5);
    } finally {
      delete(file);
      delete(zipFile.get());
    }
  }

  public void testInvalidArgs() {
    assertEquals(BundleErrorReportInfo.mainImpl(new String[0]), 1);
    assertEquals(
        BundleErrorReportInfo.mainImpl(new String[] {File.separatorChar + "this" + File.separatorChar + "path" + File.separatorChar + "does" + File.separatorChar + "not" + File.separatorChar +
            "exist" }), 1);
  }

}
