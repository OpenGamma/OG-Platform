/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Calculates the dividend yield (annual dividend / market value).
 */
public class DividendYieldCalculator implements NormalizationRule {

  /**
   * Calculates a best estimate of the dividend yield.
   * <p>
   * where, yield == annual dividend / market value
   * <p>
   *
   * @param msg  the message to normalize, not null
   * @param securityUniqueId  the data provider's unique ID of the security, not null
   * @param fieldHistory  the distributor-specific field history which the rule may choose to update, not null
   * @return {@code msg} with {@link MarketDataRequirementNames#DIVIDEND_YIELD} added,
   * with the value calculated as described above
   */
  @Override
  public MutableFudgeMsg apply(MutableFudgeMsg msg, String securityUniqueId, FieldHistoryStore fieldHistory) {

    // Implementation note: this requires MARKET_VALUE & ANNUAL_DIVIDEND, these values may appear in separate messages
    // from the underlying data provider - thus the use of the lkv store to obtain values if this message happens to be
    // missing them - this may or may not be the correct thing to do
    FudgeMsg lkv = fieldHistory.getLastKnownValues();

    Double annualdividend = msg.getDouble(MarketDataRequirementNames.ANNUAL_DIVIDEND);
    if (annualdividend == null) {
      annualdividend = lkv.getDouble(MarketDataRequirementNames.ANNUAL_DIVIDEND);
    }

    if (annualdividend == null) {
      return msg;
    }

    Double spot = msg.getDouble(MarketDataRequirementNames.MARKET_VALUE);
    if (spot == null) {
      spot = lkv.getDouble(MarketDataRequirementNames.MARKET_VALUE);
    }

    if (spot != null && spot != 0.0) {
      final Double dividendYield = annualdividend / spot;
      msg.add(MarketDataRequirementNames.DIVIDEND_YIELD, dividendYield);
      return msg;
    }

    // Fall back to last known market value - may not be needed anymore
    return lastKnownMarketValue(msg, fieldHistory);
  }

  /**
   * Tries to populate DIVIDEND_YIELD from the history.
   */
  private MutableFudgeMsg lastKnownMarketValue(
      MutableFudgeMsg msg,
      FieldHistoryStore fieldHistory) {

    FudgeMsg lkv = fieldHistory.getLastKnownValues();

    Double lastKnownMarketValue = lkv.getDouble(MarketDataRequirementNames.DIVIDEND_YIELD);
    if (lastKnownMarketValue == null) {
      return msg;
    }

    msg.add(MarketDataRequirementNames.DIVIDEND_YIELD, lastKnownMarketValue);
    return msg;
  }

}
