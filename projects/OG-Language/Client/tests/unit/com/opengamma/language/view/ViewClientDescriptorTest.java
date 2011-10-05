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

  private static final UniqueId[] VIEW_IDENTIFIERS = new UniqueId[] {UniqueId.of("Foo", "Bar"), UniqueId.of("LiveMarketData", "Bar"), UniqueId.of("UserMarketData", "Bar") };

  private void assertCycle(final ViewClientDescriptor viewClientDescriptor) {
    final String encoded = viewClientDescriptor.encode();
    final ViewClientDescriptor decoded = ViewClientDescriptor.decode(encoded);
    assertEquals(decoded, viewClientDescriptor);
  }

  public void testTickingMarketData() {
    for (UniqueId viewId : VIEW_IDENTIFIERS) {
      assertCycle(ViewClientDescriptor.tickingMarketData(viewId));
    }
  }

  public void testStaticMarketData() {
    for (UniqueId viewId : VIEW_IDENTIFIERS) {
      assertCycle(ViewClientDescriptor.staticMarketData(viewId, Instant.now()));
    }
  }

  public void testSampleMarketData() {
    for (UniqueId viewId : VIEW_IDENTIFIERS) {
      assertCycle(ViewClientDescriptor.sampleMarketData(viewId, Instant.now(), Instant.now()));
      assertCycle(ViewClientDescriptor.sampleMarketData(viewId, Instant.now(), Instant.now(), 3600));
    }
  }

  public void testTickingSnapshot() {
    for (UniqueId viewId : VIEW_IDENTIFIERS) {
      assertCycle(ViewClientDescriptor.tickingSnapshot(viewId, UniqueId.of("Foo", "Bar")));
    }
  }

  public void testStaticSnapshot() {
    for (UniqueId viewId : VIEW_IDENTIFIERS) {
      assertCycle(ViewClientDescriptor.staticSnapshot(viewId, UniqueId.of("Foo", "Bar")));
    }
  }

  public void testUnescapedViewName() {
    for (UniqueId viewId : VIEW_IDENTIFIERS) {
      final ViewClientDescriptor decoded = ViewClientDescriptor.decode(viewId.toString());
      assertEquals(decoded, ViewClientDescriptor.tickingMarketData(viewId));
    }
  }

}
