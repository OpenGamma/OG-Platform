package com.opengamma.sesame.credit.snapshot;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.link.SnapshotLink;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataSnapshot;
import com.opengamma.sesame.Environment;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SnapshotYieldCurveDataProviderFn}.
 */
@Test(groups = TestGroup.UNIT)
public class SnapshotCreditCurveDataProviderFnTest {
  
  private SnapshotCreditCurveDataProviderFn _fnWithBadLink;
  private SnapshotCreditCurveDataProviderFn _fnWithUSDCurve;
  private Environment _env;
  private static final LocalDate VALUATION_DATE = LocalDate.of(2014, 3, 27);
  
  private final CreditCurveDataKey _goodKey = CreditCurveDataKey.builder().curveName("USD").currency(Currency.USD).build();
  private final CreditCurveDataKey _badKey = CreditCurveDataKey.builder().curveName("GBP").currency(Currency.GBP).build();

  @SuppressWarnings("unchecked")
  @BeforeClass
  public void beforeClass() {

    _env = mock(Environment.class);
    when(_env.getValuationDate()).thenReturn(VALUATION_DATE);
    
    SnapshotLink<CreditCurveDataSnapshot> badLink = mock(SnapshotLink.class);
    when(badLink.resolve()).thenThrow(new DataNotFoundException("test"));
    _fnWithBadLink = new SnapshotCreditCurveDataProviderFn(badLink);
    
    CreditCurveDataSnapshot snapshot = CreditCurveDataSnapshot.builder()
                      .name("")
                      .creditCurves(ImmutableMap.of(_goodKey, mock(CreditCurveData.class)))
                      .build();
    SnapshotLink<CreditCurveDataSnapshot> goodLink = SnapshotLink.resolved(snapshot);
    
    _fnWithUSDCurve = new SnapshotCreditCurveDataProviderFn(goodLink);
  }
  
  @Test(expectedExceptions = {DataNotFoundException.class})
  public void testFnWithBadLink() {
    
    Result<CreditCurveData> result = _fnWithBadLink.retrieveCreditCurveData(_env,_goodKey);
    
    assertFalse("Link threw exception so function should fail.", result.isSuccess());
    
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
