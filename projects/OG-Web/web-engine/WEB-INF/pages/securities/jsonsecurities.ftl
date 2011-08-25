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
    "data" : [<#if searchResult??><#list searchResult.documents as item>
        "${item.uniqueId.objectId}|${item.security.name}<#if searchRequest.securityType = ''>|${item.security.securityType}</#if>"<#if item_has_next>,</#if>
     </#list> </#if>]
}
</#escape>