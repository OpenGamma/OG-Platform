package com.opengamma.sesame;

import java.util.Set;

import com.opengamma.sesame.marketdata.InflationMulticurveId;
import com.opengamma.sesame.marketdata.MarketDataEnvironment;
import com.opengamma.sesame.trade.ZeroCouponInflationSwapTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.result.FailureStatus;
import com.opengamma.util.result.Result;

/**
 * Provides inflation multicurve bundles by looking them up in the {@link MarketDataEnvironment}.
 */
public class LookupInflationProviderFn implements InflationProviderFn {

  /** Specifies which curve should be used for a trade. */
  private final CurveSelector _curveSelector;

  /**
   * @param curveSelector specifies which curve should be used for a trade
   */
  public LookupInflationProviderFn(CurveSelector curveSelector) {
    _curveSelector = ArgumentChecker.notNull(curveSelector, "curveSelectorFn");
  }

  @Override
  public Result<InflationProviderBundle> getInflationBundle(Environment env, ZeroCouponInflationSwapTrade trade) {
    Set<String> multicurveNames = _curveSelector.getMulticurveNames(trade.getTrade());

    switch (multicurveNames.size()) {
      case 0:
        return Result.failure(FailureStatus.CALCULATION_FAILED, "No curves configured for trade {}", trade);
      case 1:
        String multicurveName = multicurveNames.iterator().next();
        InflationMulticurveId multicurveId = InflationMulticurveId.of(multicurveName);
        return env.getMarketDataBundle().get(multicurveId, InflationProviderBundle.class);
      default:
        return Result.failure(FailureStatus.CALCULATION_FAILED,
                              "Only one issuer curve bundle is supported per trade. Bundle names: {}, trade: {}",
                              multicurveNames, trade);
    }
  }
}
