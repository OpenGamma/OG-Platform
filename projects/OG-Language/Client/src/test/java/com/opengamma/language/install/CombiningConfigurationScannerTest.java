/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.install;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link CombiningConfigurationScanner} class.
 */
@Test(groups = TestGroup.UNIT)
public class CombiningConfigurationScannerTest {

  public void testNoScanners() {
    final CombiningConfigurationScanner scanner = new CombiningConfigurationScanner();
    assertFalse(scanner.isComplete());
    scanner.start();
    assertTrue(scanner.isComplete());
    assertEquals(scanner.getConfigurations(), Collections.emptySet());
  }

  public void testOneScanner() {
    final CombiningConfigurationScanner scanner = new CombiningConfigurationScanner();
    scanner.addConfigurationScanner(new StaticConfigurationScanner(Arrays.asList(new Configuration(URI.create("http://localhost/A"), "A"), new Configuration(URI.create("http://localhost/B"), "B"))));
    assertFalse(scanner.isComplete());
    scanner.start();
    assertTrue(scanner.isComplete());
    assertEquals(scanner.getConfigurations().size(), 2);
  }

  public void testTwoScanners() {
    final CombiningConfigurationScanner scanner = new CombiningConfigurationScanner();
    scanner.addConfigurationScanner(new StaticConfigurationScanner(Arrays.asList(new Configuration(URI.create("http://localhost/A"), "A"), new Configuration(URI.create("http://localhost/B"), "B"))));
    scanner.addConfigurationScanner(new StaticConfigurationScanner(Arrays.asList(new Configuration(URI.create("http://localhost/B"), "B"), new Configuration(URI.create("http://localhost/C"), "C"))));
    assertFalse(scanner.isComplete());
    scanner.start();
    assertTrue(scanner.isComplete());
    assertEquals(scanner.getConfigurations().size(), 3);
  }

}
