/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.OptionType;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * Function to classify positions by Currency.
 *
 */
public class StocksPutsCallsAggregationFunction implements AggregationFunction<String> {
  private boolean _useAttributes;
    
  private static final String NAME = "Stocks/Puts/Calls";
  private static final String NA = "N/A"; 
  private static final String STOCKS = "Long";
  private static final String PUTS = "Puts";
  private static final String CALLS = "Calls";
  private SecuritySource _secSource;
  
  public StocksPutsCallsAggregationFunction(SecuritySource secSource) {
    this(secSource, true);
  }
  
  public StocksPutsCallsAggregationFunction(SecuritySource secSource, boolean useAttributes) {
    _secSource = secSource;
    _useAttributes = useAttributes;
  }
  
  @Override
  public String classifyPosition(final Position position) {
    if (_useAttributes) {
      Map<String, String> attributes = position.getAttributes();
      if (attributes.containsKey(getName())) {
        return attributes.get(getName());
      } else {
        return NA;
      }
    } else {
      position.getSecurityLink().resolve(_secSource);
      FinancialSecurityVisitor<String> visitor = new FinancialSecurityVisitor<String>() {
  
        @Override
        public String visitBondSecurity(BondSecurity security) {
          return NA;
        }
  
        @Override
        public String visitCashSecurity(CashSecurity security) {
          return NA;
        }
  
        @Override
        public String visitEquitySecurity(EquitySecurity security) {
          return STOCKS;
        }
  
        @Override
        public String visitFRASecurity(FRASecurity security) {
          return NA;
        }
  
        @Override
        public String visitFutureSecurity(FutureSecurity security) {
          return NA;
        }
  
        @Override
        public String visitSwapSecurity(SwapSecurity security) {
          return NA;
        }
  
        @Override
        public String visitEquityIndexOptionSecurity(EquityIndexOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }
  
        @Override
        public String visitEquityOptionSecurity(EquityOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }
        
        @Override
        public String visitEquityBarrierOptionSecurity(EquityBarrierOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }
  
        @Override
        public String visitFXOptionSecurity(FXOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }
  
        @Override
        public String visitNonDeliverableFXOptionSecurity(NonDeliverableFXOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }
        
        @Override
        public String visitSwaptionSecurity(SwaptionSecurity security) {
          return NA;
        }
  
        @Override
        public String visitIRFutureOptionSecurity(IRFutureOptionSecurity security) {
          return security.getOptionType() == OptionType.CALL ? CALLS : PUTS;
        }
  
        @Override
        public String visitFXBarrierOptionSecurity(FXBarrierOptionSecurity security) {
          return security.getCallAmount() > 0 ? CALLS : PUTS; // check this!
        }
  
        @Override
        public String visitFXSecurity(FXSecurity security) {
          return NA;
        }
  
        @Override
        public String visitFXForwardSecurity(FXForwardSecurity security) {
          return NA;
        }

        @Override
        public String visitNonDeliverableFXForwardSecurity(NonDeliverableFXForwardSecurity security) {
          return NA;
        }
        
        @Override
        public String visitCapFloorSecurity(CapFloorSecurity security) {
          return NA;
        }
  
        @Override
        public String visitCapFloorCMSSpreadSecurity(CapFloorCMSSpreadSecurity security) {
          return NA;
        }
  
        @Override
        public String visitEquityVarianceSwapSecurity(EquityVarianceSwapSecurity security) {
          return NA;
        }
        
      };
      if (position.getSecurity() instanceof FinancialSecurity) {
        FinancialSecurity finSec = (FinancialSecurity) position.getSecurity();
        return finSec.accept(visitor);
      }
      return NA;
    }
  }

  public String getName() {
    return NAME;
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Arrays.asList(STOCKS, CALLS, PUTS, NA);
  }
}
