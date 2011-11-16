<#escape x as x?html>
<@page title="Security - ${security.name}">

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
      <#case "CASH">
        <@rowout label="Amount">${security.amount}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Maturity">${security.maturity.toLocalDate()} - ${security.maturity.zone}</@rowout>
        <@rowout label="Rate">${security.rate}</@rowout>
        <@rowout label="Region">${security.regionId?replace("_", " ")}</@rowout>
        <#break>
      <#case "EQUITY">
        <@rowout label="Short name">${security.shortName}</@rowout>
        <@rowout label="Exchange">${security.exchange}</@rowout>
        <@rowout label="Company name">${security.companyName}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="GICS code">
          ${security.gicsCode.sectorCode}&nbsp;
          ${security.gicsCode.industryGroupCode}&nbsp;
          ${security.gicsCode.industryCode}&nbsp;
          ${security.gicsCode.subIndustryCode}
        </@rowout>
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
        <@rowout label="Interest accrual date">${security.interestAccrualDate.toLocalDate()} - ${security.interestAccrualDate.zone}</@rowout>
        <@rowout label="Settlement date">${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}</@rowout>
        <@rowout label="First coupon date">${security.firstCouponDate.toLocalDate()} - ${security.firstCouponDate.zone}</@rowout>
        <@rowout label="Issuance price">${security.issuancePrice}</@rowout>
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
        
        <#if futureSecurityType == "BondFuture">
            <@rowout label="Underlying Bond"></@rowout>
            <#list basket?keys as key>
              <@rowout label="">${key} - ${basket[key]}</@rowout>
            </#list>
        <#else>
            <@rowout label="Underlying identifier">${security.underlyingId.scheme.name?replace("_", " ")} - ${security.underlyingId.value}</@rowout>
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
      <#case "SWAP">
        <@rowout label="Trade date">${security.tradeDate.toLocalDate()} - ${security.tradeDate.zone}</@rowout>
        <@rowout label="Effective date">${security.effectiveDate.toLocalDate()} - ${security.effectiveDate.zone}</@rowout>
        <@rowout label="Maturity date">${security.maturityDate.toLocalDate()} - ${security.maturityDate.zone}</@rowout>
        <@rowout label="Counterparty">${security.counterparty}</@rowout>
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
      <#case "FX FORWARD">
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
    </#switch>
<@space />
<#list security.externalIdBundle.externalIds as item>
    <@rowout label="Identifier">${item.scheme.name?replace("_", " ")} - ${item.value}</@rowout>
</#list>
</@subsection>
</@section>


<#-- SECTION Update security -->
<@section title="Update security" if=!deleted>
  <@form method="PUT" action="${uris.security()}">
  <p>
    <@rowin><input type="submit" value="Update" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Delete security -->
<@section title="Delete security" if=!deleted>
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
