/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Management interface for a reference rate repository implementation
 */
public interface ManageableReferenceRateRepository extends ReferenceRateRepository {

  UniqueIdentifier addReferenceRate(IdentifierBundle bundle, String name, DayCount dayCount, BusinessDayConvention businessDayConvention, int settlementDays);

}
