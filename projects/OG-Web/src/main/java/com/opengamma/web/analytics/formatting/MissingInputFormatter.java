/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import com.opengamma.engine.cache.MissingInput;
import com.opengamma.engine.value.ValueSpecification;

/*package*/ class MissingInputFormatter extends AbstractFormatter<MissingInput> {

  /*package*/ MissingInputFormatter() {
    super(MissingInput.class);
    addFormatter(new Formatter<MissingInput>(Format.HISTORY) {
      @Override
      Object format(MissingInput value, ValueSpecification valueSpec, Object inlineKey) {
        return null;
      }
    });
  }

  @Override
  public Object formatCell(MissingInput value, ValueSpecification valueSpec, Object inlineKey) {
    return value.toString();
  }

  @Override
  public DataType getDataType() {
    return DataType.STRING;
  }
  
}
