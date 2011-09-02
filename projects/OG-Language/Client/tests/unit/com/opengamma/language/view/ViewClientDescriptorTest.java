/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;

/**
 * Tests the {@link ViewClientDescriptor} class.
 */
@Test
public class ViewClientDescriptorTest {

  private static final String[] VIEW_NAMES = new String[] {"Foo", "Foo~", "Foo$" };

  private void assertCycle(final ViewClientDescriptor viewClientDescriptor) {
    final String encoded = viewClientDescriptor.encode();
    System.out.println(encoded);
    final ViewClientDescriptor decoded = ViewClientDescriptor.decode(encoded);
    assertEquals(decoded, viewClientDescriptor);
  }

  public void testTickingMarketData() {
    for (String viewName : VIEW_NAMES) {
      assertCycle(ViewClientDescriptor.tickingMarketData(viewName));
    }
  }

  public void testStaticMarketData() {
    for (String viewName : VIEW_NAMES) {
      assertCycle(ViewClientDescriptor.staticMarketData(viewName, Instant.now()));
    }
  }

  public void testTickingSnapshot() {
    for (String viewName : VIEW_NAMES) {
      assertCycle(ViewClientDescriptor.tickingSnapshot(viewName, UniqueId.of("Foo", "Bar")));
    }
  }

  public void testStaticSnapshot() {
    for (String viewName : VIEW_NAMES) {
      assertCycle(ViewClientDescriptor.staticSnapshot(viewName, UniqueId.of("Foo", "Bar")));
    }
  }

}
