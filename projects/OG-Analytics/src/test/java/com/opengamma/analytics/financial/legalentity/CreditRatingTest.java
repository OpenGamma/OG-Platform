/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests the credit rating object
 */
@Test(groups = TestGroup.UNIT)
public class CreditRatingTest {
  /** A rating string */
  private static final String RATING = "AAA";
  /** A rating description */
  private static final String DESCRIPTION = "Prime";
  /** An agency name */
  private static final String AGENCY = "Agency";
  /** Is the rating long term */
  private static final boolean IS_LONG_TERM = false;

  /**
   * Testing failure for null rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRating1() {
    CreditRating.of(null, AGENCY, IS_LONG_TERM);
  }

  /**
   * Testing failure for null rating
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRating2() {
    CreditRating.of(null, DESCRIPTION, AGENCY, IS_LONG_TERM);
  }

  /**
   * Testing failure for null description
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDescription() {
    CreditRating.of(RATING, null, AGENCY, IS_LONG_TERM);
  }

  /**
   * Tests getters, hashCode and equals
   */
  @Test
  public void testObject() {
    CreditRating rating = CreditRating.of(RATING, AGENCY, IS_LONG_TERM);
    CreditRating other = CreditRating.of(RATING, AGENCY, IS_LONG_TERM);
    assertEquals(AGENCY, rating.getAgencyName());
    assertEquals(RATING, rating.getRating());
    assertNull(rating.getRatingDescription());
    assertEquals(IS_LONG_TERM, rating.isLongTerm());
    assertEquals(rating, other);
    assertEquals(rating.hashCode(), other.hashCode());
    other = CreditRating.of(DESCRIPTION, AGENCY, IS_LONG_TERM);
    assertFalse(rating.equals(other));
    other = CreditRating.of(RATING, DESCRIPTION, IS_LONG_TERM);
    assertFalse(rating.equals(other));
    other = CreditRating.of(RATING, AGENCY, !IS_LONG_TERM);
    assertFalse(rating.equals(other));
    rating = CreditRating.of(RATING, DESCRIPTION, AGENCY, IS_LONG_TERM);
    assertEquals(AGENCY, rating.getAgencyName());
    assertEquals(RATING, rating.getRating());
    assertEquals(DESCRIPTION, rating.getRatingDescription());
    assertEquals(IS_LONG_TERM, rating.isLongTerm());
    other = CreditRating.of(RATING, DESCRIPTION, AGENCY, IS_LONG_TERM);
    assertEquals(rating, other);
    assertEquals(rating.hashCode(), other.hashCode());
    other = CreditRating.of(DESCRIPTION, DESCRIPTION, AGENCY, IS_LONG_TERM);
    assertFalse(rating.equals(other));
    other = CreditRating.of(RATING, AGENCY, AGENCY, IS_LONG_TERM);
    assertFalse(rating.equals(other));
    other = CreditRating.of(RATING, DESCRIPTION, DESCRIPTION, IS_LONG_TERM);
    assertFalse(rating.equals(other));
    other = CreditRating.of(RATING, DESCRIPTION, AGENCY, !IS_LONG_TERM);
    assertFalse(rating.equals(other));
  }
}
