<#escape x as x?html>
<#include "security-header.ftl"> 
        "legalEntityId":"${security.legalEntityId.scheme.name}~${security.legalEntityId.value}",
        "currency":"${security.currency}",
        "regionId":"${security.regionId.scheme.name}~${security.regionId.value}",
        "issueDate":"${security.issueDate.toLocalDate()}",
        "maturityDate":"${security.maturityDate.expiry}",
        "maturityAccuracy":"${security.maturityDate.accuracy?replace("_", " ")}",
        "yieldConvention":"${security.yieldConvention.conventionName}",
        "dayCount":"${security.dayCount.conventionName}",
        "minimumIncrement":"${security.minimumIncrement}",
        "daysToSettle":"${security.daysToSettle}",
<#include "security-footer.ftl"> 
</#escape>