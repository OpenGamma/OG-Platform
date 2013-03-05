/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma
 group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

public class AdditionalAttributes {

  @XmlElement(name = "attribute")
  private List<Attribute> _attributes;

  public List<Attribute> getAttributes() {
    return _attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    _attributes = attributes;
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Attribute {

    @XmlAttribute(name = "name")
    private String _name;

    @XmlValue
    private String _value;

    public String getName() {
      return _name;
    }

    public void setName(String name) {
      _name = name;
    }

    public String getValue() {
      return _value;
    }

    public void setValue(String value) {
      _value = value;
    }
  }
}