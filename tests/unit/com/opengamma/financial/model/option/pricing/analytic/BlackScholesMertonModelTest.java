/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.time.Instant;
import javax.time.InstantProvider;

import org.junit.Test;

import com.opengamma.financial.greeks.Delta;
import com.opengamma.financial.greeks.Gamma;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.Price;
import com.opengamma.financial.greeks.Rho;
import com.opengamma.financial.greeks.Theta;
import com.opengamma.financial.greeks.TimeBucketedRho;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 * @author emcleod
 */
public class BlackScholesMertonModelTest {
  private static final InstantProvider DATE = Instant.millisInstant(1000);
  private static final DiscountCurve CONSTANT_CURVE = new ConstantInterestRateDiscountCurve(0.09);
  private static final DiscountCurve CURVE;
  private static final double B = 0.09;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(0.19);

  static {
    Map<Double, Double> data = new HashMap<Double, Double>();
    data.put(0.25, 0.09);
    data.put(1.5, 0.09);
    data.put(2.5, 0.1);
    data.put(3.5, 0.05);
    CURVE = new DiscountCurve(data, new LinearInterpolator1D());
  }

  @Test
  public void test() {
    Expiry expiry = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1));
    EuropeanVanillaOptionDefinition definition = new EuropeanVanillaOptionDefinition(75, expiry, true);
    StandardOptionDataBundle vars = new StandardOptionDataBundle(CONSTANT_CURVE, B, SURFACE, 72, DATE);
    List<Greek> requiredGreeks = Arrays.asList(new Greek[] { new Price(), new Delta(), new Gamma(), new Rho(), new Theta() });
    System.out.println(new BlackScholesMertonModel().getGreeks(definition, vars, requiredGreeks));
    vars = new StandardOptionDataBundle(CURVE, B, SURFACE, 72, DATE);
    requiredGreeks = Arrays.asList(new Greek[] { new Price(), new Delta(), new Gamma(), new Rho(), new Theta(), new TimeBucketedRho() });
    System.out.println(new BlackScholesMertonModel().getGreeks(definition, vars, requiredGreeks));
  }
}
