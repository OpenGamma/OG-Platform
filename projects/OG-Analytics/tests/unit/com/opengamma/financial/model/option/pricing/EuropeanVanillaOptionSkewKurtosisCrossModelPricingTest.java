/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.financial.model.option.definition.EdgeworthSkewKurtosisBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.SkewKurtosisOptionDataBundle;
import com.opengamma.financial.model.option.pricing.analytic.ModifiedCorradoSuSkewnessKurtosisModel;
import com.opengamma.financial.model.option.pricing.tree.BinomialOptionModel;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.surface.ConstantDoublesSurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanVanillaOptionSkewKurtosisCrossModelPricingTest {
  private static final Double STRIKE = 9.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  @SuppressWarnings("unused")
  private static final SkewKurtosisOptionDataBundle NORMAL_DATA = new SkewKurtosisOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.08)), 0.08, new VolatilitySurface(
      ConstantDoublesSurface.from(0.3)), 10.,
      DATE, 0., 3.);
  @SuppressWarnings("unused")
  private static final SkewKurtosisOptionDataBundle DATA = new SkewKurtosisOptionDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.08)), 0.08, new VolatilitySurface(
      ConstantDoublesSurface.from(0.3)), 10., DATE, 1.,
      3.);
  @SuppressWarnings("unused")
  private static final List<Greek> REQUIRED_GREEKS = Arrays.asList(Greek.FAIR_PRICE);

  @Test
  public void testNormal() {
    @SuppressWarnings("unused")
    final OptionDefinition call = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    @SuppressWarnings("unused")
    final OptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> model1 = new ModifiedCorradoSuSkewnessKurtosisModel();
    @SuppressWarnings("unused")
    final OptionModel<OptionDefinition, SkewKurtosisOptionDataBundle> model2 = new BinomialOptionModel<SkewKurtosisOptionDataBundle>(new EdgeworthSkewKurtosisBinomialOptionModelDefinition(), 10);
    // System.out.println(model1.getGreeks(call, DATA, REQUIRED_GREEKS));
    // System.out.println(model2.getGreeks(call, DATA, REQUIRED_GREEKS));
    // System.out.println(new BlackScholesMertonModel().getGreeks(call, DATA,
    // REQUIRED_GREEKS));
  }
}
