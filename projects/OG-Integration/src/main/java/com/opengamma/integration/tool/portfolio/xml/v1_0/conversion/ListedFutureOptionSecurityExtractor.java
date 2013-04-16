/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZoneOffset;

import com.opengamma.OpenGammaRuntimeException;
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

public class ListedFutureOptionSecurityExtractor extends AbstractListedSecurityExtractor<FutureOptionSecurityDefinition> {

  public ListedFutureOptionSecurityExtractor(FutureOptionSecurityDefinition securityDefinition) {
    super(securityDefinition);
  }

  @Override
  protected ManageableSecurity createSecurity() {

    ExternalId underlyingId = _securityDefinition.getUnderlyingId().toExternalId();
    Expiry expiry = new Expiry(_securityDefinition.getFutureExpiry().atDay(1).atStartOfDay(
        ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);
    String exchange = _securityDefinition.getExchange();
    Currency currency = _securityDefinition.getCurrency();
    int pointValue = _securityDefinition.getPointValue();
    boolean isMargined = _securityDefinition.isIsMargined();
    double strike = _securityDefinition.getStrike().doubleValue();
    OptionType optionType = _securityDefinition.getOptionType();
    ExerciseType exerciseType = _securityDefinition.getExerciseType().convert();

    switch (_securityDefinition.getListedFutureOptionType()) {

      case EQUITY_INDEX_FUTURE_OPTION:
        return new EquityIndexFutureOptionSecurity(exchange, expiry, exerciseType, underlyingId, pointValue,
                                                   isMargined, currency, strike, optionType);

      case EQUITY_DIVIDEND_FUTURE_OPTION:
        return new EquityIndexDividendFutureOptionSecurity(exchange,  expiry, exerciseType, underlyingId, pointValue,
                                                           isMargined, currency, strike, optionType);

      default:
        // The xml validation should prevent this from happening
        throw new PortfolioParsingException("Unrecognised listed option type: " + _securityDefinition.getListedFutureOptionType());
    }
  }
}
