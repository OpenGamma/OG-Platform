<#escape x as x?html>
{
    "header": {
        "type": "Securities",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields": ["id","name" <#if searchRequest.securityType = ''>, "type"</#if>]
    },
    "data" : [<#if searchResult??><#list searchResult.securities as security>
        "${security.uniqueId.objectId}|${security.name}<#if searchRequest.securityType = ''>|${security.securityType}</#if>"<#if security_has_next>,</#if>
     </#list> </#if>]
}
</#escape>