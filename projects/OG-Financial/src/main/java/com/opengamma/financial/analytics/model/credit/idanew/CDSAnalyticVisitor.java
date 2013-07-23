package com.opengamma.financial.analytics.model.credit.idanew;

import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.StubType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.isdanew.CDSAnalytic;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.id.ExternalId;

/**
 * Creates a {@link CDSAnalytic} object from the security
 * Throws an exception if the security cannot be converted.
 */
public class CDSAnalyticVisitor extends FinancialSecurityVisitorAdapter<CDSAnalytic> {

  private final LocalDate _valuationDate;
  private final HolidaySource _holidaySource;
  private final RegionSource _regionSource;
  private final LocalDate _startDate;
  private final LocalDate _maturityDate;

  public CDSAnalyticVisitor(final LocalDate valuationDate, final HolidaySource holidaySource, final RegionSource regionSource) {
    _valuationDate = valuationDate;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _startDate = null;
    _maturityDate = null;
  }

  /**
   * Used if start and maturity dates should be fixed to a value different to that of the cds. e.g. when creating instruments for a credit curve
   * @param valuationDate
   * @param holidaySource
   * @param regionSource
   * @param startDate
   * @param maturityDate
   */
  public CDSAnalyticVisitor(final LocalDate valuationDate, final HolidaySource holidaySource, final RegionSource regionSource,
                            final LocalDate startDate, final LocalDate maturityDate) {
    _valuationDate = valuationDate;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _startDate = startDate;
    _maturityDate = maturityDate;
  }

  @Override
  public CDSAnalytic visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    final ExternalId regionId = security.getRegionId();
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, _regionSource.getHighestLevelRegion(regionId));
    final StubType stubType = security.getStubType().toAnalyticsType();
    final CDSAnalytic cdsAnalytic = new CDSAnalytic(_valuationDate,
                                                    security.getEffectiveDate().toLocalDate(),
                                                    _valuationDate.plusDays(3), //FIXME: Hard code or get from somewhere else?
                                                    _startDate == null ? security.getStartDate().toLocalDate() : _startDate,
                                                    _maturityDate == null ? security.getMaturityDate().toLocalDate() : _maturityDate,
                                                    true, //FIXME: Do we have this info anywhere?
                                                    getPeriodFrequency(security.getCouponFrequency()).getPeriod(),
                                                    stubType,
                                                    security.isProtectionStart(),
                                                    security.getRecoveryRate(),
                                                    security.getBusinessDayConvention(),
                                                    calendar,
                                                    security.getDayCount()
    );
    return cdsAnalytic;
  }

  private PeriodFrequency getPeriodFrequency(final Frequency frequency) {
    if (frequency instanceof PeriodFrequency) {
      return (PeriodFrequency) frequency;
    }
    if (frequency instanceof SimpleFrequency) {
      return ((SimpleFrequency) frequency).toPeriodFrequency();
    }
    throw new OpenGammaRuntimeException("Can only handle PeriodFrequency and SimpleFrequency");
  }

}
