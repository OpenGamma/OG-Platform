<#escape x as x?html>
<#include "security-header.ftl"> 
        "amount":"${security.amount}",
        "currency":"${security.currency}",
        "maturity": {
              "date": "${security.settlement.toLocalDate()}",
              "zone": "${security.settlement.zone}"
          },
<#include "security-footer.ftl"> 
</#escape>