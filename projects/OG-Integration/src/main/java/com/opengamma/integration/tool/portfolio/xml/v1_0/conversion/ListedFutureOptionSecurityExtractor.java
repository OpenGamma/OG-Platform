/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZoneOffset;

import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FutureOptionSecurityDefinition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Extractor for future option securities.
 */
public class ListedFutureOptionSecurityExtractor
    extends AbstractListedSecurityExtractor<FutureOptionSecurityDefinition> {

  /**
   * Creates an instance.
   * 
   * @param securityDefinition  the definition, not null
   */
  public ListedFutureOptionSecurityExtractor(FutureOptionSecurityDefinition securityDefinition) {
    super(securityDefinition);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity() {
    FutureOptionSecurityDefinition defn = getSecurityDefinition();
    ExternalId underlyingId = defn.getUnderlyingId().toExternalId();
    Expiry expiry = new Expiry(defn.getFutureExpiry().atDay(1).atStartOfDay(ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);
    String exchange = defn.getExchange();
    Currency currency = defn.getCurrency();
    int pointValue = defn.getPointValue();
    boolean isMargined = defn.isIsMargined();
    double strike = defn.getStrike().doubleValue();
    OptionType optionType = defn.getOptionType();
    ExerciseType exerciseType = defn.getExerciseType().convert();

    switch (defn.getListedFutureOptionType()) {
      case EQUITY_INDEX_FUTURE_OPTION:
        return new EquityIndexFutureOptionSecurity(exchange, expiry, exerciseType, underlyingId, pointValue,
                                                   isMargined, currency, strike, optionType);
      case EQUITY_DIVIDEND_FUTURE_OPTION:
        return new EquityIndexDividendFutureOptionSecurity(exchange,  expiry, exerciseType, underlyingId, pointValue,
                                                           isMargined, currency, strike, optionType);
      default:
        // The xml validation should prevent this from happening
        throw new PortfolioParsingException("Unrecognised listed option type: " + defn.getListedFutureOptionType());
    }
  }

}
