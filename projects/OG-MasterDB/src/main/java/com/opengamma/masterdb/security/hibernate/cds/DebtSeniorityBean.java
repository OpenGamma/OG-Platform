/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import com.opengamma.masterdb.security.hibernate.EnumBean;

/**
 * Hibernate bean for storing debt seniority type
 */
public class DebtSeniorityBean extends EnumBean {
  
  protected DebtSeniorityBean() {
  }

  public DebtSeniorityBean(String debtSeniorityType) {
    super(debtSeniorityType);
  }

}
