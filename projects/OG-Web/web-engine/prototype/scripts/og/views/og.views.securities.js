/*
 * @copyright 2009 - present by OpenGamma Inc
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
            search,
            ui = common.util.ui,
            layout = og.views.common.layout,
            module = this,
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
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
                        'OK': function () {
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
                    title: 'Delete Security?',
                    message: 'Are you sure you want to permanently delete this security?',
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
                    'selector': '.OG-js-search', 'page_type': 'securities',
                    'columns': [
                        {
                            id: 'type', toolTip: 'type',
                            name: null,
                            field: 'type', width: 100, filter_type: 'select'
                        },
                        {
                            id: 'name', toolTip: 'name',
                            name: '<input type="text" placeholder="Name" '
                                + 'class="og-js-name-filter" style="width: 280px;">',
                            field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input'
                        }
                    ]
                },
                toolbar: {
                    'default': {
                        buttons: [
                            {name: 'delete', enabled: 'OG-disabled'},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
                    },
                    active: {
                        buttons: [
                            {name: 'delete', handler: toolbar_buttons['delete']},
                            {name: 'new', handler: toolbar_buttons['new']}
                        ],
                        location: '.OG-toolbar'
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
                    var $html = $.tmpl(template, {
                        name: 'Securities',
                        recent_list: history.get_html('history.securities.recent') || 'no recently viewed securities'
                    });
                    $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                    $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                    og.views.common.layout.inner.close('north'), $('.ui-layout-inner-north').empty();
                    ui.toolbar(options.toolbar['default']);
                }});
            },
            details_page = function (args) {
                api.rest.securities.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message);
                        var json = result.data, text_handler,
                            security_type = json.template_data.securityType.toLowerCase(),
                            template = module.name + '.' + security_type;
                        history.put({
                            name: json.template_data.name,
                            item: 'history.securities.recent',
                            value: routes.current().hash
                        });
                        api.text({module: template, handler: text_handler = function (template, error) {
                            if (error) {
                                og.dev.warn('using default security template for security type: ' + security_type);
                                return api.text({module: module.name + '.default', handler: text_handler});
                            }
                            var error_html = '\
                                    <section class="OG-box og-box-glass og-box-error OG-shadow-light">\
                                        This security has been deleted\
                                    </section>\
                                ',
                                $html = $.tmpl(template, json.template_data),
                                layout = og.views.common.layout,
                                html = [], id, json_id = json.identifiers;
                            $('.ui-layout-inner-center .ui-layout-header').html($html.find('> header'));
                            $('.ui-layout-inner-center .ui-layout-content').html($html.find('> section'));
                            ui.toolbar(options.toolbar.active);
                            if (json.template_data && json.template_data.deleted) {
                                $('.ui-layout-inner-north').html(error_html);
                                layout.inner.sizePane('north', '0');
                                layout.inner.open('north');
                                $('.OG-toolbar .og-js-delete').addClass('OG-disabled').unbind();
                            } else {
                                layout.inner.close('north');
                                $('.ui-layout-inner-north').empty();
                            }
                            for (id in json_id) {
                                if (json_id.hasOwnProperty(id)) {
                                    html.push('<tr><td><span>', json_id[id].split('-')[0],
                                              '<span></td><td>', json_id[id].split('-')[1], '</td></tr>');
                                }
                            }
                            $('.OG-security .og-js-identifiers').html(html.join(''));
                            details.favorites();
                            ui.message({location: '.ui-layout-inner-center', destroy: true});
                        }});
                    },
                    id: args.id,
                    loading: function () {
                        ui.message({
                            location: '.ui-layout-inner-center',
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
                route: '/' + page_name + '/:id/name:?/type:?', method: module.name + '.load_' + page_name
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
                    }}
                ]});
                if (args.id) return;
                default_details_page();
            },
            load_filter: function (args) {
                var search_filter = function () {
                        var filter_name = options.slickgrid.columns[0].name;
                        if (!filter_name || filter_name === 'loading') // wait until type filter is populated
                            return setTimeout(search_filter, 500);
                        search.filter($.extend(args, {filter: true}));
                };
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
                search_filter();
            },
            load_delete: function (args) {securities.search(args), routes.go(routes.hash(module.rules.load, {}));},
            load_new_securities: load_securities_without.partial('new'),
            load_securities: function (args) {
                check_state({args: args, conditions: [{new_page: securities.load}]});
                securities.details(args);
            },
            search: function (args) {
                if (!search) search = common.search_results.core();
                if (options.slickgrid.columns[0].name === 'loading')
                    return setTimeout(securities.search.partial(args), 500);
                if (options.slickgrid.columns[0].name === null) return api.rest.securities.get({
                    meta: true,
                    handler: function (result) {
                        options.slickgrid.columns[0].name = [
                            '<select class="og-js-type-filter" style="width: 80px">',
                            result.data.types.reduce(function (acc, type) {
                                return acc + '<option value="' + type + '">' + type + '</option>';
                            }, '<option value="">Type</option>'),
                            '</select>'
                        ].join('');
                        securities.search(args);
                    },
                    loading: function () {options.slickgrid.columns[0].name = 'loading';}
                });
                search.load($.extend(options.slickgrid, {url: args}));
            },
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});