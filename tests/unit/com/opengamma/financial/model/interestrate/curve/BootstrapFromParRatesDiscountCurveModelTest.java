/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.interestrate.curve;

import java.util.HashSet;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.definition.FixedInterestRateInstrumentDefinition;
import com.opengamma.financial.model.interestrate.definition.ParBondInstrumentDefinition;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class BootstrapFromParRatesDiscountCurveModelTest {

  @Test
  public void test() {
    final ZonedDateTime date = DateUtil.getUTCDate(2009, 1, 1);
    final Set<FixedInterestRateInstrumentDefinition> data = new HashSet<FixedInterestRateInstrumentDefinition>();
    data.add(new ParBondInstrumentDefinition(new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 1)), 0.06));
    data.add(new ParBondInstrumentDefinition(new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 2)), 0.07));
    data.add(new ParBondInstrumentDefinition(new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 3)), 0.075));
    data.add(new ParBondInstrumentDefinition(new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 4)), 0.08));
    data.add(new ParBondInstrumentDefinition(new Expiry(DateUtil.getDateOffsetWithYearFraction(date, 5)), 0.085));
    final DiscountCurve curve = new BootstrapFromParRatesDiscountCurveModel(new LinearInterpolator1D()).getCurve(data, date);
    System.out.println(curve.getData());
  }
}
