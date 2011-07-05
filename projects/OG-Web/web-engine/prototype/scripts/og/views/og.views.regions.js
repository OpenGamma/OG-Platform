/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.regions',
    dependencies: [
        'og.common.routes',
        'og.common.masthead.menu',
        'og.common.search_results.core',
        'og.common.util.ui.message',
        'og.views.common.layout',
        'og.common.layout.resize',
        'og.common.util.ui.toolbar',
        'og.common.util.history'
    ],
    obj: function () {
        var api = og.api.rest, routes = og.common.routes, module = this, regions,
            masthead = og.common.masthead, search = og.common.search_results.core(), details = og.common.details,
            ui = og.common.util.ui, layout = og.views.common.layout, history = og.common.util.history,
            page_name = module.name.split('.').pop(),
            resize = og.common.layout.resize,
            check_state = og.views.common.state.check.partial('/' + page_name),
            search_options = {
                'selector': '.OG-js-search', 'page_type': 'regions',
                'columns': [
                    {id: 'name', field: 'name',width: 300, cssClass: 'og-link', filter_type: 'input',
                        name: '<input type="text" placeholder="Name" class="og-js-name-filter" style="width: 280px;">'}
                ]
            },
            default_toolbar_options = {
                buttons: [
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'new', enabled: 'OG-disabled'}
                ],
                location: '.OG-toolbar'
            },
            active_toolbar_options = {
                buttons: [
                    {name: 'delete', enabled: 'OG-disabled'},
                    {name: 'new', enabled: 'OG-disabled'}
                ],
                location: '.OG-toolbar'
            },
            default_page = function () {
                og.api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Regions',
                        recent_list: history.get_html('history.regions.recent') || 'no recently viewed regions'
                    }).appendTo($('.OG-js-details-panel .OG-details').empty());
                    ui.toolbar(default_toolbar_options);
                    $('.OG-js-details-panel .og-box-error').empty().hide(), resize();
               }});
            },
            new_page = function (args) {
                masthead.menu.set_tab(page_name);
                layout('default');
                regions.search(args);
            };
        module.rules = {
            load: {route: '/' + page_name + '/name:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?', method: module.name + '.load_filter'},
            load_regions: {route: '/' + page_name + '/:id/:node?/name:?', method: module.name + '.load_' + page_name}
        };
        return regions = {
            details: function (args) {
                api.regions.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var f = details.region_functions;
                        var json = result.data;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.regions.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            $.tmpl(template, json.template_data).appendTo($('.OG-js-details-panel .OG-details').empty());
                            $('.OG-js-details-panel .og-box-error').empty().hide(), resize();
                            f.render_keys('.OG-region .og-js-keys', json.keys);
                            f.render_regions('.OG-region .og-js-parent_regions', json.parent);
                            f.render_regions('.OG-region .og-js-child_regions', json.child);
                            ui.message({location: '.OG-js-details-panel', destroy: true});
                            ui.toolbar(active_toolbar_options);
                            resize({element: '.OG-details-container', offsetpx: -41});
                            resize({element: '.OG-details-container .og-details-content', offsetpx: -48});
                            details.favorites();
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '#OG-details',
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    },
                    update: regions.details.partial(args)
                });
            },
            load: function (args) {
                check_state({args: args, conditions: [{new_page: new_page}]});
                if (!args.id) default_page();
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [{
                    new_page: function () {
                        state = {filter: true};
                        regions.load(args);
                        return args.id ? routes.go(routes.hash(module.rules.load_regions, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }
                }]});
                search.filter(args);
            },
            load_regions: function (args) {
                check_state({args: args, conditions: [{new_page: regions.load}]});
                regions.details(args);
            },
            search: function (args) {search.load($.extend(search_options, {url: args}));},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});