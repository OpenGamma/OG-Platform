/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;

import org.testng.annotations.Test;

import com.google.common.io.Files;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BerkeleyDBTempTargetRepositoryTest {

  private BerkeleyDBTempTargetRepository createTempTargetRepository() {
    final File tmp = Files.createTempDir();
    final BerkeleyDBTempTargetRepository targets = new BerkeleyDBTempTargetRepository(tmp);
    targets.start();
    assertTrue(targets.isRunning());
    return targets;
  }

  public void testNoGenerations() {
    final BerkeleyDBTempTargetRepository targets = createTempTargetRepository();
    try {
      assertNull(targets.getOldGeneration(0));
      assertNull(targets.getNewGeneration(0));
      assertFalse(targets.copyOldToNewGeneration(0, new ArrayList<Long>()));
      targets.nextGeneration();
    } finally {
      targets.stop();
    }
  }

  public void testTargetMatches() {
    final BerkeleyDBTempTargetRepository targets = createTempTargetRepository();
    try {
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Foo")), UniqueId.of("Tmp", "0"));
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Bar")), UniqueId.of("Tmp", "1"));
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Foo")), UniqueId.of("Tmp", "0"));
      assertEquals(targets.getNewGeneration(0), new MockTempTarget("Foo"));
      assertEquals(targets.getNewGeneration(1), new MockTempTarget("Bar"));
      assertTrue(targets.copyOldToNewGeneration(0, new ArrayList<Long>()));
      targets.nextGeneration();
      assertEquals(targets.getOldGeneration(0), new MockTempTarget("Foo"));
      assertEquals(targets.getOldGeneration(1), new MockTempTarget("Bar"));
      assertNull(targets.getNewGeneration(0));
      assertNull(targets.getNewGeneration(1));
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Cow")), UniqueId.of("Tmp", "2"));
      assertEquals(targets.getNewGeneration(2), new MockTempTarget("Cow"));
    } finally {
      targets.stop();
    }
  }

  public void testRotation() {
    final BerkeleyDBTempTargetRepository targets = createTempTargetRepository();
    try {
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Foo")), UniqueId.of("Tmp", "0"));
      final long t = System.nanoTime();
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Bar")), UniqueId.of("Tmp", "1"));
      assertTrue(targets.copyOldToNewGeneration(0, new ArrayList<Long>()));
      targets.nextGeneration();
      assertEquals(targets.locateOrStoreImpl(new MockTempTarget("Cow")), UniqueId.of("Tmp", "2"));
      assertTrue(targets.copyOldToNewGeneration(t, new ArrayList<Long>()));
      targets.nextGeneration();
      assertNull(targets.getOldGeneration(0));
      assertEquals(targets.getOldGeneration(1), new MockTempTarget("Bar"));
      assertEquals(targets.getOldGeneration(2), new MockTempTarget("Cow"));
    } finally {
      targets.stop();
    }
  }

}
