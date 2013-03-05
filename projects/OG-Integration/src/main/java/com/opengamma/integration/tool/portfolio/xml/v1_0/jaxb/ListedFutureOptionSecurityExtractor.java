/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.threeten.bp.ZoneOffset;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

public class ListedFutureOptionSecurityExtractor implements ListedSecurityExtractor {

  private final FutureOptionSecurityDefinition _securityDefinition;

  public ListedFutureOptionSecurityExtractor(FutureOptionSecurityDefinition securityDefinition) {
    _securityDefinition = securityDefinition;
  }

  @Override
  public ManageableSecurity[] extract() {

    ManageableSecurity security = createSecurity();

    security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
        new HashCodeBuilder()
            .append(security.getClass())
            .append(security)
            .toHashCode())));

    return new ManageableSecurity[]{security};
  }

  private ManageableSecurity createSecurity() {

    ExternalId underlyingId = _securityDefinition.getUnderlyingId().toExternalId();
    Expiry expiry = new Expiry(_securityDefinition.getFutureExpiry().atDay(1).atStartOfDay(
        ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);
    String exchange = _securityDefinition.getExchange();
    Currency currency = _securityDefinition.getCurrency();
    int pointValue = _securityDefinition.getPointValue();
    boolean isMargined = _securityDefinition.isIsMargined();
    double strike = _securityDefinition.getStrike().doubleValue();
    OptionType optionType = _securityDefinition.getOptionType();
    com.opengamma.financial.security.option.ExerciseType exerciseType = _securityDefinition.getExerciseType().convert();

    switch (_securityDefinition.getListedFutureOptionType()) {

      case EQUITY_INDEX_FUTURE_OPTION:
        return new EquityIndexFutureOptionSecurity(exchange, expiry, exerciseType, underlyingId, pointValue,
                                                   isMargined, currency, strike, optionType);

      case EQUITY_DIVIDEND_FUTURE_OPTION:
        return new EquityIndexDividendFutureOptionSecurity(exchange,  expiry, exerciseType, underlyingId, pointValue,
                                                           isMargined, currency, strike, optionType);

      default:
        throw new OpenGammaRuntimeException("Unrecognised listed option type: " + _securityDefinition.getListedFutureOptionType());
    }
  }
}