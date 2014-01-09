/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test the methods of RollDateAdjusterUtils
 */
@Test(groups = TestGroup.UNIT)
public class RollDateAdjusterUtilsTest {

  private static final RollDateAdjuster QUARTERLY_IMM_ADJUSTER = RollDateAdjusterFactory.getAdjuster(RollDateAdjusterFactory.QUARTERLY_IMM_ROLL_STRING);

  @Test
  public void nthDate() {
    final ZonedDateTime startingPoint = DateUtils.getUTCDate(2013, 9, 3);
    final int[] nbRoll = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
    final ZonedDateTime[] expectedDates = new ZonedDateTime[] {DateUtils.getUTCDate(2013, 9, 18), DateUtils.getUTCDate(2013, 12, 18), DateUtils.getUTCDate(2014, 3, 19),
      DateUtils.getUTCDate(2014, 6, 18), DateUtils.getUTCDate(2014, 9, 17), DateUtils.getUTCDate(2014, 12, 17), DateUtils.getUTCDate(2015, 3, 18),
      DateUtils.getUTCDate(2015, 6, 17), DateUtils.getUTCDate(2015, 9, 16), DateUtils.getUTCDate(2015, 12, 16), DateUtils.getUTCDate(2016, 3, 16),
      DateUtils.getUTCDate(2016, 6, 15) };
    int nbTest = nbRoll.length;
    for (int loopt = 0; loopt < nbTest; loopt++) {
      ZonedDateTime rolledDate = RollDateAdjusterUtils.nthDate(startingPoint, QUARTERLY_IMM_ADJUSTER, nbRoll[loopt]);
      assertEquals("RollDateAdjusterUtils - nthDate", expectedDates[loopt], rolledDate);
    }
  }

}
