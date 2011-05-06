<#escape x as x?html>
{
    "templateData": {
      "name":"${security.name}",
      "objectId":"${security.uniqueId.objectId}",
      "versionId":"${security.uniqueId.version}",
<#-- deprecated -->
      "uniqueId":{"Value":"${security.uniqueId.value}","Scheme":"${security.uniqueId.scheme}","Version":"${security.uniqueId.version}"},
<#if deleted>
      "deleted":"${securityDoc.versionToInstant}",
</#if>
    	"securityType":"${security.securityType}",
    <#switch security.securityType>
      <#case "EQUITY">
    	"shortName":"${security.shortName}",
    	"exchange":"${security.exchange}",
    	"companyName":"${security.companyName}",
    	"currency":"${security.currency}",
    	"exchangeCode":"${security.exchangeCode}",
    	"gicsCode":"${security.gicsCode}"
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
        "redemptionValue":"${security.redemptionValue}"
      <#break>
      <#case "FUTURE">
        "expirydate": {
            "datetime": "${security.expiry.expiry.toOffsetDateTime()}",
            "timezone": "${security.expiry.expiry.zone}"
        },
        "expiryAccuracy":"${security.expiry.accuracy?replace("_", " ")}",
        "tradingExchange":"${security.tradingExchange}",
        "settlementExchange":"${security.settlementExchange}",
        "redemptionValue":"${security.currency}"
        <#break>
      <#case "EQUITY_OPTION">
        "exerciseType":"${security.exerciseType}",
        "payOffStyle":"${security.payoffStyle}",
        "optionType":"${security.optionType}",
        "strike":"${security.strike}",
        "expiryDate":"${security.expiry.expiry}",
        "expiryAccuracy":"${security.expiry.accuracy?replace("_", " ")}",
        "underlyingIdentifier":"${security.underlyingIdentifier?replace("_", " ")}",
        "currency":"${security.currency}"
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
        }
        <#break>
    </#switch>
    },
    "identifiers": {<#list security.identifiers.identifiers as item> "${item.scheme.name}":"${item.scheme.name}-${item.value}"<#if item_has_next>,</#if> </#list>}
}
</#escape>