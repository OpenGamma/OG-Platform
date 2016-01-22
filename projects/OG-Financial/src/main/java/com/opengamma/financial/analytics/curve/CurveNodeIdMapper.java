/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve;

import java.io.Serializable;
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
import com.opengamma.core.config.ConfigGroups;
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
@Config(description = "Curve node ID mapper", group = ConfigGroups.CURVES)
public class CurveNodeIdMapper implements Serializable {
  /** The name of this configuration */
  private final String _name;
  /** Curve instrument providers for bill nodes */
  private final Map<Tenor, CurveInstrumentProvider> _billNodeIds;
  /** Curve instrument providers for bond nodes */
  private final Map<Tenor, CurveInstrumentProvider> _bondNodeIds;
  /** Curve instrument providers for calendar swap nodes */
  private final Map<Tenor, CurveInstrumentProvider> _calendarSwapNodeIds;
  /** Curve instrument providers for cash nodes */
  private final Map<Tenor, CurveInstrumentProvider> _cashNodeIds;
  /** Curve instrument providers for continuously-compounded rate nodes */
  private final Map<Tenor, CurveInstrumentProvider> _continuouslyCompoundedRateNodeIds;
  /** Curve instrument providers for periodically-compounded rate nodes */
  private final Map<Tenor, CurveInstrumentProvider> _periodicallyCompoundedRateNodeIds;
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
  /** Curve instrument providers for FX swap nodes */
  private final Map<Tenor, CurveInstrumentProvider> _fxSwapNodeIds;
  /** Curve instrument providers for IMM FRA nodes */
  private final Map<Tenor, CurveInstrumentProvider> _immFRANodeIds;
  /** Curve instrument providers for IMM swap nodes */
  private final Map<Tenor, CurveInstrumentProvider> _immSwapNodeIds;
  /** Curve instrument providers for rate future nodes */
  private final Map<Tenor, CurveInstrumentProvider> _rateFutureNodeIds;
  /** Curve instrument providers for swap nodes */
  private final Map<Tenor, CurveInstrumentProvider> _swapNodeIds;
  /** Curve instrument providers for three-leg basis swap nodes */
  private final Map<Tenor, CurveInstrumentProvider> _threeLegBasisSwapNodeIds;
  /** Curve instrument providers for zero coupon inflation nodes */
  private final Map<Tenor, CurveInstrumentProvider> _zeroCouponInflationNodeIds;


  /**
   * Builder class for CurveNodeIdMapper
   */
  public static final class Builder {
    /** The name of this configuration */
    private String _name;
    /** Curve instrument providers for bill nodes */
    private Map<Tenor, CurveInstrumentProvider> _billNodeIds;
    /** Curve instrument providers for bond nodes */
    private Map<Tenor, CurveInstrumentProvider> _bondNodeIds;
    /** Curve instrument providers for calendar swap nodes */
    private Map<Tenor, CurveInstrumentProvider> _calendarSwapNodeIds;
    /** Curve instrument providers for cash nodes */
    private Map<Tenor, CurveInstrumentProvider> _cashNodeIds;
    /** Curve instrument providers for continuously-compounded rate nodes */
    private Map<Tenor, CurveInstrumentProvider> _continuouslyCompoundedRateNodeIds;
    /** Curve instrument providers for periodically-compounded rate nodes */
    private Map<Tenor, CurveInstrumentProvider> _periodicallyCompoundedRateNodeIds;
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
    /** Curve instrument providers for FX swap nodes */
    private Map<Tenor, CurveInstrumentProvider> _fxSwapNodeIds;
    /** Curve instrument providers for IMM FRA nodes */
    private Map<Tenor, CurveInstrumentProvider> _immFRANodeIds;
    /** Curve instrument providers for IMM swap nodes */
    private Map<Tenor, CurveInstrumentProvider> _immSwapNodeIds;
    /** Curve instrument providers for rate future nodes */
    private Map<Tenor, CurveInstrumentProvider> _rateFutureNodeIds;
    /** Curve instrument providers for swap nodes */
    private Map<Tenor, CurveInstrumentProvider> _swapNodeIds;
    /** Curve instrument providers for three-leg basis swap nodes */
    private Map<Tenor, CurveInstrumentProvider> _threeLegBasisSwapNodeIds;
    /** Curve instrument providers for zero coupon inflation nodes */
    private Map<Tenor, CurveInstrumentProvider> _zeroCouponInflationNodeIds;

