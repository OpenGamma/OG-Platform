/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import static com.opengamma.financial.convention.InMemoryConventionBundleMaster.simpleNameSecurityId;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableSet;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionWithData;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorCMSSpreadDefinition;
import com.opengamma.analytics.financial.instrument.annuity.AnnuityCapFloorIborDefinition;
import com.opengamma.analytics.financial.instrument.fra.ForwardRateAgreementDefinition;
import com.opengamma.analytics.financial.instrument.future.BondFutureDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.FederalFundsFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureOptionMarginTransactionDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.analytics.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationYearOnYearDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedInflationZeroCouponDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapFixedONSimplifiedDefinition;
import com.opengamma.analytics.financial.instrument.swap.SwapMultilegDefinition;
import com.opengamma.analytics.financial.instrument.swap.TotalReturnSwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.timeseries.DateConstraint;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesBundle;
import com.opengamma.financial.analytics.timeseries.HistoricalTimeSeriesFunctionUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.index.OvernightIndex;
import com.opengamma.financial.security.index.PriceIndex;
import com.opengamma.financial.security.index.SwapIndex;
import com.opengamma.financial.security.irs.FloatingInterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapLeg;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.InflationIndexSwapLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.TotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolutionResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.timeseries.DoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleEntryIterator;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeriesBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Convert an OG-Financial Security to its OG-Analytics Derivative form as seen from now
 */
public class FixedIncomeConverterDataProvider {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(FixedIncomeConverterDataProvider.class);
  /** The security source */
  private final SecuritySource _securitySource;

  /** The convention source **/
  // TODO: [PLAT-5966] Remove this convention source
  private final ConventionBundleSource _conventionSource;

  private final HistoricalTimeSeriesResolver _timeSeriesResolver;

  /** The first fixing date of a swap is not stored at the security level.
   * One needs to estimate how far before the effective date the fixing time-series is required. **/
  private static final int DAYS_BEFORE_EFFECTIVE = 180;

