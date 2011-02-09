/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.yield.YieldConventionFactory;
import com.opengamma.financial.instrument.bond.BondConvention;
import com.opengamma.financial.instrument.bond.BondDefinition;
import com.opengamma.financial.instrument.bond.BondForwardDefinition;
import com.opengamma.financial.instrument.bond.BondFutureDefinition;
import com.opengamma.financial.instrument.cash.CashDefinition;
import com.opengamma.financial.instrument.fra.FRADefinition;
import com.opengamma.financial.instrument.irfuture.IRFutureConvention;
import com.opengamma.financial.instrument.irfuture.IRFutureDefinition;
import com.opengamma.financial.instrument.swap.FixedSwapLegDefinition;
import com.opengamma.financial.instrument.swap.FloatingSwapLegDefinition;
import com.opengamma.financial.instrument.swap.SwapConvention;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FixedIncomeInstrumentDefinitionVisitorTest {
  private static final DayCount DC = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ICMA");
  private static final BusinessDayConvention BD = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Following");
  private static final Calendar C = new MondayToFridayCalendar("F");
  private static final BondConvention BOND_CONVENTION = new BondConvention(2, DC, BD, C, false, "A", 0, YieldConventionFactory.INSTANCE.getYieldConvention("US Treasury"));
  private static final SwapConvention SWAP_CONVENTION = new SwapConvention(2, DC, BD, C, false, "A");
  private static final IRFutureConvention IRF_CONVENTION = new IRFutureConvention(2, DC, BD, C, 0.25, "A");
  private static final LocalDate[] LOCAL_DATES = new LocalDate[] {LocalDate.of(2011, 1, 1), LocalDate.of(2012, 1, 1)};
  private static final ZonedDateTime[] ZONED_DATES = new ZonedDateTime[] {DateUtil.getUTCDate(2011, 1, 1)};
  private static final BondDefinition BOND = new BondDefinition(LOCAL_DATES, LOCAL_DATES, 0.02, 1, BOND_CONVENTION);
  private static final BondForwardDefinition BOND_FORWARD = new BondForwardDefinition(BOND, LocalDate.of(2011, 7, 1), BOND_CONVENTION);
  private static final BondFutureDefinition BOND_FUTURE = new BondFutureDefinition(new BondDefinition[] {BOND}, new double[] {1}, BOND_CONVENTION, LocalDate.of(2010, 1, 1));
  private static final CashDefinition CASH = new CashDefinition(DateUtil.getUTCDate(2011, 1, 1), 0.04, BOND_CONVENTION);
  private static final FixedSwapLegDefinition FIXED_SWAP_LEG = new FixedSwapLegDefinition(ZONED_DATES[0], ZONED_DATES, ZONED_DATES, 100, 0.03, SWAP_CONVENTION);
  private static final FloatingSwapLegDefinition FLOATING_SWAP_LEG = new FloatingSwapLegDefinition(ZONED_DATES[0], ZONED_DATES, ZONED_DATES, ZONED_DATES, ZONED_DATES, 100, 0.04, SWAP_CONVENTION);
  private static final FRADefinition FRA = new FRADefinition(DateUtil.getUTCDate(2011, 1, 1), DateUtil.getUTCDate(2012, 1, 1), 0.02, SWAP_CONVENTION);
  private static final IRFutureDefinition IR_FUTURE = new IRFutureDefinition(DateUtil.getUTCDate(2011, 1, 1), DateUtil.getUTCDate(2011, 4, 1), 95, IRF_CONVENTION);
  @SuppressWarnings("synthetic-access")
  private static final MyVisitor<Object, String> VISITOR = new MyVisitor<Object, String>();

  @Test
  public void test() {
    final Object o = "G";
    assertEquals(BOND.accept(VISITOR), "Bond2");
    assertEquals(BOND.accept(VISITOR, o), "Bond1");
    assertEquals(BOND_FORWARD.accept(VISITOR), "BondForward2");
    assertEquals(BOND_FORWARD.accept(VISITOR, o), "BondForward1");
    assertEquals(BOND_FUTURE.accept(VISITOR), "BondFuture2");
    assertEquals(BOND_FUTURE.accept(VISITOR, o), "BondFuture1");
    assertEquals(CASH.accept(VISITOR), "Cash2");
    assertEquals(CASH.accept(VISITOR, o), "Cash1");
    assertEquals(FIXED_SWAP_LEG.accept(VISITOR), "FixedSwapLeg2");
    assertEquals(FIXED_SWAP_LEG.accept(VISITOR, o), "FixedSwapLeg1");
    assertEquals(FLOATING_SWAP_LEG.accept(VISITOR), "FloatingSwapLeg2");
    assertEquals(FLOATING_SWAP_LEG.accept(VISITOR, o), "FloatingSwapLeg1");
    assertEquals(FRA.accept(VISITOR), "FRA2");
    assertEquals(FRA.accept(VISITOR, o), "FRA1");
    assertEquals(IR_FUTURE.accept(VISITOR), "IRFuture2");
    assertEquals(IR_FUTURE.accept(VISITOR, o), "IRFuture1");
  }

  private static class MyVisitor<T, U> implements FixedIncomeInstrumentDefinitionVisitor<T, String> {

    @Override
    public String visit(final FixedIncomeInstrumentDefinition<?> definition, final T data) {
      return definition.accept(this, data);
    }

    @Override
    public String visit(final FixedIncomeInstrumentDefinition<?> definition) {
      return definition.accept(this);
    }

    @Override
    public String visitBondDefinition(final BondDefinition bond, final T data) {
      return "Bond1";
    }

    @Override
    public String visitBondDefinition(final BondDefinition bond) {
      return "Bond2";
    }

    @Override
    public String visitBondForwardDefinition(final BondForwardDefinition bondForward, final T data) {
      return "BondForward1";
    }

    @Override
    public String visitBondForwardDefinition(final BondForwardDefinition bondForward) {
      return "BondForward2";
    }

    @Override
    public String visitBondFutureDefinition(final BondFutureDefinition bondFuture, final T data) {
      return "BondFuture1";
    }

    @Override
    public String visitBondFutureDefinition(final BondFutureDefinition bondFuture) {
      return "BondFuture2";
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash, final T data) {
      return "Cash1";
    }

    @Override
    public String visitCashDefinition(final CashDefinition cash) {
      return "Cash2";
    }

    @Override
    public String visitFixedSwapLegDefinition(final FixedSwapLegDefinition fixedSwapLeg, final T data) {
      return "FixedSwapLeg1";
    }

    @Override
    public String visitFixedSwapLegDefinition(final FixedSwapLegDefinition fixedSwapLeg) {
      return "FixedSwapLeg2";
    }

    @Override
    public String visitFloatingSwapLegDefinition(final FloatingSwapLegDefinition floatingSwapLeg, final T data) {
      return "FloatingSwapLeg1";
    }

    @Override
    public String visitFloatingSwapLegDefinition(final FloatingSwapLegDefinition floatingSwapLeg) {
      return "FloatingSwapLeg2";
    }

    @Override
    public String visitFRADefintion(final FRADefinition fra, final T data) {
      return "FRA1";
    }

    @Override
    public String visitFRADefinition(final FRADefinition fra) {
      return "FRA2";
    }

    @Override
    public String visitIRFutureDefinition(final IRFutureDefinition irFuture, final T data) {
      return "IRFuture1";
    }

    @Override
    public String visitIRFutureDefinition(final IRFutureDefinition irFuture) {
      return "IRFuture2";
    }

  }
}
