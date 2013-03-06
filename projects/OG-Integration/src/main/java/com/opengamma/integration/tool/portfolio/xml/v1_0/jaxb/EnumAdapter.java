/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.base.CaseFormat;

public abstract class EnumAdapter<E extends Enum<E>> extends XmlAdapter<String, Enum<E>> {

  private final CaseFormat _camelFormat = isAttributeAdapter() ? CaseFormat.LOWER_CAMEL : CaseFormat.UPPER_CAMEL;

  @Override
  public Enum<E> unmarshal(String v) throws Exception {
    return getEnum().valueOf(getEnum().getDeclaringClass(), _camelFormat.to(CaseFormat.UPPER_UNDERSCORE, v));
  }

  protected abstract Enum<E> getEnum();

  protected abstract boolean isAttributeAdapter();

  @Override
  public String marshal(Enum<E> v) throws Exception {
    return CaseFormat.UPPER_UNDERSCORE.to(_camelFormat, v.name());
  }
}
