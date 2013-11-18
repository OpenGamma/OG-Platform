package com.opengamma.financial.analytics.model.credit.isdanew;

import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.credit.isdastandardmodel.CDSAnalytic;
import com.opengamma.analytics.financial.credit.isdastandardmodel.StubType;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
import com.opengamma.financial.analytics.model.credit.IMMDateGenerator;
import com.opengamma.financial.convention.HolidaySourceCalendarAdapter;
import com.opengamma.financial.convention.businessday.BusinessDayDateUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.frequency.SimpleFrequency;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
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
  private final double _recoveryRate;

  public CDSAnalyticVisitor(final LocalDate valuationDate, final HolidaySource holidaySource, final RegionSource regionSource, final double recoveryRate) {
    _valuationDate = valuationDate;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _startDate = null;
    _maturityDate = null;
    _recoveryRate = recoveryRate;
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
                            final LocalDate startDate, final LocalDate maturityDate, final double recoveryRate) {
    _valuationDate = valuationDate;
    _holidaySource = holidaySource;
    _regionSource = regionSource;
    _startDate = startDate;
    _maturityDate = maturityDate;
    _recoveryRate = recoveryRate;
  }

  @Override
  public CDSAnalytic visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getNotional().getCurrency());
    final StubType stubType = security.getStubType().toAnalyticsType();
    final Period period = (IMMDateGenerator.isIMMDate(security.getMaturityDate())) ? getPeriodFrequency(security.getCouponFrequency()).getPeriod() :
        Period.ofMonths(6); // non IMM forced to semi annual
    final CDSAnalytic cdsAnalytic = new CDSAnalytic(_valuationDate,
                                                    security.getEffectiveDate().toLocalDate(),
                                                    // Hard code or get from somewhere?
                                                    BusinessDayDateUtils.addWorkDays(_valuationDate, 3, calendar),
                                                    _startDate == null ? security.getStartDate().toLocalDate() : _startDate,
                                                    _maturityDate == null ? security.getMaturityDate().toLocalDate() : _maturityDate,
                                                    true, // Do we have this info anywhere?
                                                    period,
                                                    stubType,
                                                    security.isProtectionStart(),
                                                    _recoveryRate,
                                                    security.getBusinessDayConvention(),
                                                    calendar,
                                                    security.getDayCount());
    return cdsAnalytic;
  }

  @Override
  public CDSAnalytic visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    final Calendar calendar = new HolidaySourceCalendarAdapter(_holidaySource, security.getNotional().getCurrency());
    final StubType stubType = security.getStubType().toAnalyticsType();
    final Period period = (IMMDateGenerator.isIMMDate(security.getMaturityDate())) ? getPeriodFrequency(security.getCouponFrequency()).getPeriod() :
        Period.ofMonths(6); // non IMM forced to semi annual
    final CDSAnalytic cdsAnalytic = new CDSAnalytic(_valuationDate,
                                                    security.getEffectiveDate().toLocalDate(),
                                                    // Hard code or get from somewhere?
                                                    BusinessDayDateUtils.addWorkDays(_valuationDate, 3, calendar),
                                                    _startDate == null ? security.getStartDate().toLocalDate() : _startDate,
                                                    _maturityDate == null ? security.getMaturityDate().toLocalDate() : _maturityDate,
                                                    true, // Do we have this info anywhere?
                                                    period,
                                                    stubType,
                                                    security.isProtectionStart(),
                                                    _recoveryRate,
                                                    security.getBusinessDayConvention(),
                                                    calendar,
                                                    security.getDayCount()
    );
    return cdsAnalytic;
  }

  public static PeriodFrequency getPeriodFrequency(final Frequency frequency) {
    if (frequency instanceof PeriodFrequency) {
      return (PeriodFrequency) frequency;
    }
    if (frequency instanceof SimpleFrequency) {
      return ((SimpleFrequency) frequency).toPeriodFrequency();
    }
    throw new OpenGammaRuntimeException("Can only handle PeriodFrequency and SimpleFrequency");
  }

}
