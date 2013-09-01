/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.Period;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.region.Region;
import com.opengamma.core.region.RegionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.financial.analytics.conversion.CalendarUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingRateType;
import com.opengamma.financial.security.swap.FloatingSpreadIRLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.Tenor;

/**
 * Converts specifications into fully resolved security definitions
 */
public class FixedIncomeStripIdentifierAndMaturityBuilder {
  private static final Logger s_logger = LoggerFactory.getLogger(FixedIncomeStripIdentifierAndMaturityBuilder.class);
  private static final LocalTime CASH_EXPIRY_TIME = LocalTime.of(11, 00);

  private final RegionSource _regionSource;
  private final ConventionBundleSource _conventionBundleSource;
  private final SecuritySource _secSource;
  private final HolidaySource _holidaySource;

  // TODO: Don't accept a SecuritySource here; use the ComputationTargetResolver
  public FixedIncomeStripIdentifierAndMaturityBuilder(final RegionSource regionSource, final ConventionBundleSource conventionBundleSource, final SecuritySource secSource,
      final HolidaySource holidaySource) {
    _regionSource = regionSource;
    _conventionBundleSource = conventionBundleSource;
    _secSource = secSource;
    _holidaySource = holidaySource;
  }

  public InterpolatedYieldCurveSpecificationWithSecurities resolveToSecurity(final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues) {
    final Collection<FixedIncomeStripWithSecurity> securityStrips = new TreeSet<>();
    final LocalDate curveDate = curveSpecification.getCurveDate();
    for (final FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      final InstrumentHandler handler = getInstrumentHandler(strip);
      final Security security = handler.getSecurity(this, curveSpecification, marketValues, strip);
      final ZonedDateTime maturity = handler.getMaturity(this, curveDate, strip, security);
      final Tenor resolvedTenor = Tenor.of(Period.between(curveDate, maturity.toLocalDate()));
      securityStrips.add(new FixedIncomeStripWithSecurity(strip.getStrip(), resolvedTenor, maturity, strip.getSecurity(), security));
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, curveSpecification.getName(), curveSpecification.getCurrency(), curveSpecification.getInterpolator(),
        curveSpecification.interpolateYield(), securityStrips);
  }

  // TODO: Implement the "getRequirements" methods and use this to make sure that target resolver caches are pre-populated at execution time
  public Set<ValueRequirement> getResolutionRequirements(final InterpolatedYieldCurveSpecification curveSpecification) {
    final Set<ValueRequirement> requirements = new HashSet<>();
    for (final FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      final InstrumentHandler handler = getInstrumentHandler(strip);
      requirements.addAll(handler.getRequirements(this, curveSpecification, strip));
    }
    return requirements;
  }

  private abstract static class InstrumentHandler {

    public abstract Security getSecurity(FixedIncomeStripIdentifierAndMaturityBuilder self, InterpolatedYieldCurveSpecification curveSpecification, SnapshotDataBundle marketValues,
        FixedIncomeStripWithIdentifier strip);

    public abstract ZonedDateTime getMaturity(FixedIncomeStripIdentifierAndMaturityBuilder self, LocalDate curveDate, FixedIncomeStripWithIdentifier strip, Security security);

    public abstract Set<ValueRequirement> getRequirements(FixedIncomeStripIdentifierAndMaturityBuilder self, InterpolatedYieldCurveSpecification curveSpecification,
        FixedIncomeStripWithIdentifier strip);

  }

