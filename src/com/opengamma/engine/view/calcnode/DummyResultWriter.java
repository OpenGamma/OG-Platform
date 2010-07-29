/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;


/**
 * 
 */
public class DummyResultWriter implements ResultWriter, Serializable {

  @Override
  public void write(AbstractCalculationNode node, CalculationJobResult result) {
  }

}
