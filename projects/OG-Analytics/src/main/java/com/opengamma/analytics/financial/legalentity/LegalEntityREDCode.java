/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.legalentity;

import com.opengamma.util.ArgumentChecker;

/**
 * Gets the region of an {@link LegalEntity}.
 */
public class LegalEntityREDCode implements LegalEntityMeta<LegalEntityWithREDCode> {

  @Override
  public Object getMetaData(final LegalEntityWithREDCode obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getRedCode();
  }

}
