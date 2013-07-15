/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.gadget',
    dependencies: ['og.common', 'og.api'],
    obj: function () {
        var module = this, view, common = og.common, routes = common.routes,
            gadgets = common.gadgets, content = '.OG-gadget-container', $content = $(content);
        return view = {
            init: function () {
                for (var rule in view.rules)
                    routes.add(view.rules[rule]);
            },
            block: function (args) {
                var block = args.block.split('.').reduce(function (acc, val) {return acc[val];}, window), form;
                form = new og.common.util.ui.Form({selector: content});
                form.children.push(new block({form: form}));
                form.dom();
            },
            gadgetscontainer: function (args) {
                og.api.rest.compressor.get({content: args.data}).pipe(function (result) {
                    var gadgets = new og.common.gadgets.GadgetsContainer('.OG-gadgets-container-', 'center');
                    gadgets.add(result.data.data);
                    $(window).off('unload.gadgetscontainer').on('unload.gadgets.container', function () {
                        $('.OG-gadgets-container-center').remove();
                        gadgets.alive();
                    });
                });
            },
            grid: function (args) {
                og.api.rest.compressor.get({content: args.data}).pipe(function (result) {
                    new og.common.gadgets.Grid({selector: content, sparklines: false, source: result.data.data});
                });
            },
            positions: function (args) {
                $content.html('\
                    <section class="OG-gadgets-positions-container og-js-positions"></section>\
                    <section class="og-js-trades"></section>\
                ');
                gadgets.positions({
                    id: args.id, selector: '.og-js-positions', editable: false, external_links: true
                });
                if (args.trades === 'true')
                    gadgets.trades({id: args.id, selector: '.og-js-trades', editable: false, height: 150});
            },
            root: function () {$content.html('No gadget was specified.');},
            securities: function (args) {
                $content.html('<section></section>');
                new gadgets.SecuritiesIdentifiers({id: args.id, selector: '#gadget_content section'});
            },
            timeseries: function (args) {
                var options = {selector: '.OG-timeseries-container', datapoints_link: false};
                $content.html('<section class="' + options.selector.substring(1) + '"></section>');
                if (args.id) options.id = args.id; else options.data = og.api.common.cache_get(args.key);
                if (args.key) og.api.common.cache_del(args.key);
                if (!options.data && !options.id) return $('#gadget_content').html('There is no data to load.');
                new og.common.Timeseries(options);
            },
            rules: {
                root: {route: '/', method: module.name + '.root'},
                block: {route: '/block/:block', method: module.name + '.block'},
                gadgetscontainer: {route: '/gadgetscontainer/:data', method: module.name + '.gadgetscontainer'},
                grid: {route: '/grid/:data', method: module.name + '.grid'},
                positions: {route: '/positions/:id/trades:?', method: module.name + '.positions'},
                securities: {route: '/securities/:id', method: module.name + '.securities'},
                timeseries: {route: '/timeseries/id:?/key:?', method: module.name + '.timeseries'}
            }
        }
    }
});
