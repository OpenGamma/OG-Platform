/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import javax.time.Instant;

import org.testng.annotations.Test;

/**
 * Tests the {@link ExternalFunctionCache} class.
 */
@Test
public class ExternalFunctionCacheTest {

  private File createTempFolder() {
    final File tmp = new File(System.getProperty("java.io.tmpdir"));
    assertTrue(tmp.exists());
    final File privateTmp = new File(tmp, System.getProperty("user.name") + "-" + getClass().getName() + "-" + System.currentTimeMillis());
    assertFalse(privateTmp.exists());
    privateTmp.mkdir();
    return privateTmp;
  }

  public void testLoadMissing() {
    final File temp = createTempFolder();
    try {
      System.setProperty(ExternalFunctionCache.CACHE_PATH_PROPERTY, temp.toString());
      final ExternalFunctionCache cache = ExternalFunctionCache.load();
      assertNotNull(cache);
      assertEquals(cache.getTimestamp(), Instant.EPOCH);
    } finally {
      temp.delete();
    }
  }

  public void testSaveAndLoad() {
    final File temp = createTempFolder();
    try {
      System.setProperty(ExternalFunctionCache.CACHE_PATH_PROPERTY, temp.toString());
      final Instant ts = Instant.now();
      ExternalFunctionCache cache = ExternalFunctionCache.create(ts, Arrays.asList(ExternalFunctionCacheTest.class.getName()));
      cache.save();
      try {
        assertTrue(new File(temp, ExternalFunctionCache.CACHE_FILE_NAME).exists());
        cache = ExternalFunctionCache.load();
        assertEquals(cache.getTimestamp(), ts);
        assertEquals(cache.getClassNames().size(), 1);
        assertEquals(cache.getClassNames().iterator().next(), ExternalFunctionCacheTest.class.getName());
      } finally {
        new File(temp, ExternalFunctionCache.CACHE_FILE_NAME).delete();
      }
    } finally {
      temp.delete();
    }
  }

  public void testGetClasses() throws ClassNotFoundException {
    final ExternalFunctionCache cache = ExternalFunctionCache.create(Instant.now(), Arrays.asList(ExternalFunctionCacheTest.class.getName()));
    final Collection<Class<?>> classes = cache.getClasses();
    assertEquals(classes.size(), 1);
    assertEquals(classes.iterator().next(), ExternalFunctionCacheTest.class);
  }

}
