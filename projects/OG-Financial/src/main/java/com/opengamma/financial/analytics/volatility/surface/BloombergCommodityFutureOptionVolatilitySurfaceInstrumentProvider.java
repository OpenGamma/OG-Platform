/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import java.util.Map;

import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.GoldFutureOptionExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.LiveCattleFutureOptionExpiryCalculator;
import com.opengamma.financial.convention.expirycalc.SoybeanFutureOptionExpiryCalculator;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider extends BloombergFutureOptionVolatilitySurfaceInstrumentProvider {

  private static final Map<String, ExchangeTradedInstrumentExpiryCalculator> EXPIRY_RULES;
  static {
    EXPIRY_RULES = Maps.newHashMap();
    EXPIRY_RULES.put("BO", SoybeanFutureOptionExpiryCalculator.getInstance());      // Soy oil
    EXPIRY_RULES.put("BZ", SoybeanFutureOptionExpiryCalculator.getInstance());      // Brent Crude -- temp for 2 character code in surface name
    EXPIRY_RULES.put("BZA", SoybeanFutureOptionExpiryCalculator.getInstance());      // Brent Crude
    EXPIRY_RULES.put("GC", GoldFutureOptionExpiryCalculator.getInstance());         // Gold
    EXPIRY_RULES.put("LC", LiveCattleFutureOptionExpiryCalculator.getInstance());   // Live Cattle
    EXPIRY_RULES.put("S ", SoybeanFutureOptionExpiryCalculator.getInstance());      // Soy
  }

  /**
   * Uses the default ticker scheme (BLOOMBERG_TICKER_WEAK)
   * @param futureOptionPrefix the prefix to the resulting code (e.g. "S " for Soybeans), not null
   * @param postfix the postfix to the resulting code (e.g. Comdty), not null
   * @param dataFieldName the name of the data field, not null. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the exchange id, not null
   */
  public BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName);
  }

  /**
   * @param futureOptionPrefix the prefix to the resulting code (e.g. "S " for Soybeans), not null
   * @param postfix the postfix to the resulting code (e.g. Comdty), not null
   * @param dataFieldName the name of the data field, not null. Expecting MarketDataRequirementNames.IMPLIED_VOLATILITY or OPT_IMPLIED_VOLATILITY_MID
   * @param useCallAboveStrike the strike above which to use calls rather than puts, not null
   * @param exchangeIdName the exchange id, not null
   * @param schemeName the ticker scheme name, not null
   */
  public BloombergCommodityFutureOptionVolatilitySurfaceInstrumentProvider(final String futureOptionPrefix, final String postfix, final String dataFieldName, final Double useCallAboveStrike,
      final String exchangeIdName, final String schemeName) {
    super(futureOptionPrefix, postfix, dataFieldName, useCallAboveStrike, exchangeIdName, schemeName);
  }

  /**
   * Provides an ExternalID for Bloomberg ticker,
   * given a reference date and an integer offset, the n'th subsequent option <p>
   * The format is prefix + expiry code + callPutFlag + strike + postfix <p>
   * e.g. S U3P 45 Comdty
   * <p>
   * @param futureOptionNumber n'th future following curve date, not null
   * @param strike option's strike, not null
   * @param surfaceDate date of curve validity; valuation date, not null
   * @return the id of the Bloomberg ticker
   */
  @Override
  public ExternalId getInstrument(final Number futureOptionNumber, final Double strike, final LocalDate surfaceDate) {
    ArgumentChecker.notNull(futureOptionNumber, "futureOptionNumber");
    ArgumentChecker.notNull(strike, "strike");
    ArgumentChecker.notNull(surfaceDate, "surface date");
    final String prefix = getFutureOptionPrefix();
    final StringBuffer ticker = new StringBuffer();
    ticker.append(prefix);
    final ExchangeTradedInstrumentExpiryCalculator expiryRule = getExpiryRuleCalculator();
    final LocalDate expiryDate = expiryRule.getExpiryMonth(futureOptionNumber.intValue(), surfaceDate);
    final String expiryCode = BloombergFutureUtils.getShortExpiryCode(expiryDate);
    ticker.append(expiryCode);
    ticker.append(strike > useCallAboveStrike() ? "C" : "P");
    ticker.append(" ");
    // temp workaround for BZA which has 2 decimal places - need to find the proper rule.
    //if (prefix.equals("BZA")) {
    //  ticker.append(withTwoDigits.format(strike));
    //} else {
      ticker.append(strike);
    //}
    ticker.append(" ");
    ticker.append(getPostfix());
    return ExternalId.of(getScheme(), ticker.toString());
  }

  /**
   * Gets the expiryRules.
   * @return the expiryRules
   */
  public static Map<String, ExchangeTradedInstrumentExpiryCalculator> getExpiryRules() {
    return EXPIRY_RULES;
  }

  @Override
  public ExchangeTradedInstrumentExpiryCalculator getExpiryRuleCalculator() {
    final ExchangeTradedInstrumentExpiryCalculator expiryRule = EXPIRY_RULES.get(getFutureOptionPrefix());
    if (expiryRule == null) {
      throw new OpenGammaRuntimeException("No expiry rule has been setup for " + getFutureOptionPrefix() + ". Determine week and day pattern and add to EXPIRY_RULES.");
    }
    return expiryRule;
  }

}
