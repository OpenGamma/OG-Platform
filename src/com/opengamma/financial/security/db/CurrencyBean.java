package com.opengamma.financial.security.db;

import javax.persistence.Entity;

public class CurrencyBean extends EnumBean {
  protected CurrencyBean() {
  }

  public CurrencyBean(String isoCode) {
    super(isoCode);
  }
}
