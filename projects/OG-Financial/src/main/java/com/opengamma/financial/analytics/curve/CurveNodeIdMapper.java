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
import com.opengamma.core.config.Config;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.fudgemsg.CurveSpecificationBuilderConfigurationFudgeBuilder;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Contains maps of tenors to curve instrument providers (which generates market data tickers) for {@link CurveNode} types. These
 * maps are then used to generate market data requests in curve construction.
 */
@Config
public class CurveNodeIdMapper {
  /**
   * The names of the curve instrument providers.
   */
  public static final List<String> s_curveIdMapperNames = getCurveIdMapperNames();

  private final Map<Tenor, CurveInstrumentProvider> _cashNodeIds;
  private final Map<Tenor, CurveInstrumentProvider> _creditSpreadNodeIds;
  private final Map<Tenor, CurveInstrumentProvider> _swapNodeIds;

  /**
   * @param cashNodeIds The cash node ids
   * @param creditSpreadNodeIds The credit spread node ids
   * @param swapNodeIds The swap node ids
   */
  public CurveNodeIdMapper(final Map<Tenor, CurveInstrumentProvider> cashNodeIds, final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds,
      final Map<Tenor, CurveInstrumentProvider> swapNodeIds) {
    _cashNodeIds = cashNodeIds;
    _creditSpreadNodeIds = creditSpreadNodeIds;
    _swapNodeIds = swapNodeIds;
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

  /**
   * Gets the cash node ids.
   * @return The cash node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getCashNodeIds() {
    if (_cashNodeIds != null) {
      return Collections.unmodifiableMap(_cashNodeIds);
    }
    return null;
  }

  /**
   * Gets the credit spread node ids.
   * @return The credit spread node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getCreditSpreadNodeIds() {
    if (_creditSpreadNodeIds != null) {
      return Collections.unmodifiableMap(_creditSpreadNodeIds);
    }
    return null;
  }

  /**
   * Gets the swap node ids.
   * @return The swap node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getSwapNodeIds() {
    if (_swapNodeIds != null) {
      return Collections.unmodifiableMap(_swapNodeIds);
    }
    return null;
  }

  /**
   * Gets the external id of the cash node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getCashNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_cashNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get cash node id provider");
    }
    return getStaticSecurity(_cashNodeIds, curveDate, tenor);
  }

  /**
   * Gets the external id of the credit spread node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getCreditSpreadNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_creditSpreadNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get credit spread node id provider");
    }
    return getStaticSecurity(_creditSpreadNodeIds, curveDate, tenor);
  }

  /**
   * Gets the external id of the swap node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getSwapNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_swapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get swap node id provider");
    }
    return getStaticSecurity(_swapNodeIds, curveDate, tenor);
  }

  /**
   * Gets the unique tenors for all curve node types sorted by period.
   * @return All unique tenors
   */
  public SortedSet<Tenor> getAllTenors() {
    final SortedSet<Tenor> allTenors = new TreeSet<>();
    if (_cashNodeIds != null) {
      allTenors.addAll(_cashNodeIds.keySet());
    }
    if (_creditSpreadNodeIds != null) {
      allTenors.addAll(_creditSpreadNodeIds.keySet());
    }
    if (_swapNodeIds != null) {
      allTenors.addAll(_swapNodeIds.keySet());
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
    return ObjectUtils.equals(_cashNodeIds, other._cashNodeIds) &&
        ObjectUtils.equals(_creditSpreadNodeIds, other._creditSpreadNodeIds) &&
        ObjectUtils.equals(_swapNodeIds, other._swapNodeIds);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_cashNodeIds == null) ? 0 : _cashNodeIds.hashCode());
    result = prime * result + ((_creditSpreadNodeIds == null) ? 0 : _creditSpreadNodeIds.hashCode());
    result = prime * result + ((_swapNodeIds == null) ? 0 : _swapNodeIds.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
