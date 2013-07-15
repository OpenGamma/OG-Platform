/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class AdditionalAttributes {

  @XmlElement(name = "attribute")
  private List<Attribute> _attributes;

  public List<Attribute> getAttributes() {
    return _attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    _attributes = attributes;
  }

}
