/*
 * @copyright 2009 - 2011 by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.securities',
    dependencies: [
        'og.api.rest',
        'og.api.text',
        'og.common.masthead.menu',
        'og.common.routes',
        'og.common.search_results.core',
        'og.common.util.history',
        'og.common.util.ui.dialog',
        'og.common.util.ui.message',
        'og.common.util.ui.toolbar',
        'og.views.common.layout',
        'og.views.common.state'
    ],
    obj: function () {
        var api = og.api,
            common = og.common,
            details = common.details,
            history = common.util.history,
            masthead = common.masthead,
            routes = common.routes,
            search = common.search_results.core(),
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this,
            page_name = 'securities',
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            securities,
            toolbar_buttons = {
                'new': function () {ui.dialog({
                    type: 'input',
                    title: 'Add Securities',
                    fields: [
                        {type: 'select', name: 'Scheme Type', id: 'scheme-type',
                                options: [
                                    {name: 'Bloomberg Ticker', value: 'BLOOMBERG_TICKER'},
                                    {name: 'Bloomberg Ticker/Coupon/Maturity', value: 'BLOOMBERG_TCM'},
                                    {name: 'Bloomberg BUID', value: 'BLOOMBERG_BUID'},
                                    {name: 'CUSIP', value: 'CUSIP'},
                                    {name: 'ISIN', value: 'ISIN'},
                                    {name: 'RIC', value: 'RIC'},
                                    {name: 'SEDOL', value: 'CSEDOL1'}
                                ]
                        },
                        {type: 'textarea', name: 'Identifiers', id: 'identifiers'}
                    ],
                    buttons: {
                        'Ok': function () {
                            $(this).dialog('close');
                            api.rest.securities.put({
                                handler: function (r) {
                                    if (r.error) return ui.dialog({type: 'error', message: r.message});
                                    if (r.data.data.length === 1) {
                                        routes.go(routes.hash(module.rules.load_new_securities,
                                                $.extend({}, routes.last().args, {
                                                    id: r.data.data[0].split('|')[1], 'new': true
                                                })
                                        ));
                                    } else routes.go(routes.hash(module.rules.load));
                                },
                                scheme_type: ui.dialog({return_field_value: 'scheme-type'}),
                                identifier: ui.dialog({return_field_value: 'identifiers'})
                            });
                        }
                    }
                })},
                'delete': function () {ui.dialog({
                    type: 'confirm',
                    title: 'Delete portfolio?',
                    message: 'Are you sure you want to permanently delete this portfolio?',
                    buttons: {
                        'Delete': function () {
                            var obj = {
                                id: routes.last().args.id,
                                handler: function (r) {
                                    var last = routes.last();
                                    if (r.error) return ui.dialog({type: 'error', message: r.message});
                                    routes.go(routes.hash(module.rules.load_delete,
                                            $.extend(true, {deleted: true}, last.args)
                                    ));
                                }
                            };
                            $(this).dialog('close');
                            api.rest.securities.del(obj);
                        }
                    }
                })}
            },
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': 'securities',
                    'columns': [
                        {
                            id: 'type', name: 'Type', field: 'type', width: 80,
                            filter_type: 'select',
                            filter_type_options: ['BOND', 'CASH', 'OPTION', 'FRA', 'FUTURE', 'EQUITY', 'SWAP']
                        },
                        {
                            id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link',
                            filter_type: 'input'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', enabled: 'OG-disabled'},
                            {name: 'edit', enabled: 'OG-disabled'},
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'favorites', enabled: 'OG-disabled'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    },
                    active: {
                        buttons: [
                            {name: 'new', handler: toolbar_buttons['new']},
                            {name: 'up', handler: 'handler'},
                            {name: 'edit', handler: 'handler'},
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'favorites', handler: 'handler'}
                        ],
                        location: '.OG-toolbar .og-js-buttons'
                    }
                }
            },
            load_securities_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: securities.load, stop: true}]});
                delete args[field];
                securities.search(args);
                routes.go(routes.hash(module.rules.load_securities, args));
            },
            default_details_page = function () {
                api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Securities',
                        favorites_list: history.get_html('history.securities.favorites') || 'no favorited securities',
                        recent_list: history.get_html('history.securities.recent') || 'no recently viewed securities',
                        new_list: history.get_html('history.securities.new') || 'no new securities'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            },
            details_page = function (args) {
                ui.toolbar(options.toolbar.active);
                api.rest.securities.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        details_json = result.data;
                        history.put({
                            name: details_json.templateData.name,
                            item: 'history.securities.recent',
                            value: routes.current().hash
                        });
                        api.text({module: module.name + '.' + details_json.templateData.securityType,
                                handler: function (template) {
                            var $warning, warning_message = 'This security has been deleted',
                                html = [], id, json = details_json.identifiers;
                            $.tmpl(template, details_json.templateData).appendTo($('#OG-details .og-main').empty());
                            $warning = $('#OG-details .OG-warning-message');
                            if (details_json.templateData.deleted) $warning.html(warning_message).show();
                                else $warning.empty().hide();
                            for (id in json) if (json.hasOwnProperty(id))
                                    html.push('<div><strong>', json[id], '</strong></div>');
                            $('.OG-security .og-js-identifiers').html(html.join(''));
                            details.favorites();
                            ui.message({location: '#OG-details', destroy: true});
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '#OG-details',
                            message: {0: 'loading...', 3000: 'still loading...'}
                        });
                    }
                });
            },
            state = {};
        module.rules = {
            load: {route: '/' + page_name + '/name:?/type:?', method: module.name + '.load'},
            load_filter: {route: '/' + page_name + '/filter:/:id?/name:?/type:?',
                    method: module.name + '.load_filter'},
            load_delete: {
                route: '/' + page_name + '/:id/deleted:/name:?/type:?', method: module.name + '.load_delete'
            },
            load_securities: {
                route: '/' + page_name + '/:id/name:?/type:?/type:?', method: module.name + '.load_' + page_name
            },
            load_new_securities: {
                route: '/' + page_name + '/:id/new:/name:?/type:?', method: module.name + '.load_new_' + page_name
            }
        };
        return securities = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        securities.search(args);
                        masthead.menu.set_tab(page_name);
                        layout('default');
                    }}
                ]});
                if (args.id) return;
                default_details_page();
                ui.toolbar(options.toolbar['default']);
            },
            load_filter: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function () {
                        state = {filter: true};
                        securities.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_securities, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function (args) {securities.search(args), routes.go(routes.hash(module.rules.load, {}));},
            load_new_securities: load_securities_without.partial('new'),
            load_securities: function (args) {
                check_state({args: args, conditions: [{new_page: securities.load}]});
                securities.details(args);
            },
            search: function (args) {search.load($.extend(options.slickgrid, {url: args}));},
            details: function (args) {details_page(args);},
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});