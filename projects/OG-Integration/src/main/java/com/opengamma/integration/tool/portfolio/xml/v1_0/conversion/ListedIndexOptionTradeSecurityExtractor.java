/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.threeten.bp.ZoneOffset;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.ListedIndexOptionTrade;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

public class ListedIndexOptionTradeSecurityExtractor extends TradeSecurityExtractor<ListedIndexOptionTrade> {

  @Override
  public ManageableSecurity[] extractSecurity(ListedIndexOptionTrade trade) {

    switch (trade.getListedIndexOptionType()) {
      case EQUITY_DIVIDEND_OPTION:
        throw new OpenGammaRuntimeException("EquityIndexDividendOption is not yet supported");
      case EQUITY_INDEX_OPTION:
        ExternalId underlyingId = trade.getUnderlyingId().toExternalId();
        ExerciseType exerciseType = trade.getExerciseType().convert();

        // We are only give month/year (e.g. MAR13) so arbitrarily use the first day of
        // the month but set the accuracy to reflect that
        Expiry expiry = new Expiry(trade.getOptionExpiry().atDay(1).atStartOfDay(ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);

        EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(trade.getOptionType(),
                                                                                            trade.getStrike().doubleValue(),
                                                                                            trade.getCurrency(),
                                                                                            underlyingId,
                                                                                            exerciseType,
                                                                                            expiry,
                                                                                            trade.getPointValue(),
                                                                                            trade.getExchange());
        security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
            new HashCodeBuilder()
                .append(security.getClass())
                .append(trade.getOptionType())
                .append(trade.getStrike())
                .append(trade.getCurrency())
                .append(underlyingId)
                .append(exerciseType)
                .append(trade.getOptionExpiry())
                .append(trade.getPointValue())
                .append(trade.getExchange()).toHashCode()
        )));
        return securityArray(security);

      default:
        throw new OpenGammaRuntimeException("Unrecognised listed option type: " + trade.getListedIndexOptionType());
    }
  }


}