  private static final InstrumentHandler s_cash = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final CashSecurity cashSecurity = self.getCash(curveSpecification, strip, marketValues);
      if (cashSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve cash curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return cashSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final CashSecurity cashSecurity = (CashSecurity) security;
      final Region region = self._regionSource.getHighestLevelRegion(cashSecurity.getRegionId());
      ZoneId timeZone = region.getTimeZone();
      timeZone = ensureZone(timeZone);
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_fra3m = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final FRASecurity fraSecurity = self.getFRA(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
      if (fraSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return fraSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final FRASecurity fraSecurity = (FRASecurity) security;
      return fraSecurity.getEndDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_fra6m = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final FRASecurity fraSecurity = self.getFRA(curveSpecification, strip, marketValues, Tenor.SIX_MONTHS);
      if (fraSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return fraSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final FRASecurity fraSecurity = (FRASecurity) security;
      return fraSecurity.getEndDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_fra = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final FRASecurity fraSecurity = self.getFRA(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
      if (fraSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return fraSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final FRASecurity fraSecurity = (FRASecurity) security;
      return fraSecurity.getEndDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_future = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
      final FutureSecurity futureSecurity = self.getFuture(strip);
      if (futureSecurity == null) {
        throw new OpenGammaRuntimeException("Security source did not contain future curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return futureSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final FutureSecurity futureSecurity = (FutureSecurity) security;
      return futureSecurity.getExpiry().getExpiry().plusMonths(3); //TODO shouldn't hard-code to 3 - find out why comparator in FixedIncomeStrip isn't working properly
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_libor = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final CashSecurity rateSecurity = self.getCash(curveSpecification, strip, marketValues);
      if (rateSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve Libor curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return rateSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final CashSecurity rateSecurity = (CashSecurity) security;
      final Region region2 = self._regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
      ZoneId timeZone2 = region2.getTimeZone();
      timeZone2 = ensureZone(timeZone2);
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_euribor = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final CashSecurity rateSecurity = self.getCash(curveSpecification, strip, marketValues);
      if (rateSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve Euribor curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return rateSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final CashSecurity rateSecurity = (CashSecurity) security;
      final Region region2 = self._regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
      ZoneId timeZone2 = region2.getTimeZone();
      timeZone2 = ensureZone(timeZone2);
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_cdor = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final CashSecurity rateSecurity = self.getCash(curveSpecification, strip, marketValues);
      if (rateSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve CDOR curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return rateSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final CashSecurity rateSecurity = (CashSecurity) security;
      final Region region2 = self._regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
      ZoneId timeZone2 = region2.getTimeZone();
      timeZone2 = ensureZone(timeZone2);
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_cibor = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final CashSecurity rateSecurity = self.getCash(curveSpecification, strip, marketValues);
      if (rateSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve CIBOR curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return rateSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final CashSecurity rateSecurity = (CashSecurity) security;
      final Region region2 = self._regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
      ZoneId timeZone2 = region2.getTimeZone();
      timeZone2 = ensureZone(timeZone2);
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_stibor = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final CashSecurity rateSecurity = self.getCash(curveSpecification, strip, marketValues);
      if (rateSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve STIBOR curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return rateSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final CashSecurity rateSecurity = (CashSecurity) security;
      final Region region2 = self._regionSource.getHighestLevelRegion(rateSecurity.getRegionId());
      ZoneId timeZone2 = region2.getTimeZone();
      timeZone2 = ensureZone(timeZone2);
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_swap = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      // In case there's any old curve definitions hanging around - assume that all swaps are 3m
      // TODO get defaults from convention? (e.g. USD = 3m, EUR = 6M)
      final SwapSecurity swapSecurity = self.getSwap(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
      if (swapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return swapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity swapSecurity = (SwapSecurity) security;
      return swapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_swap3m = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final SwapSecurity swapSecurity = self.getSwap(curveSpecification, strip, marketValues, Tenor.THREE_MONTHS);
      if (swapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return swapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity swapSecurity = (SwapSecurity) security;
      return swapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_swap6m = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final SwapSecurity swapSecurity = self.getSwap(curveSpecification, strip, marketValues, Tenor.SIX_MONTHS);
      if (swapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return swapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity swapSecurity = (SwapSecurity) security;
      return swapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_swap12m = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final SwapSecurity swapSecurity = self.getSwap(curveSpecification, strip, marketValues, Tenor.ONE_YEAR);
      if (swapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return swapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity swapSecurity = (SwapSecurity) security;
      return swapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_swap28d = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final SwapSecurity swapSecurity = self.get28DaySwap(curveSpecification, strip, marketValues);
      if (swapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return swapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity swapSecurity = (SwapSecurity) security;
      return swapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_tenorSwap = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final SwapSecurity tenorSwapSecurity = self.getTenorSwap(curveSpecification, strip, marketValues);
      if (tenorSwapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return tenorSwapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity tenorSwapSecurity = (SwapSecurity) security;
      return tenorSwapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_oisSwap = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      return self.getOISSwap(curveSpecification, strip, marketValues);
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      return curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(ZoneOffset.UTC);
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_periodicZeroDeposit = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final PeriodicZeroDepositSecurity depositSecurity = getPeriodicZeroDeposit(curveSpecification, strip, marketValues);
      return depositSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final PeriodicZeroDepositSecurity depositSecurity = (PeriodicZeroDepositSecurity) security;
      return depositSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static final InstrumentHandler s_basisSwap = new InstrumentHandler() {

    @Override
    public Security getSecurity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification, final SnapshotDataBundle marketValues,
        final FixedIncomeStripWithIdentifier strip) {
      final SwapSecurity basisSwapSecurity = self.getBasisSwap(curveSpecification, strip, marketValues);
      if (basisSwapSecurity == null) {
        throw new OpenGammaRuntimeException("Could not resolve basis swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification);
      }
      return basisSwapSecurity;
    }

    @Override
    public ZonedDateTime getMaturity(final FixedIncomeStripIdentifierAndMaturityBuilder self, final LocalDate curveDate, final FixedIncomeStripWithIdentifier strip, final Security security) {
      final SwapSecurity basisSwapSecurity = (SwapSecurity) security;
      return basisSwapSecurity.getMaturityDate();
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FixedIncomeStripIdentifierAndMaturityBuilder self, final InterpolatedYieldCurveSpecification curveSpecification,
        final FixedIncomeStripWithIdentifier strip) {
      // TODO: Implement this
      throw new UnsupportedOperationException("TODO");
    }

  };

  private static InstrumentHandler getInstrumentHandler(final FixedIncomeStripWithIdentifier strip) {
    switch (strip.getInstrumentType()) {
      case CASH:
        return s_cash;
      case FRA_3M:
        return s_fra3m;
      case FRA_6M:
        return s_fra6m;
      case FRA:
        return s_fra;
      case FUTURE:
        return s_future;
      case LIBOR:
        return s_libor;
      case EURIBOR:
        return s_euribor;
      case CDOR:
        return s_cdor;
      case CIBOR:
        return s_cibor;
      case STIBOR:
        return s_stibor;
      case SWAP:
        return s_swap;
      case SWAP_3M:
        return s_swap3m;
      case SWAP_6M:
        return s_swap6m;
      case SWAP_12M:
        return s_swap12m;
      case SWAP_28D:
        return s_swap28d;
      case TENOR_SWAP:
        return s_tenorSwap;
      case OIS_SWAP:
        return s_oisSwap;
      case PERIODIC_ZERO_DEPOSIT:
        return s_periodicZeroDeposit;
      case BASIS_SWAP:
        return s_basisSwap;
      default:
        throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
    }
  }

  private CashSecurity getCash(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final ConventionBundle cashConvention = _conventionBundleSource.getConventionBundle(strip.getSecurity());
    if (cashConvention == null) {
      throw new OpenGammaRuntimeException("No convention for cash " + strip.getSecurity() + " so can't establish business day convention");
    }
    if (cashConvention.getRegion() == null) {
      throw new OpenGammaRuntimeException("Region for strip " + strip + " was null");
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, cashConvention.getRegion());
    if (calendar == null) {
      throw new OpenGammaRuntimeException("Calendar was null");
    }
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(curveDate, cashConvention.getSettlementDays(), calendar);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, cashConvention.getPeriod(), cashConvention.getBusinessDayConvention(), calendar, cashConvention.isEOMConvention());
    final Double rate = marketValues.getDataPoint(strip.getSecurity());
    if (rate == null) {
      throw new OpenGammaRuntimeException("No market data for " + strip.getSecurity());
    }
    final CashSecurity sec = new CashSecurity(spec.getCurrency(), spec.getRegion(), startDate, endDate, cashConvention.getDayCount(), rate, 1.0d);
    sec.setExternalIdBundle(ExternalIdBundle.of(strip.getSecurity()));
    return sec;
  }

  private FRASecurity getFRA(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues, final Tenor tenor) {
    final ExternalId fraIdentifier = strip.getSecurity();
    final int months = tenor.getPeriod().getMonths();
    final ExternalId underlyingId = getUnderlyingId(spec, strip);
    Period fraPeriod;
    final Currency ccy = spec.getCurrency();
    BusinessDayConvention businessDayConvention;
    boolean eom;
    Calendar calendar;
    ExternalId underlyingIdentifier;
    int settlementDays;
    if (underlyingId == null) {
      s_logger.info("Could not get convention for underlying from {}; trying tenor-based convention", strip);
      final ConventionBundle fraConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months
          + "M_FRA"));
      if (fraConvention == null) {
        throw new OpenGammaRuntimeException("Could not get convention for " + fraIdentifier + ": tried "
            + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_FRA"));
      }
      underlyingIdentifier = fraConvention.getSwapFloatingLegInitialRate();
      final ConventionBundle iborConvention = _conventionBundleSource.getConventionBundle(underlyingIdentifier);
      underlyingIdentifier = fraConvention.getSwapFloatingLegInitialRate();
      fraPeriod = iborConvention.getPeriod();
      businessDayConvention = iborConvention.getBusinessDayConvention();
      eom = iborConvention.isEOMConvention();
      calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fraConvention.getSwapFloatingLegRegion());
      settlementDays = iborConvention.getSettlementDays();
    } else {
      ConventionBundle fraConvention = _conventionBundleSource.getConventionBundle(underlyingId);
      if (fraConvention == null || fraConvention.getIdentifiers().size() != 1) {
        s_logger.info("Could not get unique convention for underlying from {}; trying tenor-based convention", strip);
        fraConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months
            + "M_FRA"));
        if (fraConvention == null) {
          throw new OpenGammaRuntimeException("Could not get convention for " + fraIdentifier + ": tried "
              + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_FRA"));
        }
        final ConventionBundle iborConvention = _conventionBundleSource.getConventionBundle(fraConvention.getSwapFloatingLegInitialRate());
        fraPeriod = iborConvention.getPeriod();
        businessDayConvention = fraConvention.getSwapFloatingLegBusinessDayConvention();
        eom = fraConvention.isEOMConvention();
        calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fraConvention.getSwapFloatingLegRegion());
        underlyingIdentifier = underlyingId;
        settlementDays = fraConvention.getSwapFloatingLegSettlementDays();
      } else {
        fraPeriod = fraConvention.getPeriod();
        businessDayConvention = fraConvention.getBusinessDayConvention();
        eom = fraConvention.isEOMConvention();
        calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, fraConvention.getRegion());
        underlyingIdentifier = Iterables.getOnlyElement(fraConvention.getIdentifiers());
        settlementDays = fraConvention.getSettlementDays();
      }
    }
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC); // TODO: review?
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, settlementDays, calendar);
    final Period endPeriod = strip.getMaturity().getPeriod();
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(spotDate, endPeriod, businessDayConvention, calendar, eom);
    final Period startPeriod = endPeriod.minus(fraPeriod).normalized(); // TODO: check period >0?
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spotDate, startPeriod, businessDayConvention, calendar, eom);
    final ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(startDate, -settlementDays, calendar);
    if (marketValues.getDataPoint(strip.getSecurity()) == null) {
      throw new OpenGammaRuntimeException("Could not get market data for " + strip);
    }
    return new FRASecurity(ccy, spec.getRegion(), startDate, endDate, marketValues.getDataPoint(strip.getSecurity()), 1.0d, underlyingIdentifier, fixingDate);
  }

  private FutureSecurity getFuture(final FixedIncomeStripWithIdentifier strip) {
    return (FutureSecurity) _secSource.getSingle(ExternalIdBundle.of(strip.getSecurity()));
  }

  private SwapSecurity getSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues, final Tenor resetTenor) {
    if (spec.getCurrency().equals(Currency.BRL)) {
      return getBRLSwap(spec, strip, marketValues);
    }
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.getDataPoint(swapIdentifier);
    if (rate == null) {
      throw new OpenGammaRuntimeException("No market data for " + swapIdentifier);
    }
    final long months = resetTenor.getPeriod().toTotalMonths();
    final ConventionBundle fixedLegConvention = getFixedLegConvention(spec, strip, swapIdentifier);
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final String counterparty = "";
    Calendar calendar;
    ExternalId floatingRateId;
    FloatingInterestRateLeg iborLeg;
    final ExternalId underlyingId = getUnderlyingId(spec, strip);
    if (underlyingId == null) {
      s_logger.info("Could not get convention for underlying from {}; trying tenor-based convention", strip);
      final ExternalId id = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_" + months + "M_SWAP");
      ConventionBundle floatingLegConvention = _conventionBundleSource.getConventionBundle(id);
      if (floatingLegConvention == null) {
        floatingLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_" + months + "_SWAP"));
        if (floatingLegConvention == null) {
          throw new OpenGammaRuntimeException("Could not get floating leg convention for swap strip " + strip);
        }
      }
      calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, floatingLegConvention.getSwapFloatingLegRegion());
      floatingRateId = floatingLegConvention.getSwapFloatingLegInitialRate();
      if (floatingRateId == null) {
        throw new OpenGammaRuntimeException("Could not get floating rate id from convention");
      }
      iborLeg = new FloatingInterestRateLeg(floatingLegConvention.getSwapFloatingLegDayCount(), floatingLegConvention.getSwapFloatingLegFrequency(),
          floatingLegConvention.getSwapFloatingLegRegion(), floatingLegConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingRateId,
          FloatingRateType.IBOR);
    } else {
      final ConventionBundle underlyingConvention = _conventionBundleSource.getConventionBundle(underlyingId);
      if (underlyingConvention == null || underlyingConvention.getIdentifiers().size() != 1) {
        s_logger.info("Could not get unique convention for underlying from {}; trying tenor-based convention", strip);
        ConventionBundle floatingLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_" + months + "M_SWAP"));
        if (floatingLegConvention == null) {
          floatingLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
              spec.getCurrency().getCode() + "_" + months + "_SWAP"));
          if (floatingLegConvention == null) {
            throw new OpenGammaRuntimeException("Could not get floating leg convention for swap strip " + strip);
          }
        }
        calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, floatingLegConvention.getSwapFloatingLegRegion());
        floatingRateId = floatingLegConvention.getSwapFloatingLegInitialRate();
        if (floatingRateId == null) {
          throw new OpenGammaRuntimeException("Could not get floating rate id from convention");
        }
        iborLeg = new FloatingInterestRateLeg(floatingLegConvention.getSwapFloatingLegDayCount(), floatingLegConvention.getSwapFloatingLegFrequency(),
            floatingLegConvention.getSwapFloatingLegRegion(), floatingLegConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingRateId,
            FloatingRateType.IBOR);
      } else {
        calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, underlyingConvention.getRegion());
        final ExternalId underlyingTicker = Iterables.getOnlyElement(underlyingConvention.getIdentifiers());
        iborLeg = new FloatingInterestRateLeg(underlyingConvention.getDayCount(), PeriodFrequency.of(underlyingConvention.getPeriod()),
            underlyingConvention.getRegion(), underlyingConvention.getBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, underlyingTicker,
            FloatingRateType.IBOR);
      }
    }
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, fixedLegConvention.getSwapFixedLegSettlementDays(), calendar);
    final ZonedDateTime maturityDate = spotDate.plus(strip.getMaturity().getPeriod());
    final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(fixedLegConvention.getSwapFixedLegDayCount(), fixedLegConvention.getSwapFixedLegFrequency(),
        fixedLegConvention.getSwapFixedLegRegion(), fixedLegConvention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, rate);
    final SwapSecurity swap = new SwapSecurity(curveDate, spotDate, maturityDate, counterparty, iborLeg, fixedLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getBRLSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.getDataPoint(swapIdentifier);
    if (rate == null) {
      throw new OpenGammaRuntimeException("No market data for " + swapIdentifier);
    }
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final ConventionBundle convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, "BRL_DI_SWAP"));
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, convention.getSwapFloatingLegRegion());
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, convention.getSwapFixedLegSettlementDays(), calendar);
    final ZonedDateTime maturityDate = spotDate.plus(strip.getMaturity().getPeriod());
    final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(convention.getSwapFixedLegDayCount(), convention.getSwapFixedLegFrequency(),
        convention.getSwapFixedLegRegion(), convention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, rate);
    final FloatingInterestRateLeg iborLeg = new FloatingInterestRateLeg(convention.getSwapFloatingLegDayCount(), convention.getSwapFloatingLegFrequency(),
        convention.getSwapFloatingLegRegion(), convention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, convention.getSwapFloatingLegInitialRate(),
        FloatingRateType.OIS); //convention type is wrong but it's ignored in the converter anyway.
    final String counterparty = "";
    final SwapSecurity swap = new SwapSecurity(curveDate, spotDate, maturityDate, counterparty, iborLeg, fixedLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity get28DaySwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.getDataPoint(swapIdentifier);
    if (rate == null) {
      throw new OpenGammaRuntimeException("No market data for " + swapIdentifier);
    }
    final ConventionBundle fixedLegConvention =
        _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_28D_SWAP"));
    if (fixedLegConvention == null) {
      throw new OpenGammaRuntimeException("Could not get fixed leg convention for " + swapIdentifier);
    }
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final String counterparty = "";
    Calendar calendar;
    ExternalId floatingRateId;
    FloatingInterestRateLeg iborLeg;
    final ExternalId underlyingId = getUnderlyingId(spec, strip);
    if (underlyingId == null) {
      s_logger.info("Could not get convention for underlying from {}; trying tenor-based convention", strip);
      final ExternalId id = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_28D_SWAP");
      final ConventionBundle floatingLegConvention = _conventionBundleSource.getConventionBundle(id);
      if (floatingLegConvention == null) {
        throw new OpenGammaRuntimeException("Could not get floating leg convention for swap strip " + strip);
      }
      calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, floatingLegConvention.getSwapFloatingLegRegion());
      floatingRateId = floatingLegConvention.getSwapFloatingLegInitialRate();
      if (floatingRateId == null) {
        throw new OpenGammaRuntimeException("Could not get floating rate id from convention");
      }
      iborLeg = new FloatingInterestRateLeg(floatingLegConvention.getSwapFloatingLegDayCount(), floatingLegConvention.getSwapFloatingLegFrequency(),
          floatingLegConvention.getSwapFloatingLegRegion(), floatingLegConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingRateId,
          FloatingRateType.IBOR);
    } else {
      final ConventionBundle underlyingConvention = _conventionBundleSource.getConventionBundle(underlyingId);
      if (underlyingConvention == null || underlyingConvention.getIdentifiers().size() != 1) {
        s_logger.info("Could not get unique convention for underlying from {}; trying tenor-based convention", strip);
        final ConventionBundle floatingLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_28D_SWAP"));
        if (floatingLegConvention == null) {
          throw new OpenGammaRuntimeException("Could not get floating leg convention for swap strip " + strip);
        }
        calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, floatingLegConvention.getSwapFloatingLegRegion());
        floatingRateId = floatingLegConvention.getSwapFloatingLegInitialRate();
        if (floatingRateId == null) {
          throw new OpenGammaRuntimeException("Could not get floating rate id from convention");
        }
        iborLeg = new FloatingInterestRateLeg(floatingLegConvention.getSwapFloatingLegDayCount(), floatingLegConvention.getSwapFloatingLegFrequency(),
            floatingLegConvention.getSwapFloatingLegRegion(), floatingLegConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingRateId,
            FloatingRateType.IBOR);
      } else {
        calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, underlyingConvention.getRegion());
        final ExternalId underlyingTicker = Iterables.getOnlyElement(underlyingConvention.getIdentifiers());
        iborLeg = new FloatingInterestRateLeg(underlyingConvention.getDayCount(), PeriodFrequency.of(underlyingConvention.getPeriod()),
            underlyingConvention.getRegion(), underlyingConvention.getBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, underlyingTicker,
            FloatingRateType.IBOR);
      }
    }
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, fixedLegConvention.getSwapFixedLegSettlementDays(), calendar);
    final ZonedDateTime maturityDate = spotDate.plus(strip.getMaturity().getPeriod());
    final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(fixedLegConvention.getSwapFixedLegDayCount(), fixedLegConvention.getSwapFixedLegFrequency(),
        fixedLegConvention.getSwapFixedLegRegion(), fixedLegConvention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, rate);
    final SwapSecurity swap = new SwapSecurity(curveDate, spotDate, maturityDate, counterparty, iborLeg, fixedLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getBasisSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final FixedIncomeStrip fixedIncomeStrip = strip.getStrip();
    final IndexType payIndexType = fixedIncomeStrip.getPayIndexType();
    final Tenor payTenor = fixedIncomeStrip.getPayTenor();
    final IndexType receiveIndexType = fixedIncomeStrip.getReceiveIndexType();
    final Tenor receiveTenor = fixedIncomeStrip.getReceiveTenor();
    final String ccy = spec.getCurrency().getCode();
    final ExternalId payFloatingReferenceRateId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, payIndexType + "_" + ccy + "_" + payTenor.getPeriod().toString());
    final ExternalId receiveFloatingReferenceRateId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, receiveIndexType + "_" + ccy + "_" + receiveTenor.getPeriod().toString());
    final ConventionBundle payConvention = _conventionBundleSource.getConventionBundle(payFloatingReferenceRateId);
    final ConventionBundle receiveConvention = _conventionBundleSource.getConventionBundle(receiveFloatingReferenceRateId);
    final Calendar payCalendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, payConvention.getRegion());
    final ZonedDateTime paySpotDate = ScheduleCalculator.getAdjustedDate(curveDate, payConvention.getSettlementDays(), payCalendar);
    final ZonedDateTime payMaturityDate = paySpotDate.plus(strip.getMaturity().getPeriod());
    final String counterparty = "";
    final InterestRateNotional notional = new InterestRateNotional(spec.getCurrency(), 1);
    final ExternalId payRegionIdentifier = payConvention.getRegion();
    final DayCount payDayCount = payConvention.getDayCount();
    final Frequency payFrequency = PeriodFrequency.of(fixedIncomeStrip.getPayTenor().getPeriod());
    final BusinessDayConvention payBusinessDayConvention = payConvention.getBusinessDayConvention();
    final FloatingRateType payFloatingRateType = getFloatingTypeFromIndexType(fixedIncomeStrip.getPayIndexType());
    final ExternalId receiveRegionIdentifier = receiveConvention.getRegion();
    final DayCount receiveDayCount = receiveConvention.getDayCount();
    final Frequency receiveFrequency = PeriodFrequency.of(fixedIncomeStrip.getReceiveTenor().getPeriod());
    final BusinessDayConvention receiveBusinessDayConvention = receiveConvention.getBusinessDayConvention();
    final FloatingRateType receiveFloatingRateType = getFloatingTypeFromIndexType(fixedIncomeStrip.getReceiveIndexType());
    final double spread = marketValues.getDataPoint(swapIdentifier);
    // Implementation note: By convention the spread is on the first leg (shorter tenor)
    final FloatingSpreadIRLeg payLeg = new FloatingSpreadIRLeg(payDayCount, payFrequency, payRegionIdentifier, payBusinessDayConvention, notional, false, payFloatingReferenceRateId,
        payFloatingRateType, spread);
    final FloatingInterestRateLeg receiveLeg = new FloatingInterestRateLeg(receiveDayCount, receiveFrequency, receiveRegionIdentifier, receiveBusinessDayConvention, notional, false,
        receiveFloatingReferenceRateId, receiveFloatingRateType);
    //TODO don't use pay spot date and maturity date automatically
    final SwapSecurity swap = new SwapSecurity(curveDate, paySpotDate, payMaturityDate, counterparty, payLeg, receiveLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getTenorSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final ExternalId swapIdentifier = strip.getSecurity();
    final Double rate = marketValues.getDataPoint(swapIdentifier);
    final LocalDate curveDate = spec.getCurveDate();
    final ZonedDateTime tradeDate = curveDate.atTime(11, 00).atZone(ZoneOffset.UTC);
    final ZonedDateTime effectiveDate = DateUtils.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(ZoneOffset.UTC);
    final ZonedDateTime maturityDate = curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(ZoneOffset.UTC);
    final ConventionBundle convention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_TENOR_SWAP"));
    final String counterparty = "";
    final ConventionBundle payLegFloatRateConvention = _conventionBundleSource.getConventionBundle(convention.getBasisSwapPayFloatingLegInitialRate());
    final ConventionBundle receiveLegFloatRateConvention = _conventionBundleSource.getConventionBundle(convention.getBasisSwapReceiveFloatingLegInitialRate());
    final ExternalId payLegFloatRateBloombergTicker = payLegFloatRateConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    final ExternalId receiveLegFloatRateBloombergTicker = receiveLegFloatRateConvention.getIdentifiers().getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
    if (rate == null) {
      throw new OpenGammaRuntimeException("Could not get spread; was trying " + swapIdentifier);
    }
    final double spread = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.

    final SwapSecurity swap = new SwapSecurity(tradeDate, effectiveDate, maturityDate, counterparty, new FloatingInterestRateLeg(convention.getBasisSwapPayFloatingLegDayCount(),
        convention.getBasisSwapPayFloatingLegFrequency(), convention.getBasisSwapPayFloatingLegRegion(), convention.getBasisSwapPayFloatingLegBusinessDayConvention(), new InterestRateNotional(
            spec.getCurrency(), 1), false, payLegFloatRateBloombergTicker, FloatingRateType.IBOR), new FloatingSpreadIRLeg(convention.getBasisSwapReceiveFloatingLegDayCount(),
                convention.getBasisSwapReceiveFloatingLegFrequency(), convention.getBasisSwapReceiveFloatingLegRegion(), convention.getBasisSwapReceiveFloatingLegBusinessDayConvention(),
                new InterestRateNotional(spec.getCurrency(), 1), false, receiveLegFloatRateBloombergTicker, FloatingRateType.IBOR, spread));
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private SwapSecurity getOISSwap(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final FixedIncomeStrip underlyingStrip = strip.getStrip();
    final ExternalId swapIdentifier = strip.getSecurity();
    final ConventionBundle swapConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_OIS_SWAP"));
    if (swapConvention == null) {
      throw new OpenGammaRuntimeException("Could not get convention for id " + ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getCode() + "_OIS_SWAP"));
    }
    if (!swapConvention.getSwapFloatingLegFrequency().equals(swapConvention.getSwapFixedLegFrequency())) {
      throw new OpenGammaRuntimeException("Payment frequencies for the fixed and floating legs did not match");
    }
    final Calendar calendar = CalendarUtils.getCalendar(_regionSource, _holidaySource, swapConvention.getSwapFloatingLegRegion());
    final ZonedDateTime curveDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime spotDate = ScheduleCalculator.getAdjustedDate(curveDate, swapConvention.getSwapFixedLegSettlementDays(), calendar);
    final ZonedDateTime maturityDate = spotDate.plus(strip.getMaturity().getPeriod());
    final String counterparty = "";
    final Double rate = marketValues.getDataPoint(swapIdentifier);
    if (rate == null) {
      throw new OpenGammaRuntimeException("rate was null on " + strip + " from " + spec);
    }
    Frequency floatingFrequency;
    final ExternalId floatingReferenceRateId;
    if (underlyingStrip.getResetTenor() != null) {
      final Period resetTenor = underlyingStrip.getResetTenor().getPeriod();
      floatingFrequency = PeriodFrequency.of(resetTenor);
      final IndexType indexType = underlyingStrip.getIndexType();
      floatingReferenceRateId = ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, indexType + "_" + resetTenor.toString());
    } else {
      floatingFrequency = swapConvention.getSwapFloatingLegFrequency();
      if (floatingFrequency == null) {
        throw new OpenGammaRuntimeException("Could not get floating leg frequency from convention");
      }
      floatingReferenceRateId = swapConvention.getSwapFloatingLegInitialRate();
      if (floatingReferenceRateId == null) {
        throw new OpenGammaRuntimeException("Could not get floating reference rate from convention");
      }
    }
    final FloatingInterestRateLeg oisLeg = new FloatingInterestRateLeg(swapConvention.getSwapFloatingLegDayCount(), floatingFrequency,
        swapConvention.getSwapFloatingLegRegion(), swapConvention.getSwapFloatingLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, floatingReferenceRateId,
        FloatingRateType.OIS);
    final FixedInterestRateLeg fixedLeg = new FixedInterestRateLeg(swapConvention.getSwapFixedLegDayCount(), swapConvention.getSwapFixedLegFrequency(), swapConvention.getSwapFixedLegRegion(),
        swapConvention.getSwapFixedLegBusinessDayConvention(), new InterestRateNotional(spec.getCurrency(), 1), false, rate);
    final SwapSecurity swap = new SwapSecurity(curveDate, spotDate, maturityDate, counterparty, oisLeg, fixedLeg);
    swap.setExternalIdBundle(ExternalIdBundle.of(swapIdentifier));
    return swap;
  }

  private static PeriodicZeroDepositSecurity getPeriodicZeroDeposit(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip, final SnapshotDataBundle marketValues) {
    final ExternalId id = strip.getSecurity();
    final Currency currency = spec.getCurrency();
    final ZonedDateTime startDate = spec.getCurveDate().atStartOfDay(ZoneOffset.UTC);
    final ZonedDateTime maturityDate = startDate.plus(strip.getMaturity().getPeriod());
    final double rate = marketValues.getDataPoint(id);
    final int compoundingPeriodsPerYear = strip.getStrip().getPeriodsPerYear();
    final ExternalId region = spec.getRegion();
    final PeriodicZeroDepositSecurity deposit = new PeriodicZeroDepositSecurity(currency, startDate, maturityDate, rate, compoundingPeriodsPerYear, region);
    deposit.setExternalIdBundle(ExternalIdBundle.of(id));
    return deposit;
  }

  private static ZoneId ensureZone(final ZoneId zone) {
    if (zone != null) {
      return zone;
    }
    return ZoneOffset.UTC;
  }

  private static FloatingRateType getFloatingTypeFromIndexType(final IndexType indexType) {
    switch (indexType) {
      case Libor:
        return FloatingRateType.IBOR;
      case Euribor:
        return FloatingRateType.IBOR;
      case BBSW:
        return FloatingRateType.IBOR;
    }
    throw new OpenGammaRuntimeException("Cannot handle index type " + indexType);
  }

  private static ExternalId getUnderlyingId(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip) {
    if (strip.getStrip().getIndexType() == null) {
      return null;
    }
    final String tenorString = strip.getStrip().getResetTenor().getPeriod().toTotalMonths() + "m";
    final String fixingRateName = spec.getCurrency().getCode() + " " + strip.getStrip().getIndexType().name().toUpperCase() + " " + tenorString;
    return ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, fixingRateName);
  }

  private ConventionBundle getFixedLegConvention(final InterpolatedYieldCurveSpecification spec, final FixedIncomeStripWithIdentifier strip,
      final ExternalId swapIdentifier) {
    ConventionBundle fixedLegConvention;
    switch (strip.getStrip().getInstrumentType()) {
      case SWAP_3M:
        fixedLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_3M_SWAP"));
        if (fixedLegConvention != null) {
          return fixedLegConvention;
        }
      case SWAP_6M:
        fixedLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_6M_SWAP"));
        if (fixedLegConvention != null) {
          return fixedLegConvention;
        }
      case SWAP_12M:
        fixedLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_12M_SWAP"));
        if (fixedLegConvention != null) {
          return fixedLegConvention;
        }
      default:
        fixedLegConvention = _conventionBundleSource.getConventionBundle(ExternalId.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME,
            spec.getCurrency().getCode() + "_SWAP"));
        if (fixedLegConvention != null) {
          return fixedLegConvention;
        }
    }
    throw new OpenGammaRuntimeException("Could not get fixed leg convention for " + swapIdentifier);
  }

}
