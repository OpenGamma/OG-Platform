/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.masterdb;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Abstract integration test accessing local masters.
 */
@Test
public abstract class AbstractLocalMastersTest {

  private static LocalMastersTestUtils s_testUtils;

  @BeforeClass(groups = TestGroup.INTEGRATION)
  public static synchronized void setupSuite() {
    System.out.println("Setup LocalMastersTestUtils");
    if (s_testUtils == null) {
      System.out.println("Starting LocalMastersTestUtils");
      s_testUtils = new LocalMastersTestUtils();
    }
  }

  protected MastersTestUtils getTestHelper() {
    final LocalMastersTestUtils testUtils = s_testUtils;
    if (testUtils == null) {
      throw new IllegalStateException("LocalMastersTestUtils must not be null");
    }
    return testUtils;
  }

  @AfterSuite(groups = TestGroup.INTEGRATION)
  public static final void cleanupTestUtils() throws Exception {
    s_testUtils.tearDown();
  }

}
