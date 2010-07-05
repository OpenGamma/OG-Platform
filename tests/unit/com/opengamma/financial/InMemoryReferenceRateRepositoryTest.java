/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial;

import static com.opengamma.id.IdentificationScheme.BLOOMBERG_TICKER;

import org.junit.Test;

import junit.framework.Assert;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.businessday.FollowingBusinessDayConvention;
import com.opengamma.financial.convention.businessday.ModifiedBusinessDayConvention;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyDayCount;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;


/**
 * Unit test for InMemoryReferenceRateRepository
 */
public class InMemoryReferenceRateRepositoryTest {

  @Test
  public void testRepostiory() {
    ReferenceRateRepository repo = new InMemoryReferenceRateRepository();
    ReferenceRate referenceRate = repo.getReferenceRate(Identifier.of(InMemoryReferenceRateRepository.SIMPLE_NAME_SCHEME, "LIBOR O/N"));
    BusinessDayConvention modified = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified");
    BusinessDayConvention following = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
    DayCount act360 = DayCountFactory.INSTANCE.getDayCount("Actual/360");
    Assert.assertEquals("LIBOR O/N", referenceRate.getName());
    Assert.assertEquals(IdentifierBundle.of(Identifier.of(BLOOMBERG_TICKER, "US00O/N Curncy"), Identifier.of(InMemoryReferenceRateRepository.SIMPLE_NAME_SCHEME, "LIBOR O/N")), referenceRate.getIdentifiers());
    Assert.assertEquals(UniqueIdentifier.of(InMemoryReferenceRateRepository.IN_MEMORY_UNIQUE_SCHEME.getName(), "1"), referenceRate.getUniqueIdentifier());
    Assert.assertEquals(act360, referenceRate.getDayCount());
    Assert.assertEquals(following, referenceRate.getBusinessDayConvention());
    Assert.assertEquals(2, referenceRate.getSettlementDays());
    
    ReferenceRate referenceRate2 = repo.getReferenceRate(Identifier.of(InMemoryReferenceRateRepository.SIMPLE_NAME_SCHEME, "LIBOR 3m"));
    Assert.assertEquals("LIBOR 3m", referenceRate2.getName());
    Assert.assertEquals(IdentifierBundle.of(Identifier.of(BLOOMBERG_TICKER, "US0003M Curncy"), Identifier.of(InMemoryReferenceRateRepository.SIMPLE_NAME_SCHEME, "LIBOR 3m")), referenceRate2.getIdentifiers());
    Assert.assertEquals(UniqueIdentifier.of(InMemoryReferenceRateRepository.IN_MEMORY_UNIQUE_SCHEME.getName(), "6"), referenceRate2.getUniqueIdentifier());
    Assert.assertEquals(act360, referenceRate2.getDayCount());
    Assert.assertEquals(modified, referenceRate2.getBusinessDayConvention());
    Assert.assertEquals(2, referenceRate2.getSettlementDays());
  }
}
