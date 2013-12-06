package com.opengamma.analytics.financial.instrument.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BondIborTransactionDefinitionTest {

  //Quarterly Libor6m 2Y
  private static final Currency CUR = Currency.EUR;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final String ISSUER_NAME = "Issuer";
  private static final DayCount DAY_COUNT = DayCounts.ACT_ACT_ISDA;
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventions.FOLLOWING;
  private static final boolean IS_EOM = false;
  private static final Period IBOR_TENOR = Period.ofMonths(3);
  private static final DayCount IBOR_DAY_COUNT = DayCounts.THIRTY_U_360;
  private static final int IBOR_SPOT_LAG = 2;
  private static final BusinessDayConvention IBOR_BUSINESS_DAY = BusinessDayConventions.MODIFIED_FOLLOWING;
  private static final boolean IBOR_IS_EOM = false;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, IBOR_TENOR, IBOR_SPOT_LAG, IBOR_DAY_COUNT, IBOR_BUSINESS_DAY, IBOR_IS_EOM, "Ibor");
  private static final Period BOND_TENOR = Period.ofYears(2);
  private static final int SETTLEMENT_DAYS = 3; // Standard for euro-bonds.
  private static final ZonedDateTime START_ACCRUAL_DATE = DateUtils.getUTCDate(2011, 7, 13);
  private static final ZonedDateTime MATURITY_DATE = START_ACCRUAL_DATE.plus(BOND_TENOR);
  private static final BondIborSecurityDefinition BOND_DESCRIPTION = BondIborSecurityDefinition.from(MATURITY_DATE, START_ACCRUAL_DATE, IBOR_INDEX, SETTLEMENT_DAYS, DAY_COUNT, BUSINESS_DAY, IS_EOM, ISSUER_NAME, CALENDAR);
  // Transaction
  private static final double PRICE = 0.90;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtils.getUTCDate(2011, 8, 18);
  private static final double QUANTITY = 100000000; //100m
  private static final BondIborTransactionDefinition BOND_TRANSACTION = new BondIborTransactionDefinition(BOND_DESCRIPTION, QUANTITY, SETTLEMENT_DATE, PRICE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullUnderlying() {
    new BondIborTransactionDefinition(null, QUANTITY, SETTLEMENT_DATE, PRICE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSettle() {
    new BondIborTransactionDefinition(BOND_DESCRIPTION, QUANTITY, null, PRICE);
  }

  @Test
  public void testGetters() {
    assertEquals(PRICE, BOND_TRANSACTION.getPrice());
    assertEquals(QUANTITY, BOND_TRANSACTION.getQuantity());
    assertEquals(SETTLEMENT_DATE, BOND_TRANSACTION.getSettlementDate());
    assertEquals(BOND_DESCRIPTION, BOND_TRANSACTION.getUnderlyingBond());
    assertEquals(DateUtils.getUTCDate(2011, 7, 13), BOND_TRANSACTION.getPreviousAccrualDate());
    assertEquals(DateUtils.getUTCDate(2011, 10, 13), BOND_TRANSACTION.getNextAccrualDate());
  }
}
