package com.opengamma.sesame.credit.curve;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.financial.analytics.isda.credit.YieldCurveData;
import com.opengamma.financial.analytics.isda.credit.YieldCurveDataSnapshot;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.marketdata.MarketDataBundle;
import com.opengamma.sesame.marketdata.YieldCurveDataId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Tests {@link DefaultYieldCurveDataProviderFn}.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultYieldCurveDataProviderFnTest {

  private static YieldCurveData YIELD_CURVE_DATA;
  static {
    YIELD_CURVE_DATA = YieldCurveData.builder()
        .cashDayCount(DayCounts.ACT_360)
        .currency(Currency.USD)
        .curveBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
        .curveDayCount(DayCounts.ACT_365)
        .regionId(null)//weekend only calendar
        .spotDate(LocalDate.of(2014, 1, 1))
        .swapDayCount(DayCounts.THIRTY_360)
        .swapFixedLegInterval(Tenor.ONE_YEAR)
        .cashData(ImmutableSortedMap.of(Tenor.ONE_MONTH, 0.00445))
        .swapData(ImmutableSortedMap.of(Tenor.ONE_YEAR, 0.00445))
        .build();
  }
  private Environment _env;
  private DefaultYieldCurveDataProviderFn _fnWithUSDCurve;
  @SuppressWarnings("unchecked")
  @BeforeClass
  public void beforeClass() {

    _env = mock(Environment.class);
    MarketDataBundle bundle = mock(MarketDataBundle.class);
    when(_env.getMarketDataBundle()).thenReturn(bundle);
    String name = "Name";
    YieldCurveDataId goodId = YieldCurveDataId.of(name);
    YieldCurveDataSnapshot snapshot = YieldCurveDataSnapshot.builder()
                      .name("")
                      .yieldCurves(ImmutableMap.of(Currency.USD, YIELD_CURVE_DATA))
                      .build();
    when(bundle.get(goodId, YieldCurveDataSnapshot.class)).thenReturn(Result.success(snapshot));
    _fnWithUSDCurve = new DefaultYieldCurveDataProviderFn(name);
  }

  @Test
  public void testFnMissingData() {
    Result<YieldCurveData> result = _fnWithUSDCurve.retrieveYieldCurveData(_env, Currency.GBP);
    assertFalse("GBP is missing so result should be failure.", result.isSuccess());
  }

  @Test
  public void testFn() {
    Result<YieldCurveData> result = _fnWithUSDCurve.retrieveYieldCurveData(_env, Currency.USD);
    assertTrue("USD present in snapshot so should succeed.", result.isSuccess());
  }

}
