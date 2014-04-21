/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.opengamma.integration.regression.RegressionIO.Format;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link RegressionIO} class.
 */
@Test(groups = TestGroup.UNIT)
public class RegressionIOTest {

  private static class TestInstance extends RegressionIO {

    public TestInstance(File baseFile, Format format) {
      super(baseFile, format);
    }

    @Override
    public void write(String type, Object o, String identifier) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public Object read(String type, String identifier) throws IOException {
      throw new UnsupportedOperationException();
    }

    @Override
    public List<String> enumObjects(String type) throws IOException {
      throw new UnsupportedOperationException();
    }

  }

  public void testBulkWrite() throws IOException {
    final Format format = Mockito.mock(Format.class);
    final Map<String, Object> objects = Maps.newHashMap();
    final RegressionIO instance = new TestInstance(new File("Test"), format) {
      @Override
      public void write(final String type, final Object o, final String identifier) {
        assertEquals(type, "tests");
        assertNull(objects.put(identifier, o));
      }
    };
    instance.beginWrite();
    instance.write("tests", ImmutableMap.<String, Object>of("Foo", "Foo instance", "Bar", "Bar instance"));
    instance.endWrite();
    Mockito.verify(format).openWrite(null);
    Mockito.verify(format).closeWrite(null);
    assertEquals(objects, ImmutableMap.<String, Object>of("Foo", "Foo instance", "Bar", "Bar instance"));
  }

  public void testBulkRead() throws IOException {
    final Format format = Mockito.mock(Format.class);
    final RegressionIO instance = new TestInstance(new File("Test"), format) {

      @Override
      public List<String> enumObjects(final String type) {
        return Arrays.asList("Foo", "Bar");
      }

      @Override
      public Object read(final String type, final String identifier) {
        assertEquals(type, "tests");
        return identifier + " instance";
      }

    };
    instance.beginRead();
    final Map<String, Object> objects = instance.readAll("tests");
    instance.endRead();
    Mockito.verify(format).openRead(null);
    Mockito.verify(format).closeRead(null);
    assertEquals(objects, ImmutableMap.<String, Object>of("Foo", "Foo instance", "Bar", "Bar instance"));
  }

}
