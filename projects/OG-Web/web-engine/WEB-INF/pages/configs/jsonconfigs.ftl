<#escape x as x?html>
{
    "header" : {
        "type":"Configs",
        <#if searchResult??>
        "pgIdx": ${"${paging.firstItem}"?replace(',','')},
        "pgSze": ${"${paging.pagingSize}"?replace(',','')},
        "pgTtl": ${"${paging.totalItems}"?replace(',','')},
        </#if>
        "dataFields":["id","name"<#if type = ''>,"type"</#if>]
    },
    "data" : [<#if searchResult??>
       <#list searchResult.documents as doc>
           "${doc.uniqueId.objectId}|${doc.config.name}<#if type = ''>|${doc.config.value.class.simpleName}</#if>"<#if doc_has_next>,</#if>
       </#list>
    </#if>]
}
</#escape>