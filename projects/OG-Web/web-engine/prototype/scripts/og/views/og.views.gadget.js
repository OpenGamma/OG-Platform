/*
 * @copyright 2012 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.gadget',
    dependencies: ['og.common', 'og.api'],
    obj: function () {
        var module = this, view, common = og.common, routes = common.routes;
        return view = {
            init: function () {for (var rule in view.rules) routes.add(view.rules[rule]);},
            root: function () {$('#gadget_content').html('No gadget was specified.');},
            timeseries: function (args) {
                var options = {selector: '.OG-timeseries-gadget', datapoints_link: false};
                $('#gadget_content').html('<section class="' + options.selector.substring(1) + '"></section>');
                if (args.id) options.id = args.id; else options.data = og.api.common.get_cache(args.key);
                if (args.key) og.api.common.del_cache(args.key);
                if (!options.data && !options.id) return $('#gadget_content').html('There is no data to load.');
                common.gadgets.timeseries(options);
            },
            rules: {
                root: {route: '/', method: module.name + '.root'},
                timeseries: {route: '/timeseries/id:?/key:?', method: module.name + '.timeseries'}
            }
        }
    }
});
