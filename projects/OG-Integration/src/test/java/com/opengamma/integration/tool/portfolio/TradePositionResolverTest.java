/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Iterator;

import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.opengamma.integration.tool.portfolio.xml.TradePositionResolver;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TradePositionResolverTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTradeIdsCannotBeNull() {
    new TradePositionResolver(null);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetPositionsCannotBeCalledBeforeResolve() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.getPositions();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetOrphansCannotBeCalledBeforeResolve() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.getOrphans();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetDuplicatesCannotBeCalledBeforeResolve() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.getDuplicateTrades();
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testGetUnknownsCannotBeCalledBeforeResolve() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.getUnknownTrades();
  }

  @Test
  public void testPositionsAreEmptyWhenNoneAdded() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.resolve();
    assertTrue(resolver.getPositions().isEmpty());
  }

  @Test
  public void testAllTradesAreOrphansWhenNoPositionsAdded() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1", "T2"));
    resolver.resolve();
    assertEquals(resolver.getOrphans(), ImmutableSet.of("T1", "T2"));
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testPositionsCannotBeAddedAfterResolve() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.resolve();
    resolver.addToPosition("P1", "T1");
  }

  @Test
  public void testSimplePositionAddition() {
    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.addToPosition("P1", "T1");
    resolver.resolve();

    assertFalse(resolver.getOrphans().iterator().hasNext());
    Multimap<String, String> positions = resolver.getPositions();
    assertEquals(positions.size(), 1);
    assertEquals(positions.get("P1"), ImmutableSet.of("T1"));
  }

  @Test
  public void testDuplicatePositionTradeCombinationIsNotIgnored() {
    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.addToPosition("P1", "T1");
    resolver.addToPosition("P1", "T1");
    resolver.resolve();

    assertFalse(resolver.getOrphans().iterator().hasNext());
    Multimap<String, String> positions = resolver.getPositions();
    assertEquals(positions.size(), 2);
    assertEquals(positions.get("P1"), ImmutableList.of("T1", "T1"));
  }

  @Test
  public void testPositionAddition() {
    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1","T2","T3","T4","T5"));
    resolver.addToPosition("P1", "T1");
    resolver.addToPosition("P2", "T2");
    resolver.addToPosition("P1", "T3");
    resolver.addToPosition("P2", "T4");
    resolver.addToPosition("P1", "T5");
    resolver.resolve();

    assertFalse(resolver.getOrphans().iterator().hasNext());
    Multimap<String, String> positions = resolver.getPositions();
    assertEquals(positions.keySet().size(), 2);
    assertEquals(positions.size(), 5);
    assertEquals(positions.get("P1"), ImmutableSet.of("T1","T3","T5"));
    assertEquals(positions.get("P2"), ImmutableSet.of("T2","T4"));
  }

  @Test
  public void testMissingTradesAreIdentified() {

    // The XMl load process should mean this can't happen in
    // the actual load, but ...
    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.addToPosition("P1", "T1");
    resolver.addToPosition("P1", "T2");
    resolver.resolve();

    assertEquals(ImmutableSet.copyOf(resolver.getUnknownTrades()), ImmutableSet.of("T2"));
  }

  @Test
  public void testDuplicatedTradesAreIdentified() {
    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1"));
    resolver.addToPosition("P1", "T1");
    resolver.addToPosition("P2", "T1");
    resolver.resolve();

    // Multimap comparison seems somewhat awkward, therefore do comparisons with
    // the more familiar java collection methods
    Multimap<String, String> duplicateTrades = resolver.getDuplicateTrades();
    assertEquals(duplicateTrades.keySet().size(), 1);
    assertEquals(duplicateTrades.get("T1"), ImmutableSet.of("P1", "P2"));
  }

  @Test
  public void testCombined() {

    TradePositionResolver resolver = new TradePositionResolver(ImmutableSet.of("T1","T2","T3","T4","T5"));
    resolver.addToPosition("P1", "T1");
    resolver.addToPosition("P2", "T1");
    resolver.addToPosition("P2", "T2");
    resolver.addToPosition("P1", "T3");
    resolver.addToPosition("P2", "T4");
    resolver.addToPosition("P3", "T6");
    resolver.resolve();

    Iterator<String> orphans = resolver.getOrphans().iterator();
    assertEquals(orphans.next(), "T5");
    assertFalse(orphans.hasNext());

    Multimap<String, String> positions = resolver.getPositions();
    assertEquals(positions.keySet().size(), 3);
    assertEquals(positions.get("P1"), ImmutableList.of("T1", "T3"));
    assertEquals(positions.get("P2"), ImmutableList.of("T1", "T2", "T4"));
    assertEquals(positions.get("P3"), ImmutableList.of("T6"));

    assertEquals(ImmutableSet.copyOf(resolver.getUnknownTrades()), ImmutableSet.of("T6"));

    Multimap<String, String> duplicateTrades = resolver.getDuplicateTrades();
    assertEquals(duplicateTrades.keySet().size(), 1);
    assertEquals(duplicateTrades.get("T1"), ImmutableSet.of("P1", "P2"));
  }
}
