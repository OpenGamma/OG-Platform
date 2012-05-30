/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

/**
 * Hibernate storage for a contract category.
 */
public class ContractCategoryBean extends EnumWithDescriptionBean {

  protected ContractCategoryBean() {
  }

  public ContractCategoryBean(String categoryName) {
    super(categoryName, null);
  }

  public ContractCategoryBean(String categoryName, String description) {
    super(categoryName, description);
  }

}
