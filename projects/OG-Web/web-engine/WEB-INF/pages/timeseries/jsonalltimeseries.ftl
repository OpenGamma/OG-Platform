<#escape x as x?html>
{
    "header": {
        "type": "Time Series",
        "dataFields": ["id","identifiers","datasource","dataprovider","datafield","observationtime"],
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "count": ${"${paging.pagingSize}"?replace(',','')}
        </#if>
    },
    "data": [<#if searchResult??><#list searchResult.documents as item>
        "${item.uniqueId.objectId}|<#list item.identifiers.iterator() as identifier>${identifier}<#if identifier_has_next>, </#if></#list>|${item.dataSource}|${item.dataProvider}|${item.dataField}|${item.observationTime}"
    <#if item_has_next>,</#if></#list> </#if>]
}
</#escape>