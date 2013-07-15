<#escape x as x?html>
<#include "security-header.ftl"> 
        "factors":[
          <#list factorExposuresList as factorExposure>
            {
              "factorType":"${factorExposure.factorType}",
              "factorName":"${factorExposure.factorName}",
              "node":"${factorExposure.node}",
              "priceTsId":"${factorExposure.priceTsId}",
              "lastPrice":"${factorExposure.lastPrice}",
              "exposureTsId":"${factorExposure.exposureTsId}",
              "lastExposure":"${factorExposure.lastExposure}",
              "convexityTsId":"${factorExposure.convexityTsId}",
              "lastConvexity":"${factorExposure.lastConvexity}"
            }
            <#if factorExposure_has_next>,</#if>
          </#list>
        ],
<#include "security-footer.ftl"> 
</#escape>
