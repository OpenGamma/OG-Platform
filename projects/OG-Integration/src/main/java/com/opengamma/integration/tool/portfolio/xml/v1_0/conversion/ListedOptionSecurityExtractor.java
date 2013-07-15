/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.conversion;

import org.threeten.bp.ZoneOffset;

import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.id.ExternalId;
import com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb.OptionSecurityDefinition;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Extractor for option securities.
 */
public class ListedOptionSecurityExtractor extends AbstractListedSecurityExtractor<OptionSecurityDefinition> {

  /**
   * Creates an instance.
   * 
   * @param securityDefinition  the definition, not null
   */
  public ListedOptionSecurityExtractor(OptionSecurityDefinition securityDefinition) {
    super(securityDefinition);
  }

  //-------------------------------------------------------------------------
  @Override
  protected ManageableSecurity createSecurity() {
    OptionSecurityDefinition defn = getSecurityDefinition();
    switch (defn.getListedOptionType()) {
      case EQUITY_DIVIDEND_OPTION:
        throw new PortfolioParsingException("EquityIndexDividendOption is not yet supported");
      case EQUITY_INDEX_OPTION:
        ExternalId underlyingId = defn.getUnderlyingId().toExternalId();
        ExerciseType exerciseType = defn.getExerciseType().convert();

        // We are only give month/year (e.g. MAR13) so arbitrarily use the first day of
        // the month but set the accuracy to reflect that
        Expiry expiry = new Expiry(defn.getOptionExpiry().atDay(1).atStartOfDay(ZoneOffset.UTC),
                                   ExpiryAccuracy.MONTH_YEAR);

        return new EquityIndexOptionSecurity(defn.getOptionType(),
                                             defn.getStrike().doubleValue(),
                                             defn.getCurrency(),
                                             underlyingId,
                                             exerciseType,
                                             expiry,
                                             defn.getPointValue(),
                                             defn.getExchange());
      default:
        // Should be prevented by XML parsing
        throw new PortfolioParsingException("Unrecognised listed option type: " + defn.getListedOptionType());
    }
  }

}
