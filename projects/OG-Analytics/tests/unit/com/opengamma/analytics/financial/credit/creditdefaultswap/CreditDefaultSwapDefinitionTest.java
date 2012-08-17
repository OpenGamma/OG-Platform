/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap;

import com.opengamma.util.money.Currency;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * 
 */
public class CreditDefaultSwapDefinitionTest {
  
  final String buysellprotection = "Buy";
  
  final String protectionbuyer = "ABC";
  final String protectionseller = "XYZ";
  final String referenceentity = "C";
  
  private static final Currency currency = Currency.USD;
  
  final String debtseniority = "Senior";
  final String restructuringclause = "NR";
     
  private static final Calendar calendar = new MondayToFridayCalendar("A");
  
  final double notional = 10000000.0;
  final double parspread = 60.0;
  
  final double valuationrecoveryrate = 0.40;
  final double curverecoveryrate = 0.40;
  
  final boolean includeaccruedpremium = true;
  
//private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  //private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
}