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
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

public class ListedOptionSecurityExtractor implements ListedSecurityExtractor{

  private final OptionSecurityDefinition _securityDefinition;

  public ListedOptionSecurityExtractor(OptionSecurityDefinition securityDefinition) {
    _securityDefinition = securityDefinition;
  }

  @Override
  public ManageableSecurity[] extract() {

    switch (_securityDefinition.getListedOptionType()) {
      case EQUITY_DIVIDEND_OPTION:
        throw new OpenGammaRuntimeException("EquityIndexDividendOption is not yet supported");
      case EQUITY_INDEX_OPTION:
        ExternalId underlyingId = _securityDefinition.getUnderlyingId().toExternalId();
        com.opengamma.financial.security.option.ExerciseType exerciseType = _securityDefinition.getExerciseType().convert();

        // We are only give month/year (e.g. MAR13) so arbitrarily use the first day of
        // the month but set the accuracy to reflect that
        Expiry expiry = new Expiry(_securityDefinition.getOptionExpiry().atDay(1).atStartOfDay(ZoneOffset.UTC), ExpiryAccuracy.MONTH_YEAR);

        EquityIndexOptionSecurity security = new EquityIndexOptionSecurity(_securityDefinition.getOptionType(),
                                                                           _securityDefinition.getStrike().doubleValue(),
                                                                           _securityDefinition.getCurrency(),
                                                                           underlyingId,
                                                                           exerciseType,
                                                                           expiry,
                                                                           _securityDefinition.getPointValue(),
                                                                           _securityDefinition.getExchange());

        security.addExternalId(ExternalId.of("XML_LOADER", Integer.toHexString(
            new HashCodeBuilder()
                .append(security.getClass())
                .append(_securityDefinition.getOptionType())
                .append(_securityDefinition.getStrike())
                .append(_securityDefinition.getCurrency())
                .append(underlyingId)
                .append(exerciseType)
                .append(_securityDefinition.getOptionExpiry())
                .append(_securityDefinition.getPointValue())
                .append(_securityDefinition.getExchange())
                .toHashCode())));

        return new ManageableSecurity[]{security};

      default:
        throw new OpenGammaRuntimeException("Unrecognised listed option type: " + _securityDefinition.getListedOptionType());
    }
  }
}
