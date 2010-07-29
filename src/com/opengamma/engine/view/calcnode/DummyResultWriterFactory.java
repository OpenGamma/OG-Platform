/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

/**
 * 
 */
public class DummyResultWriterFactory implements ResultWriterFactory {

  @Override
  public ResultWriter create(CalculationJobSpecification jobSpec, List<CalculationJobItem> items, String hostId) {
    return new DummyResultWriter();
  }

}
