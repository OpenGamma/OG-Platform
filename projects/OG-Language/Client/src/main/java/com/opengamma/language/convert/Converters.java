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
    converters.add(ArrayDepthConverter.INSTANCE);
    converters.add(ArrayTypeConverter.INSTANCE);
    converters.add(BoxingConverter.INSTANCE);
    converters.add(DataConverter.INSTANCE);
    converters.add(EnumConverter.INSTANCE);
    converters.add(new FudgeTypeConverter(getFudgeContext()));
    converters.add(ListConverter.INSTANCE);
    converters.add(MapConverter.INSTANCE);
    converters.add(PrimitiveArrayConverter.INSTANCE);
    converters.add(PrimitiveConverter.INSTANCE);
    converters.add(SetConverter.INSTANCE);
    converters.add(ValueConverter.INSTANCE);
  }

}
