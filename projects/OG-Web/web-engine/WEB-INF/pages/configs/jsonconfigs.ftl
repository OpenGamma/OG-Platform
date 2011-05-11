<#escape x as x?html>
{
    "header" : {
        "dataFields":["id","name"<#if type = ''>,"type"</#if>],
        "type":"Configs",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "count": ${"${paging.pagingSize}"?replace(',','')}
        </#if>
    },
    "data" : [<#if searchResult??>
       <#list searchResult.documents as item>
           "${item.uniqueId.objectId}|${item.name}<#if type = ''>|${item.value.class.simpleName}</#if>"<#if item_has_next>,</#if>
       </#list>
    </#if>]
}
</#escape>