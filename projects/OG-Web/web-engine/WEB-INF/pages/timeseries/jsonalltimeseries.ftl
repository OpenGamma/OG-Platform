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
        "${item.info.uniqueId.objectId}|<#list item.info.externalIdBundle.externalIds as identifier>${identifier}<#if identifier_has_next>, </#if></#list>|${item.info.dataSource}|${item.info.dataProvider}|${item.info.dataField}|${item.info.observationTime}"
    <#if item_has_next>,</#if></#list> </#if>]
}
</#escape>