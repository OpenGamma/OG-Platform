<#escape x as x?html>
{
    "template_data": {
    <#switch security.securityType>
      <#case "FRA">
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "endDate": {
              "date": "${security.endDate.toLocalDate()}",
              "zone": "${security.endDate.zone}"
          },
        "rate":"${security.rate}",
        "region":"${security.regionId?replace("_", " ")}",
        "startDate": {
              "date": "${security.startDate.toLocalDate()}",
              "zone": "${security.startDate.zone}"
         },
      <#break>
      <#case "CASH">
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "maturity": {
              "date": "${security.maturity.toLocalDate()}",
              "zone": "${security.maturity.zone}"
          },
        "rate":"${security.rate}",
        "region":"${security.regionId?replace("_", " ")}",
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
        "dayCount":"${security.dayCount.conventionName}",
        "guaranteeType":"${security.guaranteeType}",
        "businessDayConvention":"${security.businessDayConvention}",
        "announcementDate":"${security.announcementDate}",
        "interestAccrualDate": {
            "date": "${security.interestAccrualDate.toLocalDate()}",
            "zone": "${security.interestAccrualDate.zone}"
        },
        "settlementDate": {
            "date": "${security.settlementDate.toLocalDate()}",
            "zone": "${security.settlementDate.zone}"
        },
        "firstCouponDate": {
            "date": "${security.firstCouponDate.toLocalDate()}",
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
            "underlyingBond":{<#list basket?keys as key>"${key}":"${basket[key]}"<#if key_has_next>,</#if></#list>},
        <#else>
            "underlyingId":"${security.underlyingId.scheme.name}-${security.underlyingId.value}",
        </#if>

        <#break>
      <#case "EQUITY_OPTION">
        "currency":"${security.currency}",
        "exchange":"${security.exchange}",
        "exerciseType":"${customRenderer.printExerciseType(security.exerciseType)}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "optionType":"${security.optionType}",
        "pointValue":"${security.pointValue}",
        "strike":"${security.strike}",
        "underlyingExternalId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        "underlyingName":"${underlyingSecurity.name}",
        "underlyingOid":"${underlyingSecurity.uniqueId.objectId}",
        <#break>

      <#case "SWAP">
        "tradeDate": {
            "date": "${security.tradeDate.toLocalDate()}",
            "zone": "${security.tradeDate.zone}"
        },
        "effectiveDate": {
            "date": "${security.effectiveDate.toLocalDate()}",
            "zone": "${security.effectiveDate.zone}"
        },
        "maturityDate": {
            "date": "${security.maturityDate.toLocalDate()}",
            "zone": "${security.maturityDate.zone}"
        },
        "counterparty":"${security.counterparty}",
        "payLeg":{
          "dayCount":"${security.payLeg.dayCount.conventionName}",
	      "frequency":"${security.payLeg.frequency.conventionName}",
	      "regionId": {
	          "scheme": "${security.payLeg.regionId.scheme.name}",
	          "value": "${security.payLeg.regionId.value}"
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
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}"
	        <#break>
	        <#case "FloatingSpreadInterestRateLeg">
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}",
              "spread":"${security.payLeg.spread}"
          <#break>
          <#case "FloatingGearingInterestRateLeg">
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}",
              "gearing":"${security.payLeg.gearing}"
          <#break>
	      </#switch>
        },
        "receiveLeg":{
          "dayCount":"${security.receiveLeg.dayCount.conventionName}",
	      "frequency":"${security.receiveLeg.frequency.conventionName}",
	      "regionId": {
	          "scheme": "${security.receiveLeg.regionId.scheme.name}",
	          "value": "${security.receiveLeg.regionId.value}"
	      },
	      "businessDayConvention":"${security.receiveLeg.businessDayConvention.conventionName}",
	      "notional": {
	          "amount": "${security.receiveLeg.notional.amount}",
              "currency": "${security.payLeg.notional.currency}"
	      },
          <#switch receiveLegType>
            <#case "FixedInterestRateLeg">
              "interestRateLeg":"${security.receiveLeg.rate}"
            <#break>
            <#case "FloatingInterestRateLeg">
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}"
            <#break>
            <#case "FloatingSpreadInterestRateLeg">
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}",
              "spread":"${security.receiveLeg.spread}"
            <#break>
            <#case "FloatingGearingInterestRateLeg">
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}",
              "gearing":"${security.receiveLeg.gearing}"
            <#break>
          </#switch>
        },
        <#break>
       <#case "FX_FORWARD">
        "forwardDate":"${security.forwardDate.toLocalDate()} - ${security.forwardDate.zone}",
        "region":"${security.regionId.scheme}-${security.regionId.value}",
        "underlyingId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        <#break>
       <#case "FX">
        "payAmount":"${security.payAmount}",
        "payCurrency":"${security.payCurrency}",
        "receiveAmount":"${security.receiveAmount}",
        "receiveCurrency":"${security.receiveCurrency}",
        "region":"${security.regionId.scheme}-${security.regionId.value}",
        <#break>
       <#case "FX_BARRIER_OPTION">
        "barrierDirection":"${security.barrierDirection}",
        "barrierLevel":"${security.barrierLevel}",
        "barrierType":"${security.barrierType}",
        "callAmount":"${security.callAmount}",
        "callCurrency":"${security.callCurrency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isLong":"${security.long?string?upper_case}",
        "monitoringType":"${security.monitoringType}",
        "putAmount":"${security.putAmount}",
        "putCurrency":"${security.putCurrency}",
        "samplingFrequency":"${security.samplingFrequency}",
        "settlementDate":"${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}",
        <#break>
       <#case "FX_OPTION">
        "callAmount":"${security.callAmount}",
        "callCurrency":"${security.callCurrency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isLong":"${security.long?string?upper_case}",
        "putAmount":"${security.putAmount}",
        "putCurrency":"${security.putCurrency}",
        "settlementDate":"${security.settlementDate.toLocalDate()} - ${security.settlementDate.zone}",
        <#break>
       <#case "EQUITY_INDEX_OPTION">
        "currency":"${security.currency}",
        "exchange":"${security.exchange}",
        "exerciseType":"${customRenderer.printExerciseType(security.exerciseType)}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "optionType":"${security.optionType}",
        "pointValue":"${security.pointValue}",
        "strike":"${security.strike}",
        "underlyingId":"${security.underlyingId.scheme} - ${security.underlyingId.value}",
        <#break>
       <#case "SWAPTION">
        "currency":"${security.currency}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isCashSettled":"${security.cashSettled?string?upper_case}",
        "isLong":"${security.long?string?upper_case}",
        "isPayer":"${security.payer?string?upper_case}",
        "underlyingId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        <#break>
       <#case "IRFUTURE_OPTION">
        "currency":"${security.currency}",
        "exchange":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "exerciseType":"${customRenderer.printExerciseType(security.exerciseType)}",
        "expiry":"${security.expiry.expiry.toLocalDate()} - ${security.expiry.expiry.zone}",
        "isMargined":"${security.margined?string?upper_case}",
        "optionType":"${security.optionType}",
        "pointValue":"${security.pointValue}",
        "strike":"${security.strike}",
        "underlyingId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        <#break>
    </#switch>
    "name": "${security.name}",
    "object_id": "${security.uniqueId.objectId}",
    "version_id": "${security.uniqueId.version}",
    <#if deleted>
    "deleted": "${securityDoc.versionToInstant}",
    </#if>
    "securityType":"${security.securityType}" },
    "identifiers": {<#list security.externalIdBundle.externalIds as item> "${item.scheme.name}":"${item.scheme.name}-${item.value}"<#if item_has_next>,</#if> </#list>}
}
</#escape>