<#escape x as x?html>
{
    "header" : {
        "type":"Configs",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "page": ${"${paging.page}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
        </#if>
        "dataFields":["id","name"<#if type = ''>,"type"</#if>]
    },
    "data" : [<#if searchResult??>
       <#list searchResult.documents as item>
           "${item.uniqueId.objectId}|${item.name}<#if type = ''>|${item.value.class.simpleName}</#if>"<#if item_has_next>,</#if>
       </#list>
    </#if>]
}
</#escape>