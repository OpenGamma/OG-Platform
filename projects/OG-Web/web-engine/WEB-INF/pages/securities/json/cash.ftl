<#escape x as x?html>
<#include "security-header.ftl"> 
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "maturity": {
              "date": "${security.maturity.toLocalDate()}",
              "zone": "${security.maturity.zone}"
          },
        "rate":"${security.rate}",
        "region":"${security.regionId?replace("_", " ")}",
<#include "security-footer.ftl"> 
</#escape>