/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.regression.RegressionIO.Format;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SubdirsRegressionIO} class
 */
@Test(groups = TestGroup.UNIT)
public class SubdirsRegressionIOTest {

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

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstructorNotDirectory() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      file.createNewFile();
      new SubdirsRegressionIO(file, Mockito.mock(Format.class), false);
    } finally {
      delete(file);
    }
  }

  public void testConstructorDirectoryExists() {
    final File file = new File(tmpdir(), name());
    try {
      file.mkdirs();
      new SubdirsRegressionIO(file, Mockito.mock(Format.class), false);
    } finally {
      delete(file);
    }
  }

  public void testConstructorNotExistsNoCreate() {
    final File file = new File(tmpdir(), name());
    try {
      assertFalse(file.exists());
      new SubdirsRegressionIO(file, Mockito.mock(Format.class), false);
      assertFalse(file.exists());
    } finally {
      delete(file);
    }
  }

  public void testConstructorNotExistsCreate() {
    final File file = new File(tmpdir(), name());
    try {
      assertFalse(file.exists());
      new SubdirsRegressionIO(file, Mockito.mock(Format.class), true);
      assertTrue(file.exists());
      assertTrue(file.isDirectory());
    } finally {
      delete(file);
    }
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testConstructorNotExistsCreateFail() {
    @SuppressWarnings("serial")
    final File file = new File(tmpdir(), name()) {
      @Override
      public boolean mkdirs() {
        return false;
      }
    };
    try {
      assertFalse(file.exists());
      new SubdirsRegressionIO(file, Mockito.mock(Format.class), true);
    } finally {
      delete(file);
    }
  }

  public void testWrite() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      Mockito.when(format.getLogicalFileExtension(null)).thenReturn(".obj");
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      instance.beginWrite();
      instance.write("foo", "Foo instance", "0");
      instance.endWrite();
      Mockito.verify(format).write(Mockito.<Object>any(), Mockito.eq("Foo instance"), Mockito.<OutputStream>any());
      assertTrue((new File(new File(file, "foo"), "0.obj")).exists());
    } finally {
      delete(file);
    }
  }

  public void testRead() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      (new File(file, "foo")).mkdirs();
      (new File(new File(file, "foo"), "0")).createNewFile();
      Mockito.when(format.read(Mockito.<Object>any(), Mockito.<InputStream>any())).thenReturn("Foo instance");
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      instance.beginRead();
      assertEquals(instance.read("foo", "0"), "Foo instance");
      instance.endRead();
    } finally {
      delete(file);
    }
  }

  @Test(expectedExceptions = FileNotFoundException.class)
  public void testReadFileNotFound() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      (new File(file, "bar")).mkdirs();
      instance.beginRead();
      instance.read("foo", "0");
    } finally {
      delete(file);
    }
  }

  @Test(expectedExceptions = FileNotFoundException.class)
  public void testReadDirectoryNotFound() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      instance.beginRead();
      instance.read("foo", "0");
    } finally {
      delete(file);
    }
  }

  public void testEnumObjectsNotExist() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      instance.beginRead();
      assertEquals(instance.enumObjects("foo"), Collections.emptyList());
      instance.endRead();
    } finally {
      delete(file);
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEnumObjectsNotDirectory() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      (new File(file, "foo")).createNewFile();
      instance.beginRead();
      instance.enumObjects("foo");
    } finally {
      delete(file);
    }
  }

  public void testEnumObjectsEmpty() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      (new File(file, "foo")).mkdirs();
      instance.beginRead();
      assertEquals(instance.enumObjects("foo"), Collections.emptyList());
      instance.endRead();
    } finally {
      delete(file);
    }
  }

  public void testEnumObjectsNoExt() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      (new File(file, "foo")).mkdirs();
      (new File((new File(file, "foo")), "0")).createNewFile();
      (new File((new File(file, "foo")), "1")).createNewFile();
      instance.beginRead();
      assertEquals(new HashSet<String>(instance.enumObjects("foo")), ImmutableSet.of("0", "1"));
      instance.endRead();
    } finally {
      delete(file);
    }
  }

  public void testEnumObjectsExt() throws IOException {
    final File file = new File(tmpdir(), name());
    try {
      final Format format = Mockito.mock(Format.class);
      Mockito.when(format.getLogicalFileExtension(null)).thenReturn(".obj");
      final RegressionIO instance = new SubdirsRegressionIO(file, format, true);
      (new File(file, "foo")).mkdirs();
      (new File((new File(file, "foo")), "0")).createNewFile();
      (new File((new File(file, "foo")), "1.obj")).createNewFile();
      (new File((new File(file, "foo")), "2.obj")).createNewFile();
      (new File((new File(file, "foo")), "3")).createNewFile();
      instance.beginRead();
      assertEquals(new HashSet<String>(instance.enumObjects("foo")), ImmutableSet.of("1", "2"));
      instance.endRead();
    } finally {
      delete(file);
    }
  }

}
