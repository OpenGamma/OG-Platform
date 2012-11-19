/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.analytics.financial.credit.RestructuringClause;

/**
 * Hibernate bean for storing the {@link RestructuringClause} type
 */
public class RestructuringClauseBean extends EnumBean {

  protected RestructuringClauseBean() {
  }

  public RestructuringClauseBean(final String restructuringClauseName) {
    super(restructuringClauseName);
  }
}
