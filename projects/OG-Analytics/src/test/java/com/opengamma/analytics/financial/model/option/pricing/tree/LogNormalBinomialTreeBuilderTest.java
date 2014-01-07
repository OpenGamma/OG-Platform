/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.option.pricing.tree;

import static org.testng.AssertJUnit.assertEquals;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.analytics.financial.model.option.definition.GeneralLogNormalOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.OptionDefinition;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.CEVPriceFunction;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.tree.RecombiningBinomialTree;
import com.opengamma.analytics.financial.model.volatility.BlackImpliedVolatilityFormula;
import com.opengamma.analytics.financial.model.volatility.surface.DriftSurface;
import com.opengamma.analytics.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.surface.FunctionalDoublesSurface;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Expiry;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class LogNormalBinomialTreeBuilderTest {

  private static final double SPOT = 100;
  private static final double FORWARD;
  private static final double T = 5.0;
  private static final double BETA = 0.4;
  private static final YieldAndDiscountCurve YIELD_CURVE = YieldCurve.from(ConstantDoublesCurve.from(0.05));
  private static final double ATM_VOL = 0.20;
  private static final double SIGMA_BETA;
  private static final ZonedDateTime DATE = DateUtils.getUTCDate(2010, 7, 1);
  private static final OptionDefinition OPTION;
  private static final BinomialTreeBuilder<GeneralLogNormalOptionDataBundle> BUILDER = new LogNormalBinomialTreeBuilder<>();
  private static final DriftSurface DRIFTLESS;
  private static final BlackImpliedVolatilityFormula BLACK_IMPLIED_VOL = new BlackImpliedVolatilityFormula();
  private static final CEVPriceFunction CEV_PRICE = new CEVPriceFunction();

  static {
    SIGMA_BETA = ATM_VOL * Math.pow(SPOT, 1 - BETA);
    FORWARD = SPOT / YIELD_CURVE.getDiscountFactor(T);
    OPTION = new EuropeanVanillaOptionDefinition(FORWARD, new Expiry(DateUtils.getDateOffsetWithYearFraction(DATE, T)), true);

    final Function<Double, Double> driftless = new Function<Double, Double>() {
      @Override
      public Double evaluate(final Double... tk) {
        Validate.isTrue(tk.length == 2);
        return 0.0;
      }
    };

    DRIFTLESS = new DriftSurface(FunctionalDoublesSurface.from(driftless));
  }

  private static final Function<Double, Double> FLAT_LOCAL_VOL = new Function<Double, Double>() {
    @Override
    public Double evaluate(final Double... tk) {
      Validate.isTrue(tk.length == 2);
      return ATM_VOL;
    }
  };

  private static final Function<Double, Double> TIME_DEPENDENT_LOCAL_VOL = new Function<Double, Double>() {
    @Override
    public Double evaluate(final Double... tk) {
      Validate.isTrue(tk.length == 2);
      final double t = tk[0];
      return (2 * ATM_VOL - t * ATM_VOL / T);
    }
  };

  private static final Function<Double, Double> CEV_LOCAL_VOL = new Function<Double, Double>() {
    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double... tk) {
      Validate.isTrue(tk.length == 2);

      final double f = tk[1];
      final double sigma = SIGMA_BETA * Math.pow(f, BETA - 1);
      // return Math.min(sigma,100*ATM_VOL);
      return sigma;
    }
  };

  private static final GeneralLogNormalOptionDataBundle DATA = new GeneralLogNormalOptionDataBundle(YIELD_CURVE, DRIFTLESS, new VolatilitySurface(FunctionalDoublesSurface.from(FLAT_LOCAL_VOL)),
      FORWARD, DATE);

  @Test
  public void testPriceFlat() {
    final RecombiningBinomialTree<BinomialTreeNode<Double>> assetPriceTree = BUILDER.buildAssetTree(T, DATA, 200);
    RecombiningBinomialTree<BinomialTreeNode<Double>> optionPriceTree = BUILDER.buildOptionPriceTree(OPTION, DATA, assetPriceTree);

    EuropeanVanillaOption o = new EuropeanVanillaOption(FORWARD, T, true);
    final BlackFunctionData data = new BlackFunctionData(FORWARD, YIELD_CURVE.getDiscountFactor(T), 0);
    double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, o, optionPriceTree.getNode(0, 0).getValue());

    //double impVol = BlackImpliedVolFormula.impliedVol(optionPriceTree.getNode(0, 0).getValue(), FORWARD, FORWARD, YIELD_CURVE.getDiscountFactor(T), T, true);
    assertEquals(ATM_VOL, impVol, 1e-3);
    for (int i = 0; i < 10; i++) {
      final double m = -1.5 + 3.0 * i / 10.0;
      final double strike = FORWARD * Math.exp(ATM_VOL * Math.sqrt(T) * m);
      final OptionDefinition option = new EuropeanVanillaOptionDefinition(strike, OPTION.getExpiry(), OPTION.isCall());
      optionPriceTree = BUILDER.buildOptionPriceTree(option, DATA, assetPriceTree);
      o = new EuropeanVanillaOption(strike, T, OPTION.isCall());
      optionPriceTree = BUILDER.buildOptionPriceTree(option, DATA, assetPriceTree);
      impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(data, o, optionPriceTree.getNode(0, 0).getValue());
      //      impVol = BlackImpliedVolFormula.impliedVol(optionPriceTree.getNode(0, 0).getValue(), FORWARD, strike, YIELD_CURVE.getDiscountFactor(T), T, true);
      // System.out.println(strike+"\t"+impVol);
      assertEquals(ATM_VOL, impVol, 1e-3);
    }
  }

  @Test
  public void testPriceTimeDependent() {
    final GeneralLogNormalOptionDataBundle data = new GeneralLogNormalOptionDataBundle(YIELD_CURVE, DRIFTLESS, new VolatilitySurface(FunctionalDoublesSurface.from(TIME_DEPENDENT_LOCAL_VOL)), FORWARD,
        DATE);
    final RecombiningBinomialTree<BinomialTreeNode<Double>> assetPriceTree = BUILDER.buildAssetTree(T, data, 200);
    RecombiningBinomialTree<BinomialTreeNode<Double>> optionPriceTree = BUILDER.buildOptionPriceTree(OPTION, data, assetPriceTree);
    final double vol = Math.sqrt(7.0 / 3.0) * ATM_VOL;
    EuropeanVanillaOption o = new EuropeanVanillaOption(FORWARD, T, true);
    final BlackFunctionData bfd = new BlackFunctionData(FORWARD, YIELD_CURVE.getDiscountFactor(T), 0);
    double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(bfd, o, optionPriceTree.getNode(0, 0).getValue());
    //    double impVol = BlackImpliedVolFormula.impliedVol(optionPriceTree.getNode(0, 0).getValue(), FORWARD, FORWARD, df, T, true);
    assertEquals(vol, impVol, 1e-3);
    for (int i = 0; i < 10; i++) {
      final double m = -1.5 + 3.0 * i / 10.0;
      final double strike = FORWARD * Math.exp(ATM_VOL * Math.sqrt(T) * m);
      final OptionDefinition option = new EuropeanVanillaOptionDefinition(strike, OPTION.getExpiry(), OPTION.isCall());
      optionPriceTree = BUILDER.buildOptionPriceTree(option, data, assetPriceTree);
      o = new EuropeanVanillaOption(strike, T, OPTION.isCall());
      optionPriceTree = BUILDER.buildOptionPriceTree(option, DATA, assetPriceTree);
      impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(bfd, o, optionPriceTree.getNode(0, 0).getValue());
      //      impVol = BlackImpliedVolFormula.impliedVol(optionPriceTree.getNode(0, 0).getValue(), FORWARD, strike, df, T, true);
      // System.out.println(strike+"\t"+impVol);
      assertEquals(vol, impVol, 1e-3);
    }
  }

  @Test
  public void testCEV() {
    final GeneralLogNormalOptionDataBundle data = new GeneralLogNormalOptionDataBundle(YIELD_CURVE, DRIFTLESS, new VolatilitySurface(FunctionalDoublesSurface.from(CEV_LOCAL_VOL)), FORWARD, DATE);
    final RecombiningBinomialTree<BinomialTreeNode<Double>> assetPriceTree = BUILDER.buildAssetTree(T, data, 200);
    RecombiningBinomialTree<BinomialTreeNode<Double>> optionPriceTree = BUILDER.buildOptionPriceTree(OPTION, data, assetPriceTree);
    EuropeanVanillaOption o = new EuropeanVanillaOption(FORWARD, T, true);
    final CEVFunctionData cfd = new CEVFunctionData(FORWARD, YIELD_CURVE.getDiscountFactor(T), SIGMA_BETA, BETA);

    for (int i = 0; i < 10; i++) {
      final double m = -1.5 + 3.0 * i / 10.0;
      final double strike = FORWARD * Math.exp(ATM_VOL * Math.sqrt(T) * m);
      final OptionDefinition option = new EuropeanVanillaOptionDefinition(strike, OPTION.getExpiry(), OPTION.isCall());
      optionPriceTree = BUILDER.buildOptionPriceTree(option, data, assetPriceTree);
      o = new EuropeanVanillaOption(strike, T, true);
      final double cevPrice = CEV_PRICE.getPriceFunction(o).evaluate(cfd);
      final double cevVol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(FORWARD, YIELD_CURVE.getDiscountFactor(T), SIGMA_BETA), o, cevPrice);
      final double impVol = BLACK_IMPLIED_VOL.getImpliedVolatility(new BlackFunctionData(FORWARD, YIELD_CURVE.getDiscountFactor(T), SIGMA_BETA), o, optionPriceTree.getNode(0, 0).getValue());
      //      final double cevPrice = CEVFormula.optionPrice(FORWARD, strike, BETA, df, SIGMA_BETA, T, true);
      //      final double cevVol = BlackImpliedVolFormula.impliedVol(cevPrice, FORWARD, strike, df, T, true);
      //      final double impVol = BlackImpliedVolFormula.impliedVol(optionPriceTree.getNode(0, 0).getValue(), FORWARD, strike, df, T, true);
      // System.out.println(strike + "\t" + cevVol  + "\t" + impVol);
      assertEquals(cevVol, impVol, 1e-3);
    }
  }

}
