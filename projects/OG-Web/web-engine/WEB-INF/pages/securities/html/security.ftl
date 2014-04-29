<#escape x as x?html>
<#setting number_format="0.#####">
<@page title="Security - ${security.name}" jquery=true aceXmlEditor=true>

<@section css="info" if=deleted>
  <p>This security has been deleted</p>
</@section>


<#-- SECTION Security output -->
<@section title="Security">
  <p>
    <@rowout label="Name">${security.name}</@rowout>
    <@rowout label="Reference">${security.uniqueId.value}, version ${security.uniqueId.version}, <a href="${uris.securityVersions()}">view history</a></@rowout>
  </p>

<#-- SUBSECTION Main data -->
<@subsection title="Detail" if=!deleted>
    <@rowout label="Type">${security.securityType?replace("_", " ")}</@rowout>
    <#switch security.securityType>
      <#case "FRA">
        <@rowout label="Amount">${security.amount}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="StartDate">${security.startDate.toLocalDate()} - ${security.startDate.zone}</@rowout>
        <@rowout label="EndDate">${security.endDate.toLocalDate()} - ${security.endDate.zone}</@rowout>
        <@rowout label="Rate">${security.rate}</@rowout>
        <@rowout label="Region">${security.regionId?replace("_", " ")}</@rowout>
      <#break>
      <#case "BOND_TOTAL_RETURN_SWAP">
        <@rowout label="Effective Date">${security.effectiveDate.toLocalDate()}</@rowout>
        <@rowout label="Maturity">${security.maturityDate.toLocalDate()}</@rowout>
        <@rowout label="Notional">${security.notionalCurrency} ${security.notionalAmount}</@rowout>
        <@rowout label="Asset Id">${security.assetId}}</@rowout>
        <@rowout label="Floating Rate Id">${security.fundingLeg.floatingReferenceRateId}</@rowout>
        <@rowout label="Spreads"></@rowout>
          <#list spreads?keys as key>
            <@rowout label="">${key} - ${spread[key]}</@rowout>
          </#list>
        <@rowout label="Payment Date Calendar"></@rowout>
          <#list paymentDateCalendars?keys as key>
            <@rowout label="">${key} - ${paymentDateCalendar[key]}</@rowout>
          </#list>
        <@rowout label="Payment Settlement Days">${security.paymentSettlementDays}</@rowout>
        <@rowout label="Payment Business Day Convention">${security.paymentBusinessDayConvention}</@rowout>
        <@rowout label="Payment Frequency">${security.paymentFrequency}</@rowout>
        <#if security.dates?has_content>
          <#list paymentDates?keys as key>
            <@rowout label="">${key}</@rowout>
            </#list>
        </#if>
      <#break>
      <#case "EQUITY_TOTAL_RETURN_SWAP">
        <@rowout label="Effective Date">${security.effectiveDate.toLocalDate()}</@rowout>
        <@rowout label="Maturity">${security.maturityDate.toLocalDate()}</@rowout>
        <@rowout label="Notional">${security.notionalCurrency} ${security.notionalAmount}</@rowout>
        <@rowout label="Asset Id">${security.assetId}}</@rowout>
        <@rowout label="Number of Shares">${security.numberOfShares}</@rowout>
        <@rowout label="Dividend Percentage">${security.dividendPercentage}</@rowout>
        <@rowout label="Floating Rate Id">${security.fundingLeg.floatingReferenceRateId}</@rowout>
        <@rowout label="Spreads"></@rowout>
          <#list spreads?keys as key>
            <@rowout label="">${key} - ${spread[key]}</@rowout>
          </#list>
        <@rowout label="Payment Date Calendar"></@rowout>
          <#list paymentDateCalendars?keys as key>
            <@rowout label="">${key} - ${paymentDateCalendar[key]}</@rowout>
          </#list>
        <@rowout label="Payment Settlement Days">${security.paymentSettlementDays}</@rowout>
        <@rowout label="Payment Business Day Convention">${security.paymentBusinessDayConvention}</@rowout>
        <@rowout label="Payment Frequency">${security.paymentFrequency}</@rowout>
        <#if security.dates?has_content>
          <#list paymentDates?keys as key>
            <@rowout label="">${key}</@rowout>
            </#list>
        </#if>
      <#break>
      <#case "CASH_BALANCE">
        <@rowout label="Amount">${security.amount}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <#break>
      <#case "CASH">
        <@rowout label="Amount">${security.amount}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Maturity">${security.maturity.toLocalDate()} - ${security.maturity.zone}</@rowout>
        <@rowout label="Rate">${security.rate}</@rowout>
        <@rowout label="Region">${security.regionId?replace("_", " ")}</@rowout>
        <#break>
      <#case "CASHFLOW">
        <@rowout label="Amount">${security.amount}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Settlement">${security.settlement.toLocalDate()} - ${security.settlement.zone}</@rowout>
        <#break>
      <#case "EQUITY">
        <@rowout label="Short name">${security.shortName}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Company name">${security.companyName}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <#if security.gicsCode?has_content>
          <@rowout label="GICS code">
            ${security.gicsCode.sectorCode}&nbsp;
            ${security.gicsCode.industryGroupCode}&nbsp;
            ${security.gicsCode.industryCode}&nbsp;
            ${security.gicsCode.subIndustryCode}
          </@rowout>
        </#if>
        <#break>
      <#case "BILL">
        <@rowout label="Issuer">${security.legalEntityId}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Region">${security.regionId}</@rowout>
        <@rowout label="Issue date">${security.issueDate.toLocalDate()}</@rowout>
        <@rowout label="Maturity date">${security.maturityDate.expiry.toLocalDate()}</@rowout>
        <@rowout label="Yield convention">${security.yieldConvention.conventionName}</@rowout>
        <@rowout label="Day count convention">${security.dayCount.conventionName}</@rowout>
        <@rowout label="Minimum increment">${security.minimumIncrement}</@rowout>
        <@rowout label="Days to settle">${security.daysToSettle}</@rowout>
        <#break> 
      <#case "FLOATING_RATE_NOTE">
        <@rowout label="Issuer">${security.legalEntityId}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Region">${security.regionId}</@rowout>
        <@rowout label="Issue date">${security.issueDate.toLocalDate()}</@rowout>
        <@rowout label="Maturity date">${security.maturityDate.expiry.toLocalDate()}</@rowout>
        <@rowout label="Day count convention">${security.dayCount.conventionName}</@rowout>
        <@rowout label="Minimum increment">${security.minimumIncrement}</@rowout>
        <@rowout label="Days to settle">${security.daysToSettle}</@rowout>
        <@rowout label="Reset days">${security.resetDays}</@rowout>
        <@rowout label="Benchmark rate">${security.benchmarkRateId}</@rowout>
        <@rowout label="Spread">${security.spread}</@rowout>
        <@rowout label="Leverage factor">${security.leverageFactor}</@rowout>
        <@rowout label="Coupon frequency">${security.couponFrequency.conventionName}</@rowout>       
        <#break> 
      <#case "BOND">
        <@rowout label="Issuer name">${security.issuerName}</@rowout>
        <@rowout label="Issuer type">${security.issuerType}</@rowout>
        <@rowout label="Issuer domicile">${security.issuerDomicile}</@rowout>
        <@rowout label="Market">${security.market}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Yield convention">${security.yieldConvention.conventionName}</@rowout>
        <#if security.guaranteeType?has_content>
          <@rowout label="Guarantee type">${security.guaranteeType}</@rowout>
        </#if>
        <@rowout label="Last trade date">${security.lastTradeDate.expiry}</@rowout>
        <@rowout label="Last trade accuracy">${security.lastTradeDate.accuracy?replace("_", " ")}</@rowout>
        <@rowout label="Coupon type">${security.couponType}</@rowout>
        <@rowout label="Coupon rate">${security.couponRate}</@rowout>
        <@rowout label="Coupon frequency">${security.couponFrequency.conventionName}</@rowout>
        <@rowout label="Day count convention">${security.dayCount.conventionName}</@rowout>
        <#if security.businessDayConvention?has_content>
          <@rowout label="Business day convention">${security.businessDayConvention}</@rowout>
        </#if>
        <#if security.announcementDate?has_content>
          <@rowout label="Announcement date">${security.announcementDate}</@rowout>
        </#if>
        <#if security.interestAccrualDate?has_content>
            <@rowout label="Interest accrual date">${security.interestAccrualDate.toLocalDate()} - ${security.interestAccrualDate.zone}</@rowout>
        </#if>
        <#if security.settlementDate?has_content>
          <@rowout label="Settlement date">${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}</@rowout>
        </#if>
        <#if security.firstCouponDate?has_content>
            <@rowout label="First coupon date">${security.firstCouponDate.toLocalDate()} - ${security.firstCouponDate.zone}</@rowout>
        </#if>
        <#if security.issuancePrice?has_content>
          <@rowout label="Issuance price">${security.issuancePrice}</@rowout>
        </#if>
        <@rowout label="Total amount issued">${security.totalAmountIssued}</@rowout>
        <@rowout label="Minimum amount">${security.minimumAmount}</@rowout>
        <@rowout label="Minimum increment">${security.minimumIncrement}</@rowout>
        <@rowout label="Par amount">${security.parAmount}</@rowout>
        <@rowout label="Redemption value">${security.redemptionValue}</@rowout>
        <#break>
      <#case "FUTURE">
        <@rowout label="Expiry date">${security.expiry.expiry}</@rowout>
        <@rowout label="Expiry accuracy">${security.expiry.accuracy?replace("_", " ")}</@rowout>
        <@rowout label="Trading exchange">${security.tradingExchange}</@rowout>
        <@rowout label="Settlement exchange">${security.settlementExchange}</@rowout>
        <@rowout label="Redemption value">${security.currency}</@rowout>
        <@rowout label="Unit Amount">${security.unitAmount}</@rowout>
        <#if futureSecurityType == "BondFuture">
          <@rowout label="First delivery date">${security.firstDeliveryDate}</@rowout>
          <@rowout label="Last delivery date">${security.lastDeliveryDate}</@rowout>
          <@rowout label="Underlying Bond"></@rowout>
          <#list basket?keys as key>
            <@rowout label="">${key} - ${basket[key]}</@rowout>
          </#list>
        <#else>
          <#if futureSecurityType != "MetalFuture">
            <#if security.underlyingId?has_content>
              <@rowout label="Underlying identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
            </#if>
          </#if>
        </#if>

        <#break>
      <#case "EQUITY_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Exercise type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Option type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <#if underlyingSecurity?has_content>
            <@rowout label="Underlying security"><a href="${uris.security(underlyingSecurity)}">${underlyingSecurity.name}</a></@rowout>
        <#else>
            <@rowout label="Underlying identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        </#if>
        <#break>
      <#case "EQUITY_BARRIER_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Exercise type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Option type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <#if underlyingSecurity?has_content>
            <@rowout label="Underlying security"><a href="${uris.security(underlyingSecurity)}">${underlyingSecurity.name}</a></@rowout>
        <#else>
            <@rowout label="Underlying identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        </#if>

        <@rowout label="Barrier Direction">${security.barrierDirection}</@rowout>
        <@rowout label="Barrier Level">${security.barrierLevel}</@rowout>
        <@rowout label="Barrier Type">${security.barrierType}</@rowout>
        <@rowout label="Sampling Frequency">${security.samplingFrequency}</@rowout>
        <@rowout label="Monitoring Type">${security.monitoringType}</@rowout>

        <#break>
      <#case "SWAP">
        <@rowout label="Trade date">${security.tradeDate.toLocalDate()} - ${security.tradeDate.zone}</@rowout>
        <@rowout label="Effective date">${security.effectiveDate.toLocalDate()} - ${security.effectiveDate.zone}</@rowout>
        <@rowout label="Maturity date">${security.maturityDate.toLocalDate()} - ${security.maturityDate.zone}</@rowout>
        <@rowout label="Counterparty">${security.counterparty}</@rowout>
        <@rowout label="Exchange initial notional">${security.exchangeInitialNotional?string("TRUE","FALSE")}</@rowout>
        <@rowout label="Exchange final notional">${security.exchangeFinalNotional?string("TRUE","FALSE")}</@rowout>
        <@subsection title="Pay leg">
          <@rowout label="Day count">${security.payLeg.dayCount.conventionName}</@rowout>
          <@rowout label="Frequency">${security.payLeg.frequency.conventionName}</@rowout>
          <@rowout label="Region identifier">${security.payLeg.regionId}</@rowout>
          <@rowout label="Business day convention">${security.payLeg.businessDayConvention.conventionName}</@rowout>
          <@rowout label="Notional notional">${security.payLeg.notional.amount} ${security.payLeg.notional.currency}</@rowout>
	      <#switch payLegType>
	        <#case "FixedInterestRateLeg">
              <@rowout label="Interest rate leg">${security.payLeg.rate}</@rowout>
	        <#break>
	        <#case "FloatingInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.payLeg.floatingReferenceRateId}</@rowout>
              <@rowout label="Initial floating rate">${security.payLeg.initialFloatingRate}</@rowout>
	        <#break>
	        <#case "FloatingSpreadInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.payLeg.floatingReferenceRateId}</@rowout>
              <@rowout label="Initial floating rate">${security.payLeg.initialFloatingRate}</@rowout>
              <@rowout label="Spread">${security.payLeg.spread}</@rowout>
          <#break>
          <#case "FloatingGearingInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.payLeg.floatingReferenceRateId}</@rowout>
              <@rowout label="Initial floating rate">${security.payLeg.initialFloatingRate}</@rowout>
              <@rowout label="Spread">${security.payLeg.gearing}</@rowout>
          <#break>
	      </#switch>
        </@subsection>
        <@subsection title="Receive leg">
          <@rowout label="Day count">${security.receiveLeg.dayCount.conventionName}</@rowout>
          <@rowout label="Frequency">${security.receiveLeg.frequency.conventionName}</@rowout>
          <@rowout label="Region identifier">${security.receiveLeg.regionId}</@rowout>
          <@rowout label="Business day convention">${security.receiveLeg.businessDayConvention.conventionName}</@rowout>
          <@rowout label="Notional notional">${security.receiveLeg.notional.amount} ${security.payLeg.notional.currency}</@rowout>
          <#switch receiveLegType>
            <#case "FixedInterestRateLeg">
              <@rowout label="Interest rate leg">${security.receiveLeg.rate}</@rowout>
            <#break>
            <#case "FloatingInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.receiveLeg.floatingReferenceRateId}</@rowout>
              <@rowout label="Initial floating rate">${security.receiveLeg.initialFloatingRate}</@rowout>
            <#break>
            <#case "FloatingSpreadInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.receiveLeg.floatingReferenceRateId}</@rowout>
              <@rowout label="Initial floating rate">${security.receiveLeg.initialFloatingRate}</@rowout>
              <@rowout label="Spread">${security.receiveLeg.spread}</@rowout>
            <#break>
            <#case "FloatingGearingInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.receiveLeg.floatingReferenceRateId}</@rowout>
              <@rowout label="Initial floating rate">${security.receiveLeg.initialFloatingRate}</@rowout>
              <@rowout label="Spread">${security.receiveLeg.gearing}</@rowout>
            <#break>
          </#switch>
        </@subsection>
        <#break>
      <#case "CDS_INDEX_DEFINITION">
        <@rowout label="Version">${security.version}</@rowout>
        <@rowout label="Series">${security.series}</@rowout>
        <@rowout label="Family">${security.family}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Terms"><#list terms as tenor>${tenor}<#if tenor_has_next>,</#if></#list></@rowout>
        <@subsection title="Components">
          <#list components as component>
              <@rowout label="Name">${component.name}</@rowout>
              <@rowout label="Obligor Identifier">${component.obligorRedCode.scheme.name?replace("_", " ")} - ${component.obligorRedCode.value}</@rowout>
              <@rowout label="Weight">${component.weight}</@rowout>
              <#if component.bondId??>
              <@rowout label="Bond Identifier">${component.bondId.scheme.name?replace("_", " ")} - ${component.bondId.value}</@rowout>
              </#if>
              <#if component_has_next><@space /></#if>
          </#list>
        </@subsection>
        <#break>
      <#case "FX FORWARD">
        <@rowout label="Forward Date">${security.forwardDate.toLocalDate()} - ${security.forwardDate.zone}</@rowout>
        <@rowout label="Region Identifier">${security.regionId.scheme.name?replace("_", " ")} - ${security.regionId.value}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "NONDELIVERABLE_FX FORWARD">
        <@rowout label="Forward Date">${security.forwardDate.toLocalDate()} - ${security.forwardDate.zone}</@rowout>
        <@rowout label="Region Identifier">${security.regionId.scheme.name?replace("_", " ")} - ${security.regionId.value}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "FX">
        <@rowout label="Pay Amount">${security.payAmount}</@rowout>
        <@rowout label="Pay Currency">${security.payCurrency}</@rowout>
        <@rowout label="Receive Amount">${security.receiveAmount}</@rowout>
        <@rowout label="Receive Currency">${security.receiveCurrency}</@rowout>
        <@rowout label="Region">${security.regionId.scheme.name?replace("_", " ")} - ${security.regionId.value}</@rowout>
        <#break>
      <#case "FX_BARRIER_OPTION">
        <@rowout label="Barrier Direction">${security.barrierDirection}</@rowout>
        <@rowout label="Barrier Level">${security.barrierLevel}</@rowout>
        <@rowout label="Barrier Type">${security.barrierType}</@rowout>
        <@rowout label="Call Amount">${security.callAmount}</@rowout>
        <@rowout label="Call Currency">${security.callCurrency}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="IsLong">${security.isLong?string?upper_case}</@rowout>
        <@rowout label="Monitoring Type">${security.monitoringType}</@rowout>
        <@rowout label="Put Amount">${security.putAmount}</@rowout>
        <@rowout label="Put Currency">${security.putCurrency}</@rowout>
        <@rowout label="Sampling Frequency">${security.samplingFrequency}</@rowout>
        <@rowout label="Settlement Date">${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}</@rowout>
        <#break>
      <#case "FX_OPTION">
        <@rowout label="Call Amount">${security.callAmount}</@rowout>
        <@rowout label="Call Currency">${security.callCurrency}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="IsLong">${security.long?string?upper_case}</@rowout>
        <@rowout label="Put Amount">${security.putAmount}</@rowout>
        <@rowout label="Put Currency">${security.putCurrency}</@rowout>
        <@rowout label="Settlement Date">${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}</@rowout>
        <#break>
      <#case "NONDELIVERABLE_FX_OPTION">
        <@rowout label="Call Amount">${security.callAmount}</@rowout>
        <@rowout label="Call Currency">${security.callCurrency}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="IsLong">${security.long?string?upper_case}</@rowout>
        <@rowout label="Put Amount">${security.putAmount}</@rowout>
        <@rowout label="Put Currency">${security.putCurrency}</@rowout>
        <@rowout label="Settlement Date">${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}</@rowout>
        <@rowout label="Delivery Currency">${security.deliveryCurrency}</@rowout>
        <#break>
      <#case "FX_VOLATILITY_SWAP">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Base Currency">${security.baseCurrency}</@rowout>
        <@rowout label="Counter Currency">${security.counterCurrency}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Notional">${security.notional}</@rowout>
        <@rowout label="Settlement Date">${security.settlementDate}</@rowout>
        <@rowout label="Maturity Date">${security.maturityDate}</@rowout>
        <@rowout label="Volatility Swap Type">${security.volatilitySwapType}</@rowout>
        <@rowout label="Annualization Factor">${security.annualizationFactor}</@rowout>
        <@rowout label="First Observation Date">${security.firstObservationDate}</@rowout>
        <@rowout label="Last Observation Date">${security.lastObservationDate}</@rowout>
        <@rowout label="Observation Frequency">${security.observationFrequency}</@rowout>
        <#break>
      <#case "EQUITY_INDEX_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "SWAPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Is Cash Settled">${security.cashSettled?string?upper_case}</@rowout>
        <@rowout label="Is Long">${security.long?string?upper_case}</@rowout>
        <@rowout label="Is Payer">${security.payer?string?upper_case}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "IRFUTURE_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Is Margined">${security.margined?string?upper_case}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "COMMODITYFUTURE_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Trading Exchange">${security.tradingExchange}</@rowout>
        <@rowout label="Settlement Exchange">${security.settlementExchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "FXFUTURE_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Trading Exchange">${security.tradingExchange}</@rowout>
        <@rowout label="Settlement Exchange">${security.settlementExchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "BONDFUTURE_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Trading Exchange">${security.tradingExchange}</@rowout>
        <@rowout label="Settlement Exchange">${security.settlementExchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "EQUITY_INDEX_FUTURE_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Is Margined">${security.margined?string?upper_case}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "EQUITY_INDEX_DIVIDEND_FUTURE_OPTION">
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Exercise Type">${customRenderer.printExerciseType(security.exerciseType)}</@rowout>
        <@rowout label="Expiry">${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}</@rowout>
        <@rowout label="Is Margined">${security.margined?string?upper_case}</@rowout>
        <@rowout label="Option Type">${security.optionType}</@rowout>
        <@rowout label="Point Value">${security.pointValue}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Underlying Identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
        <#break>
      <#case "SWAP_INDX">
      <#case "IBOR_INDEX">
        <@rowout label="Convention Identifier">${security.conventionId.scheme.name} - ${security.conventionId.value}</@rowout>
        <#if security.indexFamilyId?has_content>
          <@rowout label="Index Family Identifier">${security.indexFamilyId.scheme.name} - ${security.indexFamilyId.value}</@rowout>
        <#else>
          <@rowout label="Index Family Identifier">(empty)</@rowout>
        </#if>
        <@rowout label="Tenor">${security.tenor.toFormattedString()}</@rowout>
        <#break>        
      <#case "PRICE_INDEX">
      <#case "OVERNIGHT_INDEX">
        <@rowout label="Convention Identifier">${security.conventionId.scheme.name} - ${security.conventionId.value}</@rowout>
        <#if security.indexFamilyId?has_content>
          <@rowout label="Index Family Identifier">${security.indexFamilyId.scheme.name} - ${security.indexFamilyId.value}</@rowout>
        <#else>
          <@rowout label="Index Family Identifier">(empty)</@rowout>
        </#if>
        <#break>  
      <#case "INDEX_FAMILY">
        <@subsection title="Family Member Entries">
          <#if members?has_content>
            <#list members?keys as key>
              <@rowout label="${key}">${members[key].scheme.name} - ${members[key].value}</@rowout>
            </#list>
          <#else>
            No Members
          </#if>
        </@subsection>
        <#break> 
    </#switch>
<@space />
<#list security.externalIdBundle.externalIds as item>
    <@rowout label="Identifier">${item.scheme.name?replace("_", " ")} - ${item.value}</@rowout>
</#list>
</@subsection>
</@section>


<#-- SECTION Update security -->
<@section title="Update security" if=!deleted && userSecurity.isPermitted('SecurityMaster:edit:update')>
  <@form method="PUT" action="${uris.security()}" id="updateSecurityForm">
  <p>
    <@rowin>
      <div id="ace-xml-editor"></div>
    </@rowin>
    <@rowin><input type="hidden" name="securityXml" id="security-xml"/></@rowin>
    <input type="hidden" name="type" value="xml"/>
    <@rowin><input type="submit" value="Update" /></@rowin>

<#noescape><@xmlEditorScript formId="updateSecurityForm" inputId="security-xml" xmlValue="${securityXml}"></@xmlEditorScript></#noescape>
  </p>
  </@form>
</@section>

<#-- SECTION Reload security -->
<@section title="Reload security" if=!deleted && userSecurity.isPermitted('SecurityMaster:edit:update')>
  <@form method="PUT" action="${uris.security()}">
  <p>
    <input type="hidden" name="type" value="id"/>
    <@rowin><input type="submit" value="Reload" /></@rowin>
  </p>
  </@form>
</@section>

<#-- SECTION Delete security -->
<@section title="Delete security" if=!deleted && userSecurity.isPermitted('SecurityMaster:edit:remove')>
  <@form method="DELETE" action="${uris.security()}">
  <p>
    <@rowin><input type="submit" value="Delete" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.securityVersions()}">History of this security</a><br />
    <a href="${uris.securities()}">Security search</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>


<#-- END -->
</@page>
</#escape>
