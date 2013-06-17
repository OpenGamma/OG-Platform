/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isda;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

/**
 * 
 */
public class ISDACompliantScheduleGeneratorTest {

  @Test
  public void getIntegrationNodesAsDatesTest() {
    final LocalDate startDate = LocalDate.of(2013, 2, 13);
    final LocalDate endDate = LocalDate.of(2015, 6, 30);
    final LocalDate[] discCurveDates = new LocalDate[] {LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2014, 7, 2), LocalDate.of(2015, 6, 30)};
    final LocalDate[] spreadCurveDates = new LocalDate[] {LocalDate.of(2013, 2, 23), LocalDate.of(2013, 4, 13), LocalDate.of(2014, 2, 17), LocalDate.of(2017, 4, 30), LocalDate.of(2014, 7, 2),
        LocalDate.of(2015, 4, 30)};

    final LocalDate[] expected = new LocalDate[] {startDate, LocalDate.of(2013, 2, 23), LocalDate.of(2013, 3, 13), LocalDate.of(2013, 4, 12), LocalDate.of(2013, 4, 13), LocalDate.of(2014, 2, 17),
        LocalDate.of(2014, 7, 2), LocalDate.of(2015, 4, 30), endDate};
    final int n = expected.length;

    LocalDate[] res = ISDACompliantScheduleGenerator.getIntegrationNodesAsDates(startDate, endDate, discCurveDates, spreadCurveDates);
    assertEquals("",n,res.length);
    for(int i=0;i<n;i++) {
      assertTrue(expected[i].equals(res[i]));
    }

  }

}
