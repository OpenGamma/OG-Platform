/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

public class FutureSecurityExtractor extends AbstractListedSecurityExtractor<FutureSecurityDefinition> {

  public FutureSecurityExtractor(FutureSecurityDefinition securityDefinition) {
    super(securityDefinition);
  }

  @Override
  protected ManageableSecurity createSecurity() {

    ExternalId underlyingId = _securityDefinition.getUnderlyingId().toExternalId();
    Expiry expiry = new Expiry(_securityDefinition.getFutureExpiry().atDay(1).atStartOfDay(ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);

    String exchange = _securityDefinition.getExchange();
    Currency currency = _securityDefinition.getCurrency();
    int pointValue = _securityDefinition.getPointValue();
    String settlementExchange = _securityDefinition.getSettlementExchange();
    ZonedDateTime settlementDate = _securityDefinition.getSettlementDate().atStartOfDay(ZoneOffset.UTC);

    switch (_securityDefinition.getFutureType()) {

      case EQUITY_DIVIDEND_FUTURE:

        return new EquityIndexDividendFutureSecurity(
            expiry,
            exchange,
            settlementExchange,
            currency,
            pointValue,
            settlementDate,
            underlyingId,
            _securityDefinition.getFutureCategory());

      case EQUITY_INDEX_FUTURE:
        throw new OpenGammaRuntimeException("EquityIndexFuture is not yet supported");

      default:
        throw new OpenGammaRuntimeException("Unrecognised listed option type: " + _securityDefinition.getFutureType());
    }
  }
}