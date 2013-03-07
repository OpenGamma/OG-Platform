/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.money.Currency;

public class CurrencyAdapter extends XmlAdapter<String, Currency> {

  @Override
  public Currency unmarshal(String v) throws Exception {
    return Currency.of(v);
  }

  @Override
  public String marshal(Currency v) throws Exception {
    return v.getCode();
  }
}
