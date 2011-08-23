/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.convert;

import java.util.Collection;

import org.fudgemsg.FudgeContext;

import com.opengamma.language.invoke.AbstractTypeConverterProvider;
import com.opengamma.language.invoke.TypeConverter;
import com.opengamma.util.ArgumentChecker;

/**
 * Constructs instances of the standard converters.
 */
public final class Converters extends AbstractTypeConverterProvider {

  private FudgeContext _fudgeContext = FudgeContext.GLOBAL_DEFAULT;

  public void setFudgeContext(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  protected void loadTypeConverters(final Collection<TypeConverter> converters) {
    converters.add(new ArrayDepthConverter());
    converters.add(new ArrayTypeConverter());
    converters.add(new BoxingConverter());
    converters.add(new DataConverter());
    converters.add(new EnumConverter());
    converters.add(new FudgeTypeConverter(getFudgeContext()));
    converters.add(new ListConverter());
    converters.add(new MapConverter());
    converters.add(new PrimitiveArrayConverter());
    converters.add(new PrimitiveConverter());
    converters.add(new SetConverter());
    converters.add(new ValueConverter());
  }

}
