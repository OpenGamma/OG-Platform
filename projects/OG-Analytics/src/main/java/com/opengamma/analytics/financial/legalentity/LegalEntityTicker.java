/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the ticker of an {@link LegalEntity}.
 */
public class LegalEntityTicker implements LegalEntityMeta<LegalEntity> {

  @Override
  public Object getMetaData(final LegalEntity legalEntity) {
    ArgumentChecker.notNull(legalEntity, "obligor");
    return legalEntity.getTicker();
  }

}
