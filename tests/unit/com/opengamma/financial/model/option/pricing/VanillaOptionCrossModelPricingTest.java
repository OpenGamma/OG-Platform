/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.model.interestrate.curve.ConstantInterestRateDiscountCurve;
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

/**
 * 
 * @author emcleod
 */
public class VanillaOptionCrossModelPricingTest {
  private static final Double STRIKE = 9.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2009, 1, 1);
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantInterestRateDiscountCurve(0.08), 0.08, new ConstantVolatilitySurface(0.3), 10.,
      DATE);
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> CRR = new CoxRossRubinsteinBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> LR = new LeisenReimerBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> RB = new RendlemanBartterBinomialOptionModelDefinition();
  private static final BinomialOptionModelDefinition<OptionDefinition, StandardOptionDataBundle> TRISGEORGIS = new TrisgeorgisBinomialOptionModelDefinition();
  private static final List<Greek> REQUIRED_GREEKS = Arrays.asList(Greek.PRICE);

  @Test
  public void testEuropeanOption() {
    final double eps = 1e-3;
    final OptionDefinition call = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    final OptionDefinition put = new EuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false);
    final AnalyticOptionModel<OptionDefinition, StandardOptionDataBundle> bsm = new BlackScholesMertonModel();
    TreeOptionModel<OptionDefinition, StandardOptionDataBundle> binomial = new BinomialOptionModel<StandardOptionDataBundle>(CRR);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(LR);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(1001, RB);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(TRISGEORGIS);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bsm.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
  }

  @Test
  public void testAmericanOption() {
    final double eps = 1e-2;
    final AmericanVanillaOptionDefinition call = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, true);
    final AmericanVanillaOptionDefinition put = new AmericanVanillaOptionDefinition(STRIKE, EXPIRY, false);
    final AnalyticOptionModel<AmericanVanillaOptionDefinition, StandardOptionDataBundle> bs = new BjerksundStenslandModel();
    TreeOptionModel<OptionDefinition, StandardOptionDataBundle> binomial = new BinomialOptionModel<StandardOptionDataBundle>(CRR);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(LR);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(1001, RB);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    binomial = new BinomialOptionModel<StandardOptionDataBundle>(TRISGEORGIS);
    assertEquals((Double) binomial.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(call, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
    assertEquals((Double) binomial.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE).getResult(), (Double) bs.getGreeks(put, DATA, REQUIRED_GREEKS).get(Greek.PRICE)
        .getResult(), eps);
  }
}
