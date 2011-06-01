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
      <#case "Cash">
        <@rowout label="Amount">${security.amount}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <@rowout label="Maturity">${security.maturity.date} - ${security.maturity.zone}</@rowout>
        <@rowout label="Rate">${security.rate}</@rowout>
        <@rowout label="Region">${security.region?replace("_", " ")}</@rowout>
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
        <@rowout label="Day count convention">${security.dayCountConvention.conventionName}</@rowout>
        <#if security.businessDayConvention?has_content>
          <@rowout label="Business day convention">${security.businessDayConvention}</@rowout>
        </#if>  
        <#if security.announcementDate?has_content>
          <@rowout label="Announcement date">${security.announcementDate}</@rowout>
        </#if>  
        <@rowout label="Interest accrual date">${security.interestAccrualDate.date} - ${security.interestAccrualDate.zone}</@rowout>
        <@rowout label="Settlement date">${security.settlementDate.date} - ${security.settlementDate.zone}</@rowout>
        <@rowout label="First coupon date">${security.firstCouponDate.date} - ${security.firstCouponDate.zone}</@rowout>
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
        <#break>
      <#case "EQUITY_OPTION">
        <@rowout label="Exercise type">${security.exerciseType}</@rowout>
        <@rowout label="Pay off style">${security.payoffStyle}</@rowout>
        <@rowout label="Option type">${security.optionType}</@rowout>
        <@rowout label="Strike">${security.strike}</@rowout>
        <@rowout label="Expiry date">${security.expiry.expiry}</@rowout>
        <@rowout label="Expiry accuracy">${security.expiry.accuracy?replace("_", " ")}</@rowout>
        <@rowout label="Underlying identifier">${security.underlyingIdentifier?replace("_", " ")}</@rowout>
        <@rowout label="Currency">${security.currency}</@rowout>
        <#break>
      <#case "SWAP">
        <@rowout label="Trade date">${security.tradeDate.date} - ${security.tradeDate.zone}</@rowout>
        <@rowout label="Effective date">${security.effectiveDate.date} - ${security.effectiveDate.zone}</@rowout>
        <@rowout label="Maturity date">${security.maturityDate.date} - ${security.maturityDate.zone}</@rowout>
        <@rowout label="Counterparty">${security.counterparty}</@rowout>
        <@subsection title="Pay leg">
          <@rowout label="Day count">${security.payLeg.dayCount.conventionName}</@rowout>
	      <@rowout label="Frequency">${security.payLeg.frequency.conventionName}</@rowout>
	      <@rowout label="Region identifier">${security.payLeg.regionIdentifier}</@rowout>
	      <@rowout label="Business day convention">${security.payLeg.businessDayConvention.conventionName}</@rowout>
	      <@rowout label="Notional notional">${security.payLeg.notional.amount} ${security.payLeg.notional.currency}</@rowout>
	      <#switch payLegType>
	        <#case "FixedInterestRateLeg">
              <@rowout label="Interest rate leg">${security.payLeg.rate}</@rowout>
	        <#break>
	        <#case "FloatingInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.payLeg.floatingReferenceRateIdentifier}</@rowout>
              <@rowout label="Initial floating rate">${security.payLeg.initialFloatingRate}</@rowout>
              <@rowout label="Spread">${security.payLeg.spread}</@rowout>
	        <#break>
	      </#switch>
        </@subsection>
        <@subsection title="Receive leg">
          <@rowout label="Day count">${security.receiveLeg.dayCount.conventionName}</@rowout>
	      <@rowout label="Frequency">${security.receiveLeg.frequency.conventionName}</@rowout>
	      <@rowout label="Region identifier">${security.receiveLeg.regionIdentifier}</@rowout>
	      <@rowout label="Business day convention">${security.receiveLeg.businessDayConvention.conventionName}</@rowout>
	      <@rowout label="Notional notional">${security.receiveLeg.notional.amount} ${security.payLeg.notional.currency}</@rowout>
          <#switch payLegType>
            <#case "FixedInterestRateLeg">
              <@rowout label="Interest rate leg">${security.receiveLeg.rate}</@rowout>
            <#break>
            <#case "FloatingInterestRateLeg">
              <@rowout label="Floating reference rate id">${security.receiveLeg.floatingReferenceRateIdentifier}</@rowout>
              <@rowout label="Initial floating rate">${security.receiveLeg.initialFloatingRate}</@rowout>
              <@rowout label="Spread">${security.receiveLeg.spread}</@rowout>
            <#break>
          </#switch>
        </@subsection>
        <#break>
    </#switch>
<@space />
<#list security.identifiers.identifiers as item>
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
