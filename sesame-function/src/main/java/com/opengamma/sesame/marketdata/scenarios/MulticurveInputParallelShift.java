/**
 * Copyright (C) 2015 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata.scenarios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.ShiftType;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNodeWithIdentifier;
import com.opengamma.financial.analytics.ircurve.strips.RateFutureNode;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Applies a parallel shift to the market quotes used to build a multicurve bundle.
 */
public class MulticurveInputParallelShift implements Perturbation {

  private static final Logger s_logger = LoggerFactory.getLogger(MulticurveInputParallelShift.class);

  private final ShiftType _shiftType;
  private final double _shiftAmount;

  private MulticurveInputParallelShift(ShiftType shiftType, double shiftAmount) {
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
    _shiftAmount = shiftAmount;
  }

  /**
   * Creates a shift that adds a fixed amount to every market data value.
   * <p>
   * Futures market data is handled as a special case so the shifted data makes sense.
   *
   * @param shiftAmount the amount to add to each market data value
   * @return a shift that adds a fixed amount to each market data value
   */
  public static MulticurveInputParallelShift absolute(double shiftAmount) {
    return new MulticurveInputParallelShift(ShiftType.ABSOLUTE, shiftAmount);
  }

  /**
   * Creates a shift that multiplies every market data value by a fixed factor.
   * <p>
   * Futures market data is handled as a special case so the shifted data makes sense
   *
   * @param shiftAmount the factor to multiply the values by
   * @return a shift that multiplies the market data values by a fixed factor
   */
  public static MulticurveInputParallelShift relative(double shiftAmount) {
    return new MulticurveInputParallelShift(ShiftType.RELATIVE, shiftAmount);
  }

  /**
   * Applies the shift to the curve input data.
   *
   * @param marketData a piece of market data with type {@link CurveInputs}
   * @param matchDetails details of the match which the {@link MarketDataFilter} was applied to the market data
   * @return the shifted data
   */
  @Override
  public CurveInputs apply(Object marketData, MatchDetails matchDetails) {
    SnapshotDataBundle shiftedData = new SnapshotDataBundle();
    CurveInputs curveInputs = ((CurveInputs) marketData);
    SnapshotDataBundle nodeData = curveInputs.getNodeData();

    for (CurveNodeWithIdentifier nodeWithId : curveInputs.getNodes()) {
      CurveNode node = nodeWithId.getCurveNode();
      ExternalId id = nodeWithId.getIdentifier();
      Double value = nodeData.getDataPoint(id);

      if (value != null) {
        shiftedData.setDataPoint(id, shift(value, node));
      } else {
        s_logger.info("No data found for curve node with ID {}", id);
      }
    }
    return new CurveInputs(curveInputs.getNodes(), shiftedData);
  }

  private double shift(double value, CurveNode node) {
    // futures are quoted the other way round, i.e. (1 - value)
    if (node instanceof RateFutureNode) {
      return 1 - shift(1 - value);
    } else {
      return shift(value);
    }
  }

  private double shift(double value) {
    switch (_shiftType) {
      case ABSOLUTE:
        return value + _shiftAmount;
      case RELATIVE:
        return value * (1 + _shiftAmount);
      default:
        // should never happen
        throw new IllegalStateException("Unexpected shift type: " + _shiftType);
    }
  }

  @Override
  public Class<CurveInputs> getMarketDataType() {
    return CurveInputs.class;
  }

  @Override
  public Class<? extends MatchDetails> getMatchDetailsType() {
    return MulticurveMatchDetails.class;
  }

  @Override
  public Target getTargetType() {
    return Target.INPUT;
  }
}
