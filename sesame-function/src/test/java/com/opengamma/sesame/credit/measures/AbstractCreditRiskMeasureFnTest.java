package com.opengamma.sesame.credit.measures;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.testng.AssertJUnit.assertTrue;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.core.link.SecurityLink;
import com.opengamma.financial.analytics.isda.credit.CreditCurveData;
import com.opengamma.financial.analytics.isda.credit.CreditCurveDataKey;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.sesame.Environment;
import com.opengamma.sesame.credit.CdsData;
import com.opengamma.sesame.credit.CreditPricingSampleData;
import com.opengamma.sesame.credit.IsdaCompliantCreditCurveFn;
import com.opengamma.sesame.credit.IsdaCreditCurve;
import com.opengamma.sesame.credit.converter.IndexCdsConverterFn;
import com.opengamma.sesame.credit.converter.LegacyCdsConverterFn;
import com.opengamma.sesame.credit.converter.StandardCdsConverterFn;
import com.opengamma.sesame.credit.market.IndexCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.LegacyCdsMarketDataResolverFn;
import com.opengamma.sesame.credit.market.StandardCdsMarketDataResolverFn;
import com.opengamma.util.money.Currency;
import com.opengamma.util.result.Result;

/**
 * Tests {@link AbstractCreditRiskMeasureFn} flow.
 */
@PrepareForTest(IsdaCreditCurve.class)
public class AbstractCreditRiskMeasureFnTest extends PowerMockTestCase {
  
  
  private AbstractCreditRiskMeasureFn<RiskResult> _creditRiskMeasureFn;
  private Environment _env;
  private LegacyCDSSecurity _legCds;
  private StandardCDSSecurity _cds;
  private IndexCDSSecurity _cdx;
  private IndexCDSDefinitionSecurity _cdxDef;
  private SecurityLink<IndexCDSDefinitionSecurity> _cdxDefLink;
  
  //standard cds fields:
  private final InterestRateNotional _notional = new InterestRateNotional(Currency.USD, 1000000);
  private final double _coupon = 0.01;

  //cdx fields used in the AbstractCreditRiskMeasureFn
  private final ExternalId _externalId = ExternalId.of("test", "123");
  private final CreditDefaultSwapIndexComponent _component =
      new CreditDefaultSwapIndexComponent(_externalId.getValue(),
                                          _externalId,
                                          0.95,
                                          _externalId);
  private final CDSIndexComponentBundle _bundle = CDSIndexComponentBundle.of(_component);

