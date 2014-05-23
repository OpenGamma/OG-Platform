/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test Fudge encoding.
 */
@Test(groups = TestGroup.UNIT)
public class VersionCorrectionFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Instant INSTANT1 = Instant.ofEpochSecond(1);
  private static final Instant INSTANT2 = Instant.ofEpochSecond(2);

  public void test_instants() {
    VersionCorrection object = VersionCorrection.of(INSTANT1, INSTANT2);
    assertEncodeDecodeCycle(VersionCorrection.class, object);
  }

  public void test_latest() {
    VersionCorrection object = VersionCorrection.LATEST;
    assertEncodeDecodeCycle(VersionCorrection.class, object);
  }

}
