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

  protected LegalEntityREDCode() {
  }

  public static Builder builder() {
    return new Builder();
  }

  @Override
  public Object getMetaData(final LegalEntityWithREDCode obligor) {
    ArgumentChecker.notNull(obligor, "obligor");
    return obligor.getRedCode();
  }

  public static class Builder {

    protected Builder() {
    }

    public LegalEntityREDCode create() {
      return new LegalEntityREDCode();
    }
  }

}
