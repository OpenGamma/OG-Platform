/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.fudgemsg.FudgeMsgEnvelope;

import com.google.common.base.Preconditions;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeMap;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.InflationBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.financial.security.capfloor.CapFloorSecurity;
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
import com.opengamma.financial.security.deposit.ContinuousZeroDepositSecurity;
import com.opengamma.financial.security.deposit.PeriodicZeroDepositSecurity;
import com.opengamma.financial.security.deposit.SimpleZeroDepositSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.financial.security.forward.AgricultureForwardSecurity;
import com.opengamma.financial.security.forward.EnergyForwardSecurity;
import com.opengamma.financial.security.forward.MetalForwardSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
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
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
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
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.financial.sensitivities.SecurityEntryData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.lambdava.functions.Function1;
import com.opengamma.master.security.RawSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;

/**
 * General utility method applying to Financial Securities
 */
public class FinancialSecurityUtils {

  private static ComputationTargetTypeMap<Function1<ComputationTarget, ValueProperties>> s_getCurrencyConstraint = getCurrencyConstraint();

  private static ComputationTargetTypeMap<Function1<ComputationTarget, ValueProperties>> getCurrencyConstraint() {
    final ComputationTargetTypeMap<Function1<ComputationTarget, ValueProperties>> map = new ComputationTargetTypeMap<>();
    map.put(ComputationTargetType.POSITION, new Function1<ComputationTarget, ValueProperties>() {
      @Override
      public ValueProperties execute(final ComputationTarget target) {
        final Security security = target.getPosition().getSecurity();
        final Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        } else {
          return ValueProperties.none();
        }
      }
    });
    map.put(ComputationTargetType.SECURITY, new Function1<ComputationTarget, ValueProperties>() {
      @Override
      public ValueProperties execute(final ComputationTarget target) {
        final Security security = target.getSecurity();
        final Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        } else {
          return ValueProperties.none();
        }
      }
    });
    map.put(ComputationTargetType.TRADE, new Function1<ComputationTarget, ValueProperties>() {
      @Override
      public ValueProperties execute(final ComputationTarget target) {
        final Security security = target.getTrade().getSecurity();
        final Currency ccy = getCurrency(security);
        if (ccy != null) {
          return ValueProperties.with(ValuePropertyNames.CURRENCY, ccy.getCode()).get();
        } else {
          return ValueProperties.none();
        }
      }
    });
    map.put(ComputationTargetType.CURRENCY, new Function1<ComputationTarget, ValueProperties>() {
      @Override
      public ValueProperties execute(final ComputationTarget target) {
        return ValueProperties.with(ValuePropertyNames.CURRENCY, target.getUniqueId().getValue()).get();
      }
    });
    return map;
  }

  /**
   *
   * @param target the computation target being examined.
   * @return ValueProperties containing a constraint of the CurrencyUnit or empty if not possible
   */
  public static ValueProperties getCurrencyConstraint(final ComputationTarget target) {
    final Function1<ComputationTarget, ValueProperties> operation = s_getCurrencyConstraint.get(target.getType());
    if (operation != null) {
      return operation.execute(target);
    } else {
      return ValueProperties.none();
    }
  }

  /**
   * @param security the security to be examined.
   * @return an ExternalId for a Region, where it is possible to determine, null otherwise.
   */
  public static ExternalId getRegion(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;

      final ExternalId regionId = finSec.accept(new FinancialSecurityVisitorSameValueAdapter<ExternalId>(null) {

        @Override
        public ExternalId visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
        }

        @Override
        public ExternalId visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
        }

        @Override
        public ExternalId visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, security.getIssuerDomicile());
        }

        @Override
        public ExternalId visitCashSecurity(final CashSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitFRASecurity(final FRASecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return security.getRegion();
        }

        @Override
        public ExternalId visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return security.getRegion();
        }

        @Override
        public ExternalId visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return security.getRegion();
        }

        @Override
        public ExternalId visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          return security.getRegionId();
        }

        @Override
        public ExternalId visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          return security.getRegionId();
        }
      });

      return regionId;
    }
    return null;
  }

  /**
   * @param security the security to be examined.
   * @return an ExternalId for an Exchange, where it is possible to determine, null otherwise.
   */
  public static ExternalId getExchange(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;

      final ExternalId regionId = finSec.accept(new FinancialSecurityVisitorSameValueAdapter<ExternalId>(null) {
        @Override
        public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }

        @Override
        public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }

        @Override
        public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }

        @Override
        public ExternalId visitEquitySecurity(final EquitySecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchangeCode());
        }

        @Override
        public ExternalId visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitBondFutureSecurity(final BondFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitFXFutureSecurity(final FXFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitStockFutureSecurity(final StockFutureSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getTradingExchange());
        }

        @Override
        public ExternalId visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
          return ExternalId.of(ExternalSchemes.ISO_MIC, security.getExchange());
        }
      });
      return regionId;
    }
    return null;
  }

  /**
   * @param security the security to be examined.
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Currency getCurrency(final Security security) {  // CSIGNORE
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;

      final Currency ccy = finSec.accept(new FinancialSecurityVisitor<Currency>() {

        @Override
        public Currency visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitInflationBondSecurity(final InflationBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCashSecurity(final CashSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCashFlowSecurity(final CashFlowSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquitySecurity(final EquitySecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFRASecurity(final FRASecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitSwapSecurity(final SwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return payLeg.getCurrency();
            }
          }
          return null;
        }

        @Override
        public Currency visitForwardSwapSecurity(final ForwardSwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return payLeg.getCurrency();
            }
          }
          return null;
        }

        @Override
        public Currency visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXOptionSecurity(final FXOptionSecurity security) {
          throw new UnsupportedOperationException("FX securities do not have a currency");
        }

        @Override
        public Currency visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          throw new UnsupportedOperationException("FX securities do not have a currency");
        }

        @Override
        public Currency visitSwaptionSecurity(final SwaptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
          return equityIndexDividendFutureOptionSecurity.getCurrency();
        }

        @Override
        public Currency visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity equityIndexFutureOptionSecurity) {
          return equityIndexFutureOptionSecurity.getCurrency();
        }

        @Override
        public Currency visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          throw new UnsupportedOperationException("FX Barrier Options do not have a currency");
        }

        @Override
        public Currency visitFXForwardSecurity(final FXForwardSecurity security) {
          throw new UnsupportedOperationException("FX forward securities do not have a currency");
        }

        @Override
        public Currency visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          throw new UnsupportedOperationException("Non-deliverable FX forward securities do not have a currency");
        }

        @Override
        public Currency visitCapFloorSecurity(final CapFloorSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          throw new UnsupportedOperationException("FX digital option securities do not have a currency");
        }

        @Override
        public Currency visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          throw new UnsupportedOperationException("NDF FX digital option securities do not have a currency");
        }

        @Override
        public Currency visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitBondFutureSecurity(final BondFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFXFutureSecurity(final FXFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitStockFutureSecurity(final StockFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitMetalForwardSecurity(final MetalForwardSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCDSSecurity(final CDSSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          return security.getNotional().getCurrency();
        }

        @Override
        public Currency visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          return security.getCurrency();
        }

        @Override
        public Currency visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return payLeg.getCurrency();
            }
          }
          return null;
        }

        @Override
        public Currency visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return payLeg.getCurrency();
            }
          }
          return null;
        }

      });
      return ccy;
    } else if (security instanceof RawSecurity) {
      final RawSecurity rawSecurity = (RawSecurity) security;
      if (security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
        final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        final SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
        return securityEntryData.getCurrency();
      }
    }

    return null;
  }

  /**
   * @param security the security to be examined.
   * @param securitySource a security source
   * @return a Currency, where it is possible to determine a single Currency association, null otherwise.
   */
  public static Collection<Currency> getCurrencies(final Security security, final SecuritySource securitySource) {  // CSIGNORE

    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final Collection<Currency> ccy = finSec.accept(new FinancialSecurityVisitor<Collection<Currency>>() {
        @Override
        public Collection<Currency> visitCorporateBondSecurity(final CorporateBondSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitInflationBondSecurity(final InflationBondSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCashSecurity(final CashSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCashFlowSecurity(final CashFlowSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquitySecurity(final EquitySecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFRASecurity(final FRASecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitSwapSecurity(final SwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return Collections.singletonList(payLeg.getCurrency());
            } else {
              final Collection<Currency> collection = new ArrayList<Currency>();
              collection.add(payLeg.getCurrency());
              collection.add(receiveLeg.getCurrency());
              return collection;
            }
          }
          return null;
        }

        @Override
        public Collection<Currency> visitForwardSwapSecurity(final ForwardSwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return Collections.singletonList(payLeg.getCurrency());
            } else {
              final Collection<Currency> collection = new ArrayList<Currency>();
              collection.add(payLeg.getCurrency());
              collection.add(receiveLeg.getCurrency());
              return collection;
            }
          }
          return null;
        }

        @Override
        public Collection<Currency> visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXOptionSecurity(final FXOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          //deliveryCurrency is always already covered
          return currencies;
        }

        @Override
        public Collection<Currency> visitSwaptionSecurity(final SwaptionSecurity security) {
          // REVIEW: jim 1-Aug-2011 -- should we include the currencies of the underlying?
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
          return Collections.singleton(commodityFutureOptionSecurity.getCurrency());
        }

        @Override
        public Collection<Currency> visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
          return Collections.singleton(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitFXForwardSecurity(final FXForwardSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getPayCurrency());
          currencies.add(security.getReceiveCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getPayCurrency());
          currencies.add(security.getReceiveCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitCapFloorSecurity(final CapFloorSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCapFloorCMSSpreadSecurity(final CapFloorCMSSpreadSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          final Collection<Currency> currencies = new ArrayList<Currency>();
          currencies.add(security.getCallCurrency());
          currencies.add(security.getPutCurrency());
          return currencies;
        }

        @Override
        public Collection<Currency> visitSimpleZeroDepositSecurity(final SimpleZeroDepositSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitPeriodicZeroDepositSecurity(final PeriodicZeroDepositSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitContinuousZeroDepositSecurity(final ContinuousZeroDepositSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitBondFutureSecurity(final BondFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFXFutureSecurity(final FXFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitStockFutureSecurity(final StockFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitMetalForwardSecurity(final MetalForwardSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCDSSecurity(final CDSSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          return Collections.singletonList(security.getNotional().getCurrency());
        }

        @Override
        public Collection<Currency> visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          return Collections.singletonList(security.getCurrency());
        }

        @Override
        public Collection<Currency> visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return Collections.singletonList(payLeg.getCurrency());
            }
            final Collection<Currency> collection = new ArrayList<Currency>();
            collection.add(payLeg.getCurrency());
            collection.add(receiveLeg.getCurrency());
            return collection;
          }
          return null;
        }

        @Override
        public Collection<Currency> visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
          if (security.getPayLeg().getNotional() instanceof InterestRateNotional && security.getReceiveLeg().getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional payLeg = (InterestRateNotional) security.getPayLeg().getNotional();
            final InterestRateNotional receiveLeg = (InterestRateNotional) security.getReceiveLeg().getNotional();
            if (payLeg.getCurrency().equals(receiveLeg.getCurrency())) {
              return Collections.singletonList(payLeg.getCurrency());
            }
            final Collection<Currency> collection = new ArrayList<Currency>();
            collection.add(payLeg.getCurrency());
            collection.add(receiveLeg.getCurrency());
            return collection;
          }
          return null;
        }

      });
      return ccy;
    } else if (security instanceof RawSecurity) {
      final RawSecurity rawSecurity = (RawSecurity) security;
      if (security.getSecurityType().equals(SecurityEntryData.EXTERNAL_SENSITIVITIES_SECURITY_TYPE)) {
        final FudgeMsgEnvelope msg = OpenGammaFudgeContext.getInstance().deserialize(rawSecurity.getRawData());
        final SecurityEntryData securityEntryData = OpenGammaFudgeContext.getInstance().fromFudgeMsg(SecurityEntryData.class, msg.getMessage());
        return Collections.singleton(securityEntryData.getCurrency());
      }
    }
    return null;
  }

  /**
   * Check if a security is exchange traded
   *
   * @param security the security to be examined.
   * @return true if exchange traded or false otherwise.
   */
  public static boolean isExchangeTraded(final Security security) {
    boolean result = false;
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;

      final Boolean isExchangeTraded = finSec.accept(
          FinancialSecurityVisitorAdapter.<Boolean>builder().
          sameValueForSecurityVisitor(false).
          equitySecurityVisitor(true).
          futureSecurityVisitor(true).
          equityIndexOptionVisitor(true).
          equityOptionVisitor(true).
          equityBarrierOptionVisitor(true).
          bondFutureOptionSecurityVisitor(true).
          equityIndexFutureOptionVisitor(true).
          irfutureOptionVisitor(true).
          interestRateFutureSecurityVisitor(true).
          federalFundsFutureSecurityVisitor(true).
          create());

      result = isExchangeTraded == null ? false : isExchangeTraded;
    }

    return result;
  }

  /**
   * Returns the underlying id of a security (e.g. the id of the equity underlying an equity future).
   * @param security The security, not null
   * @return The id of the underlying of a security, where it is possible to identify this, or null
   */
  public static ExternalId getUnderlyingId(final Security security) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      return finSec.accept(new FinancialSecurityVisitorSameValueAdapter<ExternalId>(null) {

        @Override
        public ExternalId visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitMetalForwardSecurity(final MetalForwardSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitStockFutureSecurity(final StockFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityFutureSecurity(final EquityFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitIndexFutureSecurity(final IndexFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitMetalFutureSecurity(final MetalFutureSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityOptionSecurity(final EquityOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitEquityVarianceSwapSecurity(final EquityVarianceSwapSecurity security) {
          return security.getSpotUnderlyingId();
        }

        @Override
        public ExternalId visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          return security.getUnderlyingId();
        }

        @Override
        public ExternalId visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          return security.getReferenceEntity();
        }

        @Override
        public ExternalId visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          return security.getUnderlyingId();
        }
      });
    }
    return null;
  }

  public static CurrencyAmount getNotional(final Security security, final CurrencyPairs currencyPairs, final SecuritySource securitySource) {
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity finSec = (FinancialSecurity) security;
      final CurrencyAmount notional = finSec.accept(new FinancialSecurityVisitorAdapter<CurrencyAmount>() {

        @Override
        public CurrencyAmount visitSwapSecurity(final SwapSecurity security) {
          final SwapLeg payNotional = security.getPayLeg();
          final SwapLeg receiveNotional = security.getReceiveLeg();
          if (payNotional.getNotional() instanceof InterestRateNotional && receiveNotional.getNotional() instanceof InterestRateNotional) {
            final InterestRateNotional pay = (InterestRateNotional) payNotional.getNotional();
            final InterestRateNotional receive = (InterestRateNotional) receiveNotional.getNotional();
            if (Double.compare(pay.getAmount(), receive.getAmount()) == 0) {
              return CurrencyAmount.of(pay.getCurrency(), pay.getAmount());
            }
          }
          throw new OpenGammaRuntimeException("Can only handle interest rate notionals with the same amounts");
        }

        @Override
        public CurrencyAmount visitFXOptionSecurity(final FXOptionSecurity security) {
          final Currency currency1 = security.getPutCurrency();
          final double amount1 = security.getPutAmount();
          final Currency currency2 = security.getCallCurrency();
          final double amount2 = security.getCallAmount();
          final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(currency1, currency2);
          if (currencyPair.getBase().equals(currency1)) {
            return CurrencyAmount.of(currency1, amount1);
          }
          return CurrencyAmount.of(currency2, amount2);
        }

        @Override
        public CurrencyAmount visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
          final Currency currency1 = security.getPutCurrency();
          final double amount1 = security.getPutAmount();
          final Currency currency2 = security.getCallCurrency();
          final double amount2 = security.getCallAmount();
          final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(currency1, currency2);
          if (currencyPair.getBase().equals(currency1)) {
            return CurrencyAmount.of(currency1, amount1);
          }
          return CurrencyAmount.of(currency2, amount2);
        }

        @Override
        public CurrencyAmount visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
          final Currency currency = security.getDeliveryCurrency();
          final double amount = security.getCallCurrency().equals(currency) ? security.getCallAmount() : security.getPutAmount();
          return CurrencyAmount.of(currency, amount);
        }

        @Override
        public CurrencyAmount visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
          final Currency currency1 = security.getPutCurrency();
          final double amount1 = security.getPutAmount();
          final Currency currency2 = security.getCallCurrency();
          final double amount2 = security.getCallAmount();
          final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(currency1, currency2);
          if (currencyPair.getBase().equals(currency1)) {
            return CurrencyAmount.of(currency1, amount1);
          }
          return CurrencyAmount.of(currency2, amount2);
        }

        @Override
        public CurrencyAmount visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
          final Currency currency = security.getPaymentCurrency();
          final double amount = security.getCallCurrency().equals(currency) ? security.getCallAmount() : security.getPutAmount();
          return CurrencyAmount.of(currency, amount);
        }

        @Override
        public CurrencyAmount visitFXForwardSecurity(final FXForwardSecurity security) {
          final Currency currency1 = security.getPayCurrency();
          final double amount1 = security.getPayAmount();
          final Currency currency2 = security.getReceiveCurrency();
          final double amount2 = security.getReceiveAmount();
          final CurrencyPair currencyPair = currencyPairs.getCurrencyPair(currency1, currency2);
          if (currencyPair.getBase().equals(currency1)) {
            return CurrencyAmount.of(currency1, amount1);
          }
          return CurrencyAmount.of(currency2, amount2);
        }

        @Override
        public CurrencyAmount visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
          final InterestRateNotional notional = security.getNotional();
          final int sign = security.isBuy() ? 1 : -1;
          return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
        }

        @Override
        public CurrencyAmount visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
          final InterestRateNotional notional = security.getNotional();
          final int sign = security.isBuy() ? 1 : -1;
          return CurrencyAmount.of(notional.getCurrency(), sign * notional.getAmount());
        }

        @Override
        public CurrencyAmount visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getMinimumAmount();
          return CurrencyAmount.of(currency, notional);
        }

        @Override
        public CurrencyAmount visitCorporateBondSecurity(final CorporateBondSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getMinimumAmount();
          return CurrencyAmount.of(currency, notional);
        }

        @Override
        public CurrencyAmount visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getMinimumAmount();
          return CurrencyAmount.of(currency, notional);
        }

        @Override
        public CurrencyAmount visitSwaptionSecurity(final SwaptionSecurity security) {
          final Security underlying = securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
          Preconditions.checkState(underlying instanceof SwapSecurity, "Failed to resolve underlying SwapSecurity. DB record potentially corrupted. '%s' returned.", underlying);
          final CurrencyAmount notional = visitSwapSecurity((SwapSecurity) underlying);
          if (security.isLong()) {
            return notional;
          }
          return notional.multipliedBy(-1);
        }

        @Override
        public CurrencyAmount visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getPointValue();
          return CurrencyAmount.of(currency, notional);
        }

        @Override
        public CurrencyAmount visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getUnitAmount();
          return CurrencyAmount.of(currency, notional);
        }

        @Override
        public CurrencyAmount visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
          final Currency currency = security.getCurrency();
          final Security underlying = securitySource.getSingle(security.getUnderlyingId().toBundle());
          Preconditions.checkState(underlying instanceof InterestRateFutureSecurity, "Failed to resolve underlying InterestRateFutureSecurity. " +
              "DB record potentially corrupted. '%s' returned.", underlying);
          return visitInterestRateFutureSecurity((InterestRateFutureSecurity) underlying);
        }

        @Override
        public CurrencyAmount visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getUnitAmount();
          return CurrencyAmount.of(currency, notional);
        }

        @Override
        public CurrencyAmount visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
          final InterestRateNotional notional = security.getNotional();
          return CurrencyAmount.of(notional.getCurrency(), notional.getAmount());
        }

        @Override
        public CurrencyAmount visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
          final Currency currency = security.getCurrency();
          final double notional = security.getNotional();
          return CurrencyAmount.of(currency, notional);
        }
      });
      return notional;
    }
    return null;
  }
}
