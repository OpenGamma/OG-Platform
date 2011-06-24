<#escape x as x?html>
{
    "header": {
        "type": "Time Series",
        <#if searchResult??>
        "total" : ${"${paging.totalItems}"?replace(',','')},
        "pageSize": ${"${paging.pagingSize}"?replace(',','')},
        </#if>
        "dataFields": ["id","identifier","data_source","data_provider","data_field","observation_time"]
    },
    "data": [<#if searchResult??><#list searchResult.documents as item>
        "${item.series.uniqueId.objectId}|<#list item.series.identifiers.iterator() as identifier>${identifier}<#if identifier_has_next>, </#if></#list>|${item.series.dataSource}|${item.series.dataProvider}|${item.series.dataField}|${item.series.observationTime}"
    <#if item_has_next>,</#if></#list> </#if>]
}
</#escape>