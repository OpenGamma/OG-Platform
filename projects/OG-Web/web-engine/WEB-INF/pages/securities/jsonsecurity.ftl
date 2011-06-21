<#escape x as x?html>
{
    "template_data": {
    <#switch security.securityType>
      <#case "FRA">
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "endDate": {
              "date": "${security.endDate.date}",
              "zone": "${security.endDate.zone}"
          },
        "rate":"${security.rate}",
        "region":"${security.region?replace("_", " ")}",
        "startDate": {
              "date": "${security.startDate.date}",
              "zone": "${security.startDate.zone}"
         },
      <#break>
      <#case "Cash">
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "maturity": {
              "date": "${security.maturity.date}",
              "zone": "${security.maturity.zone}"
          },
        "rate":"${security.rate}",
        "region":"${security.region?replace("_", " ")}",
      <#break>
      <#case "EQUITY">
        	"shortName":"${security.shortName}",
        	"exchange":"${security.exchange}",
        	"companyName":"${security.companyName}",
        	"currency":"${security.currency}",
        	"exchangeCode":"${security.exchangeCode}",
        	"gicsCode":"${security.gicsCode}",
      <#break>
      <#case "BOND"> 
        "issuerName":"${security.issuerName}",
        "issuerType":"${security.issuerType}",
        "issuerDomicile":"${security.issuerDomicile}",
        "market":"${security.market}",
        "currency":"${security.currency}",
        "yieldConvention":"${security.yieldConvention.conventionName}",
        "lastTradeDate":"${security.lastTradeDate.expiry}",
        "lastTradeAccuracy":"${security.lastTradeDate.accuracy?replace("_", " ")}",
        "couponType":"${security.couponType}",
        "couponRate":"${security.couponRate}",
        "couponFrequency":"${security.couponFrequency.conventionName}",
        "dayCountConvention":"${security.dayCountConvention.conventionName}",
        "guaranteeType":"${security.guaranteeType}",
        "businessDayConvention":"${security.businessDayConvention}",
        "announcementDate":"${security.announcementDate}",
        "interestAccrualDate": {
            "date": "${security.interestAccrualDate.date}",
            "zone": "${security.interestAccrualDate.zone}"
        },
        "settlementDate": {
            "date": "${security.settlementDate.date}",
            "zone": "${security.settlementDate.zone}"
        },
        "firstCouponDate": {
            "date": "${security.firstCouponDate.date}",
            "zone": "${security.firstCouponDate.zone}"
        },
        "issuancePrice":"${security.issuancePrice}",
        "totalAmountIssued":"${security.totalAmountIssued}",
        "minimumAmount":"${security.minimumAmount}",
        "minimumIncrement":"${security.minimumIncrement}",
        "parAmount":"${security.parAmount}",
        "redemptionValue":"${security.redemptionValue}",
      <#break>
      <#case "FUTURE">
        "expirydate": {
            "datetime": "${security.expiry.expiry.toOffsetDateTime()}",
            "timezone": "${security.expiry.expiry.zone}"
        },
        "expiryAccuracy":"${security.expiry.accuracy?replace("_", " ")}",
        "tradingExchange":"${security.tradingExchange}",
        "settlementExchange":"${security.settlementExchange}",
        "redemptionValue":"${security.currency}",
        
        <#if futureSecurityType == "BondFuture">
            "underlying Bond":{<#list basket?keys as key>"${key}":"${basket[key]}"<#if key_has_next>,</#if></#list>},
        <#else>
            "underlyingIdentifier":"${security.underlyingIdentifier.scheme}-${security.underlyingIdentifier.value}",
        </#if>
        
        <#break>
      <#case "EQUITY_OPTION">
        "exerciseType":"${security.exerciseType}",
        "payOffStyle":"${security.payoffStyle}",
        "optionType":"${security.optionType}",
        "strike":"${security.strike}",
        "expiryDate":"${security.expiry.expiry}",
        "expiryAccuracy":"${security.expiry.accuracy?replace("_", " ")}",
        "underlyingIdentifier":"${security.underlyingIdentifier.scheme}-${security.underlyingIdentifier.value}",
        "currency":"${security.currency}",
        <#break>
      <#case "SWAP">
        "tradeDate": {
            "date": "${security.tradeDate.date}",
            "zone": "${security.tradeDate.zone}"
        },
        "effectiveDate": {
            "date": "${security.effectiveDate.date}",
            "zone": "${security.effectiveDate.zone}"
        },
        "maturityDate": {
            "date": "${security.maturityDate.date}",
            "zone": "${security.maturityDate.zone}"
        },
        "counterparty":"${security.counterparty}",
        "payLeg":{
          "dayCount":"${security.payLeg.dayCount.conventionName}",
	      "frequency":"${security.payLeg.frequency.conventionName}",
	      "regionIdentifier": {
	          "scheme": "${security.payLeg.regionIdentifier.scheme.name}",
	          "value": "${security.payLeg.regionIdentifier.value}"
	      },
	      "businessDayConvention":"${security.payLeg.businessDayConvention.conventionName}",
	      "notional": {
	        "amount": "${security.payLeg.notional.amount}",
	        "currency":"${security.payLeg.notional.currency}"
	      },
	      <#switch payLegType>
	        <#case "FixedInterestRateLeg">
              "interestRateLeg":"${security.payLeg.rate}"
	        <#break>
	        <#case "FloatingInterestRateLeg">
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateIdentifier}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}",
              "spread":"${security.payLeg.spread}"
	        <#break>
	      </#switch>
        },
        "receiveLeg":{
          "dayCount":"${security.receiveLeg.dayCount.conventionName}",
	      "frequency":"${security.receiveLeg.frequency.conventionName}",
	      "regionIdentifier": {
	          "scheme": "${security.receiveLeg.regionIdentifier.scheme.name}",
	          "value": "${security.receiveLeg.regionIdentifier.value}"
	      },
	      "businessDayConvention":"${security.receiveLeg.businessDayConvention.conventionName}",
	      "notional": {
	          "amount": "${security.receiveLeg.notional.amount}",
              "currency": "${security.payLeg.notional.currency}"
	      },
          <#switch payLegType>
            <#case "FixedInterestRateLeg">
              "interestRateLeg":"${security.receiveLeg.rate}"
            <#break>
            <#case "FloatingInterestRateLeg">
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateIdentifier}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}",
              "spread":"${security.receiveLeg.spread}"
            <#break>
          </#switch>
        },
        <#break>
    </#switch>
    "name": "${security.name}",
    "object_id": "${security.uniqueId.objectId}",
    "version_id": "${security.uniqueId.version}",
    <#if deleted>
    "deleted": "${securityDoc.versionToInstant}",
    </#if>
    "securityType":"${security.securityType}" },
    "identifiers": {<#list security.identifiers.identifiers as item> "${item.scheme.name}":"${item.scheme.name}-${item.value}"<#if item_has_next>,</#if> </#list>}
}
</#escape>