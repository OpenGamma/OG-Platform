/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fra;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.interestrate.fra.definition.ForwardRateAgreement;
import com.opengamma.financial.security.fra.FRASecurity;

/**
 * 
 */
public class FRASecuityToForwardRateAgreementConverter {

  public static ForwardRateAgreement getFRA(FRASecurity security, String fundingCurveName, String indexCurveName, double marketRate, Calendar calendar, ZonedDateTime now){
    
    
   //TODO fixing date is normally two days before settlementDate - this needs to be in FRASecurity or have some logic to handle it
    ZonedDateTime fixingDate = security.getStartDate().toZonedDateTime();
    ZonedDateTime settlementDate = security.getStartDate().toZonedDateTime();
    ZonedDateTime maturityDate = security.getEndDate().toZonedDateTime();
    
    DayCount dayCount = DayCountFactory.INSTANCE.getDayCount("Actual/360"); //TODO security.getDaycount();
    
  //all times on discount/yield/forward curves are measured ACT/ACT
    double fixingTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, fixingDate);
    double settlementTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, settlementDate);
    double maturityTime = DayCountFactory.INSTANCE.getDayCount("Actual/Actual").getDayCountFraction(now, maturityDate);
    
    double forwardYearFraction = dayCount.getDayCountFraction(fixingDate, maturityDate);
    double discountingYearFraction = dayCount.getDayCountFraction(settlementDate, maturityDate);
    
    return new ForwardRateAgreement(settlementTime, maturityTime, fixingTime, forwardYearFraction, discountingYearFraction, marketRate, fundingCurveName, indexCurveName);
  }
  
}
