/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.FutureSecurityDefinition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Extractor for future securities.
 */
public class ListedFutureSecurityExtractor
    extends AbstractListedSecurityExtractor<FutureSecurityDefinition> {

  /**
   * Creates an instance.
   * 
   * @param securityDefinition  the definition, not null
   */
  public ListedFutureSecurityExtractor(FutureSecurityDefinition securityDefinition) {
    super(securityDefinition);
  }

  @Override
  protected ManageableSecurity createSecurity() {
    FutureSecurityDefinition defn = getSecurityDefinition();
    ExternalId underlyingId = defn.getUnderlyingId().toExternalId();
    Expiry expiry = new Expiry(defn.getFutureExpiry().atDay(1).atStartOfDay(ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);
    String exchange = defn.getExchange();
    Currency currency = defn.getCurrency();
    int pointValue = defn.getPointValue();
    String settlementExchange = defn.getSettlementExchange();
    ZonedDateTime settlementDate = defn.getSettlementDate().atStartOfDay(ZoneOffset.UTC);

    switch (defn.getFutureType()) {
      case EQUITY_DIVIDEND_FUTURE:
        return new EquityIndexDividendFutureSecurity(
            expiry,
            exchange,
            settlementExchange,
            currency,
            pointValue,
            settlementDate,
            underlyingId,
            defn.getFutureCategory());
      case EQUITY_INDEX_FUTURE:
        throw new PortfolioParsingException("EquityIndexFuture is not yet supported");
      default:
        // The xml validation should prevent this from happening
        throw new PortfolioParsingException("Unrecognised listed option type: " + defn.getFutureType());
    }
  }

}
