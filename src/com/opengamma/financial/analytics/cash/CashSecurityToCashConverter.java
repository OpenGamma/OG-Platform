/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.cash;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.cash.definition.Cash;
import com.opengamma.financial.security.cash.CashSecurity;

/**
 * 
 */
public class CashSecurityToCashConverter {
//TODO all of this
  public static Cash getCash(CashSecurity security, String fundingCurveName, double marketRate, Calendar calendar, ZonedDateTime now) {
    return null;
//    ZonedDateTime startDate = security.getBusinessDayConvention().adjustDate(getStartDate(security, now)); 
//    double tradeTime = getTradeTime(security);
//    ZonedDateTime maturityDate = security.getBusinessDayConvention().adjustDate(security.getMaturity()); 
//    double paymentTime = getMaturityTime(maturityDate, now); 
//    double yearFraction = security.getDaycount().getDaycount(startDate, maturityDate);    
//    return new Cash(tradeTime, marketRate, paymentTime, yearFraction, fundingCurveName);
  }
  
  private static ZonedDateTime getStartDate(CashSecurity security, ZonedDateTime now) {
    return null;
//    if (security.getTenor() == "O/N" || security.getTenor() == "T/N") {
//      return now;
//    }
//    return now.plusDays(security.getConvention().getSettlementDays());
  }
  
  private static double getTradeTime(CashSecurity security, ZonedDateTime now) {
    return 0;
//    if (security.getTenor() == "O/N" || security.getTenor() == "T/N") {
//      return 0;
//    } 
//    DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual");
//    return actAct.getDayCountFraction(now, security.getRegion().getConvention().getSettlementDays());
  }
  
  private static double getMaturityTime(ZonedDateTime maturity, ZonedDateTime now) {
    return 0;
//    DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual");
//    return actAct.getDayCountFraction(now, maturity);    
  }
}
