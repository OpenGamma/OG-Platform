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
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
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
import com.opengamma.financial.security.swap.BillTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.BondTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.EquityTotalReturnSwapSecurity;
import com.opengamma.financial.security.swap.ForwardSwapSecurity;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.security.swap.YearOnYearInflationSwapSecurity;
import com.opengamma.financial.security.swap.ZeroCouponInflationSwapSecurity;
import com.opengamma.util.CompareUtils;

/**
 * Function to classify positions by Currency.
 *
 */
public class LongShortAggregationFunction implements AggregationFunction<String> {

  private static final String NAME = "Long/Short";
  private static final String NOT_LONG_SHORT = "N/A";
  private static final String LONG = "Long";
  private static final String SHORT = "Short";
  private static final List<String> REQUIRED = Arrays.asList(LONG, SHORT, NOT_LONG_SHORT);

  private final Comparator<Position> _comparator = new PositionComparator();
  private final SecuritySource _secSource;
  private final boolean _useAttributes;

  /**
   * Creates an instance that does not use attributes.
   *
   * @param secSource  the security source, not null
   */
  public LongShortAggregationFunction(final SecuritySource secSource) {
    this(secSource, false);
  }

  /**
   * Creates an instance.
   *
   * @param secSource  the security source, not null
   * @param useAttributes  whether to use attributes
   */
  public LongShortAggregationFunction(final SecuritySource secSource, final boolean useAttributes) {
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
        return NOT_LONG_SHORT;
      }
    } else {
      position.getSecurityLink().resolve(_secSource);
      final FinancialSecurityVisitor<String> visitor = new Visitor(position);
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

  //-------------------------------------------------------------------------
  @Override
  public int compare(final String o1, final String o2) {
    return CompareUtils.compareByList(REQUIRED, o1, o2);
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return _comparator;
  }

  private class PositionComparator implements Comparator<Position> {
    @Override
    public int compare(final Position o1, final Position o2) {
      return CompareUtils.compareWithNullLow(o1.getQuantity(), o2.getQuantity());
    }
  }

  private final class Visitor implements FinancialSecurityVisitor<String> {
    private final Position _position;

    private Visitor(final Position position) {
      _position = position;
    }

    @Override
    public String visitCorporateBondSecurity(final CorporateBondSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitBillSecurity(final BillSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitMunicipalBondSecurity(final MunicipalBondSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitInflationBondSecurity(final InflationBondSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitCashBalanceSecurity(final CashBalanceSecurity security) {
      return security.getAmount() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitCashSecurity(final CashSecurity security) {
      return security.getAmount() * _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitCashFlowSecurity(final CashFlowSecurity security) {
      return security.getAmount() * _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitEquitySecurity(final EquitySecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitFRASecurity(final FRASecurity security) {
      return security.getAmount() * _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitForwardRateAgreementSecurity(final ForwardRateAgreementSecurity security) {
      return security.getAmount() * _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitSwapSecurity(final SwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitEquityOptionSecurity(final EquityOptionSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitEquityBarrierOptionSecurity(final EquityBarrierOptionSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
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
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity commodityFutureOptionSecurity) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitBondFutureOptionSecurity(final BondFutureOptionSecurity bondFutureOptionSecurity) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitEquityIndexDividendFutureOptionSecurity(
        final EquityIndexDividendFutureOptionSecurity equityIndexDividendFutureOptionSecurity) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitEquityIndexFutureOptionSecurity(
        final EquityIndexFutureOptionSecurity equityIndexFutureOptionSecurity) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
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

    @Override
    public String visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitBondFutureSecurity(final BondFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitEquityFutureSecurity(final EquityFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitFXFutureSecurity(final FXFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitForwardSwapSecurity(final ForwardSwapSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitIndexFutureSecurity(final IndexFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitMetalFutureSecurity(final MetalFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitStockFutureSecurity(final StockFutureSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitAgricultureForwardSecurity(final AgricultureForwardSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitEnergyForwardSecurity(final EnergyForwardSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitMetalForwardSecurity(final MetalForwardSecurity security) {
      return null;  //TODO implement me !
    }

    @Override
    public String visitCDSSecurity(final CDSSecurity security) {
      return null; //TODO Should be possible to see direction of CDS trade? quantity < 0?
    }

    @Override
    public String visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
      return null; //TODO
    }

    @Override
    public String visitStandardFixedRecoveryCDSSecurity(final StandardFixedRecoveryCDSSecurity security) {
      return null; //TODO
    }

    @Override
    public String visitStandardRecoveryLockCDSSecurity(final StandardRecoveryLockCDSSecurity security) {
      return null; //TODO
    }

    @Override
    public String visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
      return null; //TODO
    }

    @Override
    public String visitLegacyFixedRecoveryCDSSecurity(final LegacyFixedRecoveryCDSSecurity security) {
      return null; //TODO
    }

    @Override
    public String visitLegacyRecoveryLockCDSSecurity(final LegacyRecoveryLockCDSSecurity security) {
      return null; //TODO
    }

    @Override
    public String visitDeliverableSwapFutureSecurity(final DeliverableSwapFutureSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitCreditDefaultSwapIndexDefinitionSecurity(final CreditDefaultSwapIndexDefinitionSecurity security) {
      throw new UnsupportedOperationException(FinancialSecurityVisitorAdapter.getUnsupportedOperationMessage(getClass(), security));
    }

    @Override
    public String visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
      throw new UnsupportedOperationException(FinancialSecurityVisitorAdapter.getUnsupportedOperationMessage(getClass(), security));
    }

    @Override
    public String visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
      throw new UnsupportedOperationException(FinancialSecurityVisitorAdapter.getUnsupportedOperationMessage(getClass(), security));
    }

    @Override
    public String visitZeroCouponInflationSwapSecurity(final ZeroCouponInflationSwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitYearOnYearInflationSwapSecurity(final YearOnYearInflationSwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitInterestRateSwapSecurity(final InterestRateSwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitFXVolatilitySwapSecurity(final FXVolatilitySwapSecurity security) {
      return security.getNotional() * _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitExchangeTradedFundSecurity(final ExchangeTradedFundSecurity security) {
      return null;
    }

    @Override
    public String visitAmericanDepositaryReceiptSecurity(final AmericanDepositaryReceiptSecurity security) {
      return null;
    }

    @Override
    public String visitEquityWarrantSecurity(final EquityWarrantSecurity security) {
      return null;
    }

    @Override
    public String visitFloatingRateNoteSecurity(final FloatingRateNoteSecurity security) {
      return _position.getQuantity().longValue() < 0 ? SHORT : LONG;
    }

    @Override
    public String visitEquityTotalReturnSwapSecurity(final EquityTotalReturnSwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitBondTotalReturnSwapSecurity(final BondTotalReturnSwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitBillTotalReturnSwapSecurity(BillTotalReturnSwapSecurity security) {
      return NOT_LONG_SHORT;
    }

    @Override
    public String visitStandardCDSSecurity(StandardCDSSecurity security) {
      return null;
    }

    @Override
    public String visitLegacyCDSSecurity(LegacyCDSSecurity security) {
      return null;
    }

    @Override
    public String visitIndexCDSSecurity(IndexCDSSecurity security) {
      return null;
    }

    @Override
    public String visitIndexCDSDefinitionSecurity(IndexCDSDefinitionSecurity security) {
      return null;
    }
  }

}
