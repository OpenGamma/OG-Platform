<#escape x as x?html>
<#include "security-header.ftl"> 
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
        "exchangeInitialNotional":"${security.exchangeInitialNotional?string("TRUE", "FALSE")}",
        "exchangeFinalNotional":"${security.exchangeFinalNotional?string("TRUE", "FALSE")}",
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
          }
          <#switch payLegType>
            <#case "FixedInterestRateLeg">
              ,
              "interestRateLeg":"${security.payLeg.rate}"
            <#break>
            <#case "FloatingInterestRateLeg">
              ,
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}",
              "floatingRateType":"${security.payLeg.floatingRateType}"
            <#break>
            <#case "FloatingSpreadInterestRateLeg">
              ,
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}",
              "spread":"${security.payLeg.spread}",
              "floatingRateType":"${security.payLeg.floatingRateType}"
            <#break>
            <#case "FloatingGearingInterestRateLeg">
              ,
              "floatingReferenceRateId":"${security.payLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.payLeg.initialFloatingRate}",
              "gearing":"${security.payLeg.gearing}",
              "floatingRateType":"${security.payLeg.floatingRateType}"
            <#break>
            <#case "FixedInflationLeg">
              ,
              "interestRateLeg":"${security.payLeg.rate}"
            <#break>
            <#case "InflationIndexLeg">
              ,
              "floatingReferenceRateId":"{security.payLeg.indexId}"
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
          }
          <#switch receiveLegType>
            <#case "FixedInterestRateLeg">
              ,
              "interestRateLeg":"${security.receiveLeg.rate}"
            <#break>
            <#case "FloatingInterestRateLeg">
              ,
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}",
              "floatingRateType":"${security.receiveLeg.floatingRateType}"
            <#break>
            <#case "FloatingSpreadInterestRateLeg">
              ,
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}",
              "spread":"${security.receiveLeg.spread}",
              "floatingRateType":"${security.receiveLeg.floatingRateType}"
            <#break>
            <#case "FloatingGearingInterestRateLeg">
              ,
              "floatingReferenceRateId":"${security.receiveLeg.floatingReferenceRateId}",
              "initialFloatingRate":"${security.receiveLeg.initialFloatingRate}",
              "gearing":"${security.receiveLeg.gearing}",
              "floatingRateType":"${security.receiveLeg.floatingRateType}"
            <#break>
            <#case "FixedInflationLeg">
              ,
              "interestRateLeg":"${security.receiveLeg.rate}"
            <#break>
            <#case "InflationIndexLeg">
              ,
              "floatingReferenceRateId":"${security.receiveLeg.indexId}"
            <#break>
          </#switch>
        },
<#include "security-footer.ftl"> 
</#escape>
