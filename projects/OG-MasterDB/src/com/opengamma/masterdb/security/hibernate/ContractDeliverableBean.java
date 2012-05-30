/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate storage for a contract category.
 */
public class ContractDeliverableBean extends EnumBean {
  
  private ContractCategoryBean _category;

  public ContractDeliverableBean(String name, ContractCategoryBean category) {
    super(name);
    _category = category;
  }

  protected ContractDeliverableBean() {
  }

  public ContractDeliverableBean(String categoryName) {
    super(categoryName);
  }

  public ContractCategoryBean getCategory() {
    return _category;
  }

  public void setCategory(ContractCategoryBean _category) {
    this._category = _category;
  }
}
