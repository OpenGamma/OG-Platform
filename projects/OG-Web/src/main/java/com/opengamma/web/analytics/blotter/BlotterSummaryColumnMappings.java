/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;

import org.joda.beans.MetaProperty;

import com.google.common.collect.Maps;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Maps the properties of each
 */
/* package */ class BlotterSummaryColumnMappings {

  private static final Map<Class<? extends ManageableSecurity>, Map<BlotterSummaryColumn, MetaProperty<?>>> s_mappings =
      Maps.newHashMap();

  static {

  }

  private static void addMappings(Class<? extends ManageableSecurity> securityType,
                                  MetaProperty<?> type,
                                  MetaProperty<?> product,
                                  MetaProperty<?> quantity,
                                  MetaProperty<?> maturity,
                                  MetaProperty<?> rate,
                                  MetaProperty<?> frequency,
                                  MetaProperty<?> direction,
                                  MetaProperty<?> floatFrequency,
                                  MetaProperty<?> index) {
    ArgumentChecker.notNull(securityType, "securityType");
    Map<BlotterSummaryColumn, MetaProperty<?>> typeMappings = Maps.newHashMap();
    typeMappings.put(BlotterSummaryColumn.TYPE, type); // TODO this is a fixed value, securityType.getSimpleName()
    typeMappings.put(BlotterSummaryColumn.PRODUCT, product);
    typeMappings.put(BlotterSummaryColumn.QUANTITY, quantity);
    typeMappings.put(BlotterSummaryColumn.MATURITY, maturity);
    typeMappings.put(BlotterSummaryColumn.RATE, rate);
    typeMappings.put(BlotterSummaryColumn.FREQUENCY, frequency);
    typeMappings.put(BlotterSummaryColumn.DIRECTION, direction);
    typeMappings.put(BlotterSummaryColumn.FLOAT_FREQUENCY, floatFrequency);
    typeMappings.put(BlotterSummaryColumn.INDEX, index);
    s_mappings.put(securityType, typeMappings);
  }

  // TODO return a property or something that yields a value?
  // TODO String valueFor(value, securityType, column)
  /* package */ MetaProperty<?> propertyFor(Class<? extends ManageableSecurity> securityType,
                                            BlotterSummaryColumn column) {
    Map<BlotterSummaryColumn, MetaProperty<?>> typeMappings = s_mappings.get(securityType);
    if (typeMappings == null) {
      return null;
    } else {
      return typeMappings.get(column);
    }
  }
}

/* package */ enum BlotterSummaryColumn {
  TYPE,
  PRODUCT,
  QUANTITY,
  MATURITY,
  RATE,
  FREQUENCY,
  DIRECTION,
  FLOAT_FREQUENCY,
  INDEX
}
