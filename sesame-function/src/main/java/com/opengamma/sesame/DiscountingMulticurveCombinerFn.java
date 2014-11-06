/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.sesame.trade.TradeWrapper;
import com.opengamma.util.result.Result;
import com.opengamma.util.tuple.Pair;

/**
 * Returns a multicurve bundle for a trade.
 */
public interface DiscountingMulticurveCombinerFn {

  /**
   * Returns the merged multicurve bundle for a specified environment, trade and FX matrix.
   *
   * @param env the environment to merge the multicurve bundle for.
   * @param trade the trade to merge the multicurve bundle for.
   * @param fxMatrix the FX matrix to include inside the multicurve bundle.
   * @return the merged multicurve bundle.
   * @deprecated use {@link #getMulticurveBundle(Environment, TradeWrapper)}
   */
  @Deprecated
  Result<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> createMergedMulticurveBundle(
      Environment env, TradeWrapper trade, FXMatrix fxMatrix);

  /**
   * Returns the multicurve bundle for a trade.
   *
   * @param env the calculation environment
   * @param trade the trade for which a multicurve is required
   * @return the multicurve to use when performing calculations for the trade
   */
  Result<MulticurveBundle> getMulticurveBundle(Environment env, TradeWrapper<?> trade);
}
