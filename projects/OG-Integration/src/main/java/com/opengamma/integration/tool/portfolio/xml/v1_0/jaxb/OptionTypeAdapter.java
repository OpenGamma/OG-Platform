/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.base.CaseFormat;
import com.opengamma.financial.security.option.OptionType;

public class OptionTypeAdapter extends XmlAdapter<String, OptionType> {

  @Override
  public OptionType unmarshal(String v) throws Exception {
    return OptionType.parse(v);
  }

  @Override
  public String marshal(OptionType v) throws Exception {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, v.name());
  }
}
