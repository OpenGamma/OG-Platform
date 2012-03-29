<!doctype html>
<!--[if IE 8 ]><html lang="en" class="no-js ie8"><![endif]-->
<!--[if IE 9 ]><html lang="en" class="no-js ie9"><![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"> <!--<![endif]-->
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="SKYPE_TOOLBAR" content="SKYPE_TOOLBAR_PARSER_COMPATIBLE" />
<title>OpenGamma</title>
<!--[if lt IE 9]><script type="text/javascript" src="/prototype/scripts/lib/html5.js"></script><![endif]-->
${ogStyle.print('og_all.css', 'all',false)}
</head>
<body>
<div class="OG-layout-admin-container">
  <div class="ui-layout-center"></div>
  <div class="ui-layout-north"><#include "modules/common/og.common.masthead.ftl"></div>
  <div class="ui-layout-south"><#include "modules/common/og.common.footer.ftl"></div>
</div>
<!--[if IE]>${ogScript.print('ie.js',false)}<![endif]-->
${ogScript.print('og_analytics.js',false)}
<script type="text/javascript" charset="utf-8">
    var fake_data = [
        {
            'template_data': {'data_field': 'PX_LAST', 'observation_time': 'ABC'},
            'timeseries': {
                'fieldLabels': ['Time', 'Value'],
                'data': [
                    [977184000000, 93.04],
                    [977270400000, 93.125],
                    [977356800000, 93.16],
                    [977443200000, 93.16],
                    [977788800000, 93.12],
                    [977875200000, 93.06],
                    [977961600000, 93],
                    [978048000000, 92.985],
                    [978393600000, 93.26],
                    [978480000000, 93.005],
                    [978566400000, 93.17],
                    [978652800000, 93.135]
                ]    
            }
        },
        {
            'template_data': {'data_field': 'PX_LAST', 'observation_time': 'DEF'},
            'timeseries': {
                'fieldLabels': ['Time', 'Value'],
                'data': [
                    [977184000000, 92.04],
                    [977270400000, 92.125],
                    [977356800000, 92.16],
                    [977443200000, 92.16],
                    [977788800000, 92.12],
                    [977875200000, 92.06],
                    [977961600000, 92],
                    [978048000000, 91.985],
                    [978393600000, 92.26],
                    [978480000000, 92.005],
                    [978566400000, 92.17],
                    [978652800000, 92.135]
                ]    
            }
        },
        {
            'template_data': {'data_field': 'PX_LAST', 'observation_time': 'GHI'},
            'timeseries': {
                'fieldLabels': ['Time', 'Value'],
                'data': [
                    [977184000000, 94.04],
                    [977270400000, 94.125],
                    [977356800000, 94.16],
                    [977443200000, 94.16],
                    [977788800000, 94.12],
                    [977875200000, 94.06],
                    [977961600000, 94],
                    [978048000000, 93.985],
                    [978393600000, 94.26],
                    [978480000000, 94.005],
                    [978566400000, 94.17],
                    [978652800000, 94.135]
                ]    
            }
        },
        {
            'template_data': {'data_field': 'PX_LAST', 'observation_time': 'JKL'},
            'timeseries': {
                'fieldLabels': ['Time', 'Value'],
                'data': [
                    [977184000000, 91.04],
                    [977270400000, 91.125],
                    [977356800000, 91.16],
                    [977443200000, 91.16],
                    [977788800000, 91.12],
                    [977875200000, 91.06],
                    [977961600000, 91],
                    [978048000000, 90.985],
                    [978393600000, 91.26],
                    [978480000000, 91.005],
                    [978566400000, 91.17],
                    [978652800000, 91.135]
                ]    
            }
        }
    ];
    var launch_ts_gadget = function (data) {
        var iframe, key = (new Date).getTime() + (function (len, str) {
            while (len) len--, str += String.fromCharCode(65 + Math.floor(Math.random() * 26));
            return str;
        })(5, '');
        iframe = '<iframe src="/jax/bundles/fm/prototype/gadget.ftl#/timeseries/key=' + key + '"\
            scrolling="no" title="Timeseries"></iframe>';
        og.api.common.set_cache(key, data);
        $(iframe).appendTo('body').dialog({
            autoOpen: true, height: 345, width: 875, modal: false,
            resizable: false, buttons: {'Close': function () {$(this).dialog('close').remove();}}
        }).css({height: '320px', width: '850px'});
    };
  $.register_module({
      name: 'og.views.gadget_demo',
      dependencies: ['og.views.common.state', 'og.views.common.layout', 'og.common.routes'],
      obj: function () {
          og.views.common.layout = og.views.common.layout.analytics();
          var api = og.api.rest, routes = og.common.routes, module = this, view,
              layout = og.views.common.layout, masthead = og.common.masthead,
              page_name = module.name.split('.').pop(),
              check_state = og.views.common.state.check.partial('/' + page_name);
          module.rules = {load: {route: '/', method: module.name + '.load'}};
          return view = {
              load: function (args) {
                  masthead.menu.set_tab(page_name);
                  $('.ui-layout-center').html($('<a href="#">LAUNCH</a>').click(function (e) {
                    e.preventDefault();
                    launch_ts_gadget(fake_data);
                  }));
              },
              init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
              rules: module.rules
          };
      }
  });
</script>
</body>
</html>