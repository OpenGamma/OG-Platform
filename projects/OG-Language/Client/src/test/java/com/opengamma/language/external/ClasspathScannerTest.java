/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.external;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.lang.annotation.ExternalFunction;

/**
 * Tests the {@link ClasspathScanner} class.
 */
@Test
public class ClasspathScannerTest {

  @ExternalFunction
  public ClasspathScannerTest() {
  }

  public void testTimestamp() {
    final ClasspathScanner scanner = new ClasspathScanner();
    final Instant instant = scanner.getTimestamp();
    assertNotNull(instant);
    assertTrue(instant.isAfter(Instant.EPOCH));
    assertFalse(Instant.now().isBefore(instant));
  }

  public void testScan() throws ClassNotFoundException {
    final ClasspathScanner scanner = new ClasspathScanner();
    final ExternalFunctionCache cache = scanner.scan();
    assertNotNull(cache);
    assertTrue(cache.getTimestamp().isAfter(Instant.EPOCH));
    final Collection<Class<?>> classes = cache.getClasses();
    assertNotNull(classes);
    assertFalse(classes.isEmpty());
  }

}