  public FixedIncomeConverterDataProvider(final ConventionBundleSource conventionSource, final SecuritySource securitySource, final HistoricalTimeSeriesResolver timeSeriesResolver) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    ArgumentChecker.notNull(securitySource, "securitySource");
    ArgumentChecker.notNull(timeSeriesResolver, "timeSeriesResolver");
    _conventionSource = conventionSource;
    _securitySource = securitySource;
    _timeSeriesResolver = timeSeriesResolver;
  }

  public HistoricalTimeSeriesResolver getHistoricalTimeSeriesResolver() {
    return _timeSeriesResolver;
  }

  //TODO: [PLAT-5966] Add java doc
  public ConventionBundleSource getConventionBundleSource() {
    return _conventionSource;
  }

  /**
   * Implementation of the conversion for a given instrument.
   */
  protected abstract class Converter<S extends Security, D extends InstrumentDefinition<?>> {

    /**
     * Returns the time series requirements that will be needed for the {@link #convert} method.
     *
     * @param security the security, not null
     * @return the set of requirements, the empty set if nothing is required, null if the conversion will not be possible (for example a missing timeseries)
     */
    public abstract Set<ValueRequirement> getTimeSeriesRequirements(S security);

    /**
     * Converts the "security" and "definition" form to its "derivative" form.
     *
     * @param security the security, not null
     * @param definition the definition, not null
     * @param now the observation time, not null
     * @param curveNames the names of the curves, not null
     * @param timeSeries the bundle containing timeseries produced to satisfy those returned by {@link #getTimeSeriesRequirements}
     * @return the derivative form, not null
     * @deprecated Use the method that does not take curve names
     */
    @Deprecated
    public abstract InstrumentDerivative convert(S security, D definition, ZonedDateTime now, String[] curveNames, HistoricalTimeSeriesBundle timeSeries);

    /**
     * Converts the "security" and "definition" form to its "derivative" form.
     *
     * @param security the security, not null
     * @param definition the definition, not null
     * @param now the observation time, not null
     * @param timeSeries the bundle containing timeseries produced to satisfy those returned by {@link #getTimeSeriesRequirements}
     * @return the derivative form, not null
     */
    public abstract InstrumentDerivative convert(S security, D definition, ZonedDateTime now, HistoricalTimeSeriesBundle timeSeries);
  }

  @SuppressWarnings("rawtypes")
  protected Converter getConverter(final Security security, final InstrumentDefinition<?> definition) {
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition to convert was null for security " + security);
    }
    if (security instanceof BondFutureSecurity) {
      return _bondFutureSecurity;
    }
    if (security instanceof FRASecurity) {
      return _fraSecurity;
    }
    if (security instanceof ForwardRateAgreementSecurity) {
      return _forwardRateAgreementSecurity;
    }
    if (security instanceof CapFloorSecurity) {
      if (((CapFloorSecurity) security).isIbor()) {
        return _capFloorIborSecurity;
      }
      return _capFloorCMSSecurity;
    }
    if (security instanceof InterestRateFutureSecurity) {
      if (definition instanceof InterestRateFutureTransactionDefinition) {
        return _irFutureTrade;
      }
      return _irFutureSecurity;
    }
    if (security instanceof FederalFundsFutureSecurity) {
      if (definition instanceof FederalFundsFutureTransactionDefinition) {
        return _fedFundsFutureTrade;
      }
      return _fedFundsFutureSecurity;
    }
    if (security instanceof IRFutureOptionSecurity) {
      if (definition instanceof InterestRateFutureOptionMarginTransactionDefinition) {
        return _irFutureOptionSecurity;
      }
    }
    if (security instanceof SwapSecurity) {
      if (definition instanceof SwapFixedInflationYearOnYearDefinition) {
        return _yearOnYearInflationSwapSecurity;
      }
      if (definition instanceof SwapFixedInflationZeroCouponDefinition) {
        return _zeroCouponInflationSwapSecurity;
      }
      if (definition instanceof SwapFixedONSimplifiedDefinition) {
        return _default;
      }
      return _swapSecurity;
    }
    if (security instanceof InterestRateSwapSecurity) {
      return _irsSecurity;
    }
    if (security instanceof CapFloorCMSSpreadSecurity) {
      return _capFloorCMSSpreadSecurity;
    }
    if (security instanceof SwaptionSecurity) {
      return _swaptionSecurity;
    }
    if (security instanceof TotalReturnSwapSecurity) {
      return _totalReturnSwapSecurity;
    }
    return _default;
  }

  @SuppressWarnings("unchecked")
  public Set<ValueRequirement> getConversionTimeSeriesRequirements(final Security security, final InstrumentDefinition<?> definition) {
    return getConverter(security, definition).getTimeSeriesRequirements(security);
  }

  /**
   * @param security The security, not null
   * @param definition The definition, not null
   * @param now The valuation time, not null
   * @param curveNames The curve names, not null
   * @param timeSeries The fixing time series, not null
   * @return An instrument derivative
   * @deprecated Use the version that does not take yield curve names
   */
  @SuppressWarnings("unchecked")
  @Deprecated
  public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames,
      final HistoricalTimeSeriesBundle timeSeries) {
    return getConverter(security, definition).convert(security, definition, now, curveNames, timeSeries);
  }

  /**
   * @param security The security, not null
   * @param definition The definition, not null
   * @param now The valuation time, not null
   * @param timeSeries The fixing time series, not null
   * @return An instrument derivative
   */
  @SuppressWarnings("unchecked")
  public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries) {
    return getConverter(security, definition).convert(security, definition, now, timeSeries);
  }

  protected HistoricalTimeSeriesResolver getTimeSeriesResolver() {
    return _timeSeriesResolver;
  }

  private final Converter<SwaptionSecurity, InstrumentDefinition<?>> _swaptionSecurity = new Converter<SwaptionSecurity, InstrumentDefinition<?>>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final SwaptionSecurity security) {
      if (security.getCurrency().equals(Currency.BRL)) {
        final ConventionBundle brlSwapConvention = _conventionSource.getConventionBundle(simpleNameSecurityId("BRL_DI_SWAP"));
        final ExternalId indexId = brlSwapConvention.getSwapFloatingLegInitialRate();
        final ConventionBundle indexConvention = getConventionBundleSource().getConventionBundle(indexId);
        if (indexConvention == null) {
          throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
        }
        final ExternalIdBundle indexIdBundle = indexConvention.getIdentifiers();
        final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(indexIdBundle, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
        if (timeSeries == null) {
          return null;
        }
        return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
            DateConstraint.VALUATION_TIME.minus(Period.ofDays(360)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
      }
      return Collections.emptySet();
    }

    @Override
    public InstrumentDerivative convert(final SwaptionSecurity security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      if (security.getCurrency().equals(Currency.BRL)) {
        @SuppressWarnings("unchecked")
        final InstrumentDefinitionWithData<?, ZonedDateTimeDoubleTimeSeries> brlDefinition = (InstrumentDefinitionWithData<?, ZonedDateTimeDoubleTimeSeries>) definition;
        final ConventionBundle brlSwapConvention = _conventionSource.getConventionBundle(simpleNameSecurityId("BRL_DI_SWAP"));
        final ExternalId indexId = brlSwapConvention.getSwapFloatingLegInitialRate();
        final ConventionBundle indexConvention = getConventionBundleSource().getConventionBundle(indexId);
        if (indexConvention == null) {
          throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
        }
        final ExternalIdBundle indexIdBundle = indexConvention.getIdentifiers();
        final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
        }
        LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
        //TODO this normalization should not be done here
        localDateTS = localDateTS.divide(100);
        final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
        // TODO: remove the zone
        return brlDefinition.toDerivative(now, indexTS, curveNames);
      }
      return definition.toDerivative(now, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final SwaptionSecurity security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries) {
      if (security.getCurrency().equals(Currency.BRL)) {
        @SuppressWarnings("unchecked")
        final InstrumentDefinitionWithData<?, ZonedDateTimeDoubleTimeSeries> brlDefinition = (InstrumentDefinitionWithData<?, ZonedDateTimeDoubleTimeSeries>) definition;
        final ConventionBundle brlSwapConvention = _conventionSource.getConventionBundle(simpleNameSecurityId("BRL_DI_SWAP"));
        final ExternalId indexId = brlSwapConvention.getSwapFloatingLegInitialRate();
        final ConventionBundle indexConvention = getConventionBundleSource().getConventionBundle(indexId);
        if (indexConvention == null) {
          throw new OpenGammaRuntimeException("No conventions found for floating reference rate " + indexId);
        }
        final ExternalIdBundle indexIdBundle = indexConvention.getIdentifiers();
        final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
        }
        LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
        //TODO this normalization should not be done here
        localDateTS = localDateTS.divide(100);
        final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
        // TODO: remove the zone
        return brlDefinition.toDerivative(now, indexTS);
      }
      return definition.toDerivative(now);
    }

  };

  private final Converter<BondFutureSecurity, BondFutureDefinition> _bondFutureSecurity = new Converter<BondFutureSecurity, BondFutureDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final BondFutureSecurity security) {
      return Collections.emptySet();
    }

    @Override
    public InstrumentDerivative convert(final BondFutureSecurity security, final BondFutureDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      // TODO [PLAT-5402] Change to security/transaction object.
      final Double referencePrice = 0.0;
      return definition.toDerivative(now, referencePrice, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final BondFutureSecurity security, final BondFutureDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      // TODO [PLAT-5402] Change to security/transaction object.
      final Double referencePrice = 0.0;
      return definition.toDerivative(now, referencePrice);
    }

  };

  private final Converter<FRASecurity, ForwardRateAgreementDefinition> _fraSecurity = new Converter<FRASecurity, ForwardRateAgreementDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final FRASecurity security) {
      final ExternalIdBundle indexIdBundle = getIndexIborIdBundle(security.getUnderlyingId());
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(indexIdBundle, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final FRASecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalIdBundle indexIdBundle = getIndexIborIdBundle(security.getUnderlyingId());
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      localDateTS = localDateTS.divide(100);
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
      // TODO: remove the zone
      return definition.toDerivative(now, indexTS, curveNames);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final FRASecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalIdBundle indexIdBundle = getIndexIborIdBundle(security.getUnderlyingId());
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      localDateTS = localDateTS.divide(100);
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
      // TODO: remove the zone
      return definition.toDerivative(now, indexTS);
    }
  };

  private final Converter<ForwardRateAgreementSecurity, ForwardRateAgreementDefinition> _forwardRateAgreementSecurity = new Converter<ForwardRateAgreementSecurity, ForwardRateAgreementDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final ForwardRateAgreementSecurity security) {
      final ExternalIdBundle indexIdBundle = getIndexIborIdBundle(security.getUnderlyingId());
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(indexIdBundle, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final ForwardRateAgreementSecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalIdBundle indexIdBundle = getIndexIborIdBundle(security.getUnderlyingId());
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      localDateTS = localDateTS.divide(100);
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
      // TODO: remove the zone
      return definition.toDerivative(now, indexTS, curveNames);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final ForwardRateAgreementSecurity security, final ForwardRateAgreementDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      ExternalIdBundle indexIdBundle;
      final ExternalId indexId = security.getUnderlyingId();
      final ConventionBundle indexConvention = _conventionSource.getConventionBundle(indexId);
      if (indexConvention == null) {  // convention lookup should be removed once ibor securities used everywhere
        indexIdBundle = getIndexIborIdBundle(security.getUnderlyingId());
      } else {
        indexIdBundle = indexConvention.getIdentifiers();
      }
      if (indexIdBundle == null) {
        throw new OpenGammaRuntimeException("Could not load ibor security or convention for " + security.getUnderlyingId());
      }
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, indexIdBundle);
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + indexIdBundle);
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      localDateTS = localDateTS.divide(100);
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
      // TODO: remove the zone
      return definition.toDerivative(now, indexTS);
    }
  };

  private final Converter<CapFloorSecurity, AnnuityCapFloorIborDefinition> _capFloorIborSecurity = new Converter<CapFloorSecurity, AnnuityCapFloorIborDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final CapFloorSecurity security) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getUnderlyingId().toBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofDays(7)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @Override
    public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorIborDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId id = security.getUnderlyingId();
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + id);
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      localDateTS = localDateTS.divide(100);
      @SuppressWarnings("synthetic-access")
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
      return definition.toDerivative(now, indexTS, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorIborDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId id = security.getUnderlyingId();
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + id);
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      localDateTS = localDateTS.divide(100);
      @SuppressWarnings("synthetic-access")
      final ZonedDateTimeDoubleTimeSeries indexTS = convertTimeSeries(now.getZone(), localDateTS);
      return definition.toDerivative(now, indexTS);
    }
  };

  private final Converter<CapFloorSecurity, AnnuityCapFloorCMSDefinition> _capFloorCMSSecurity = new Converter<CapFloorSecurity, AnnuityCapFloorCMSDefinition>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final CapFloorSecurity security) {
      final ExternalId id = security.getUnderlyingId();
      final ZonedDateTime capStartDate = security.getStartDate();
      final LocalDate startDate = capStartDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
      final ValueRequirement requirement = getIndexTimeSeriesRequirement(getIndexIborIdBundle(id), startDate);
      if (requirement == null) {
        return null;
      }
      return Collections.singleton(requirement);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorCMSDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId id = security.getUnderlyingId();
      final ZonedDateTimeDoubleTimeSeries indexTS = getIndexTimeSeries(getIndexIborIdBundle(id), now.getZone(), timeSeries);
      return definition.toDerivative(now, indexTS, curveNames);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final CapFloorSecurity security, final AnnuityCapFloorCMSDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId id = security.getUnderlyingId();
      final ZonedDateTimeDoubleTimeSeries indexTS = getIndexTimeSeries(getIndexIborIdBundle(id), now.getZone(), timeSeries);
      return definition.toDerivative(now, indexTS);
    }
  };

  private final Converter<InterestRateFutureSecurity, InterestRateFutureTransactionDefinition> _irFutureTrade = new Converter<InterestRateFutureSecurity, InterestRateFutureTransactionDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final InterestRateFutureSecurity security) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, true));
    }

    @Override
    public InstrumentDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureTransactionDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + security);
      }
      final int length = ts.getTimeSeries().size();
      if (length == 0) {
        throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
      }
      final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
      if (curveNames.length == 1) {
        final String[] singleCurve = new String[] {curveNames[0], curveNames[0] };
        return definition.toDerivative(now, lastMarginPrice, singleCurve);
      }
      return definition.toDerivative(now, lastMarginPrice, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureTransactionDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + security);
      }
      final int length = ts.getTimeSeries().size();
      if (length == 0) {
        throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
      }
      final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
      return definition.toDerivative(now, lastMarginPrice);
    }
  };

  private final Converter<InterestRateFutureSecurity, InterestRateFutureSecurityDefinition> _irFutureSecurity = new Converter<InterestRateFutureSecurity, InterestRateFutureSecurityDefinition>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final InterestRateFutureSecurity security) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, true));
    }

    @Override
    public InstrumentDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureSecurityDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + security);
      }
      final int length = ts.getTimeSeries().size();
      if (length == 0) {
        throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
      }
      final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
      if (curveNames.length == 1) {
        final String[] singleCurve = new String[] {curveNames[0], curveNames[0] };
        return definition.toDerivative(now, singleCurve);
      }
      return definition.toDerivative(now, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureSecurityDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      if (ts == null) {
        throw new OpenGammaRuntimeException("Could not get price time series for " + security);
      }
      final int length = ts.getTimeSeries().size();
      if (length == 0) {
        throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
      }
      final double lastMarginPrice = ts.getTimeSeries().getLatestValue();
      return definition.toDerivative(now);
    }
  };

  private final Converter<FederalFundsFutureSecurity, FederalFundsFutureSecurityDefinition> _fedFundsFutureSecurity =
      new Converter<FederalFundsFutureSecurity, FederalFundsFutureSecurityDefinition>() {

        @Override
        public Set<ValueRequirement> getTimeSeriesRequirements(final FederalFundsFutureSecurity security) {
          final HistoricalTimeSeriesResolutionResult futureTS = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
          if (futureTS == null) {
            return null;
          }
          final HistoricalTimeSeriesResolutionResult underlyingTS = getTimeSeriesResolver().resolve(security.getUnderlyingId().toBundle(), null, null, null,
              MarketDataRequirementNames.MARKET_VALUE, null);
          if (underlyingTS == null) {
            return null;
          }
          final Set<ValueRequirement> requirements = new HashSet<>();
          requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(futureTS, MarketDataRequirementNames.MARKET_VALUE,
              DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
          requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(underlyingTS, MarketDataRequirementNames.MARKET_VALUE,
              DateConstraint.VALUATION_TIME.minus(Period.ofMonths(4)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
          return requirements;
        }

        @Override
        public InstrumentDerivative convert(final FederalFundsFutureSecurity security, final FederalFundsFutureSecurityDefinition definition, final ZonedDateTime now, final String[] curveNames,
            final HistoricalTimeSeriesBundle timeSeries) {
          return convert(security, definition, now, timeSeries);
        }

        @Override
        public InstrumentDerivative convert(final FederalFundsFutureSecurity security, final FederalFundsFutureSecurityDefinition definition, final ZonedDateTime now,
            final HistoricalTimeSeriesBundle timeSeries) {
          final HistoricalTimeSeries underlyingTS = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId().toBundle());
          if (underlyingTS == null) {
            throw new OpenGammaRuntimeException("Could not get underlying time series for " + security.getUnderlyingId());
          }
          if (underlyingTS.getTimeSeries().size() == 0) {
            throw new OpenGammaRuntimeException("Time series for " + security.getUnderlyingId().toBundle() + " was empty");
          }
          return definition.toDerivative(now, convertTimeSeries(ZoneId.of("UTC"), underlyingTS.getTimeSeries()));
        }

      };

  private final Converter<FederalFundsFutureSecurity, FederalFundsFutureTransactionDefinition> _fedFundsFutureTrade =
      new Converter<FederalFundsFutureSecurity, FederalFundsFutureTransactionDefinition>() {

        @Override
        public Set<ValueRequirement> getTimeSeriesRequirements(final FederalFundsFutureSecurity security) {
          final HistoricalTimeSeriesResolutionResult futureTS = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
          if (futureTS == null) {
            return null;
          }
          final HistoricalTimeSeriesResolutionResult underlyingTS = getTimeSeriesResolver().resolve(security.getUnderlyingId().toBundle(), null, null, null,
              MarketDataRequirementNames.MARKET_VALUE, null);
          if (underlyingTS == null) {
            return null;
          }
          final Set<ValueRequirement> requirements = new HashSet<>();
          requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(futureTS, MarketDataRequirementNames.MARKET_VALUE,
              DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
          requirements.add(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(underlyingTS, MarketDataRequirementNames.MARKET_VALUE,
              DateConstraint.VALUATION_TIME.minus(Period.ofMonths(4)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
          return requirements;
        }

        @Override
        public InstrumentDerivative convert(final FederalFundsFutureSecurity security, final FederalFundsFutureTransactionDefinition definition, final ZonedDateTime now, final String[] curveNames,
            final HistoricalTimeSeriesBundle timeSeries) {
          return convert(security, definition, now, timeSeries);
        }

        @SuppressWarnings("unchecked")
        @Override
        public InstrumentDerivative convert(final FederalFundsFutureSecurity security, final FederalFundsFutureTransactionDefinition definition, final ZonedDateTime now,
            final HistoricalTimeSeriesBundle timeSeries) {
          final HistoricalTimeSeries futureTS = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
          if (futureTS == null) {
            throw new OpenGammaRuntimeException("Could not get price time series for " + security);
          }
          if (futureTS.getTimeSeries().size() == 0) {
            throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
          }
          final HistoricalTimeSeries underlyingTS = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getUnderlyingId().toBundle());
          if (underlyingTS == null) {
            throw new OpenGammaRuntimeException("Could not get underlying time series for " + security.getUnderlyingId());
          }
          if (underlyingTS.getTimeSeries().size() == 0) {
            throw new OpenGammaRuntimeException("Time series for " + security.getUnderlyingId().toBundle() + " was empty");
          }
          return definition.toDerivative(now, new DoubleTimeSeries[] {
            convertTimeSeries(ZoneId.of("UTC"), underlyingTS.getTimeSeries()),
            convertTimeSeries(ZoneId.of("UTC"), futureTS.getTimeSeries()) });
        }
      };

  private final Converter<IRFutureOptionSecurity, InterestRateFutureOptionMarginTransactionDefinition> _irFutureOptionSecurity = new Converter<IRFutureOptionSecurity, InterestRateFutureOptionMarginTransactionDefinition>() { // CSIGNORE

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final IRFutureOptionSecurity security) {
      final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(security.getExternalIdBundle(), null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (timeSeries == null) {
        return null;
      }
      return Collections.singleton(HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.VALUATION_TIME.minus(Period.ofMonths(1)).previousWeekDay(), true, DateConstraint.VALUATION_TIME, false));
    }

    @Override
    public InstrumentDerivative convert(final IRFutureOptionSecurity security, final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime now,
        final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      Double lastMarginPrice;
      if (now.toLocalDate().equals(definition.getTradeDate().toLocalDate())) {
        lastMarginPrice = definition.getTradePrice();
      } else {
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + security);
        }
        final int length = ts.getTimeSeries().size();
        if (length == 0) {
          throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
        }
        lastMarginPrice = ts.getTimeSeries().getLatestValue();
      }
      return definition.toDerivative(now, lastMarginPrice, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final IRFutureOptionSecurity security, final InterestRateFutureOptionMarginTransactionDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, security.getExternalIdBundle());
      Double lastMarginPrice;
      if (now.toLocalDate().equals(definition.getTradeDate().toLocalDate())) {
        lastMarginPrice = definition.getTradePrice();
      } else {
        if (ts == null) {
          throw new OpenGammaRuntimeException("Could not get price time series for " + security);
        }
        final int length = ts.getTimeSeries().size();
        if (length == 0) {
          throw new OpenGammaRuntimeException("Price time series for " + security.getExternalIdBundle() + " was empty");
        }
        lastMarginPrice = ts.getTimeSeries().getLatestValue();
      }
      return definition.toDerivative(now, lastMarginPrice);
    }
  };

  private final Converter<SwapSecurity, SwapDefinition> _swapSecurity = new Converter<SwapSecurity, SwapDefinition>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final SwapSecurity security) {
      Validate.notNull(security, "security");
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime swapStartDate = security.getEffectiveDate();
      final ZonedDateTime swapStartLocalDate = swapStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ValueRequirement payLegTS = getIndexTimeSeriesRequirement(payLeg, swapStartLocalDate);
      final ValueRequirement receiveLegTS = getIndexTimeSeriesRequirement(receiveLeg, swapStartLocalDate);
      final Set<ValueRequirement> requirements = new HashSet<>();
      if (payLegTS != null) {
        requirements.add(payLegTS);
      }
      if (receiveLegTS != null) {
        requirements.add(receiveLegTS);
      }
      return requirements;
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final SwapSecurity security, final SwapDefinition definition, final ZonedDateTime now, final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now, curveNames);
      }
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime fixingSeriesStartDate = security.getEffectiveDate().isBefore(now) ? security.getEffectiveDate() : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS }, curveNames);
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (receiveLegTS != null) {
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return definition.toDerivative(now, curveNames); // To deal with Fixed-Fixed cross currency swaps.
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final SwapSecurity security, final SwapDefinition definition, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now);
      }
      final SwapLeg payLeg = security.getPayLeg();
      final SwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime fixingSeriesStartDate = security.getEffectiveDate().isBefore(now) ? security.getEffectiveDate() : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS });
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS });
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS });
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id + "; error was " + e.getMessage());
        }
      }
      if (receiveLegTS != null) {
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS });
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS });
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return definition.toDerivative(now); // To deal with Fixed-Fixed cross currency swaps.
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }
  };

  private final Converter<CapFloorCMSSpreadSecurity, AnnuityCapFloorCMSSpreadDefinition> _capFloorCMSSpreadSecurity = new Converter<CapFloorCMSSpreadSecurity, AnnuityCapFloorCMSSpreadDefinition>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final CapFloorCMSSpreadSecurity security) {
      final ExternalId longId = security.getLongId();
      final ExternalId shortId = security.getShortId();
      final ZonedDateTime capStartDate = security.getStartDate();
      final LocalDate startDate = capStartDate.toLocalDate().minusDays(7); // To catch first fixing. SwapSecurity does not have this date.
      final ValueRequirement indexLongTS = getIndexTimeSeriesRequirement(getIndexSwapIdBundle(longId), startDate);
      if (indexLongTS == null) {
        return null;
      }
      final ValueRequirement indexShortTS = getIndexTimeSeriesRequirement(getIndexSwapIdBundle(shortId), startDate);
      if (indexShortTS == null) {
        return null;
      }
      return ImmutableSet.of(indexLongTS, indexShortTS);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final CapFloorCMSSpreadSecurity security, final AnnuityCapFloorCMSSpreadDefinition definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId longId = security.getLongId();
      final ExternalId shortId = security.getShortId();
      final ZonedDateTimeDoubleTimeSeries indexLongTS = getIndexTimeSeries(getIndexSwapIdBundle(longId), now.getZone(), timeSeries);
      final ZonedDateTimeDoubleTimeSeries indexShortTS = getIndexTimeSeries(getIndexSwapIdBundle(shortId), now.getZone(), timeSeries);
      final ZonedDateTimeDoubleTimeSeries indexSpreadTS = indexLongTS.subtract(indexShortTS);
      return definition.toDerivative(now, indexSpreadTS, curveNames);
    }

    @SuppressWarnings("synthetic-access")
    @Override
    public InstrumentDerivative convert(final CapFloorCMSSpreadSecurity security, final AnnuityCapFloorCMSSpreadDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      final ExternalId longId = security.getLongId();
      final ExternalId shortId = security.getShortId();
      final ZonedDateTimeDoubleTimeSeries indexLongTS = getIndexTimeSeries(getIndexSwapIdBundle(longId), now.getZone(), timeSeries);
      final ZonedDateTimeDoubleTimeSeries indexShortTS = getIndexTimeSeries(getIndexSwapIdBundle(shortId), now.getZone(), timeSeries);
      final ZonedDateTimeDoubleTimeSeries indexSpreadTS = indexLongTS.subtract(indexShortTS);
      return definition.toDerivative(now, indexSpreadTS);
    }
  };

  private final Converter<ZeroCouponInflationSwapSecurity, SwapFixedInflationZeroCouponDefinition> _zeroCouponInflationSwapSecurity =
      new Converter<ZeroCouponInflationSwapSecurity, SwapFixedInflationZeroCouponDefinition>() {

        @Override
        public Set<ValueRequirement> getTimeSeriesRequirements(final ZeroCouponInflationSwapSecurity security) {
          Validate.notNull(security, "security");
          final SwapLeg payLeg = security.getPayLeg();
          final SwapLeg receiveLeg = security.getReceiveLeg();
          final ZonedDateTime swapStartDate = security.getEffectiveDate();
          final ZonedDateTime swapStartLocalDate = swapStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          final ValueRequirement payLegTS = getIndexTimeSeriesRequirement(payLeg, swapStartLocalDate);
          final ValueRequirement receiveLegTS = getIndexTimeSeriesRequirement(receiveLeg, swapStartLocalDate);
          final Set<ValueRequirement> requirements = new HashSet<>();
          if (payLegTS != null) {
            requirements.add(payLegTS);
          }
          if (receiveLegTS != null) {
            requirements.add(receiveLegTS);
          }
          return requirements;
        }

        @Override
        public InstrumentDerivative convert(final ZeroCouponInflationSwapSecurity security, final SwapFixedInflationZeroCouponDefinition definition, final ZonedDateTime now,
            final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
          Validate.notNull(security, "security");
          if (timeSeries == null) {
            return definition.toDerivative(now, curveNames);
          }
          final SwapLeg payLeg = security.getPayLeg();
          final SwapLeg receiveLeg = security.getReceiveLeg();
          final ZonedDateTime fixingSeriesStartDate = security.getEffectiveDate().isBefore(now) ? security.getEffectiveDate() : now;
          final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
          final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
          if (payLegTS != null) {
            if (receiveLegTS != null) {
              try {
                return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
              } catch (final OpenGammaRuntimeException e) {
                final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
                throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
              }
            }
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS }, curveNames);
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          if (receiveLegTS != null) {
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS }, curveNames);
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) receiveLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
        }

        @Override
        public InstrumentDerivative convert(final ZeroCouponInflationSwapSecurity security, final SwapFixedInflationZeroCouponDefinition definition, final ZonedDateTime now,
            final HistoricalTimeSeriesBundle timeSeries) {
          Validate.notNull(security, "security");
          if (timeSeries == null) {
            return definition.toDerivative(now);
          }
          final SwapLeg payLeg = security.getPayLeg();
          final SwapLeg receiveLeg = security.getReceiveLeg();
          final ZonedDateTime fixingSeriesStartDate = security.getEffectiveDate().isBefore(now) ? security.getEffectiveDate() : now;
          final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          // normalisation of the timeseries
          final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
          final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
          if (payLegTS != null) {
            if (receiveLegTS != null) {
              try {
                return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS });
              } catch (final OpenGammaRuntimeException e) {
                final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
                throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
              }
            }

            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS });
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          if (receiveLegTS != null) {
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS });
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) receiveLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
        }

      };

  private final Converter<YearOnYearInflationSwapSecurity, SwapFixedInflationYearOnYearDefinition> _yearOnYearInflationSwapSecurity =
      new Converter<YearOnYearInflationSwapSecurity, SwapFixedInflationYearOnYearDefinition>() {

        @Override
        public Set<ValueRequirement> getTimeSeriesRequirements(final YearOnYearInflationSwapSecurity security) {
          Validate.notNull(security, "security");
          final SwapLeg payLeg = security.getPayLeg();
          final SwapLeg receiveLeg = security.getReceiveLeg();
          final ZonedDateTime swapStartDate = security.getEffectiveDate();
          final ZonedDateTime swapStartLocalDate = swapStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          final ValueRequirement payLegTS = getIndexTimeSeriesRequirement(payLeg, swapStartLocalDate);
          final ValueRequirement receiveLegTS = getIndexTimeSeriesRequirement(receiveLeg, swapStartLocalDate);
          final Set<ValueRequirement> requirements = new HashSet<>();
          if (payLegTS != null) {
            requirements.add(payLegTS);
          }
          if (receiveLegTS != null) {
            requirements.add(receiveLegTS);
          }
          return requirements;
        }

        @Override
        public InstrumentDerivative convert(final YearOnYearInflationSwapSecurity security, final SwapFixedInflationYearOnYearDefinition definition, final ZonedDateTime now,
            final String[] curveNames,
            final HistoricalTimeSeriesBundle timeSeries) {
          Validate.notNull(security, "security");
          if (timeSeries == null) {
            return definition.toDerivative(now, curveNames);
          }
          final SwapLeg payLeg = security.getPayLeg();
          final SwapLeg receiveLeg = security.getReceiveLeg();
          final ZonedDateTime fixingSeriesStartDate = security.getEffectiveDate().isBefore(now) ? security.getEffectiveDate() : now;
          final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
          final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
          if (payLegTS != null) {
            if (receiveLegTS != null) {
              try {
                return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
              } catch (final OpenGammaRuntimeException e) {
                final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
                throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
              }
            }
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS }, curveNames);
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          if (receiveLegTS != null) {
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS }, curveNames);
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) receiveLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
        }

        @Override
        public InstrumentDerivative convert(final YearOnYearInflationSwapSecurity security, final SwapFixedInflationYearOnYearDefinition definition,
            final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries) {
          Validate.notNull(security, "security");
          if (timeSeries == null) {
            return definition.toDerivative(now);
          }
          final SwapLeg payLeg = security.getPayLeg();
          final SwapLeg receiveLeg = security.getReceiveLeg();
          final ZonedDateTime fixingSeriesStartDate = security.getEffectiveDate().isBefore(now) ? security.getEffectiveDate() : now;
          final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
          final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
          final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
          if (payLegTS != null) {
            if (receiveLegTS != null) {
              try {
                return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS });
              } catch (final OpenGammaRuntimeException e) {
                final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
                throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
              }
            }
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS });
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) payLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          if (receiveLegTS != null) {
            try {
              return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS });
            } catch (final OpenGammaRuntimeException e) {
              final ExternalId id = ((InflationIndexSwapLeg) receiveLeg).getIndexId();
              throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
            }
          }
          throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
        }

      };

  private final Converter<Security, InstrumentDefinition<?>> _default = new Converter<Security, InstrumentDefinition<?>>() {

    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final Security security) {
      return Collections.emptySet();
    }

    @Override
    public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now, final String[] curveNames,
        final HistoricalTimeSeriesBundle timeSeries) {
      if (curveNames.length == 1) {
        final String[] singleCurve = new String[] {curveNames[0], curveNames[0] };
        return definition.toDerivative(now, singleCurve);
      }
      return definition.toDerivative(now, curveNames);
    }

    @Override
    public InstrumentDerivative convert(final Security security, final InstrumentDefinition<?> definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      return definition.toDerivative(now);
    }
  };

  private ValueRequirement getIndexTimeSeriesRequirement(final SwapLeg leg, final ZonedDateTime swapEffectiveDate) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(DAYS_BEFORE_EFFECTIVE);
      final HistoricalTimeSeriesResolutionResult ts = getTimeSeriesResolver().resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (ts == null) {
        return null;
      }
      return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(ts, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.of(startDate), true, DateConstraint.VALUATION_TIME, true);
    } else if (leg instanceof InflationIndexSwapLeg) {
      final InflationIndexSwapLeg inflationIndexLeg = (InflationIndexSwapLeg) leg;
      final ExternalIdBundle id = getIndexPriceIdBundle(inflationIndexLeg.getIndexId());
      final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(DAYS_BEFORE_EFFECTIVE);
      final HistoricalTimeSeriesResolutionResult ts = getTimeSeriesResolver().resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (ts == null) {
        return null;
      }
      return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(ts, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.of(startDate), true, DateConstraint.VALUATION_TIME, true);
    }
    return null;
  }

  private ValueRequirement getIndexTimeSeriesRequirement(final InterestRateSwapLeg leg, final ZonedDateTime swapEffectiveDate) {
    if (leg instanceof FloatingInterestRateSwapLeg) {
      final FloatingInterestRateSwapLeg floatingLeg = (FloatingInterestRateSwapLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      final LocalDate startDate = swapEffectiveDate.toLocalDate().minusDays(DAYS_BEFORE_EFFECTIVE);
      final HistoricalTimeSeriesResolutionResult ts = getTimeSeriesResolver().resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
      if (ts == null) {
        return null;
      }
      return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(ts, MarketDataRequirementNames.MARKET_VALUE,
          DateConstraint.of(startDate), true, DateConstraint.VALUATION_TIME, true);
    }
    return null;
  }

  private ZonedDateTimeDoubleTimeSeries getIndexTimeSeries(final SwapLeg leg, final ZonedDateTime swapEffectiveDate, final ZonedDateTime now,
      final HistoricalTimeSeriesBundle timeSeries) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      // Implementation note: To catch first fixing. SwapSecurity does not have this date.
      if (now.isBefore(swapEffectiveDate)) { // TODO: review if this is the correct condition
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (ts == null) {
        s_logger.info("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      if (ts.getTimeSeries().isEmpty()) {
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO remove me when KWCDC Curncy is normalised correctly
      if (localDateTS.getLatestValue() > 0.50) {
        localDateTS = localDateTS.divide(100);
      }
      return convertTimeSeries(now.getZone(), localDateTS);
    } else if (leg instanceof InflationIndexSwapLeg) {
      final InflationIndexSwapLeg indexLeg = (InflationIndexSwapLeg) leg;
      final ExternalIdBundle id = getIndexPriceIdBundle(indexLeg.getIndexId());
      // Implementation note: To catch first fixing. SwapSecurity does not have this date.
      if (now.isBefore(swapEffectiveDate)) { // TODO: review if this is the correct condition
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (ts == null) {
        s_logger.info("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      if (ts.getTimeSeries().isEmpty()) {
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO remove when inflation timeseries will be normalised properly
      if (localDateTS.getLatestValue() < 50.0) {
        localDateTS = localDateTS.multiply(100);
      }
      return convertTimeSeries(now.getZone(), localDateTS);

    }
    return null;
  }

  private ZonedDateTimeDoubleTimeSeries getIndexTimeSeries(final InterestRateSwapLeg leg, final ZonedDateTime swapEffectiveDate, final ZonedDateTime now,
      final HistoricalTimeSeriesBundle timeSeries) {
    if (leg instanceof FloatingInterestRateSwapLeg) {
      final FloatingInterestRateSwapLeg floatingLeg = (FloatingInterestRateSwapLeg) leg;
      final ExternalIdBundle id = getIndexIdForSwap(floatingLeg);
      // Implementation note: To catch first fixing. SwapSecurity does not have this date.
      if (now.isBefore(swapEffectiveDate)) { // TODO: review if this is the correct condition
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
      if (ts == null) {
        s_logger.info("Could not get time series of underlying index " + id.getExternalIds().toString() + " bundle used was " + id);
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      if (ts.getTimeSeries().isEmpty()) {
        return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(now.getZone());
      }
      LocalDateDoubleTimeSeries localDateTS = ts.getTimeSeries();
      //TODO remove me when KWCDC Curncy is normalised correctly
      if (localDateTS.getLatestValue() > 0.50) {
        localDateTS = localDateTS.divide(100);
      }
      return convertTimeSeries(now.getZone(), localDateTS);
    }
    return null;
  }

  private final Converter<InterestRateSwapSecurity, SwapDefinition> _irsSecurity = new Converter<InterestRateSwapSecurity, SwapDefinition>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final InterestRateSwapSecurity security) {
      Validate.notNull(security, "security");
      final InterestRateSwapLeg payLeg = security.getPayLeg();
      final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime swapStartDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime swapStartLocalDate = swapStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ValueRequirement payLegTS = getIndexTimeSeriesRequirement(payLeg, swapStartLocalDate);
      final ValueRequirement receiveLegTS = getIndexTimeSeriesRequirement(receiveLeg, swapStartLocalDate);
      final Set<ValueRequirement> requirements = new HashSet<>();
      if (payLegTS != null) {
        requirements.add(payLegTS);
      }
      if (receiveLegTS != null) {
        requirements.add(receiveLegTS);
      }
      return requirements;
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final InterestRateSwapSecurity security, final SwapDefinition definition, final ZonedDateTime now,
        final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now, curveNames);
      }
      final InterestRateSwapLeg payLeg = security.getPayLeg();
      final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime effectiveDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime fixingSeriesStartDate = effectiveDate.isBefore(now) ? effectiveDate : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS }, curveNames);
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (receiveLegTS != null) {
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return definition.toDerivative(now, curveNames); // To deal with Fixed-Fixed cross currency swaps.
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final InterestRateSwapSecurity security, final SwapDefinition definition, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now);
      }
      final InterestRateSwapLeg payLeg = security.getPayLeg();
      final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime effectiveDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime fixingSeriesStartDate = effectiveDate.isBefore(now) ? effectiveDate : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS });
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS });
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS });
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id + "; error was " + e.getMessage());
        }
      }
      if (receiveLegTS != null) {
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS });
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS });
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return definition.toDerivative(now); // To deal with Fixed-Fixed cross currency swaps.
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }
  };

  private final Converter<InterestRateSwapSecurity, SwapMultilegDefinition> _irsMultiLegSecurity = new Converter<InterestRateSwapSecurity, SwapMultilegDefinition>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final InterestRateSwapSecurity security) {
      Validate.notNull(security, "security");
      final InterestRateSwapLeg payLeg = security.getPayLeg();
      final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime swapStartDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime swapStartLocalDate = swapStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ValueRequirement payLegTS = getIndexTimeSeriesRequirement(payLeg, swapStartLocalDate);
      final ValueRequirement receiveLegTS = getIndexTimeSeriesRequirement(receiveLeg, swapStartLocalDate);
      final Set<ValueRequirement> requirements = new HashSet<>();
      if (payLegTS != null) {
        requirements.add(payLegTS);
      }
      if (receiveLegTS != null) {
        requirements.add(receiveLegTS);
      }
      return requirements;
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final InterestRateSwapSecurity security, final SwapMultilegDefinition definition, final ZonedDateTime now,
        final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now, curveNames);
      }
      final InterestRateSwapLeg payLeg = security.getPayLeg();
      final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime effectiveDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime fixingSeriesStartDate = effectiveDate.isBefore(now) ? effectiveDate : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS }, curveNames);
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {payLegTS, payLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (receiveLegTS != null) {
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          try {
            return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS }, curveNames);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          return definition.toDerivative(now, new ZonedDateTimeDoubleTimeSeries[] {receiveLegTS, receiveLegTS }, curveNames);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return definition.toDerivative(now, curveNames); // To deal with Fixed-Fixed cross currency swaps.
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final InterestRateSwapSecurity security, final SwapMultilegDefinition definition, final ZonedDateTime now, final HistoricalTimeSeriesBundle timeSeries) {
      Validate.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now);
      }
      final InterestRateSwapLeg payLeg = security.getPayLeg();
      final InterestRateSwapLeg receiveLeg = security.getReceiveLeg();
      final ZonedDateTime effectiveDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime fixingSeriesStartDate = effectiveDate.isBefore(now) ? effectiveDate : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries payLegTS = getIndexTimeSeries(payLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries receiveLegTS = getIndexTimeSeries(receiveLeg, fixingSeriesStartLocalDate, now, timeSeries);
      final ZonedDateTimeDoubleTimeSeries[] series = new ZonedDateTimeDoubleTimeSeries[definition.getLegs().length];

      if (payLegTS != null) {
        if (receiveLegTS != null) {
          try {
            Arrays.fill(series, payLegTS);
            series[1] = receiveLegTS;
            return definition.toDerivative(now, series);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          Arrays.fill(series, payLegTS);
          return definition.toDerivative(now, series);
        }
        try {
          Arrays.fill(series, payLegTS);
          return definition.toDerivative(now, series);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id + "; error was " + e.getMessage());
        }
      }
      if (receiveLegTS != null) {
        if ((InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_FIXED_CMS)
            || (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY)) {
          try {
            Arrays.fill(series, receiveLegTS);
            return definition.toDerivative(now, series);
          } catch (final OpenGammaRuntimeException e) {
            final ExternalId id = ((FloatingInterestRateSwapLeg) payLeg).getFloatingReferenceRateId();
            throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
          }
        }
        try {
          Arrays.fill(series, receiveLegTS);
          return definition.toDerivative(now, series);
        } catch (final OpenGammaRuntimeException e) {
          final ExternalId id = ((FloatingInterestRateSwapLeg) receiveLeg).getFloatingReferenceRateId();
          throw new OpenGammaRuntimeException("Could not get fixing value for series with identifier " + id, e);
        }
      }
      if (InterestRateInstrumentType.getInstrumentTypeFromSecurity(security) == InterestRateInstrumentType.SWAP_CROSS_CURRENCY) {
        return definition.toDerivative(now); // To deal with Fixed-Fixed cross currency swaps.
      }
      throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
    }
  };

  /**
   * Converts {@link BondTotalReturnSwapSecurity} and {@link EquityTotalReturnSwapSecurity}.
   */
  private final Converter<TotalReturnSwapSecurity, TotalReturnSwapDefinition> _totalReturnSwapSecurity = new Converter<TotalReturnSwapSecurity, TotalReturnSwapDefinition>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Set<ValueRequirement> getTimeSeriesRequirements(final TotalReturnSwapSecurity security) {
      ArgumentChecker.notNull(security, "security");
      final InterestRateSwapLeg fundingLeg = security.getFundingLeg();
      final ZonedDateTime swapStartDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime swapStartLocalDate = swapStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ValueRequirement fundingLegTS = getIndexTimeSeriesRequirement(fundingLeg, swapStartLocalDate);
      final Set<ValueRequirement> requirements = new HashSet<>();
      if (fundingLegTS != null) {
        requirements.add(fundingLegTS);
      }
      return requirements;
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final TotalReturnSwapSecurity security, final TotalReturnSwapDefinition definition, final ZonedDateTime now,
        final String[] curveNames, final HistoricalTimeSeriesBundle timeSeries) {
      return convert(security, definition, now, timeSeries);
    }

    @Override
    @SuppressWarnings({"synthetic-access" })
    public InstrumentDerivative convert(final TotalReturnSwapSecurity security, final TotalReturnSwapDefinition definition, final ZonedDateTime now,
        final HistoricalTimeSeriesBundle timeSeries) {
      ArgumentChecker.notNull(security, "security");
      if (timeSeries == null) {
        return definition.toDerivative(now);
      }
      final InterestRateSwapLeg fundingLeg = security.getFundingLeg();
      final ZonedDateTime effectiveDate = security.getEffectiveDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTime fixingSeriesStartDate = effectiveDate.isBefore(now) ? effectiveDate : now;
      final ZonedDateTime fixingSeriesStartLocalDate = fixingSeriesStartDate.toLocalDate().atStartOfDay(ZoneOffset.UTC);
      final ZonedDateTimeDoubleTimeSeries fundingLegTS = getIndexTimeSeries(fundingLeg, fixingSeriesStartLocalDate, now, timeSeries);
      return definition.toDerivative(now, fundingLegTS);
    }
  };

  private ExternalIdBundle getIndexIdForSwap(final FloatingInterestRateLeg floatingLeg) {
    if (floatingLeg.getFloatingRateType().isIbor()) {
      return getIndexIborIdBundle(floatingLeg.getFloatingReferenceRateId());
    }
    if (floatingLeg.getFloatingRateType().equals(FloatingRateType.OIS) || floatingLeg.getFloatingRateType().equals(FloatingRateType.OVERNIGHT_ARITHMETIC_AVERAGE)) {
      return getIndexOvernightIdBundle(floatingLeg.getFloatingReferenceRateId());
    }
    if (floatingLeg.getFloatingRateType().equals(FloatingRateType.CMS)) {
      return getIndexSwapIdBundle(floatingLeg.getFloatingReferenceRateId());
    }
    throw new OpenGammaRuntimeException("Unsupported floating leg type.");
  }

  // TODO: [PLAT-5976] Change the external bundle method to support any reference.
  private ExternalIdBundle getIndexIdForSwap(final FloatingInterestRateSwapLeg floatingLeg) {
    return ExternalIdBundle.of(floatingLeg.getFloatingReferenceRateId());
  }

  /**
   * Returns the ExternalIDBundle associated to an ExternalId of an IborIndex as stored in the security source.
   * @param indexId The external id.
   * @return The bundle.
   */
  private ExternalIdBundle getIndexIborIdBundle(final ExternalId indexId) {
    final Security sec = _securitySource.getSingle(indexId.toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Ibor index with id " + indexId.toBundle() + " is null");
    }
    if (!(sec instanceof com.opengamma.financial.security.index.IborIndex)) {
      throw new OpenGammaRuntimeException("Security with id " + indexId.toBundle() + " is not an IborIndex");
    }
    final com.opengamma.financial.security.index.IborIndex indexSecurity = (com.opengamma.financial.security.index.IborIndex) sec;
    return indexSecurity.getExternalIdBundle();
  }

  /**
   * Returns the ExternalIDBundle associated to an ExternalId of an OvernightIndex as stored in the security source.
   * @param indexId The external id.
   * @return The bundle.
   */
  private ExternalIdBundle getIndexOvernightIdBundle(final ExternalId indexId) {
    final Security sec = _securitySource.getSingle(indexId.toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Index with id " + indexId.toBundle() + " is null");
    }
    if (!(sec instanceof OvernightIndex)) {
      throw new OpenGammaRuntimeException("Security with id " + indexId.toBundle() + " is not an OvernightIndex");
    }
    final OvernightIndex indexSecurity = (OvernightIndex) sec;
    return indexSecurity.getExternalIdBundle();
  }

  /**
   * Returns the ExternalIDBundle associated to an ExternalId of an SwapIndex as stored in the security source.
   * @param indexId The external id.
   * @return The bundle.
   */
  private ExternalIdBundle getIndexSwapIdBundle(final ExternalId indexId) {
    final Security sec = _securitySource.getSingle(indexId.toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Index with id " + indexId.toBundle() + " is null");
    }
    if (!(sec instanceof SwapIndex)) {
      throw new OpenGammaRuntimeException("Security with id " + indexId.toBundle() + " is not a SwapIndex");
    }
    final SwapIndex indexSecurity = (SwapIndex) sec;
    return indexSecurity.getExternalIdBundle();
  }

  /**
   * Returns the ExternalIDBundle associated to an ExternalId of an PriceIndex as stored in the security source.
   * @param indexId The external id.
   * @return The bundle.
   */
  private ExternalIdBundle getIndexPriceIdBundle(final ExternalId indexId) {
    final Security sec = _securitySource.getSingle(indexId.toBundle());
    if (sec == null) {
      throw new OpenGammaRuntimeException("Index with id " + indexId.toBundle() + " is null");
    }
    if (!(sec instanceof PriceIndex)) {
      throw new OpenGammaRuntimeException("Security with id " + indexId.toBundle() + " is not a PriceIndex");
    }
    final PriceIndex indexSecurity = (PriceIndex) sec;
    return indexSecurity.getExternalIdBundle();
  }

  private ValueRequirement getIndexTimeSeriesRequirement(final ExternalIdBundle id, final LocalDate startDate) {
    final HistoricalTimeSeriesResolutionResult timeSeries = getTimeSeriesResolver().resolve(id, null, null, null, MarketDataRequirementNames.MARKET_VALUE, null);
    if (timeSeries == null) {
      return null;
    }
    return HistoricalTimeSeriesFunctionUtils.createHTSRequirement(timeSeries, MarketDataRequirementNames.MARKET_VALUE,
        DateConstraint.of(startDate), true, DateConstraint.VALUATION_TIME, true);
  }

  /**
   * Returns the time series to be used in the toDerivative method.
   *
   * @param id The ExternalId bundle.
   * @param timeZone The time zone to use for the returned series
   * @param timeSeries bundle containing the fixing timeseries
   * @return The time series.
   */
  private static ZonedDateTimeDoubleTimeSeries getIndexTimeSeries(final ExternalIdBundle id, final ZoneId timeZone, final HistoricalTimeSeriesBundle timeSeries) {
    final HistoricalTimeSeries ts = timeSeries.get(MarketDataRequirementNames.MARKET_VALUE, id);
    // Implementation note: the normalization take place in the getHistoricalTimeSeries
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get time series of underlying index " + id.getExternalIds().toString());
    }
    if (ts.getTimeSeries().isEmpty()) {
      return ImmutableZonedDateTimeDoubleTimeSeries.ofEmpty(timeZone);
    }
    return convertTimeSeries(timeZone, ts.getTimeSeries());
  }

  private static ZonedDateTimeDoubleTimeSeries convertTimeSeries(final ZoneId timeZone, final LocalDateDoubleTimeSeries localDateTS) {
    final ZonedDateTimeDoubleTimeSeriesBuilder bld = ImmutableZonedDateTimeDoubleTimeSeries.builder(timeZone);
    for (final LocalDateDoubleEntryIterator it = localDateTS.iterator(); it.hasNext();) {
      final LocalDate date = it.nextTime();
      final ZonedDateTime zdt = date.atStartOfDay(timeZone);
      bld.put(zdt, it.currentValueFast());
    }
    return bld.build();
  }

}
