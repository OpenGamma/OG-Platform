/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.obligor;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests that the migration from credit rating enums to object code works as expected.
 */
@Test(groups = TestGroup.UNIT)
@SuppressWarnings("deprecation")
public class CreditRatingDelegateTest {
  /** The generic agency name */
  private static final String GENERIC_AGENCY = "GENERIC";
  /** Fitch */
  private static final String FITCH = "Fitch";
  /** Moody's */
  private static final String MOODYS = "Moody's";
  /** Standard and Poor's */
  private static final String SANDP = "S&P";

  /**
   * Tests that the generic credit rating object is correctly converted to @{link com.opengamma.analytics.financial.obligor.CreditRating}
   */
  @Test
  public void testGenericDelegation() {
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.AAA.toString(), GENERIC_AGENCY, true), CreditRating.AAA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.AA.toString(), GENERIC_AGENCY, true), CreditRating.AA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.A.toString(), GENERIC_AGENCY, true), CreditRating.A.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.BBB.toString(), GENERIC_AGENCY, true), CreditRating.BBB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.BB.toString(), GENERIC_AGENCY, true), CreditRating.BB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.B.toString(), GENERIC_AGENCY, true), CreditRating.B.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.CCC.toString(), GENERIC_AGENCY, true), CreditRating.CCC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.CC.toString(), GENERIC_AGENCY, true), CreditRating.CC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.C.toString(), GENERIC_AGENCY, true), CreditRating.C.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.DEFAULT.toString(), GENERIC_AGENCY, true), CreditRating.DEFAULT.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRating.NR.toString(), GENERIC_AGENCY, true), CreditRating.NR.toCreditRating());
  }

  /**
   * Tests that the Fitch credit rating object is correctly converted to @{link com.opengamma.analytics.financial.obligor.CreditRating}
   */
  @Test
  public void testFitchDelegation() {
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.AAA.toString(), CreditRatingFitch.AAA.getRatingDescription(), FITCH, true), CreditRatingFitch.AAA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.AA.toString(), CreditRatingFitch.AA.getRatingDescription(), FITCH, true), CreditRatingFitch.AA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.A.toString(), CreditRatingFitch.A.getRatingDescription(), FITCH, true), CreditRatingFitch.A.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.BBB.toString(), CreditRatingFitch.BBB.getRatingDescription(), FITCH, true), CreditRatingFitch.BBB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.BB.toString(), CreditRatingFitch.BB.getRatingDescription(), FITCH, true), CreditRatingFitch.BB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.B.toString(), CreditRatingFitch.B.getRatingDescription(), FITCH, true), CreditRatingFitch.B.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.CCC.toString(), CreditRatingFitch.CCC.getRatingDescription(), FITCH, true), CreditRatingFitch.CCC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.CC.toString(), CreditRatingFitch.CC.getRatingDescription(), FITCH, true), CreditRatingFitch.CC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.C.toString(), CreditRatingFitch.C.getRatingDescription(), FITCH, true), CreditRatingFitch.C.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.DEFAULT.toString(), CreditRatingFitch.DEFAULT.getRatingDescription(), FITCH, true), CreditRatingFitch.DEFAULT.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingFitch.NR.toString(), CreditRatingFitch.NR.getRatingDescription(), FITCH, true), CreditRatingFitch.NR.toCreditRating());
  }

  /**
   * Tests that the Moody's credit rating object is correctly converted to @{link com.opengamma.analytics.financial.obligor.CreditRating}
   */
  @Test
  public void testMoodysDelegation() {
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.AAA.toString(), CreditRatingMoodys.AAA.getRatingDescription(), MOODYS, true), CreditRatingMoodys.AAA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.AA.toString(), CreditRatingMoodys.AA.getRatingDescription(), MOODYS, true), CreditRatingMoodys.AA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.A.toString(), CreditRatingMoodys.A.getRatingDescription(), MOODYS, true), CreditRatingMoodys.A.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.BBB.toString(), CreditRatingMoodys.BBB.getRatingDescription(), MOODYS, true), CreditRatingMoodys.BBB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.BB.toString(), CreditRatingMoodys.BB.getRatingDescription(), MOODYS, true), CreditRatingMoodys.BB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.B.toString(), CreditRatingMoodys.B.getRatingDescription(), MOODYS, true), CreditRatingMoodys.B.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.CCC.toString(), CreditRatingMoodys.CCC.getRatingDescription(), MOODYS, true), CreditRatingMoodys.CCC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.CC.toString(), CreditRatingMoodys.CC.getRatingDescription(), MOODYS, true), CreditRatingMoodys.CC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.C.toString(), CreditRatingMoodys.C.getRatingDescription(), MOODYS, true), CreditRatingMoodys.C.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.DEFAULT.toString(), CreditRatingMoodys.DEFAULT.getRatingDescription(), MOODYS, true), CreditRatingMoodys.DEFAULT.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingMoodys.NR.toString(), CreditRatingMoodys.NR.getRatingDescription(), MOODYS, true), CreditRatingMoodys.NR.toCreditRating());
  }

  /**
   * Tests that the S&P credit rating object is correctly converted to @{link com.opengamma.analytics.financial.obligor.CreditRating}
   */
  @Test
  public void testSAndPDelegation() {
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.AAA.toString(), CreditRatingStandardAndPoors.AAA.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.AAA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.AA.toString(), CreditRatingStandardAndPoors.AA.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.AA.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.A.toString(), CreditRatingStandardAndPoors.A.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.A.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.BBB.toString(), CreditRatingStandardAndPoors.BBB.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.BBB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.BB.toString(), CreditRatingStandardAndPoors.BB.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.BB.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.B.toString(), CreditRatingStandardAndPoors.B.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.B.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.CCC.toString(), CreditRatingStandardAndPoors.CCC.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.CCC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.CC.toString(), CreditRatingStandardAndPoors.CC.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.CC.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.C.toString(), CreditRatingStandardAndPoors.C.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.C.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.DEFAULT.toString(), CreditRatingStandardAndPoors.DEFAULT.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.DEFAULT.toCreditRating());
    assertEquals(com.opengamma.analytics.financial.legalentity.CreditRating.of(CreditRatingStandardAndPoors.NR.toString(), CreditRatingStandardAndPoors.NR.getRatingDescription(), SANDP, true), CreditRatingStandardAndPoors.NR.toCreditRating());
  }
}
