/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.formatting;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.core.id.ExternalIdOrderConfig;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/* package */ class YieldCurveDataFormatter extends AbstractFormatter<YieldCurveData> {

  private static final String DATA = "data";
  private static final String LABELS = "labels";
  private static final String ID = "ID";
  private static final String VALUE = "Value";

  private final ExternalIdOrderConfig _orderConfig;
  private final DoubleFormatter _doubleFormatter;

  /* package */ YieldCurveDataFormatter(DoubleFormatter doubleFormatter) {
    this(ExternalIdOrderConfig.DEFAULT_CONFIG, doubleFormatter);
  }

  /* package */ YieldCurveDataFormatter(ExternalIdOrderConfig config, DoubleFormatter doubleFormatter) {
    super(YieldCurveData.class);
    ArgumentChecker.notNull(config, "config");
    ArgumentChecker.notNull(doubleFormatter, "doubleFormatter");
    _orderConfig = config;
    _doubleFormatter = doubleFormatter;
    addFormatter(new Formatter<YieldCurveData>(Format.EXPANDED) {
      @Override
      Map<String, Object> format(YieldCurveData value, ValueSpecification valueSpec, Object inlineKey) {
        return formatExpanded(value, valueSpec);
      }
    });
  }

  @Override
  public String formatCell(YieldCurveData curveData, ValueSpecification valueSpec, Object inlineKey) {
    return "Data Bundle (" + curveData.getDataPoints().size() + " points)";
  }

  private Map<String, Object> formatExpanded(YieldCurveData curveData, ValueSpecification valueSpec) {
    List<List<String>> results = Lists.newArrayListWithCapacity(curveData.getDataPoints().size());
    Map<String, Object> resultsMap = Maps.newHashMap();
    for (Map.Entry<ExternalIdBundle, Double> entry : curveData.getDataPoints().entrySet()) {
      ExternalId id = _orderConfig.getPreferred(entry.getKey());
      String idStr = (id != null) ? id.toString() : "";
      String formattedValue = _doubleFormatter.formatCell(entry.getValue(), valueSpec, null);
      results.add(ImmutableList.of(idStr, formattedValue));
    }
    resultsMap.put(DATA, results);
    resultsMap.put(LABELS, ImmutableList.of(ID, VALUE));
    return resultsMap;
  }

  @Override
  public DataType getDataType() {
    return DataType.LABELLED_MATRIX_1D;
  }
}
