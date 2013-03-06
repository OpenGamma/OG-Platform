/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement
public class PositionRef {

  @XmlIDREF
  @XmlAttribute(name = "ref")
  private Position _position;

  public PositionRef() {
  }

  public PositionRef(Position position) {
    _position = position;
  }

  public Position getPosition() {
    return _position;
  }

  public void setPosition(Position position) {
    _position = position;
  }
}
