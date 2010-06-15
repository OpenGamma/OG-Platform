/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorInterestRateDataBundle;
import com.opengamma.financial.model.volatility.curve.ConstantVolatilityCurve;
import com.opengamma.util.time.DateUtil;

/**
 * 
 * @author emcleod
 */
public class HullWhiteOneFactorInterestRateModelTest {

  @Test
  public void test() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final ZonedDateTime maturity = DateUtil.getDateOffsetWithYearFraction(date, 5);
    final ZonedDateTime time = DateUtil.getDateOffsetWithYearFraction(date, 1);
    final HullWhiteOneFactorInterestRateDataBundle data = new HullWhiteOneFactorInterestRateDataBundle(date, new ConstantYieldCurve(0.05), 0.1, new ConstantVolatilityCurve(0.01));
    final HullWhiteOneFactorInterestRateModel model = new HullWhiteOneFactorInterestRateModel();
    //System.out.println(model.getInterestRateFunction(time, maturity).evaluate(data));
  }
}
