/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.db.script;

import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Tests {@link ZipFileDbScript} and {@link ZipFileDbScriptDirectory}.
 */
@Test
public class ZipFileDbScriptTest {

  public void test_traverse() throws IOException {
    File zippedScripts = getZippedScripts();
    ZipFileDbScriptDirectory baseDir = new ZipFileDbScriptDirectory(zippedScripts, "base");
    assertEquals("base", baseDir.getName());
    Set<String> expectedSubDirs = ImmutableSet.of("0", "1");
    Set<String> expectedFiles = ImmutableSet.of("0.txt", "1.txt");
    Collection<DbScriptDirectory> iDirs = baseDir.getSubdirectories();
    assertEquals(2, iDirs.size());
    for (DbScriptDirectory iDir : iDirs) {
      assertTrue(expectedSubDirs.contains(iDir.getName()));
      Collection<DbScriptDirectory> jDirs = iDir.getSubdirectories();
      assertEquals(2, jDirs.size());
      for (DbScriptDirectory jDir : jDirs) {
        assertTrue(expectedSubDirs.contains(jDir.getName()));
        Collection<DbScript> scripts = jDir.getScripts();
        assertEquals(2, scripts.size());
        for (DbScript script : scripts) {
          assertTrue(script.getName() + " not an expected file name", expectedFiles.contains(script.getName()));
          String contents = script.getScript();
          String expectedContents = "base/" + iDir.getName() + "/" + jDir.getName() + "/" + script.getName();
          assertEquals(expectedContents, contents);
        }
      }
    }
    zippedScripts.delete();
  }
  
  private File getZippedScripts() throws IOException {
    File tempFile = File.createTempFile("dbscripttest", null);
    FileOutputStream out = new FileOutputStream(tempFile);
    ZipOutputStream zippedOut = new ZipOutputStream(out);
    PrintWriter printWriter = new PrintWriter(zippedOut);
    String baseDir = "base/";
    for (int i = 0; i < 2; i++) {
      String dir1 = baseDir + i + "/";
      for (int j = 0; j < 2; j++) {
        String dir2 = dir1 + j + "/";
        for (int k = 0; k < 2; k++) {
          String filePath = dir2 + k + ".txt";
          ZipEntry entry = new ZipEntry(filePath);
          zippedOut.putNextEntry(entry);
          printWriter.print(filePath);
          printWriter.flush();
        }
      }
    }
    printWriter.close();
    zippedOut.close();
    return tempFile;
  }
  
}
