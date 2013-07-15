/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Formats {@link ValueProperties} as a grid with 3 columns: Property name, property values (comma separated) and
 * whether the property is optional.
 */
/* package */ class ValuePropertiesFormatter extends AbstractFormatter<ValueProperties> {

  /* package */ ValuePropertiesFormatter() {
    super(ValueProperties.class);
    addFormatter(new Formatter<ValueProperties>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(ValueProperties properties, ValueSpecification valueSpec, Object inlineKey) {
        Set<String> names = properties.getProperties();
        List<List<String>> matrix = Lists.newArrayListWithCapacity(names.size());
        List<String> yLabels = Lists.newArrayListWithCapacity(names.size());
        for (String name : names) {
          Set<String> values = properties.getValues(name);
          boolean optional = properties.isOptional(name);
          List<String> row = Lists.newArrayListWithCapacity(2);
          row.add(StringUtils.join(values, ", "));
          row.add(optional ? "true" : "false");
          matrix.add(row);
          yLabels.add(name);
        }
        Map<String, Object> output = Maps.newHashMap();
        output.put(LabelledMatrix2DFormatter.MATRIX, matrix);
        // TODO it would be good if the UI could handle a label for the first column: "Property"
        output.put(LabelledMatrix2DFormatter.X_LABELS, Lists.newArrayList("Property", "Value", "Optional"));
        output.put(LabelledMatrix2DFormatter.Y_LABELS, yLabels);
        return output;
      }
    });
  }

  @Override
  public Object formatCell(ValueProperties properties, ValueSpecification valueSpec, Object inlineKey) {
    return "Value Properties (" + properties.getProperties().size() + ")";
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_2D;
  }
}
