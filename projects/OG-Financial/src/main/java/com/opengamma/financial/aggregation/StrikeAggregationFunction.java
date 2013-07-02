/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Function that aggregates by option strike.
 */
public class StrikeAggregationFunction implements AggregationFunction<String> {
  /** The name of the aggregator */
  private static final String NAME = "Strike";
  /** Label for trades to which this aggregator does not apply */
  private static final String NA = "N/A";
  /** The comparator */
  private static final Comparator<Position> COMPARATOR = new SimplePositionComparator();
  /** The security source */
  private final SecuritySource _securitySource;

  /**
   * @param securitySource The security source, not null
   */
  public StrikeAggregationFunction(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _securitySource = securitySource;
  }

  @Override
  public int compare(final String arg0, final String arg1) {
    try {
      final Double d0 = Double.parseDouble(arg0);
      final Double d1 = Double.parseDouble(arg1);
      return Double.compare(d0, d1);
    } catch (final NumberFormatException e) {
      return arg0.compareTo(arg1);
    }
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptySet();
  }

  @Override
  public String classifyPosition(final Position position) {
    position.getSecurityLink().resolve(_securitySource);
    final FinancialSecurityVisitor<String> visitor = new FinancialSecurityVisitorSameValueAdapter<String>(NA) {

      @Override
      public String visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
        return security.getStrike().toString();
      }

      @Override
      public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitCapFloorSecurity(final CapFloorSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
        return NA; //TODO
      }

      @Override
      public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
        return NA; //TODO
      }

      @Override
      public String visitFXOptionSecurity(final FXOptionSecurity security) {
        return NA; //TODO
      }

      @Override
      public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
        return Double.toString(security.getStrike());
      }

      @Override
      public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
        return NA; //TODO
      }

      @Override
      public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
        return NA; //TODO
      }

      @Override
      public String visitSwaptionSecurity(final SwaptionSecurity security) {
        return NA; //TODO
      }

    };
    if (position.getSecurity() instanceof FinancialSecurity) {
      final FinancialSecurity security = (FinancialSecurity) position.getSecurity();
      return security.accept(visitor);
    }
    return NA;
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return COMPARATOR;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
