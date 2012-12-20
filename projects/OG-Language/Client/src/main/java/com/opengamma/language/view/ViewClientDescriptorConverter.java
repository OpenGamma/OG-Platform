/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.view;

import static com.opengamma.language.convert.TypeMap.ZERO_LOSS;

import java.util.Map;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.convert.ValueConversionContext;
import com.opengamma.language.definition.JavaTypeInfo;
import com.opengamma.language.invoke.AbstractTypeConverter;

/**
 * Converter for automatic resolution of strings to a default view client descriptor (e.g. ticking market data).
 */
public class ViewClientDescriptorConverter extends AbstractTypeConverter {

  /**
   * Default instance.
   */
  public static final ViewClientDescriptorConverter INSTANCE = new ViewClientDescriptorConverter();

  private static final TypeMap TO_VIEW_CLIENT_DESCRIPTOR = TypeMap.of(ZERO_LOSS, JavaTypeInfo.builder(String.class).get());

  protected ViewClientDescriptorConverter() {
  }

  @Override
  public boolean canConvertTo(final JavaTypeInfo<?> targetType) {
    return targetType.getRawClass() == ViewClientDescriptor.class;
  }

  @Override
  public void convertValue(final ValueConversionContext conversionContext, final Object value, final JavaTypeInfo<?> type) {
    final String view = (String) value;
    UniqueId viewId;
    try {
      viewId = UniqueId.parse(view);
    } catch (final IllegalArgumentException e) {
      // Not a unique identifier. Might be a view name ...
      final ViewDefinition viewDefinitionItem = conversionContext.getGlobalContext().getViewProcessor().getConfigSource().getSingle(ViewDefinition.class, view, VersionCorrection.LATEST);
      if (viewDefinitionItem != null) {
        viewId = viewDefinitionItem.getUniqueId();
      } else {
        conversionContext.setFail();
        return;
      }
    }
    conversionContext.setResult(ViewClientDescriptor.tickingMarketData(viewId, null));
  }

  @Override
  public Map<JavaTypeInfo<?>, Integer> getConversionsTo(final JavaTypeInfo<?> targetType) {
    return TO_VIEW_CLIENT_DESCRIPTOR;
  }

}
