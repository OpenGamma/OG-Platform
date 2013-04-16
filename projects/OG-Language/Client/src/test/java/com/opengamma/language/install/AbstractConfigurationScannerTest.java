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
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.language.install.ConfigurationScanner.ConfigurationListener;
import com.opengamma.util.test.TestGroup;

/**
 * Tests methods in the {@link AbstractConfigurationScanner} class.
 */
@Test(groups = TestGroup.UNIT)
public class AbstractConfigurationScannerTest {

  private static class Scanner extends AbstractConfigurationScanner {
  }

  private static class Listener implements ConfigurationListener {

    private Set<Configuration> _configurations;
    private Boolean _complete;

    @Override
    public void foundConfigurations(final Set<Configuration> configurations, final boolean complete) {
      _configurations = configurations;
      _complete = complete;
    }

    public Set<Configuration> getConfigurations() {
      final Set<Configuration> configurations = _configurations;
      _configurations = null;
      return configurations;
    }

    public Boolean isComplete() {
      return _complete;
    }

  }

  public void testAddConfigurationBeforeListener() {
    final Scanner scanner = new Scanner();
    scanner.addConfiguration(new Configuration(URI.create("http://localhost/A"), "A"));
    assertEquals(scanner.getConfigurations().size(), 1);
    assertFalse(scanner.isComplete());
    scanner.addConfiguration(new Configuration(URI.create("http://localhost/B"), "B"));
    assertEquals(scanner.getConfigurations().size(), 2);
    assertFalse(scanner.isComplete());
    final Listener listener1 = new Listener();
    scanner.addListener(listener1);
    assertEquals(listener1.getConfigurations(), null);
    assertEquals(listener1.isComplete(), null);
    scanner.complete();
    assertTrue(scanner.isComplete());
    assertEquals(listener1.getConfigurations().size(), 2);
    assertEquals(listener1.isComplete(), Boolean.TRUE);
    final Listener listener2 = new Listener();
    scanner.addListener(listener2);
    assertEquals(listener2.getConfigurations().size(), 2);
    assertEquals(listener2.isComplete(), Boolean.TRUE);
  }

  public void testAddConfigurationAfterListener() {
    final Scanner scanner = new Scanner();
    final Listener listener = new Listener();
    scanner.addListener(listener);
    scanner.addConfiguration(new Configuration(URI.create("http://localhost/A"), "A"));
    assertEquals(listener.getConfigurations().size(), 1);
    assertEquals(listener.isComplete(), Boolean.FALSE);
    scanner.addConfiguration(new Configuration(URI.create("http://localhost/B"), "B"));
    assertEquals(listener.getConfigurations().size(), 2);
    assertEquals(listener.isComplete(), Boolean.FALSE);
    scanner.complete();
    assertEquals(listener.getConfigurations().size(), 2);
    assertEquals(listener.isComplete(), Boolean.TRUE);
  }

  public void testAddConfigurationsBeforeListener() {
    final Scanner scanner = new Scanner();
    scanner.addConfigurations(Arrays.asList(new Configuration(URI.create("http://localhost/A"), "A"), new Configuration(URI.create("http://localhost/B"), "B")));
    assertEquals(scanner.getConfigurations().size(), 2);
    assertFalse(scanner.isComplete());
    final Listener listener1 = new Listener();
    scanner.addListener(listener1);
    assertEquals(listener1.getConfigurations(), null);
    assertEquals(listener1.isComplete(), null);
    scanner.complete();
    assertTrue(scanner.isComplete());
    assertEquals(listener1.getConfigurations().size(), 2);
    assertEquals(listener1.isComplete(), Boolean.TRUE);
    final Listener listener2 = new Listener();
    scanner.addListener(listener2);
    assertEquals(listener2.getConfigurations().size(), 2);
    assertEquals(listener2.isComplete(), Boolean.TRUE);
  }

  public void testAddConfigurationsAfterListener() {
    final Scanner scanner = new Scanner();
    final Listener listener = new Listener();
    scanner.addListener(listener);
    scanner.addConfigurations(Arrays.asList(new Configuration(URI.create("http://localhost/A"), "A"), new Configuration(URI.create("http://localhost/B"), "B")));
    assertEquals(listener.getConfigurations().size(), 2);
    assertEquals(listener.isComplete(), Boolean.FALSE);
    scanner.complete();
    assertEquals(listener.getConfigurations().size(), 2);
    assertEquals(listener.isComplete(), Boolean.TRUE);
  }

  public void testRemoveListener() {
    final Scanner scanner = new Scanner();
    final Listener listener = new Listener();
    scanner.addListener(listener);
    scanner.removeListener(listener);
    scanner.addConfiguration(new Configuration(URI.create("http://localhost/A"), "A"));
    assertEquals(listener.getConfigurations(), null);
    assertEquals(listener.isComplete(), null);
  }

}
