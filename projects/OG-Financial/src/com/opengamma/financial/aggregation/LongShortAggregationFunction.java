/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.util.CompareUtils;

/**
 * Function to classify positions by Currency.
 *
 */
public class LongShortAggregationFunction implements AggregationFunction<String> {
  private final boolean _useAttributes;

  private static final String NAME = "Long/Short";
  private static final String NOT_LONG_SHORT = "N/A";
  private static final String LONG = "Long";
  private static final String SHORT = "Short";

  private static final List<String> REQUIRED = Arrays.asList(LONG, SHORT, NOT_LONG_SHORT);
  private final Comparator<Position> _comparator = new PositionComparator();
  private final SecuritySource _secSource;

  public LongShortAggregationFunction(final SecuritySource secSource) {
    this(secSource, false);
  }

  public LongShortAggregationFunction(final SecuritySource secSource, final boolean useAttributes) {
    _secSource = secSource;
    _useAttributes = useAttributes;
  }

  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      final Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NOT_LONG_SHORT;
      }
    } else {
      position.getSecurityLink().resolve(_secSource);
      final FinancialSecurityVisitor<String> visitor = new FinancialSecurityVisitor<String>() {

        @Override
        public String visitBondSecurity(final BondSecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitCashSecurity(final CashSecurity security) {
          return security.getAmount() * position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitEquitySecurity(final EquitySecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitFRASecurity(final FRASecurity security) {
          return security.getAmount() * position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitFutureSecurity(final FutureSecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitSwapSecurity(final SwapSecurity security) {
          return NOT_LONG_SHORT;
        }

        @Override
        public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitFXOptionSecurity(final FXOptionSecurity security) {
          return security.isLong() ? LONG : SHORT;
        }

        @Override
        public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          return security.isLong() ? LONG : SHORT;
        }

        @Override
        public String visitSwaptionSecurity(final SwaptionSecurity security) {
          return security.isLong() ? LONG : SHORT;
        }

        @Override
        public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitEquityIndexDividendFutureOptionSecurity(
            final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          return position.getQuantity().longValue() < 0 ? SHORT : LONG;
        }

        @Override
        public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return security.isLong() ? LONG : SHORT;
        }

        @Override
        public String visitFXForwardSecurity(final FXForwardSecurity security) {
          return NOT_LONG_SHORT;
        }

        @Override
        public String visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return NOT_LONG_SHORT;
        }

        @Override
        public String visitCapFloorSecurity(final CapFloorSecurity security) {
          return NOT_LONG_SHORT;
        }

        @Override
        public String visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return NOT_LONG_SHORT;
        }

        @Override
        public String visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return NOT_LONG_SHORT;
        }

        @Override
        public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return security.isLong() ? LONG : SHORT;
        }

        @Override
        public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return security.isLong() ? LONG : SHORT;
        }

        @Override
        public String visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          throw new UnsupportedOperationException("SimpleZeroDepositSecurity should not be used in a position");
        }

        @Override
        public String visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          throw new UnsupportedOperationException("PeriodicZeroDepositSecurity should not be used in a position");
        }

        @Override
        public String visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          throw new UnsupportedOperationException("ContinuousZeroDepositSecurity should not be used in a position");
        }

      };
      if (position.getSecurity() instanceof FinancialSecurity) {
        final FinancialSecurity finSec = (FinancialSecurity) position.getSecurity();
        return finSec.accept(visitor);
      }
      return NOT_LONG_SHORT;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return REQUIRED;
  }

  @Override
  public int compare(final String o1, final String o2) {
    return CompareUtils.compareByList(REQUIRED, o1, o2);
  }

  private class PositionComparator implements Comparator<Position> {
    @Override
    public int compare(final Position o1, final Position o2) {
      return CompareUtils.compareWithNullLow(o1.getQuantity(), o2.getQuantity());
    }
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }
}
