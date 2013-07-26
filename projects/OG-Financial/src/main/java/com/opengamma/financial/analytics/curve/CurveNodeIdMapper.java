/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
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
  /** The name of this configuration */
  private final String _name;
  /** Curve instrument providers for cash nodes */
  private final Map<Tenor, CurveInstrumentProvider> _cashNodeIds;
  /** Curve instrument providers for continuously-compounded rate nodes */
  private final Map<Tenor, CurveInstrumentProvider> _continuouslyCompoundedRateNodeIds;
  /** Curve instrument providers for credit spread nodes */
  private final Map<Tenor, CurveInstrumentProvider> _creditSpreadNodeIds;
  /** Curve instrument providers for discount factor nodes */
  private final Map<Tenor, CurveInstrumentProvider> _discountFactorNodeIds;
  /** Curve instrument providers for FRA nodes */
  private final Map<Tenor, CurveInstrumentProvider> _fraNodeIds;
  /** Curve instrument providers for FX forward nodes */
  private final Map<Tenor, CurveInstrumentProvider> _fxForwardNodeIds;
  /** Curve instrument providers for rate future nodes */
  private final Map<Tenor, CurveInstrumentProvider> _rateFutureNodeIds;
  /** Curve instrument providers for swap nodes */
  private final Map<Tenor, CurveInstrumentProvider> _swapNodeIds;
  /** Curve instrument providers for zero coupon inflation nodes */
  private final Map<Tenor, CurveInstrumentProvider> _zeroCouponInflationNodeIds;

  /**
   * @param cashNodeIds The cash node ids
   * @param continuouslyCompoundedRateIds The continuously-compounded rate ids
   * @param creditSpreadNodeIds The credit spread node ids
   * @param discountFactorNodeIds The discount factor node ids
   * @param fraNodeIds The FRA node ids
   * @param fxForwardNodeIds The FX forward node ids
   * @param rateFutureNodeIds The rate future node ids
   * @param swapNodeIds The swap node ids
   * @param zeroCouponInflationNodeIds The zero coupon inflation node ids;
   */
  public CurveNodeIdMapper(final Map<Tenor, CurveInstrumentProvider> cashNodeIds,
      final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds,
      final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds,
      final Map<Tenor, CurveInstrumentProvider> discountFactorNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fraNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds,
      final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds,
      final Map<Tenor, CurveInstrumentProvider> swapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds) {
    this(null, cashNodeIds, continuouslyCompoundedRateIds, creditSpreadNodeIds, discountFactorNodeIds, fraNodeIds, fxForwardNodeIds,
        rateFutureNodeIds, swapNodeIds, zeroCouponInflationNodeIds);
  }

  /**
   * @param name The name of this configuration
   * @param cashNodeIds The cash node ids
   * @param continuouslyCompoundedRateIds The continuously-compounded rate ids
   * @param creditSpreadNodeIds The credit spread node ids
   * @param discountFactorNodeIds The discount factor node ids
   * @param fraNodeIds The FRA node ids
   * @param fxForwardNodeIds The FX forward node ids
   * @param rateFutureNodeIds The rate future node ids
   * @param swapNodeIds The swap node ids
   * @param zeroCouponInflationNodeIds The zero coupon inflation node ids;
   */
  public CurveNodeIdMapper(final String name,
      final Map<Tenor, CurveInstrumentProvider> cashNodeIds,
      final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds,
      final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds,
      final Map<Tenor, CurveInstrumentProvider> discountFactorNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fraNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds,
      final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds,
      final Map<Tenor, CurveInstrumentProvider> swapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds) {
    _name = name;
    _cashNodeIds = cashNodeIds;
    _continuouslyCompoundedRateNodeIds = continuouslyCompoundedRateIds;
    _creditSpreadNodeIds = creditSpreadNodeIds;
    _discountFactorNodeIds = discountFactorNodeIds;
    _fraNodeIds = fraNodeIds;
    _fxForwardNodeIds = fxForwardNodeIds;
    _rateFutureNodeIds = rateFutureNodeIds;
    _swapNodeIds = swapNodeIds;
    _zeroCouponInflationNodeIds = zeroCouponInflationNodeIds;
  }

  private static List<String> getCurveIdMapperNames() {
    final List<String> list = new ArrayList<>();
    for (final Field field : CurveSpecificationBuilderConfigurationFudgeBuilder.class.getDeclaredFields()) {
      if (Modifier.isStatic(field.getModifiers()) && field.isSynthetic() == false) {
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
   * Gets the name of this configuration.
   * @return The name
   */
  public String getName() {
    return _name;
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
   * Gets the continuously-compounded rate node ids.
   * @return The continuously-compounded rate node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getContinuouslyCompoundedRateNodeIds() {
    if (_continuouslyCompoundedRateNodeIds != null) {
      return Collections.unmodifiableMap(_continuouslyCompoundedRateNodeIds);
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
   * Gets the discount factor node ids.
   * @return The discount factor node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getDiscountFactorNodeIds() {
    if (_discountFactorNodeIds != null) {
      return Collections.unmodifiableMap(_discountFactorNodeIds);
    }
    return null;
  }

  /**
   * Gets the FRA node ids.
   * @return The FRA node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getFRANodeIds() {
    if (_fraNodeIds != null) {
      return Collections.unmodifiableMap(_fraNodeIds);
    }
    return null;
  }

  /**
   * Gets the FX forward node ids.
   * @return The FX forward node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getFXForwardNodeIds() {
    if (_fxForwardNodeIds != null) {
      return Collections.unmodifiableMap(_fxForwardNodeIds);
    }
    return null;
  }

  /**
   * Gets the rate future node ids.
   * @return The rate future node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getRateFutureNodeIds() {
    if (_rateFutureNodeIds != null) {
      return Collections.unmodifiableMap(_rateFutureNodeIds);
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
   * Gets the zero coupon inflation node ids.
   * @return The zero coupon inflation node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getZeroCouponInflationNodeIds() {
    if (_zeroCouponInflationNodeIds != null) {
      return Collections.unmodifiableMap(_zeroCouponInflationNodeIds);
    }
    return null;
  }

  /**
   * Gets the external id of the cash node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the node
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getCashNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_cashNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get cash node id provider for curve node id mapper called " + _name);
    }
    return getId(_cashNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the cash node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getCashNodeDataField(final Tenor tenor) {
    if (_cashNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get cash node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_cashNodeIds, tenor);
  }

  /**
   * Gets the data field type of the cash node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getCashNodeDataFieldType(final Tenor tenor) {
    if (_cashNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get cash node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_cashNodeIds, tenor);
  }

  /**
   * Gets the external id of the continuously-compounded rate node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the node
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getContinuouslyCompoundedRateNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_continuouslyCompoundedRateNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get continuously-compounded rate node id provider for curve node id mapper called " + _name);
    }
    return getId(_continuouslyCompoundedRateNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the continuously-compounded rate node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getContinuouslyCompoundedRateNodeDataField(final Tenor tenor) {
    if (_continuouslyCompoundedRateNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get continuously-compounded rate node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_continuouslyCompoundedRateNodeIds, tenor);
  }

  /**
   * Gets the data field type of the continuously-compounded rate node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getContinuouslyCompoundedRateDataFieldType(final Tenor tenor) {
    if (_continuouslyCompoundedRateNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get continuously-compounded rate node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_continuouslyCompoundedRateNodeIds, tenor);
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
      throw new OpenGammaRuntimeException("Cannot get credit spread node id provider for curve node id mapper called " + _name);
    }
    return getId(_creditSpreadNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the credit spread node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getCreditSpreadNodeDataField(final Tenor tenor) {
    if (_creditSpreadNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get credit spread node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_creditSpreadNodeIds, tenor);
  }

  /**
   * Gets the data field type of the credit spread node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getCreditSpreadNodeDataFieldType(final Tenor tenor) {
    if (_creditSpreadNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get credit spread node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_creditSpreadNodeIds, tenor);
  }

  /**
   * Gets the external id of the discount factor node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getDiscountFactorNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_discountFactorNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get discount factor node id provider for curve node id mapper called " + _name);
    }
    return getId(_discountFactorNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the discount factor node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getDiscountFactorNodeDataField(final Tenor tenor) {
    if (_discountFactorNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get discount factor node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_discountFactorNodeIds, tenor);
  }

  /**
   * Gets the data field type of the discount factor node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getDiscountFactorNodeDataFieldType(final Tenor tenor) {
    if (_discountFactorNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get discount factor node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_discountFactorNodeIds, tenor);
  }

  /**
   * Gets the external id of the FRA node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getFRANodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_fraNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FRA node id provider for curve node id mapper called " + _name);
    }
    return getId(_fraNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the FRA node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getFRANodeDataField(final Tenor tenor) {
    if (_fraNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FRA node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_fraNodeIds, tenor);
  }

  /**
   * Gets the data field type of the FRA node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getFRANodeDataFieldType(final Tenor tenor) {
    if (_fraNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FRA node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_fraNodeIds, tenor);
  }

  /**
   * Gets the external id of the FX forward node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getFXForwardNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_fxForwardNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FX forward node id provider for curve node id mapper called " + _name);
    }
    return getId(_fxForwardNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the FX forward node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getFXForwardNodeDataField(final Tenor tenor) {
    if (_fxForwardNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FX forward node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_fxForwardNodeIds, tenor);
  }

  /**
   * Gets the data field type of the FX forward node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getFXForwardNodeDataFieldType(final Tenor tenor) {
    if (_fxForwardNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FX forward node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_fxForwardNodeIds, tenor);
  }

  /**
   * Gets the external id of the rate future node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The start tenor
   * @param rateTenor The tenor of the future
   * @param numberFuturesFromTenor The number of futures from the start tenor
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getRateFutureNodeId(final LocalDate curveDate, final Tenor tenor, final Tenor rateTenor, final int numberFuturesFromTenor) {
    if (_rateFutureNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get rate future node id provider for curve node id mapper called " + _name);
    }
    final CurveInstrumentProvider mapper = _rateFutureNodeIds.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, rateTenor, numberFuturesFromTenor);
    }
    throw new OpenGammaRuntimeException("Can't get instrument mapper definition for rate future number " + numberFuturesFromTenor +
        " with time to start " + tenor + " and rate tenor " + rateTenor);
  }

  /**
   * Gets the market data field of the rate future node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getRateFutureNodeDataField(final Tenor tenor) {
    if (_rateFutureNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get rate future node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_rateFutureNodeIds, tenor);
  }

  /**
   * Gets the data field type of the rate future node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getRateFutureNodeDataFieldType(final Tenor tenor) {
    if (_rateFutureNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get rate future node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_rateFutureNodeIds, tenor);
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
      throw new OpenGammaRuntimeException("Cannot get swap node id provider for curve node id mapper called " + _name);
    }
    return getId(_swapNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the swap node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getSwapNodeDataField(final Tenor tenor) {
    if (_swapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get swap node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_swapNodeIds, tenor);
  }

  /**
   * Gets the data field type of the swap node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getSwapNodeDataFieldType(final Tenor tenor) {
    if (_swapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get swap node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_swapNodeIds, tenor);
  }

  /**
   * Gets the external id of the zero coupon inflation node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getZeroCouponInflationNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_zeroCouponInflationNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get zero coupon inflation node id provider for curve node id mapper called " + _name);
    }
    return getId(_zeroCouponInflationNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the zero coupon inflation node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getZeroCouponInflationNodeDataField(final Tenor tenor) {
    if (_zeroCouponInflationNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get zero coupon inflation node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_zeroCouponInflationNodeIds, tenor);
  }

  /**
   * Gets the data field type of the zero coupon inflation node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getZeroCouponInflationNodeDataFieldType(final Tenor tenor) {
    if (_zeroCouponInflationNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get zero coupon inflation node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_zeroCouponInflationNodeIds, tenor);
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
    if (_continuouslyCompoundedRateNodeIds != null) {
      allTenors.addAll(_continuouslyCompoundedRateNodeIds.keySet());
    }
    if (_creditSpreadNodeIds != null) {
      allTenors.addAll(_creditSpreadNodeIds.keySet());
    }
    if (_discountFactorNodeIds != null) {
      allTenors.addAll(_discountFactorNodeIds.keySet());
    }
    if (_fraNodeIds != null) {
      allTenors.addAll(_fraNodeIds.keySet());
    }
    if (_fxForwardNodeIds != null) {
      allTenors.addAll(_fxForwardNodeIds.keySet());
    }
    if (_rateFutureNodeIds != null) {
      allTenors.addAll(_rateFutureNodeIds.keySet());
    }
    if (_swapNodeIds != null) {
      allTenors.addAll(_swapNodeIds.keySet());
    }
    if (_zeroCouponInflationNodeIds != null) {
      allTenors.addAll(_zeroCouponInflationNodeIds.keySet());
    }
    return allTenors;
  }

  private static ExternalId getId(final Map<Tenor, CurveInstrumentProvider> idMapper, final LocalDate curveDate, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor);
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  private static String getMarketDataField(final Map<Tenor, CurveInstrumentProvider> idMapper, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getMarketDataField();
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  private static DataFieldType getDataFieldType(final Map<Tenor, CurveInstrumentProvider> idMapper, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getDataFieldType();
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
    return ObjectUtils.equals(_name, other._name) &&
        ObjectUtils.equals(_cashNodeIds, other._cashNodeIds) &&
        ObjectUtils.equals(_continuouslyCompoundedRateNodeIds, other._continuouslyCompoundedRateNodeIds) &&
        ObjectUtils.equals(_creditSpreadNodeIds, other._creditSpreadNodeIds) &&
        ObjectUtils.equals(_discountFactorNodeIds, other._discountFactorNodeIds) &&
        ObjectUtils.equals(_fraNodeIds, other._fraNodeIds) &&
        ObjectUtils.equals(_fxForwardNodeIds, other._fxForwardNodeIds) &&
        ObjectUtils.equals(_rateFutureNodeIds, other._rateFutureNodeIds) &&
        ObjectUtils.equals(_swapNodeIds, other._swapNodeIds) &&
        ObjectUtils.equals(_zeroCouponInflationNodeIds, other._zeroCouponInflationNodeIds);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + ((_cashNodeIds == null) ? 0 : _cashNodeIds.hashCode());
    result = prime * result + ((_continuouslyCompoundedRateNodeIds == null) ? 0 : _continuouslyCompoundedRateNodeIds.hashCode());
    result = prime * result + ((_creditSpreadNodeIds == null) ? 0 : _creditSpreadNodeIds.hashCode());
    result = prime * result + ((_discountFactorNodeIds == null) ? 0 : _discountFactorNodeIds.hashCode());
    result = prime * result + ((_fraNodeIds == null) ? 0 : _fraNodeIds.hashCode());
    result = prime * result + ((_fxForwardNodeIds == null) ? 0 : _fxForwardNodeIds.hashCode());
    result = prime * result + ((_rateFutureNodeIds == null) ? 0 : _rateFutureNodeIds.hashCode());
    result = prime * result + ((_swapNodeIds == null) ? 0 : _swapNodeIds.hashCode());
    result = prime * result + ((_zeroCouponInflationNodeIds == null) ? 0 : _zeroCouponInflationNodeIds.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
