/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.base.CaseFormat;

public abstract class AbstractEnumAdapter<E extends Enum<E>> extends XmlAdapter<String, Enum<E>> {

  private final CaseFormat _camelFormat = isAttributeAdapter() ? CaseFormat.LOWER_CAMEL : CaseFormat.UPPER_CAMEL;

  protected abstract boolean isAttributeAdapter();

  protected String fromXmlFormat(String v) {
    return _camelFormat.to(CaseFormat.UPPER_UNDERSCORE, v);
  }

  @Override
  public String marshal(Enum<E> v) throws Exception {
    return toXmlFormat(v);
  }

  private String toXmlFormat(Enum<E> v) {
    return CaseFormat.UPPER_UNDERSCORE.to(_camelFormat, v.name());
  }
}
