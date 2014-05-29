/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.FloatingRateNoteSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
import com.opengamma.financial.security.cash.CashBalanceSecurity;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;
import com.opengamma.financial.security.cds.CDSSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.LegacyFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.LegacyRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardFixedRecoveryCDSSecurity;
import com.opengamma.financial.security.cds.StandardRecoveryLockCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.credit.IndexCDSDefinitionSecurity;
import com.opengamma.financial.security.credit.IndexCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.AmericanDepositaryReceiptSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.equity.ExchangeTradedFundSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.fra.ForwardRateAgreementSecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.DeliverableSwapFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXVolatilitySwapSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.irs.InterestRateSwapSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.financial.security.option.EquityBarrierOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.EquityWarrantSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.financial.security.option.SwaptionSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Adapter for visiting all concrete asset classes.
 *
 * @param <T> Return type for visitor.
 */
public class FinancialSecurityVisitorAdapter<T> extends FutureSecurityVisitorAdapter<T> implements FinancialSecurityVisitor<T> {

  /**
   * Creates builder for a {@link FinancialSecurityVisitor}. The underlying visitor
   * has no implemented methods.
   * @param <T> The return type of the visitor
   * @return A builder
   */
  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  /**
   * Creates a builder for a {@link FinancialSecurityVisitor} that uses the
   * supplied visitor as the initial underlying.
   * @param <T> The return type of the visitor
   * @param visitor The underlying visitor, not null
   * @return A builder
   */
  public static <T> Builder<T> builder(final FinancialSecurityVisitor<T> visitor) {
    return new Builder<>(visitor);
  }

