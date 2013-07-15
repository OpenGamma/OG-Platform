/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.security.cds.AbstractCreditDefaultSwapSecurity;
import com.opengamma.id.ExternalId;

/**
 * Class uses an organization source to extract the Obligor from a CDS.
 * 
 * @param <T>  the value type
 */
public class CdsRedCodeExtractor<T> {

  private static final Logger s_logger = LoggerFactory.getLogger(CdsRedCodeExtractor.class);

  /**
   * Handler which will perform further processing of the red code.
   */
  private final RedCodeHandler<T> _redCodeHandler;

  /**
   * Creates an instance.
   * 
   * @param redCodeHandler  the handler, not null
   */
  public CdsRedCodeExtractor(RedCodeHandler<T> redCodeHandler) {
    _redCodeHandler = redCodeHandler;
  }

  //-------------------------------------------------------------------------
  /**
   * Extract the RED code from the CDS if it can be found.
   *
   * @param cds the CDS to extract the RED code  from
   * @return the RED code if found, null otherwise
   */
  public T extract(AbstractCreditDefaultSwapSecurity cds) {
    ExternalId refEntityId = cds.getReferenceEntity();
    if (refEntityId.isScheme(ExternalSchemes.MARKIT_RED_CODE)) {

      return _redCodeHandler.extract(refEntityId.getValue());
    } else {
      s_logger.warn("Unable to lookup RED code as reference entity external id uses scheme: {}", refEntityId.getScheme());
      return null;
    }
  }

}
