/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.schedulegeneration.isda;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.CreditDefaultSwapDefinitionDataSets;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class ISDACompliantPremiumLegScheduleGenerationTest {
  private static final GenerateCreditDefaultSwapPremiumLegSchedule DEPRECATED_CALCULATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();
  private static final GenerateCreditDefaultSwapPremiumLegScheduleNew CALCULATOR = new GenerateCreditDefaultSwapPremiumLegScheduleNew();
  private static final ZonedDateTime VALUATION_DATE = DateUtils.getUTCDate(2013, 1, 6);

  @Test
  public void regressionTest() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final ZonedDateTime[] deprecatedResult = DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    final ZonedDateTime[] result = CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
    assertDateArrayEquals(deprecatedResult, result);
  }

  @Test(enabled = false)
  public void timeBDeprecated() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      DEPRECATED_CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Deprecated:\t" + (endTime - startTime) / j * 100);
  }

  @Test(enabled = false)
  public void timeARefactored() {
    final CreditDefaultSwapDefinition cds = CreditDefaultSwapDefinitionDataSets.getLegacyVanillaCreditDefaultSwapDefinition().withMaturityDate(VALUATION_DATE.plusYears(10));
    final double startTime = System.currentTimeMillis();
    int j = 0;
    for (int i = 0; i < 1000000; i++) {
      CALCULATOR.constructISDACompliantCreditDefaultSwapPremiumLegSchedule(cds);
      j++;
    }
    final double endTime = System.currentTimeMillis();
    System.out.println("Refactored:\t" + (endTime - startTime) / j * 100);
  }

  private void assertDateArrayEquals(final ZonedDateTime[] expected, final ZonedDateTime[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