  @Override
  public T visitBillSecurity(final BillSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCorporateBondSecurity(final CorporateBondSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitInflationBondSecurity(final InflationBondSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCapFloorSecurity(final CapFloorSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCashBalanceSecurity(final CashBalanceSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCashSecurity(final CashSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCashFlowSecurity(final CashFlowSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityOptionSecurity(final EquityOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquitySecurity(final EquitySecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFRASecurity(final FRASecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFXForwardSecurity(final FXForwardSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFXOptionSecurity(final FXOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitForwardSwapSecurity(final ForwardSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitSwapSecurity(final SwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitSwaptionSecurity(final SwaptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitMetalForwardSecurity(final MetalForwardSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCDSSecurity(final CDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitStandardCDSSecurity(final StandardCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitIndexCDSSecurity(final IndexCDSSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  @Override
  public T visitIndexCDSDefinitionSecurity(final IndexCDSDefinitionSecurity security) {
    throw new UnsupportedOperationException(getUnsupportedOperationMessage(getClass(), security));
  }

  /**
   * Generic message for unsupported methods in FinancialSecurityVisitor implementations
   *
   * @param clazz the implementation class, not null
   * @param security the financial security, not null
   * @return the message, not null;
   */
  public static String getUnsupportedOperationMessage(final Class<?> clazz, final FinancialSecurity security) {
    ArgumentChecker.notNull(clazz, "implementation class");
    ArgumentChecker.notNull(security, "financial security");
    return "This visitor (" + clazz.getName() + ") does not support " + security.getClass().getName() + " security.";
  }

  /**
   * Builder for the visitor adapter.
   *
   * @param <T> Return type for the visitor
   */
  public static class Builder<T> {

    /**
     * Creates a builder with an underlying visitor that has no methods implemented.
     */
    protected Builder() {
      _visitor = new FinancialSecurityVisitorAdapter<>();
    }

    /**
     * Creates a builder with this underlying visitor.
     * @param visitor The visitor, not null
     */
    protected Builder(final FinancialSecurityVisitor<T> visitor) {
      _visitor = visitor;
    }

    private FinancialSecurityVisitor<T> _visitor;

    public Builder<T> billSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitBillSecurity(final BillSecurity security) {
          return visitor.visitBillSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> municipalBondSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return visitor.visitMunicipalBondSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> governmentBondSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return visitor.visitGovernmentBondSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> corporateBondSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return visitor.visitCorporateBondSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> inflationBondSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitInflationBondSecurity(final InflationBondSecurity security) {
          return visitor.visitInflationBondSecurity(security);
        }
      };
      return this;
    }
    
    public Builder<T> cashBalanceSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCashBalanceSecurity(final CashBalanceSecurity security) {
          return visitor.visitCashBalanceSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> cashSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCashSecurity(final CashSecurity security) {
          return visitor.visitCashSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> cashFlowSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCashFlowSecurity(final CashFlowSecurity security) {
          return visitor.visitCashFlowSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> deliverableSwapFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
          return visitor.visitDeliverableSwapFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equitySecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquitySecurity(final EquitySecurity security) {
          return visitor.visitEquitySecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fraSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFRASecurity(final FRASecurity security) {
          return visitor.visitFRASecurity(security);
        }

        @Override
        public T visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
          return visitor.visitForwardRateAgreementSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> swapSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitSwapSecurity(final SwapSecurity security) {
          return visitor.visitSwapSecurity(security);
        }

        @Override
        public T visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
          return visitor.visitInterestRateSwapSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityIndexOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return visitor.visitEquityIndexOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> standardVanillaCDSSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          return visitor.visitStandardVanillaCDSSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> legacyVanillaCDSSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          return visitor.visitLegacyVanillaCDSSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> creditDefaultSwapOptionSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          return visitor.visitCreditDefaultSwapOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> creditDefaultSwapIndexSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          return visitor.visitCreditDefaultSwapIndexSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityIndexDividendFutureOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
          return visitor.visitEquityIndexDividendFutureOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityIndexFutureOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
          return visitor.visitEquityIndexFutureOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return visitor.visitEquityOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityBarrierOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return visitor.visitEquityBarrierOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fxOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXOptionSecurity(final FXOptionSecurity security) {
          return visitor.visitFXOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> nonDeliverableFxOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          return visitor.visitNonDeliverableFXOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> swaptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitSwaptionSecurity(final SwaptionSecurity security) {
          return visitor.visitSwaptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> irfutureOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return visitor.visitIRFutureOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fxBarrierOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return visitor.visitFXBarrierOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fxDigitalOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return visitor.visitFXDigitalOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fxNonDeliverableDigitalOptionVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return visitor.visitNonDeliverableFXDigitalOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fxForwardVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXForwardSecurity(final FXForwardSecurity security) {
          return visitor.visitFXForwardSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> nonDeliverableFxForwardVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return visitor.visitNonDeliverableFXForwardSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> capFloorVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCapFloorSecurity(final CapFloorSecurity security) {
          return visitor.visitCapFloorSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> capFloorCMSSpreadVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return visitor.visitCapFloorCMSSpreadSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityVarianceSwapSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return visitor.visitEquityVarianceSwapSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> simpleZeroDepositSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return visitor.visitSimpleZeroDepositSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> periodicZeroDepositSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return visitor.visitPeriodicZeroDepositSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> continuousZeroDepositSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return visitor.visitContinuousZeroDepositSecurity(security);
        }
      };
      return this;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Builder<T> bondFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitBondFutureSecurity(final BondFutureSecurity security) {
          return visitor.visitBondFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> commodityFutureOptionSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
          return visitor.visitCommodityFutureOptionSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> energyFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return visitor.visitEnergyFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityIndexDividendFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return visitor.visitEquityIndexDividendFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> fxFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXFutureSecurity(final FXFutureSecurity security) {
          return visitor.visitFXFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> indexFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return visitor.visitIndexFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> metalFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return visitor.visitMetalFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> stockFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitStockFutureSecurity(final StockFutureSecurity security) {
          return visitor.visitStockFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> agricultureFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return visitor.visitAgricultureFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> equityFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return visitor.visitEquityFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> forwardSwapSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitForwardSwapSecurity(final ForwardSwapSecurity security) {
          return visitor.visitForwardSwapSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> interestRateFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return visitor.visitInterestRateFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> federalFundsFutureSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return visitor.visitFederalFundsFutureSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> agricultureForwardSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
          return visitor.visitAgricultureForwardSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> energyForwardSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
          return visitor.visitEnergyForwardSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> metalForwardSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitMetalForwardSecurity(final MetalForwardSecurity security) {
          return visitor.visitMetalForwardSecurity(security);
        }
      };
      return this;
    }

    // simple values

    public Builder<T> municipalBondSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> governmentBondSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> corporateBondSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> cashBalanceSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCashBalanceSecurity(final CashBalanceSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> cashSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCashSecurity(final CashSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equitySecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquitySecurity(final EquitySecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> bondFutureOptionSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fraSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFRASecurity(final FRASecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> swapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitSwapSecurity(final SwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityIndexOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityIndexDividendFutureOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityIndexFutureOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityBarrierOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXOptionSecurity(final FXOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> nonDeliverableFxOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> swaptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitSwaptionSecurity(final SwaptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> irfutureOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxBarrierOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxDigitalOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxNonDeliverableDigitalOptionVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxForwardVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXForwardSecurity(final FXForwardSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> nonDeliverableFxForwardVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> capFloorVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCapFloorSecurity(final CapFloorSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> capFloorCMSSpreadVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityVarianceSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> simpleZeroDepositSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> periodicZeroDepositSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> continuousZeroDepositSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> zeroCouponInflationSwapSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
          return visitor.visitZeroCouponInflationSwapSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> yearOnYearInflationSwapSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
          return visitor.visitYearOnYearInflationSwapSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> zeroCouponInflationSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> yearOnYearInflationSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public Builder<T> bondFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitBondFutureSecurity(final BondFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> commodityFutureOptionSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> energyFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityIndexDividendFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXFutureSecurity(final FXFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> indexFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> metalFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> stockFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitStockFutureSecurity(final StockFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> agricultureFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> forwardSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitForwardSwapSecurity(final ForwardSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> interestRateFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> federalFundsFutureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> agricultureForwardSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> energyForwardSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> metalForwardSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitMetalForwardSecurity(final MetalForwardSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> cdsSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCDSSecurity(final CDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> standardVanillaCDSSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> standardFixedRecoveryCDSSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> standardRecoveryLockCDSSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> legacyVanillaCDSSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> legacyFixedRecoveryCDSSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> legacyRecoveryLockCDSSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> creditDefaultSwapOptionSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> creditDefaultSwapIndexSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> interestRateSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> fxVolatilitySwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> exchangeTradedFundSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> americanDepositaryReceiptSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityWarrantSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> floatingRateNoteSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> equityTotalReturnSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> bondTotalReturnSwapSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
          return value;
        }
      };
      return this;
    }

    public Builder<T> creditSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {

        @Override
        public T visitIndexCDSDefinitionSecurity(final IndexCDSDefinitionSecurity security) {
          return value;
        }

        @Override
        public T visitStandardCDSSecurity(final StandardCDSSecurity security) {
          return value;
        }

        @Override
        public T visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
          return value;
        }

        @Override
        public T visitIndexCDSSecurity(final IndexCDSSecurity security) {
          return value;
        }

      };
      return this;
    }

    public Builder<T> futureSecurityVisitor(final T value) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return value;
        }

        @Override
        public T visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return value;
        }

        @Override
        public T visitBondFutureSecurity(final BondFutureSecurity security) {
          return value;
        }

        @Override
        public T visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return value;
        }

        @Override
        public T visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return value;
        }

        @Override
        public T visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return value;
        }

        @Override
        public T visitFXFutureSecurity(final FXFutureSecurity security) {
          return value;
        }

        @Override
        public T visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return value;
        }

        @Override
        public T visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return value;
        }

        @Override
        public T visitStockFutureSecurity(final StockFutureSecurity security) {
          return value;
        }
      };
      return this;
    }

    // bulk overriders

    public Builder<T> bondSecurityVisitor(final FinancialSecurityVisitor<T> visitor) {
      _visitor = new FinancialSecurityVisitorDelegate<T>(_visitor) {
        @Override
        public T visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return visitor.visitGovernmentBondSecurity(security);
        }

        @Override
        public T visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return visitor.visitMunicipalBondSecurity(security);
        }

        @Override
        public T visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return visitor.visitCorporateBondSecurity(security);
        }
        @Override
        public T visitInflationBondSecurity(final InflationBondSecurity security) {
          return visitor.visitInflationBondSecurity(security);
        }
      };
      return this;
    }

    public Builder<T> sameValueForSecurityVisitor(final T value) {
      _visitor = new SameValueVisitor<T>(_visitor, value);
      return this;
    }

    /**
     * Creates the {@link FinancialSecurityVisitor}
     * @return The visitor
     */
    public FinancialSecurityVisitor<T> create() {
      return new FinancialSecurityVisitorDelegate<>(_visitor);
    }
  }

}
