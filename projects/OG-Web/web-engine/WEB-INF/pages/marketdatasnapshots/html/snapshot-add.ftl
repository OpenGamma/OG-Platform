<#escape x as x?html>
<@page title="Add market data snapshot" jquery=true jqueryDate=true>

<#-- SECTION Add market data snapshot -->
<@section title="Add market data snapshot">
  <@form method="POST" action="${uris.snapshots()}">
    <script src="/green/js/marketDataSnapshot.js"></script>
  <p>
    <@rowin label="Snapshot Name"><input type="text" size="30" maxlength="80" name="name" value="${searchRequest.name}" /></@rowin>
    <#if err_viewMissing??><div class="err">The view name must be selected</div></#if>
    <@rowin label="View Name">
      <select name="view">
        <#if err_viewMissing??>
        <option value="" selected></option>
        </#if>
        <#list views as view> 
          <#if err_viewMissing??> 
            <option value="${view}">${view}</option>
          <#else>
            <option value="${view}" <#if view == selectedView>selected</#if>>${view}</option>
          </#if>
        </#list>
      </select>
    </@rowin>    
    <#if err_valutionTimeInvalid??><div class="err">Invalid valuation time</div></#if>
    <@rowin label="Valuation Time (HH:mm:ss)"><input type="text" size="30" maxlength="80" name="valuationTime" value="${valuationTime}" /></@rowin>
    <br/>
    <@subsection title="DataSources">
      <#if err_marketDataSpecsMissing??><div class="err">Data sources must be selected</div></#if>
      <@rowin label=""><b>Live</b><img src="/images/down.png/" class="hide" name="live"></@rowin>
      <@rowin label="" id="live">
        <#list liveDataSources as liveDataSource>
          <input type="checkbox" name="liveDataSources" value="${liveDataSource}" <#if selectedLiveDataSources?seq_contains(liveDataSource)>checked</#if>>${liveDataSource}<br/>
        </#list>
      </@rowin>
      <@rowin label=""><b>Historical</b><img src="/images/down.png/" class="hide" name="historical"></@rowin>
      <@rowin label="" id="historical">
        <#assign tsKeys = selectedTSResolverMap?keys>
        <#list timeseriesresolverkeys as tsResolverKey>
          <div><input type="checkbox" id="${tsResolverKey}_key" name="tsResolverKeys" value="${tsResolverKey}" <#if tsKeys?seq_contains(tsResolverKey)>checked</#if>>
                ${tsResolverKey}
          </div>
          <#list selectedTSResolverMap?keys as key>  
            <#if key == tsResolverKey>
              <#assign tsCustomDate = selectedTSResolverMap[key]>
            </#if>
          </#list>  
          <div><input type="text" class="datepicker" name="${tsResolverKey}_CustomDate" <#if tsKeys?seq_contains(tsResolverKey)> value="${tsCustomDate}" </#if>/></div>
          <br/>
        </#list>
      </@rowin>
      <#if (snapshots?size > 0)>
        <@rowin label=""><b>Snapshot</b><img src="/images/down.png/" class="hide" name="userSnapshot"></@rowin>
        <@rowin label="" id="userSnapshot">
          <#list snapshots as snapshot>
            <input  <#if selectedUserSnapshotIds?seq_contains(snapshot.uniqueId?string)>checked</#if>
              type="checkbox" name="userSnapshotIds" value="${snapshot.uniqueId?string}">${snapshot.uniqueId?string} ${snapshot.name}<br/>
          </#list>
        </@rowin>
      </#if>
    </@subsection>
    <@rowin><input type="submit" value="Add" /></@rowin>
  </p>
  </@form>
</@section>


<#-- SECTION Links -->
<@section title="Links">
  <p>
    <a href="${uris.snapshots()}">MarketDataSnapshot home</a><br />
    <a href="${homeUris.home()}">Home</a><br />
  </p>
</@section>
<#-- END -->
</@page>
</#escape>
