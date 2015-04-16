/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.credit.snapshot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.CreditCurveDataId;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DefaultCreditCurveDataProviderFnTest}.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultCreditCurveDataProviderFnTest {

  private DefaultCreditCurveDataProviderFn _fnWithUSDCurve;
  private Environment _env;
  private final CreditCurveDataKey _goodKey = CreditCurveDataKey.builder().curveName("USD").currency(Currency.USD).build();
  private final CreditCurveDataKey _badKey = CreditCurveDataKey.builder().curveName("GBP").currency(Currency.GBP).build();

  @SuppressWarnings("unchecked")
  @BeforeClass
  public void beforeClass() {

    _env = mock(Environment.class);

    MarketDataBundle bundle = mock(MarketDataBundle.class);
    when(_env.getMarketDataBundle()).thenReturn(bundle);

    String name = "Name";
    CreditCurveDataId goodId = CreditCurveDataId.of(name);

    CreditCurveDataSnapshot snapshot =
        CreditCurveDataSnapshot.builder()
                               .name("")
                               .creditCurves(ImmutableMap.of(_goodKey, mock(CreditCurveData.class)))
                               .build();

    when(bundle.get(goodId, CreditCurveDataSnapshot.class)).thenReturn(Result.success(snapshot));

    _fnWithUSDCurve = new DefaultCreditCurveDataProviderFn(name);
  }

  @Test
  public void testFnMissingData() {
    Result<CreditCurveData> result = _fnWithUSDCurve.retrieveCreditCurveData(_env, _badKey);
    assertFalse(_badKey + " is missing so result should be failure.", result.isSuccess());
  }

  @Test
  public void testFn() {
    Result<CreditCurveData> result = _fnWithUSDCurve.retrieveCreditCurveData(_env, _goodKey);
    assertTrue(_goodKey + " present in snapshot so should succeed.", result.isSuccess());
  }

}
