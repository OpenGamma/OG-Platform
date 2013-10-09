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
import com.opengamma.financial.fudgemsg.CurveNodeIdMapperBuilder;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Contains maps of tenors to curve instrument providers (which generates market data tickers) for {@link CurveNode} types. These
 * maps are then used to generate market data requests in curve construction.
 */
@Config(description = "Cuve node ID mapper")
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
  /** Curve instrument providers for deliverable swap future nodes */
  private final Map<Tenor, CurveInstrumentProvider> _deliverableSwapFutureNodeIds;
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
   * Builder class for CurveNodeIdMapper
   */
  public static final class Builder {
    /** The name of this configuration */
    private String _name;
    /** Curve instrument providers for cash nodes */
    private Map<Tenor, CurveInstrumentProvider> _cashNodeIds;
    /** Curve instrument providers for continuously-compounded rate nodes */
    private Map<Tenor, CurveInstrumentProvider> _continuouslyCompoundedRateNodeIds;
    /** Curve instrument providers for credit spread nodes */
    private Map<Tenor, CurveInstrumentProvider> _creditSpreadNodeIds;
    /** Curve instrument providers for deliverable swap future nodes */
    private Map<Tenor, CurveInstrumentProvider> _deliverableSwapFutureNodeIds;
    /** Curve instrument providers for discount factor nodes */
    private Map<Tenor, CurveInstrumentProvider> _discountFactorNodeIds;
    /** Curve instrument providers for FRA nodes */
    private Map<Tenor, CurveInstrumentProvider> _fraNodeIds;
    /** Curve instrument providers for FX forward nodes */
    private Map<Tenor, CurveInstrumentProvider> _fxForwardNodeIds;
    /** Curve instrument providers for rate future nodes */
    private Map<Tenor, CurveInstrumentProvider> _rateFutureNodeIds;
    /** Curve instrument providers for swap nodes */
    private Map<Tenor, CurveInstrumentProvider> _swapNodeIds;
    /** Curve instrument providers for zero coupon inflation nodes */
    private Map<Tenor, CurveInstrumentProvider> _zeroCouponInflationNodeIds;

    private Builder() {}

    /**
     * The name of this configuration
     * @param name the name
     * @return this
     */
    public Builder name(final String name) {
      _name = name; return this;
    }
    /**
     * Curve instrument providers for cash nodes
     * @param cashNodeIds the cashNodeIds
     * @return this
     */
    public Builder cashNodeIds(final Map<Tenor, CurveInstrumentProvider> cashNodeIds) {
      _cashNodeIds = cashNodeIds; return this;
    }
    /**
     * Curve instrument providers for continuously-compounded rate nodes
     * @param continuouslyCompoundedRateNodeIds the continuouslyCompoundedRateNodeIds
     * @return this
     */
    public Builder continuouslyCompoundedRateNodeIds(final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateNodeIds) {
      _continuouslyCompoundedRateNodeIds = continuouslyCompoundedRateNodeIds; return this;
    }
    /**
     * Curve instrument providers for credit spread nodes
     * @param creditSpreadNodeIds the creditSpreadNodeIds
     * @return this
     */
    public Builder creditSpreadNodeIds(final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds) {
      _creditSpreadNodeIds = creditSpreadNodeIds; return this;
    }
    /**
     * Curve instrument providers for deliverable swap future nodes
     * @param deliverableSwapFutureNodeIds the deliverableSwapFutureNodeIds
     * @return this
     */
    public Builder deliverableSwapFutureNodeIds(final Map<Tenor, CurveInstrumentProvider> deliverableSwapFutureNodeIds) {
      _deliverableSwapFutureNodeIds = deliverableSwapFutureNodeIds; return this;
    }
    /**
     * Curve instrument providers for discount factor nodes
     * @param discountFactorNodeIds the discountFactorNodeIds
     * @return this
     */
    public Builder discountFactorNodeIds(final Map<Tenor, CurveInstrumentProvider> discountFactorNodeIds) {
      _discountFactorNodeIds = discountFactorNodeIds; return this;
    }
    /**
     * Curve instrument providers for FRA nodes
     * @param fraNodeIds the fraNodeIds
     * @return this
     */
    public Builder fraNodeIds(final Map<Tenor, CurveInstrumentProvider> fraNodeIds) {
      _fraNodeIds = fraNodeIds; return this;
    }
    /**
     * Curve instrument providers for FX forward nodes
     * @param fxForwardNodeIds the fxForwardNodeIds
     * @return this
     */
    public Builder fxForwardNodeIds(final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds) {
      _fxForwardNodeIds = fxForwardNodeIds; return this;
    }
    /**
     * Curve instrument providers for rate future nodes
     * @param rateFutureNodeIds the rateFutureNodeIds
     * @return this
     */
    public Builder rateFutureNodeIds(final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds) {
      _rateFutureNodeIds = rateFutureNodeIds; return this;
    }
    /**
     * Curve instrument providers for swap nodes
     * @param swapNodeIds the swapNodeIds
     * @return this
     */
    public Builder swapNodeIds(final Map<Tenor, CurveInstrumentProvider> swapNodeIds) {
      _swapNodeIds = swapNodeIds; return this;
    }
    /**
     * Curve instrument providers for zero coupon inflation nodes
     * @param zeroCouponInflationNodeIds the zeroCouponInflationNodeIds
     * @return this
     */
    public Builder zeroCouponInflationNodeIds(final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds) {
      _zeroCouponInflationNodeIds = zeroCouponInflationNodeIds; return this;
    }
    /**
     * @return a new {@link CurveNodeIdMapper} instance.
     */
    public CurveNodeIdMapper build() {
      return new CurveNodeIdMapper(_name, _cashNodeIds,
          _continuouslyCompoundedRateNodeIds, _creditSpreadNodeIds,
          _deliverableSwapFutureNodeIds, _discountFactorNodeIds, _fraNodeIds, _fxForwardNodeIds,
          _rateFutureNodeIds, _swapNodeIds, _zeroCouponInflationNodeIds);
    }

  }

  @SuppressWarnings("synthetic-access")
  public static Builder builder() { return new Builder(); }

  /**
   * @param name The name of this configuration
   * @param cashNodeIds The cash node ids
   * @param continuouslyCompoundedRateIds The continuously-compounded rate ids
   * @param creditSpreadNodeIds The credit spread node ids
   * @param deliverableSwapFutureNodeIds The deliverable swap future node ids
   * @param discountFactorNodeIds The discount factor node ids
   * @param fraNodeIds The FRA node ids
   * @param fxForwardNodeIds The FX forward node ids
   * @param rateFutureNodeIds The rate future node ids
   * @param swapNodeIds The swap node ids
   * @param zeroCouponInflationNodeIds The zero coupon inflation node ids;
   */
  protected CurveNodeIdMapper(final String name,
      final Map<Tenor, CurveInstrumentProvider> cashNodeIds,
      final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds,
      final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds,
      final Map<Tenor, CurveInstrumentProvider> deliverableSwapFutureNodeIds,
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
    _deliverableSwapFutureNodeIds = deliverableSwapFutureNodeIds;
    _discountFactorNodeIds = discountFactorNodeIds;
    _fraNodeIds = fraNodeIds;
    _fxForwardNodeIds = fxForwardNodeIds;
    _rateFutureNodeIds = rateFutureNodeIds;
    _swapNodeIds = swapNodeIds;
    _zeroCouponInflationNodeIds = zeroCouponInflationNodeIds;
  }

  /**
   * Gets all fields used by the Fudge builder.
   * @return The fields
   */
  protected static List<String> getCurveIdMapperNames() {
    final List<String> list = new ArrayList<>();
    for (final Field field : CurveNodeIdMapperBuilder.class.getDeclaredFields()) {
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
   * Gets the deliverable swap future node ids.
   * @return The deliverable swap future node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getDeliverableSwapFutureNodeIds() {
    if (_deliverableSwapFutureNodeIds != null) {
      return Collections.unmodifiableMap(_deliverableSwapFutureNodeIds);
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
   * Gets the external id of the deliverable swap future node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The start tenor
   * @param swapTenor The tenor of the future
   * @param numberFuturesFromTenor The number of futures from the start tenor
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getDeliverableSwapFutureNodeId(final LocalDate curveDate, final Tenor tenor, final Tenor swapTenor, final int numberFuturesFromTenor) {
    if (_deliverableSwapFutureNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get deliverable swap future node id provider for curve node id mapper called " + _name);
    }
    final CurveInstrumentProvider mapper = _deliverableSwapFutureNodeIds.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor, swapTenor, numberFuturesFromTenor);
    }
    throw new OpenGammaRuntimeException("Can't get instrument mapper definition for deliverable swap future number " + numberFuturesFromTenor +
        " with time to start " + tenor + " and swap tenor " + swapTenor);
  }

  /**
   * Gets the market data field of the deliverable swap future node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getDeliverableSwapFutureNodeDataField(final Tenor tenor) {
    if (_deliverableSwapFutureNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get deliverable swap future node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_deliverableSwapFutureNodeIds, tenor);
  }

  /**
   * Gets the data field type of the deliverable swap future node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getDeliverableSwapFutureNodeDataFieldType(final Tenor tenor) {
    if (_deliverableSwapFutureNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get deliverable swap future node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_deliverableSwapFutureNodeIds, tenor);
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
    if (_deliverableSwapFutureNodeIds != null) {
      allTenors.addAll(_deliverableSwapFutureNodeIds.keySet());
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

  protected static ExternalId getId(final Map<Tenor, CurveInstrumentProvider> idMapper, final LocalDate curveDate, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor);
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  protected static String getMarketDataField(final Map<Tenor, CurveInstrumentProvider> idMapper, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getMarketDataField();
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  protected static DataFieldType getDataFieldType(final Map<Tenor, CurveInstrumentProvider> idMapper, final Tenor tenor) {
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
        ObjectUtils.equals(_deliverableSwapFutureNodeIds, other._deliverableSwapFutureNodeIds) &&
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
    result = prime * result + ((_deliverableSwapFutureNodeIds == null) ? 0 : _deliverableSwapFutureNodeIds.hashCode());
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
