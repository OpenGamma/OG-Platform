/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Class uses an organization source to extract the Obligor from a RED code.
 */
public class CdsObligorExtractor implements RedCodeHandler<LegalEntity> {

  /**
   * The organization source to look up the Obligor with.
   */
  private final LegalEntitySource _legalEntitySource;

  /**
   * Create the extractor ensuring the organization source is not null.
   *
   * @param legalEntitySource the organization source, must not be null
   */
  public CdsObligorExtractor(LegalEntitySource legalEntitySource) {
    ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
    _legalEntitySource = legalEntitySource;
  }

  //-------------------------------------------------------------------------
  /**
   * Extract the LegalEntity from the RED code if it can be found.
   *
   * @param redCode  the RED code to extract the Obligor from, not null
   * @return the Obligor if found, null otherwise
   */
  @Override
  public LegalEntity extract(String redCode) {
    LegalEntity legalEntity = _legalEntitySource.getSingle(ExternalId.of(ExternalSchemes.MARKIT_RED_CODE, redCode));
    return legalEntity == null ? null : legalEntity;
  }

}
