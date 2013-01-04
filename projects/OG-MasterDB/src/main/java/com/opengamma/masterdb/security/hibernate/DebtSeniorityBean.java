/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import com.opengamma.analytics.financial.credit.DebtSeniority;

/**
 * Hibernate bean for storing the {@link DebtSeniority} type
 */
public class DebtSeniorityBean extends EnumBean {

  protected DebtSeniorityBean() {
  }

  public DebtSeniorityBean(final String debtSeniorityName) {
    super(debtSeniorityName);
  }
}
