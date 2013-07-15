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
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorSameValueAdapter;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.util.CompareUtils;

/**
 * Function to classify positions by Currency.
 *
 */
public class StocksPutsCallsAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Stocks/Puts/Calls";
  private static final String NA = "N/A";
  private static final String STOCKS = "Stocks";
  private static final String PUTS = "Puts";
  private static final String CALLS = "Calls";
  private static final List<String> REQUIRED_ENTRIES = Arrays.asList(STOCKS, CALLS, PUTS, NA);

  private final SecuritySource _secSource;
  private final boolean _useAttributes;

  /**
   * Creates an instance that does not use attributes.
   * 
   * @param secSource  the security source, not null
   */
  public StocksPutsCallsAggregationFunction(final SecuritySource secSource) {
    this(secSource, false);
  }

  /**
   * Creates an instance.
   * 
   * @param secSource  the security source, not null
   * @param useAttributes  whether to use attributes
   */
  public StocksPutsCallsAggregationFunction(final SecuritySource secSource, final boolean useAttributes) {
    _secSource = secSource;
    _useAttributes = useAttributes;
  }

  //-------------------------------------------------------------------------
  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      final Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NA;
      }
    } else {
      position.getSecurityLink().resolve(_secSource);
      final FinancialSecurityVisitor<String> visitor = new FinancialSecurityVisitorSameValueAdapter<String>(NA) {
        
      
        @Override
        public String visitEquitySecurity(final EquitySecurity security) {
          return STOCKS;
        }

      
        @Override
        public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitFXOptionSecurity(final FXOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }

        @Override
        public String visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }

        @Override
        public String visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitEquityIndexDividendFutureOptionSecurity(
            final EquityIndexDividendFutureOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }

        @Override
        public String visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }

        @Override
        public String visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }

        @Override
        public String visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
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
      return NA;
    }
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return REQUIRED_ENTRIES;
  }

  @Override
  public int compare(final String entry1, final String entry2) {
    return CompareUtils.compareByList(REQUIRED_ENTRIES, entry1, entry2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return new SimplePositionComparator();
  }

}
