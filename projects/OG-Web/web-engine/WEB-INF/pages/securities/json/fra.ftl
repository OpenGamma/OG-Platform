<#escape x as x?html>
<#include "security-header.ftl"> 
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "endDate": {
              "date": "${security.endDate.toLocalDate()}",
              "zone": "${security.endDate.zone}"
          },
        "rate":"${security.rate}",
        "region":"${security.regionId?replace("_", " ")}",
        "underlyingId":"${security.underlyingId?replace("_", " ")}",
        "underlyingExternalId":"${security.underlyingId.scheme}-${security.underlyingId.value}",
        "startDate": {
              "date": "${security.startDate.toLocalDate()}",
              "zone": "${security.startDate.zone}"
         },
         <#if underlyingSecurity??>
          "underlyingName":"${underlyingSecurity.name}",
          "underlyingOid":"${underlyingSecurity.uniqueId.objectId}",
        </#if>
<#include "security-footer.ftl"> 
</#escape>