  @SuppressWarnings("unchecked")
  @BeforeMethod
  public void beforeMethod() {
    _env = mock(Environment.class);
    _legCds = mock(LegacyCDSSecurity.class);
    _cds = mock(StandardCDSSecurity.class);
    _cdx = mock(IndexCDSSecurity.class);
    _cdxDefLink = mock(SecurityLink.class);
    _cdxDef = mock(IndexCDSDefinitionSecurity.class);

    ExternalIdBundle idBundle = ExternalIdBundle.of(ExternalId.of("test", "1"));

    when(_legCds.getNotional()).thenReturn(_notional);
    when(_legCds.getExternalIdBundle()).thenReturn(idBundle);
    when(_legCds.getCoupon()).thenReturn(_coupon);
    when(_cds.getNotional()).thenReturn(_notional);
    when(_cds.getExternalIdBundle()).thenReturn(idBundle);
    when(_cds.getCoupon()).thenReturn(_coupon);
    when(_cdx.getNotional()).thenReturn(_notional);
    when(_cdx.getExternalIdBundle()).thenReturn(idBundle);
    when(_cdx.getUnderlyingIndex()).thenReturn(_cdxDefLink);
    when(_cdxDefLink.resolve()).thenReturn(_cdxDef);
    when(_cdxDef.getCoupon()).thenReturn(_coupon);
    when(_cdxDef.getComponents()).thenReturn(_bundle);

    LegacyCdsConverterFn legacyCdsConverter = mock(LegacyCdsConverterFn.class);
    StandardCdsConverterFn standardCdsConverter = mock(StandardCdsConverterFn.class);
    IndexCdsConverterFn indexCdsConverter = mock(IndexCdsConverterFn.class);
    Result<CDSAnalytic> result = mock(Result.class);
    when(result.isSuccess()).thenReturn(true);
    when(legacyCdsConverter.toCdsAnalytic(any(Environment.class), any(LegacyCDSSecurity.class), any(IsdaCreditCurve.class))).thenReturn(result);
    when(indexCdsConverter.toCdsAnalytic(any(Environment.class), any(IndexCDSSecurity.class), any(IsdaCreditCurve.class))).thenReturn(result);
    when(standardCdsConverter.toCdsAnalytic(any(Environment.class), any(StandardCDSSecurity.class), any(IsdaCreditCurve.class))).thenReturn(result);
    
    final Result<CreditCurveDataKey> keyResult = mock(Result.class);
    when(keyResult.isSuccess()).thenReturn(true);
    //can't use mockito for these calls due to a limitation resolving calls via
    //interfaces with generic params (i.e. CreditMarketDataResolverFn)
    class StandardCdsMdResolverMock implements StandardCdsMarketDataResolverFn {
      
      @Override
      public Result<CreditCurveDataKey> resolve(Environment env, StandardCDSSecurity security) {
        return keyResult;
      }
    }
    
    class LegacyCdsMdResolverMock implements LegacyCdsMarketDataResolverFn {
      
      @Override
      public Result<CreditCurveDataKey> resolve(Environment env, LegacyCDSSecurity security) {
        return keyResult;
      }
    }

    class IndexCdsMdResolverMock implements IndexCdsMarketDataResolverFn {

      @Override
      public Result<CreditCurveDataKey> resolve(Environment env, IndexCDSSecurity security) {
        return keyResult;
      }
    }
    
    IsdaCompliantCreditCurveFn curveFn = mock(IsdaCompliantCreditCurveFn.class);
    Result<IsdaCreditCurve> isdaCurveResult = mock(Result.class);
    when(isdaCurveResult.isSuccess()).thenReturn(true);
    IsdaCreditCurve curve = mock(IsdaCreditCurve.class);
    when(isdaCurveResult.getValue()).thenReturn(curve);
    CreditCurveData data = CreditPricingSampleData.createSingleNameCreditCurveData();
    when(curve.getCurveData()).thenReturn(data);
    when(curveFn.buildIsdaCompliantCreditCurve(any(Environment.class), any(CreditCurveDataKey.class))).thenReturn(isdaCurveResult);
    
    _creditRiskMeasureFn = new AbstractCreditRiskMeasureFn<RiskResult>(legacyCdsConverter,
                                                      standardCdsConverter,
                                                      indexCdsConverter,
                                                      new IndexCdsMdResolverMock(),
                                                      new StandardCdsMdResolverMock(), 
                                                      new LegacyCdsMdResolverMock(),
                                                      curveFn) {

      @Override
      protected Result<RiskResult> price(CdsData cdsData, CDSAnalytic cdsAnalytic, IsdaCreditCurve curve) {
        RiskResult result = new RiskResult();
        result._cdsData = cdsData;
        result._cdsAnalytic = cdsAnalytic;
        result._curve = curve;
        return Result.success(result);
      }
    };
    
  }
  
  class RiskResult {
    CdsData _cdsData;
    CDSAnalytic _cdsAnalytic;
    IsdaCreditCurve _curve;
  }

  @Test
  public void priceLegacyCds() {
    Result<RiskResult> result = _creditRiskMeasureFn.priceLegacyCds(_env, _legCds);
    assertTrue(result.isSuccess());
  }

  @Test
  public void priceStandardCds() {
    Result<RiskResult> result = _creditRiskMeasureFn.priceStandardCds(_env, _cds);
    assertTrue(result.isSuccess());
  }

  @Test
  public void priceIndexCds() {
    Result<RiskResult> result = _creditRiskMeasureFn.priceIndexCds(_env, _cdx);
    assertTrue(result.isSuccess());
  }
}
