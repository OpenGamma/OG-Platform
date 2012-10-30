/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.language.value;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.view.helper.AvailableOutput;
import com.opengamma.engine.view.helper.AvailableOutputs;
import com.opengamma.language.convert.AbstractMappedConverter;
import com.opengamma.language.convert.TypeMap;
import com.opengamma.language.definition.JavaTypeInfo;

/**
 * Converts a {@link AvailableOutputs} instance to a set of value requirement names. If a specific
 * language binding needs to expose more information, replace this converter with a more appropriate
 * one during context initialization.
 */
@SuppressWarnings("unchecked")
public class AvailableOutputsConverter extends AbstractMappedConverter {

  private static final JavaTypeInfo<AvailableOutputs> AVAILABLE_OUTPUTS = JavaTypeInfo.builder(AvailableOutputs.class).get();
  private static final JavaTypeInfo<Map> MAP = JavaTypeInfo.builder(Map.class).get();

  /**
   * Default instance.
   */
  public static final AvailableOutputsConverter INSTANCE = new AvailableOutputsConverter();

  protected AvailableOutputsConverter() {
    conversion(TypeMap.ZERO_LOSS, AVAILABLE_OUTPUTS, MAP, new Action<AvailableOutputs, Map>() {
      @Override
      protected Map convert(final AvailableOutputs value) {
        final Set<AvailableOutput> outputs = value.getOutputs();
        final Map<String, ValueProperties> map = Maps.newHashMapWithExpectedSize(outputs.size());
        for (AvailableOutput output : outputs) {
          map.put(output.getValueName(), output.getProperties());
        }
        return map;
      }
    });
  }

}
