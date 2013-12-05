/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.description.interestrate;

import static com.opengamma.analytics.financial.instrument.TestInstrumentDefinitionsAndDerivatives.IBOR_INDEX_1;
import static com.opengamma.analytics.financial.instrument.TestInstrumentDefinitionsAndDerivatives.INDEX_ON;

import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MulticurveProviderDiscountTest {

  private MulticurveProviderDiscount _provider;

  @BeforeMethod
  public void setup() {
    _provider = new MulticurveProviderDiscount();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingDifferentDiscountingCurveWithSameCurrencyFails() {
    _provider.setCurve(Currency.USD, mockCurve("test"));
    _provider.setCurve(Currency.USD, mockCurve("test2"));
  }

  @Test
  public void testAddingSameDiscountingCurveWithSameCurrencySucceeds() {
    YieldAndDiscountCurve test = mockCurve("test");
    _provider.setCurve(Currency.USD, test);
    _provider.setCurve(Currency.USD, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingDifferentForwardIborCurveWithSameIndexFails() {
    _provider.setCurve(IBOR_INDEX_1, mockCurve("test"));
    _provider.setCurve(IBOR_INDEX_1, mockCurve("test2"));
  }

  @Test
  public void testAddingSameForwardIborCurveWithSameIndexSucceeds() {
    YieldAndDiscountCurve test = mockCurve("test");
    _provider.setCurve(IBOR_INDEX_1, test);
    _provider.setCurve(IBOR_INDEX_1, test);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testAddingDifferentForwardONCurveWithSameIndexFails() {
    _provider.setCurve(INDEX_ON, mockCurve("test"));
    _provider.setCurve(INDEX_ON, mockCurve("test2"));
  }

  @Test
  public void testAddingSameForwardONCurveWithSameIndexSucceeds() {
    YieldAndDiscountCurve test = mockCurve("test");
    _provider.setCurve(INDEX_ON, test);
    _provider.setCurve(INDEX_ON, test);
  }

  private YieldAndDiscountCurve mockCurve(final String name) {
    return new YieldAndDiscountCurve(name) {
      @Override
      public double getForwardRate(double t) {
        return 0;
      }

      @Override
      public double[] getInterestRateParameterSensitivity(double time) {
        return new double[0];
      }

      @Override
      public int getNumberOfParameters() {
        return 0;
      }

      @Override
      public List<String> getUnderlyingCurvesNames() {
        return null;
      }

      @Override
      public double getInterestRate(Double x) {
        return 0;
      }
    };
  }
}
