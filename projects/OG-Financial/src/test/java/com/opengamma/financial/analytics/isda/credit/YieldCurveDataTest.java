package com.opengamma.financial.analytics.isda.credit;

import java.util.SortedMap;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableSortedMap;
import com.opengamma.financial.analytics.isda.credit.YieldCurveData.Builder;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Tests the custom constructor
 */
public class YieldCurveDataTest {
  
  private Builder _builder;

  @BeforeMethod
  public void beforeMethod() {
    _builder = YieldCurveData.builder()
      .calendar(new MondayToFridayCalendar("test"))
      .cashDayCount(DayCounts.ACT_360)
      .currency(Currency.USD)
      .curveBusinessDayConvention(BusinessDayConventions.MODIFIED_FOLLOWING)
      .curveDayCount(DayCounts.ACT_365)
      .spotDate(LocalDate.of(2014, 1, 1))
      .swapDayCount(DayCounts.THIRTY_360)
      .swapFixedLegInterval(Tenor.ONE_YEAR);

  }
  
  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testTenorCollision() {
    SortedMap<Tenor, Double> cashData = ImmutableSortedMap.of(Tenor.ONE_MONTH, 0.00445);

    SortedMap<Tenor, Double> swapData = ImmutableSortedMap.of(Tenor.ONE_MONTH, 0.01652);
    
    _builder.cashData(cashData).swapData(swapData);
    
    _builder.build();

  }
  
}
