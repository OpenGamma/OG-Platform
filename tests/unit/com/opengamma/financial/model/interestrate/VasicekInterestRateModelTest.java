/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.definition.VasicekDataBundle;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
public class VasicekInterestRateModelTest {

  @Test
  public void test() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    ZonedDateTime maturity = DateUtil.getDateOffsetWithYearFraction(date, 1);
    VasicekDataBundle data = new VasicekDataBundle(0.05, 0.05, 0.15, 0.01, date);
    final VasicekInterestRateModel model = new VasicekInterestRateModel();
    //System.out.println(model.getInterestRateFunction(date, maturity).evaluate(data));
    data = new VasicekDataBundle(0.08, 0.09, 0.05, 0.03, date);
    maturity = DateUtil.getDateOffsetWithYearFraction(date, 2);
    //System.out.println(model.getInterestRateFunction(date, maturity).evaluate(data));
  }
}
