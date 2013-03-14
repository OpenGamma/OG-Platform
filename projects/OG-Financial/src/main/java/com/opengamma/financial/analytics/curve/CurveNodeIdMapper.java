/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.fudgemsg.CurveSpecificationBuilderConfigurationFudgeBuilder;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
public class CurveNodeIdMapper {
  public static final List<String> s_curveIdMapperNames = getCurveIdMapperNames();

  private final Map<Tenor, CurveInstrumentProvider> _creditSpreadIds;

  public CurveNodeIdMapper(final Map<Tenor, CurveInstrumentProvider> creditSpreadIds) {
    _creditSpreadIds = creditSpreadIds;
  }

  private static List<String> getCurveIdMapperNames() {
    final List<String> list = new ArrayList<>();
    for (final Field field : CurveSpecificationBuilderConfigurationFudgeBuilder.class.getDeclaredFields()) {
      if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
        field.setAccessible(true);
        try {
          list.add((String) field.get(null));
        } catch (final Exception ex) {
          // Ignore
        }
      }
    }
    Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    return ImmutableList.copyOf(list);
  }

  public Map<Tenor, CurveInstrumentProvider> getCreditSpreadIds() {
    return Collections.unmodifiableMap(_creditSpreadIds);
  }

  public ExternalId getCreditSpreadId(final LocalDate curveDate, final Tenor tenor) {
    if (_creditSpreadIds == null) {
      throw new OpenGammaRuntimeException("Cannot get credit spread id provider");
    }
    return getStaticSecurity(_creditSpreadIds, curveDate, tenor);
  }

  public SortedSet<Tenor> getAllTenors() {
    final SortedSet<Tenor> allTenors = new TreeSet<>();
    if (_creditSpreadIds != null) {
      allTenors.addAll(_creditSpreadIds.keySet());
    }
    return allTenors;
  }

  private ExternalId getStaticSecurity(final Map<Tenor, CurveInstrumentProvider> idMapper, final LocalDate curveDate, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor);
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CurveNodeIdMapper)) {
      return false;
    }
    final CurveNodeIdMapper other = (CurveNodeIdMapper) o;
    return ObjectUtils.equals(_creditSpreadIds, other._creditSpreadIds);
  }

  @Override
  public int hashCode() {
    return _creditSpreadIds.hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
