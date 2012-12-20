/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import javax.time.Instant;

import org.testng.annotations.Test;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;

/**
 * Test Fudge encoding.
 */
@Test
public class VersionCorrectionFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Instant INSTANT1 = Instant.ofEpochSeconds(1);
  private static final Instant INSTANT2 = Instant.ofEpochSeconds(2);

  public void test_instants() {
    VersionCorrection object = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEncodeDecodeCycle(VersionCorrection.class, object);
  }

  public void test_latest() {
    VersionCorrection object = VersionCorrection.LATEST;
    assertEncodeDecodeCycle(VersionCorrection.class, object);
  }

}