    /**
     * Private constructor.
     */
    private Builder() {}

    /**
     * The name of this configuration
     * @param name the name
     * @return this
     */
    public Builder name(final String name) {
      _name = name;
      return this;
    }

    /**
     * Curve instrument providers for bill nodes
     * @param billNodeIds the billNodeIds
     * @return this
     */
    public Builder billNodeIds(final Map<Tenor, CurveInstrumentProvider> billNodeIds) {
      _billNodeIds = billNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for bond nodes
     * @param bondNodeIds the bondNodeIds
     * @return this
     */
    public Builder bondNodeIds(final Map<Tenor, CurveInstrumentProvider> bondNodeIds) {
      _bondNodeIds = bondNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for cash nodes
     * @param calendarSwapNodeIds the calendarSwapNodeIds
     * @return this
     */
    public Builder calendarSwapNodeIds(final Map<Tenor, CurveInstrumentProvider> calendarSwapNodeIds) {
      _calendarSwapNodeIds = calendarSwapNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for cash nodes
     * @param cashNodeIds the cashNodeIds
     * @return this
     */
    public Builder cashNodeIds(final Map<Tenor, CurveInstrumentProvider> cashNodeIds) {
      _cashNodeIds = cashNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for continuously-compounded rate nodes
     * @param continuouslyCompoundedRateNodeIds the continuouslyCompoundedRateNodeIds
     * @return this
     */
    public Builder continuouslyCompoundedRateNodeIds(final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateNodeIds) {
      _continuouslyCompoundedRateNodeIds = continuouslyCompoundedRateNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for periodically-compounded rate nodes
     * @param periodicallyCompoundedRateNodeIds the periodicallyCompoundedRateNodeIds
     * @return this
     */
    public Builder periodicallyCompoundedRateNodeIds(final Map<Tenor, CurveInstrumentProvider> periodicallyCompoundedRateNodeIds) {
      _periodicallyCompoundedRateNodeIds = periodicallyCompoundedRateNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for credit spread nodes
     * @param creditSpreadNodeIds the creditSpreadNodeIds
     * @return this
     */
    public Builder creditSpreadNodeIds(final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds) {
      _creditSpreadNodeIds = creditSpreadNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for deliverable swap future nodes
     * @param deliverableSwapFutureNodeIds the deliverableSwapFutureNodeIds
     * @return this
     */
    public Builder deliverableSwapFutureNodeIds(final Map<Tenor, CurveInstrumentProvider> deliverableSwapFutureNodeIds) {
      _deliverableSwapFutureNodeIds = deliverableSwapFutureNodeIds;
      return this;
    }
    /**
     * Curve instrument providers for discount factor nodes
     * @param discountFactorNodeIds the discountFactorNodeIds
     * @return this
     */
    public Builder discountFactorNodeIds(final Map<Tenor, CurveInstrumentProvider> discountFactorNodeIds) {
      _discountFactorNodeIds = discountFactorNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for FRA nodes
     * @param fraNodeIds the fraNodeIds
     * @return this
     */
    public Builder fraNodeIds(final Map<Tenor, CurveInstrumentProvider> fraNodeIds) {
      _fraNodeIds = fraNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for FX forward nodes
     * @param fxForwardNodeIds the fxForwardNodeIds
     * @return this
     */
    public Builder fxForwardNodeIds(final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds) {
      _fxForwardNodeIds = fxForwardNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for FX swap nodes.
     *
     * @param fxSwapNodeIds the fxSwapNodeIds
     * @return this
     */
    public Builder fxSwapNodeIds(final Map<Tenor, CurveInstrumentProvider> fxSwapNodeIds) {
      _fxSwapNodeIds = fxSwapNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for IMM FRA nodes
     * @param immFRANodeIds The immFRANodeIds
     * @return this
     */
    public Builder immFRANodeIds(final Map<Tenor, CurveInstrumentProvider> immFRANodeIds) {
      _immFRANodeIds = immFRANodeIds;
      return this;
    }

    /**
     * Curve instrument providers for IMM swap nodes
     * @param immSwapNodeIds The immSwapNodeIds
     * @return this
     */
    public Builder immSwapNodeIds(final Map<Tenor, CurveInstrumentProvider> immSwapNodeIds) {
      _immSwapNodeIds = immSwapNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for rate future nodes
     * @param rateFutureNodeIds the rateFutureNodeIds
     * @return this
     */
    public Builder rateFutureNodeIds(final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds) {
      _rateFutureNodeIds = rateFutureNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for swap nodes
     * @param swapNodeIds the swapNodeIds
     * @return this
     */
    public Builder swapNodeIds(final Map<Tenor, CurveInstrumentProvider> swapNodeIds) {
      _swapNodeIds = swapNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for three-leg basis swap nodes
     * @param threeLegBasisSwapNodeIds the threeLegBasisSwapNodeIds
     * @return this
     */
    public Builder threeLegBasisSwapNodeIds(final Map<Tenor, CurveInstrumentProvider> threeLegBasisSwapNodeIds) {
      _threeLegBasisSwapNodeIds = threeLegBasisSwapNodeIds;
      return this;
    }

    /**
     * Curve instrument providers for zero coupon inflation nodes
     * @param zeroCouponInflationNodeIds the zeroCouponInflationNodeIds
     * @return this
     */
    public Builder zeroCouponInflationNodeIds(final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds) {
      _zeroCouponInflationNodeIds = zeroCouponInflationNodeIds;
      return this;
    }

    /**
     * @return a new {@link CurveNodeIdMapper} instance.
     */
    public CurveNodeIdMapper build() {
      return new CurveNodeIdMapper(_name,
          _billNodeIds,
          _bondNodeIds,
          _calendarSwapNodeIds,
          _cashNodeIds,
          _continuouslyCompoundedRateNodeIds,
          _creditSpreadNodeIds,
          _deliverableSwapFutureNodeIds,
          _discountFactorNodeIds,
          _fraNodeIds,
          _fxForwardNodeIds,
          _fxSwapNodeIds,
          _immFRANodeIds,
          _immSwapNodeIds,
          _rateFutureNodeIds,
          _swapNodeIds,
          _threeLegBasisSwapNodeIds,
          _zeroCouponInflationNodeIds,
          _periodicallyCompoundedRateNodeIds);
    }

  }

  /**
   * Gets the builder.
   * @return The builder
   */
  @SuppressWarnings("synthetic-access")
  public static Builder builder() {
    return new Builder();
  }

  /**
   * @param name The name of this configuration
   * @param billNodeIds The bill node ids
   * @param bondNodeIds The bond node ids
   * @param calendarSwapNodeIds The calendar swap node ids
   * @param cashNodeIds The cash node ids
   * @param continuouslyCompoundedRateIds The continuously-compounded rate ids
   * @param creditSpreadNodeIds The credit spread node ids
   * @param deliverableSwapFutureNodeIds The deliverable swap future node ids
   * @param discountFactorNodeIds The discount factor node ids
   * @param fraNodeIds The FRA node ids
   * @param fxForwardNodeIds The FX forward node ids
   * @param immFRANodeIds The IMM FRA node ids
   * @param immSwapNodeIds The IMM swap node ids
   * @param rateFutureNodeIds The rate future node ids
   * @param swapNodeIds The swap node ids
   * @param threeLegBasisSwapNodeIds The three-leg basis swap node ids
   * @param zeroCouponInflationNodeIds The zero coupon inflation node ids;
   */
  protected CurveNodeIdMapper(final String name,
      final Map<Tenor, CurveInstrumentProvider> billNodeIds,
      final Map<Tenor, CurveInstrumentProvider> bondNodeIds,
      final Map<Tenor, CurveInstrumentProvider> calendarSwapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> cashNodeIds,
      final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds,
      final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds,
      final Map<Tenor, CurveInstrumentProvider> deliverableSwapFutureNodeIds,
      final Map<Tenor, CurveInstrumentProvider> discountFactorNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fraNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fxForwardNodeIds,
      final Map<Tenor, CurveInstrumentProvider> fxSwapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> immFRANodeIds,
      final Map<Tenor, CurveInstrumentProvider> immSwapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> rateFutureNodeIds,
      final Map<Tenor, CurveInstrumentProvider> swapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> threeLegBasisSwapNodeIds,
      final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationNodeIds,
      final Map<Tenor, CurveInstrumentProvider> periodicallyCompoundedRateIds) {
    _name = name;
    _billNodeIds = billNodeIds;
    _bondNodeIds = bondNodeIds;
    _calendarSwapNodeIds = calendarSwapNodeIds;
    _cashNodeIds = cashNodeIds;
    _continuouslyCompoundedRateNodeIds = continuouslyCompoundedRateIds;
    _creditSpreadNodeIds = creditSpreadNodeIds;
    _deliverableSwapFutureNodeIds = deliverableSwapFutureNodeIds;
    _discountFactorNodeIds = discountFactorNodeIds;
    _fraNodeIds = fraNodeIds;
    _fxForwardNodeIds = fxForwardNodeIds;
    _fxSwapNodeIds = fxSwapNodeIds;
    _immFRANodeIds = immFRANodeIds;
    _immSwapNodeIds = immSwapNodeIds;
    _rateFutureNodeIds = rateFutureNodeIds;
    _swapNodeIds = swapNodeIds;
    _threeLegBasisSwapNodeIds = threeLegBasisSwapNodeIds;
    _zeroCouponInflationNodeIds = zeroCouponInflationNodeIds;
    _periodicallyCompoundedRateNodeIds = periodicallyCompoundedRateIds;
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
   * Gets the bill node ids.
   * @return The bill node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getBillNodeIds() {
    if (_billNodeIds != null) {
      return Collections.unmodifiableMap(_billNodeIds);
    }
    return null;
  }

  /**
   * Gets the bond node ids.
   * @return The bond node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getBondNodeIds() {
    if (_bondNodeIds != null) {
      return Collections.unmodifiableMap(_bondNodeIds);
    }
    return null;
  }

  /**
   * Gets the calendar swap node ids.
   * @return The calendar swap node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getCalendarSwapNodeIds() {
    if (_calendarSwapNodeIds != null) {
      return Collections.unmodifiableMap(_calendarSwapNodeIds);
    }
    return null;
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
   * Gets the periodically-compounded rate node ids.
   * @return The periodically-compounded rate node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getPeriodicallyCompoundedRateNodeIds() {
    if (_periodicallyCompoundedRateNodeIds != null) {
      return Collections.unmodifiableMap(_periodicallyCompoundedRateNodeIds);
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
   *
   * @return the FX forward node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getFXForwardNodeIds() {
    if (_fxForwardNodeIds != null) {
      return Collections.unmodifiableMap(_fxForwardNodeIds);
    }
    return null;
  }

  /**
   * Gets the FX swap node ids.
   *
   * @return the FX swap node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getFXSwapNodeIds() {
    if (_fxSwapNodeIds != null) {
      return Collections.unmodifiableMap(_fxSwapNodeIds);
    }
    return null;
  }

  /**
   * Gets the IMM FRA node ids.
   * @return The IMM FRA node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getIMMFRANodeIds() {
    if (_immFRANodeIds != null) {
      return Collections.unmodifiableMap(_immFRANodeIds);
    }
    return null;
  }

  /**
   * Gets the IMM swap node ids.
   * @return The IMM swap node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getIMMSwapNodeIds() {
    if (_immSwapNodeIds != null) {
      return Collections.unmodifiableMap(_immSwapNodeIds);
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
   * Gets the three-leg basis swap node ids.
   * @return The three-leg basis swap node ids
   */
  public Map<Tenor, CurveInstrumentProvider> getThreeLegBasisSwapNodeIds() {
    if (_threeLegBasisSwapNodeIds != null) {
      return Collections.unmodifiableMap(_threeLegBasisSwapNodeIds);
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
   * Gets the external id of the bill node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the node
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getBillNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_billNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get bill node id provider for curve node id mapper called " + _name);
    }
    return getId(_billNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the bill node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getBillNodeDataField(final Tenor tenor) {
    if (_billNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get bill node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_billNodeIds, tenor);
  }

  /**
   * Gets the data field type of the bond node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getBillNodeDataFieldType(final Tenor tenor) {
    if (_billNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get bill node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_billNodeIds, tenor);
  }

  /**
   * Gets the external id of the bond node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the node
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getBondNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_bondNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get bond node id provider for curve node id mapper called " + _name);
    }
    return getId(_bondNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the bond node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getBondNodeDataField(final Tenor tenor) {
    if (_bondNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get bond node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_bondNodeIds, tenor);
  }

  /**
   * Gets the data field type of the bond node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getBondNodeDataFieldType(final Tenor tenor) {
    if (_bondNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get bond node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_bondNodeIds, tenor);
  }

  /**
   * Gets the external id of the calendar swap node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param startTenor The start tenor
   * @param startDateNumber The start calendar date number.
   * @param maturityDateNumber The maturity calendar date number.
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getCalendarSwapNodeId(final LocalDate curveDate, final Tenor startTenor, final int startDateNumber, final int maturityDateNumber) {
    if (_calendarSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get calendar swap node id provider for curve node id mapper called " + _name);
    }
    final CurveInstrumentProvider mapper = _calendarSwapNodeIds.get(startTenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, startTenor, startDateNumber, maturityDateNumber);
    }
    throw new OpenGammaRuntimeException("Can't get instrument mapper definition for calendar swap with time to start " + startTenor +
        " with start period number " + startDateNumber + " and end period number " + maturityDateNumber);
  }

  /**
   * Gets the market data field of the calendar swap node at a particular tenor.
   * @param tenor The tenor ???
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getCalendarSwapNodeDataField(final Tenor tenor) {
    if (_calendarSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get calendar swap node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_calendarSwapNodeIds, tenor);
  }

  /**
   * Gets the data field type of the calendar swap node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getCalendarSwapNodeDataFieldType(final Tenor tenor) {
    if (_calendarSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get calendar swap node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_calendarSwapNodeIds, tenor);
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
   * Gets the external id of the periodically-compounded rate node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the node
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getPeriodicallyCompoundedRateNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_periodicallyCompoundedRateNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get periodically-compounded rate node id provider for curve node id mapper called " + _name);
    }
    return getId(_periodicallyCompoundedRateNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the periodically-compounded rate node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getPeriodicallyCompoundedRateNodeDataField(final Tenor tenor) {
    if (_periodicallyCompoundedRateNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get periodically-compounded rate node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_periodicallyCompoundedRateNodeIds, tenor);
  }

  /**
   * Gets the data field type of the continuously-compounded rate node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getPeriodicallyCompoundedRateDataFieldType(final Tenor tenor) {
    if (_periodicallyCompoundedRateNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get periodically-compounded rate node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_periodicallyCompoundedRateNodeIds, tenor);
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
   * Gets the external id of the FX swap node at a particular tenor that is valid for that curve date.
   * @param curveDate the curve date
   * @param tenor the tenor
   * @return the external id
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getFXSwapNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_fxSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FX swap node id provider for curve node id mapper called " + _name);
    }
    return getId(_fxSwapNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the FX swap node at a particular tenor.
   * @param tenor the tenor
   * @return the market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getFXSwapNodeDataField(final Tenor tenor) {
    if (_fxSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FX swap node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_fxSwapNodeIds, tenor);
  }

  /**
   * Gets the data field type of the FX swap node at a particular tenor.
   * @param tenor the tenor
   * @return the data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getFXSwapNodeDataFieldType(final Tenor tenor) {
    if (_fxSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get FX swap node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_fxSwapNodeIds, tenor);
  }

  /**
   * Gets the external id of the IMM FRA node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param startTenor The start tenor
   * @param startNumberFromTenor The number of IMM periods from the start tenor to the FRA start
   * @param endNumberFromTenor The number of IMM periods from the start tenor to the FRA end
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getIMMFRANodeId(final LocalDate curveDate, final Tenor startTenor, final int startNumberFromTenor, final int endNumberFromTenor) {
    if (_immFRANodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get IMM FRA node id provider for curve node id mapper called " + _name);
    }
    final CurveInstrumentProvider mapper = _immFRANodeIds.get(startTenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, startTenor, startNumberFromTenor, endNumberFromTenor);
    }
    throw new OpenGammaRuntimeException("Can't get instrument mapper definition for IMM FRA with time to start " + startTenor +
        " with start IMM period number " + startNumberFromTenor + " and end IMM period number " + endNumberFromTenor);
  }

  /**
   * Gets the market data field of the IMM FRA node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getIMMFRANodeDataField(final Tenor tenor) {
    if (_immFRANodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get IMM FRA node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_immFRANodeIds, tenor);
  }

  /**
   * Gets the data field type of the IMM FRA node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getIMMFRANodeDataFieldType(final Tenor tenor) {
    if (_immFRANodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get IMM FRA node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_immFRANodeIds, tenor);
  }

  /**
   * Gets the external id of the IMM swap node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param startTenor The start tenor
   * @param startNumberFromTenor The number of IMM periods from the start tenor to the swap start
   * @param endNumberFromTenor The number of IMM periods from the start tenor to the swap end
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getIMMSwapNodeId(final LocalDate curveDate, final Tenor startTenor, final int startNumberFromTenor, final int endNumberFromTenor) {
    if (_immSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get IMM swap node id provider for curve node id mapper called " + _name);
    }
    final CurveInstrumentProvider mapper = _immSwapNodeIds.get(startTenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, startTenor, startNumberFromTenor, endNumberFromTenor);
    }
    throw new OpenGammaRuntimeException("Can't get instrument mapper definition for IMM swap with time to start " + startTenor +
        " with start IMM period number " + startNumberFromTenor + " and end IMM period number " + endNumberFromTenor);
  }

  /**
   * Gets the market data field of the IMM swap node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getIMMSwapNodeDataField(final Tenor tenor) {
    if (_immSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get IMM swap node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_immSwapNodeIds, tenor);
  }

  /**
   * Gets the data field type of the IMM swap node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getIMMSwapNodeDataFieldType(final Tenor tenor) {
    if (_immSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get IMM swap node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_immSwapNodeIds, tenor);
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
   * Gets the external id of the three-leg basis swap node at a particular tenor that is valid for that curve date.
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id of the security
   * @throws OpenGammaRuntimeException if the external id for this tenor and date could not be found.
   */
  public ExternalId getThreeLegBasisSwapNodeId(final LocalDate curveDate, final Tenor tenor) {
    if (_threeLegBasisSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get three-leg basis swap node id provider for curve node id mapper called " + _name);
    }
    return getId(_threeLegBasisSwapNodeIds, curveDate, tenor);
  }

  /**
   * Gets the market data field of the three-leg basis swap node at a particular tenor.
   * @param tenor The tenor
   * @return The market data field
   * @throws OpenGammaRuntimeException if the market data field for this tenor could not be found.
   */
  public String getThreeLegBasisSwapNodeDataField(final Tenor tenor) {
    if (_threeLegBasisSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get three-leg basis swap node id provider for curve node id mapper called " + _name);
    }
    return getMarketDataField(_threeLegBasisSwapNodeIds, tenor);
  }

  /**
   * Gets the data field type of the three-leg basis swap node at a particular tenor.
   * @param tenor The tenor
   * @return The data field type
   * @throws OpenGammaRuntimeException if the data field type for this tenor could not be found.
   */
  public DataFieldType getThreeLegBasisSwapNodeDataFieldType(final Tenor tenor) {
    if (_threeLegBasisSwapNodeIds == null) {
      throw new OpenGammaRuntimeException("Cannot get three-leg basis swap node id provider for curve node id mapper called " + _name);
    }
    return getDataFieldType(_threeLegBasisSwapNodeIds, tenor);
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
    if (_billNodeIds != null) {
      allTenors.addAll(_billNodeIds.keySet());
    }
    if (_bondNodeIds != null) {
      allTenors.addAll(_bondNodeIds.keySet());
    }
    if (_cashNodeIds != null) {
      allTenors.addAll(_cashNodeIds.keySet());
    }
    if (_continuouslyCompoundedRateNodeIds != null) {
      allTenors.addAll(_continuouslyCompoundedRateNodeIds.keySet());
    }
    if (_periodicallyCompoundedRateNodeIds != null) {
      allTenors.addAll(_periodicallyCompoundedRateNodeIds.keySet());
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
    if (_fxSwapNodeIds != null) {
      allTenors.addAll(_fxSwapNodeIds.keySet());
    }
    if (_immFRANodeIds != null) {
      allTenors.addAll(_immFRANodeIds.keySet());
    }
    if (_immSwapNodeIds != null) {
      allTenors.addAll(_immSwapNodeIds.keySet());
    }
    if (_rateFutureNodeIds != null) {
      allTenors.addAll(_rateFutureNodeIds.keySet());
    }
    if (_swapNodeIds != null) {
      allTenors.addAll(_swapNodeIds.keySet());
    }
    if (_threeLegBasisSwapNodeIds != null) {
      allTenors.addAll(_threeLegBasisSwapNodeIds.keySet());
    }
    if (_zeroCouponInflationNodeIds != null) {
      allTenors.addAll(_zeroCouponInflationNodeIds.keySet());
    }
    return allTenors;
  }

  /**
   * Gets the external id of a node for a curve date and tenor.
   * @param idMapper The id mapper
   * @param curveDate The curve date
   * @param tenor The tenor
   * @return The external id
   */
  protected static ExternalId getId(final Map<Tenor, CurveInstrumentProvider> idMapper, final LocalDate curveDate, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getInstrument(curveDate, tenor);
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  /**
   * Gets the market data field of a node for a tenor.
   * @param idMapper The id mapper
   * @param tenor The tenor
   * @return The market data field
   */
  protected static String getMarketDataField(final Map<Tenor, CurveInstrumentProvider> idMapper, final Tenor tenor) {
    final CurveInstrumentProvider mapper = idMapper.get(tenor);
    if (mapper != null) {
      return mapper.getMarketDataField();
    }
    throw new OpenGammaRuntimeException("Cannot get id mapper definition for " + tenor);
  }

  /**
   * Gets the data field type of a node for a tenor.
   * @param idMapper The id mapper
   * @param tenor The tenor
   * @return The data field type
   */
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
        ObjectUtils.equals(_billNodeIds, other._billNodeIds) &&
        ObjectUtils.equals(_bondNodeIds, other._bondNodeIds) &&
        ObjectUtils.equals(_cashNodeIds, other._cashNodeIds) &&
        ObjectUtils.equals(_continuouslyCompoundedRateNodeIds, other._continuouslyCompoundedRateNodeIds) &&
        ObjectUtils.equals(_periodicallyCompoundedRateNodeIds, other._periodicallyCompoundedRateNodeIds) &&
        ObjectUtils.equals(_creditSpreadNodeIds, other._creditSpreadNodeIds) &&
        ObjectUtils.equals(_deliverableSwapFutureNodeIds, other._deliverableSwapFutureNodeIds) &&
        ObjectUtils.equals(_discountFactorNodeIds, other._discountFactorNodeIds) &&
        ObjectUtils.equals(_fraNodeIds, other._fraNodeIds) &&
        ObjectUtils.equals(_fxForwardNodeIds, other._fxForwardNodeIds) &&
        ObjectUtils.equals(_fxSwapNodeIds, other._fxSwapNodeIds) &&
        ObjectUtils.equals(_immFRANodeIds, other._immFRANodeIds) &&
        ObjectUtils.equals(_immSwapNodeIds, other._immSwapNodeIds) &&
        ObjectUtils.equals(_rateFutureNodeIds, other._rateFutureNodeIds) &&
        ObjectUtils.equals(_swapNodeIds, other._swapNodeIds) &&
        ObjectUtils.equals(_threeLegBasisSwapNodeIds, other._threeLegBasisSwapNodeIds) &&
        ObjectUtils.equals(_zeroCouponInflationNodeIds, other._zeroCouponInflationNodeIds);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + ((_billNodeIds == null) ? 0 : _billNodeIds.hashCode());
    result = prime * result + ((_bondNodeIds == null) ? 0 : _bondNodeIds.hashCode());
    result = prime * result + ((_cashNodeIds == null) ? 0 : _cashNodeIds.hashCode());
    result = prime * result + ((_continuouslyCompoundedRateNodeIds == null) ? 0 : _continuouslyCompoundedRateNodeIds.hashCode());
    result = prime * result + ((_periodicallyCompoundedRateNodeIds == null) ? 0 : _periodicallyCompoundedRateNodeIds.hashCode());
    result = prime * result + ((_creditSpreadNodeIds == null) ? 0 : _creditSpreadNodeIds.hashCode());
    result = prime * result + ((_deliverableSwapFutureNodeIds == null) ? 0 : _deliverableSwapFutureNodeIds.hashCode());
    result = prime * result + ((_discountFactorNodeIds == null) ? 0 : _discountFactorNodeIds.hashCode());
    result = prime * result + ((_fraNodeIds == null) ? 0 : _fraNodeIds.hashCode());
    result = prime * result + ((_fxForwardNodeIds == null) ? 0 : _fxForwardNodeIds.hashCode());
    result = prime * result + ((_fxSwapNodeIds == null) ? 0 : _fxSwapNodeIds.hashCode());
    result = prime * result + ((_immFRANodeIds == null) ? 0 : _immFRANodeIds.hashCode());
    result = prime * result + ((_immSwapNodeIds == null) ? 0 : _immSwapNodeIds.hashCode());
    result = prime * result + ((_rateFutureNodeIds == null) ? 0 : _rateFutureNodeIds.hashCode());
    result = prime * result + ((_swapNodeIds == null) ? 0 : _swapNodeIds.hashCode());
    result = prime * result + ((_threeLegBasisSwapNodeIds == null) ? 0 : _threeLegBasisSwapNodeIds.hashCode());
    result = prime * result + ((_zeroCouponInflationNodeIds == null) ? 0 : _zeroCouponInflationNodeIds.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

}
