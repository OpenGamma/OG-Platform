<#escape x as x?html>
<#include "security-header.ftl"> 
        "legalEntityId":"${security.legalEntityId.scheme.name}~${security.legalEntityId.value}",
        "currency":"${security.currency}",
        "regionId":"${security.regionId.scheme.name}~${security.regionId.value}",
        "issueDate":"${security.issueDate.toLocalDate()}",
        "maturityDate":"${security.maturityDate.expiry}",
        "maturityAccuracy":"${security.maturityDate.accuracy?replace("_", " ")}",
        "dayCount":"${security.dayCount.conventionName}",
        "minimumIncrement":"${security.minimumIncrement}",
        "daysToSettle":"${security.daysToSettle}",
        "resetDays":"${security.resetDays}",
        "benchmarkRateId":"${security.benchmarkRateId.scheme.name}~${security.benchmarkRateId.value}",
        "spread":"${security.spread}",
        "leverageFactor":"${security.leverageFactor}",
        "couponFrequency":"${security.couponFrequency.conventionName}",
<#include "security-footer.ftl"> 
</#escape>