<#escape x as x?html>
{
    "header": {
        "type": "Securities",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.pageNumber}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
        </#if>
        "dataFields": ["id","name" <#if searchRequest.securityType = ''>, "type"</#if>]
    },
    "data" : [<#if searchResult??><#list searchResult.documents as item>
        "${item.uniqueId.objectId}|${item.security.name}<#if searchRequest.securityType = ''>|${item.security.securityType}</#if>"<#if item_has_next>,</#if>
     </#list> </#if>]
}
</#escape>