/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.option.definition.AmericanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.BinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.CoxRossRubinsteinBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.LeisenReimerBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.OptionDefinition;
import com.opengamma.financial.model.option.definition.RendlemanBartterBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.option.definition.TrisgeorgisBinomialOptionModelDefinition;
import com.opengamma.financial.model.option.pricing.analytic.AnalyticOptionModel;
import com.opengamma.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.financial.model.option.pricing.analytic.BlackScholesMertonModel;
import com.opengamma.financial.model.option.pricing.tree.BinomialOptionModel;
import com.opengamma.financial.model.option.pricing.tree.TreeOptionModel;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

public class VanillaOptionCrossModelPricingTest {
  private static final Double STRIKE = 9.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantYieldCurve(0.08), 0.08, new ConstantVolatilitySurface(0.3), 10., DATE);
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> CRR = new CoxRossRubinsteinBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> LR = new LeisenReimerBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> RB = new RendlemanBartterBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> TRISGEORGIS = new TrisgeorgisBinomialOptionModelDefinition();
  private static final Set<Greek> REQUIRED_GREEKS = Sets.newHashSet(Greek.FAIR_PRICE, Greek.DELTA, Greek.GAMMA);
  private static final double EPS = 0.02;

  @Test
  public void testEuropeanOption() {
    final OptionDefinition call = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    final OptionDefinition put = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false);
    final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> bsm = new BlackScholesMertonModel();
    TreeOptionModel<OptionDefinition, StandardOptionDataBundle> binomial = new BinomialOptionModel<StandardOptionDataBundle>(CRR, 1001, 5);
    testGreeks(call, binomial, bsm);
    testGreeks(put, binomial, bsm);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(LR, 1001, 5);
    testGreeks(call, binomial, bsm);
    testGreeks(put, binomial, bsm);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(RB, 1001, 5);
    testGreeks(call, binomial, bsm);
    testGreeks(put, binomial, bsm);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(TRISGEORGIS, 1001, 5);
    testGreeks(call, binomial, bsm);
    testGreeks(put, binomial, bsm);
  }

  @Test
  public void testAmericanOption() {
    final AmericanVanillaOptionDefinition call = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    final OptionDefinition put = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, false);
    final AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> bs = new BjerksundStenslandModel();
    TreeOptionModel<OptionDefinition, StandardOptionDataBundle> binomial = new BinomialOptionModel<StandardOptionDataBundle>(CRR);
    testGreeks(call, binomial, bs);
    testGreeks(put, binomial, bs);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(LR, 1001, 5);
    testGreeks(call, binomial, bs);
    testGreeks(put, binomial, bs);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(RB, 1001, 5);
    testGreeks(call, binomial, bs);
    testGreeks(put, binomial, bs);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(TRISGEORGIS, 1001, 5);
    testGreeks(call, binomial, bs);
    testGreeks(put, binomial, bs);
  }

  @SuppressWarnings("unchecked")
  private <T extends OptionDefinition, U extends T> void testGreeks(final T definition, final OptionModel<T, StandardOptionDataBundle> first, final OptionModel<U, StandardOptionDataBundle> second) {
    final GreekResultCollection firstResult = first.getGreeks(definition, DATA, REQUIRED_GREEKS);
    final GreekResultCollection secondResult = second.getGreeks((U) definition, DATA, REQUIRED_GREEKS);
    assertEquals(firstResult.size(), secondResult.size());
    final Iterator<Greek> iter = firstResult.keySet().iterator();
    while (iter.hasNext()) {
      final Greek greek = iter.next();
      final Double result = firstResult.get(greek);
      assertTrue(secondResult.contains(greek));
      assertEquals(result, secondResult.get(greek), EPS);
    }
  }
}
