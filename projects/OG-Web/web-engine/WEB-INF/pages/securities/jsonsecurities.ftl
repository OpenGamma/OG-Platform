<#escape x as x?html>
{
    "header": {
        "dataFields": ["id","name" <#if searchRequest.securityType = ''>, "type"</#if>],
        "type": "Securities",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "count": ${"${paging.pagingSize}"?replace(',','')}
        </#if>
    },
    "data" : [<#if searchResult??><#list searchResult.documents as item>
        "${item.uniqueId.objectId}|${item.security.name}<#if searchRequest.securityType = ''>|${item.security.securityType}</#if>"<#if item_has_next>,</#if>
     </#list> </#if>]
}
</#escape>