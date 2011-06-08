/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.portfolios',
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
            page_name = module.name.split('.').pop(),
            check_state = og.views.common.state.check.partial('/' + page_name),
            details_json = {},
            portfolios,
            toolbar_buttons = {
                'new': function () {
                    ui.dialog({
                        type: 'input',
                        title: 'Add New Portfolio',
                        fields: [{type: 'input', name: 'Portfolio Name', id: 'name'}],
                        buttons: {
                            "Ok": function () {
                                $(this).dialog('close');
                                api.rest.portfolios.put({
                                    handler: function (r) {
                                        if (r.error) return ui.dialog({type: 'error', message: r.message});
                                        routes.go(routes.hash(module.rules.load_new_portfolios,
                                                $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
                                        ));
                                    },
                                    name: ui.dialog({return_field_value: 'name'})
                                });
                            }
                        }
                    })
                },
                'delete': function () {
                    ui.dialog({
                        type: 'confirm',
                        title: 'Delete portfolio?',
                        message: 'Are you sure you want to permanently delete this portfolio?',
                        buttons: {
                            'Delete': function () {
                                var obj = {
                                    id: routes.last().args.id,
                                    handler: function (r) {
                                        var last = routes.last(), args_obj = {};
                                        if (r.error) return ui.dialog({type: 'error', message: r.message});
                                        if (details_json.template_data.parent_node_id) {
                                            args_obj.node = details_json.template_data.parent_node_id;
                                            args_obj.id = details_json.template_data.id;
                                        }
                                        routes.go(routes.hash(module.rules.load_delete,
                                                $.extend(true, {}, last.args, {deleted: true}, args_obj)
                                        ));
                                    }
                                };
                                if (routes.last().args.node) obj.node = routes.last().args.node;
                                $(this).dialog('close');
                                api.rest.portfolios.del(obj);
                            }
                        }
                    })
                }
            },
            options = {
                slickgrid: {
                    'selector': '.og-js-results-slick', 'page_type': page_name,
                    'columns': [
                        {id: 'name', name: 'Name', field: 'name', width: 300, cssClass: 'og-link', filter_type: 'input'}
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
            default_details_page = function () {
                og.api.text({module: 'og.views.default', handler: function (template) {
                    $.tmpl(template, {
                        name: 'Portfolios',
                        favorites_list: history.get_html('history.portfolios.favorites') || 'no favorited portfolios',
                        recent_list: history.get_html('history.portfolios.recent') || 'no recently viewed portfolios',
                        new_list: history.get_html('history.portfolios.new') || 'no new portfolios'
                    }).appendTo($('#OG-details .og-main').empty());
                }});
            },
            details_page = function (args) {
                var hook_up_add_portfolio_form = function () {
                        var $input = $('.OG-portfolio .og-js-create-portfolio-node'), $button = $input.find('+ button'),
                            do_update;
                        do_update = function () {
                            if ($input.val() === ('' || 'name')) return;
                            api.rest.portfolios.put({
                                handler: function (r) {
                                    if (r.error) {ui.dialog({type: 'error', message: r.message}); return}
                                    routes.go(routes.hash(module.rules.load_new_portfolios,
                                            $.extend({},routes.current().args, {'new': true})
                                    ));
                                },
                                name: $input.val(),
                                id: details_json.template_data.id,
                                node: details_json.template_data.node,
                                'new': true
                            });
                        };
                        $input.unbind('keydown').bind('keydown',
                                function (e) {if (e.keyCode + '' === '13') do_update();});
                        $button.unbind('click').bind('click', function (e) {e.stopPropagation(), do_update();});
                        ui.toggle_text_on_focus.set_selector('.OG-portfolio .og-js-create-portfolio-node');
                    },
                    hook_up_add_position_form = function () {
                        var $input = $('.OG-portfolio .og-js-add-position'), $button = $input.find('+ button'),
                            do_update;
                        do_update = function (e, id) {
                            if (e && e.keyCode === 13) return; // If enter was pressed on the autosuggest list
                            api.rest.portfolios.put({
                                handler: function (r) {
                                    if (r.error) return ui.dialog({type: 'error', message: r.message});
                                    // TODO: prevent search from reloading
                                    routes.go(routes.hash(module.rules.load_new_portfolios,
                                         $.extend({}, routes.last().args,
                                             {id: details_json.template_data.id, 'new': true})
                                    ));
                                },
                                position: id ? id.item.value : $input.val(),
                                id: details_json.template_data.id,
                                node: details_json.template_data.node
                           });
                        };
                        $input.autocomplete({
                            source: function (obj, callback) {
                                api.rest.positions.get({
                                    handler: function (r) {
                                        callback(
                                            r.data.data.map(function (val) {
                                                var arr = val.split('|');
                                                return {value: arr[0], label: arr[1], id: arr[0], node: arr[1]};
                                            })
                                        );
                                    },
                                    loading: '', page_size: 10, page: 1,
                                    identifier: '*' + obj.term.replace(/\s/g, '*') + '*'
                                });
                            },
                            minLength: 1,
                            select: function (e, ui) {do_update(e, ui);}
                        });
                        $button.unbind('click').bind('click', function (e) {e.stopPropagation(), do_update();});
                        $input.bind('keydown', function (e) {if (e.keyCode + '' === '13') do_update();});
                        ui.toggle_text_on_focus.set_selector('.OG-portfolio .og-js-add-position');
                    },
                    render_portfolio_rows = function (selector, json, handler) {
                        var $parent = $(selector), id = json.template_data.id, portfolios = json.portfolios,
                            rule = og.views.portfolios.rules['load_portfolios'], length = portfolios.length,
                            render, iterator, CHUNK = 500;
                        if (!portfolios[0]) return $parent.html('<tr><td>No Portfolios</td></tr>'), handler();
                        $parent.empty();
                        iterator = function (acc, val) {
                            acc.push(
                                '<tr><td><a href="#', routes.hash(rule, {id: id, node: val.id}), '">',
                                val.name, '</a></td></tr>'
                            );
                            return acc;
                        };
                        render = function (start, end) {
                            if (start >= length) return handler();
                            var str = portfolios.slice(start, end).reduce(iterator, []).join('');
                            $parent.append(str);
                            setTimeout(render.partial(end, end + CHUNK), 0);
                        };
                        render(0, CHUNK);
                    },
                    render_position_rows = function (selector, json, handler) {
                        var $parent = $(selector), positions = json.positions, length = positions.length, render,
                            iterator, rule = og.views.positions.rules['load_positions'], CHUNK = 500;
                        if (!positions[0]) return $parent.html('<tr><td colspan="2">No Positions</td></tr>'), handler();
                        $parent.empty();
                        iterator = function (acc, val) {
                            acc.push(
                                '<tr><td><a href="#', routes.hash(rule, {id: val.id}), '">', val.name,
                                '</a></td><td>', val.quantity, '</td></tr>'
                            );
                            return acc;
                        };
                        render = function (start, end) {
                            if (start >= length) return handler();
                            var str = positions.slice(start, end).reduce(iterator, []).join('');
                            $parent.append(str);
                            setTimeout(render.partial(end, end + CHUNK), 0);
                        };
                        render(0, CHUNK);
                    };
                ui.toolbar(options.toolbar.active);
                api.rest.portfolios.get({
                    handler: function (result) {
                        if (result.error) return alert(result.message); // TODO: replace with UI error dialog
                        details_json = result.data;
                        history.put({
                            name: details_json.template_data.name,
                            item: 'history.portfolios.recent',
                            value: routes.current().hash
                        });
                        og.api.text({module: module.name, handler: function (template) {
                            var stop_loading = (function () {
                                var total_parts = 2, parts_loaded = 0;
                                return function () {
                                    if (++parts_loaded !== total_parts) return;
                                    ui.message({location: '#OG-details', destroy: true})
                                };
                            })(),
                            $warning, warning_message = 'This portfolio has been deleted';
                            $.tmpl(template, details_json.template_data).appendTo($('#OG-details .og-main').empty());
                            $warning = $('#OG-details .OG-warning-message');
                            if (details_json.template_data.deleted) $warning.html(warning_message).show();
                                else $warning.empty().hide();
                            hook_up_add_portfolio_form(), hook_up_add_position_form();
                            render_portfolio_rows('.OG-portfolio .og-js-portfolios', details_json, stop_loading);
                            render_position_rows('.OG-portfolio .og-js-positions', details_json, stop_loading);
                            ui.content_editable({
                                attribute: 'data-og-editable',
                                handler: function () {
                                    routes.go(routes.hash(module.rules.load_edit_portfolios, $.extend(args, {
                                        edit: 'true'
                                    })));
                                }
                            });
                            details.favorites();
                        }});
                    },
                    id: args.id,
                    node: args.node,
                    loading: function () {
                        ui.message({location: '#OG-details', message: {0: 'loading...', 3000: 'still loading...'}});
                    }
                });
            },
            load_portfolios_without = function (field, args) {
                check_state({args: args, conditions: [{new_page: portfolios.load}]});
                delete args[field];
                portfolios.search(args);
                routes.go(routes.hash(module.rules.load_portfolios, args));
            };
        module.rules = {
            load: {route: '/' + page_name + '/name:?', method: module.name + '.load'},
            load_filter_node: {
                route: '/' + page_name + '/filter:/:id/:node?/name:?', method: module.name + '.load_filter'
            },
            load_filter: {
                route: '/' + page_name + '/filter:/:id?/name:?', method: module.name + '.load_filter'
            },
            load_delete_node: {
                route: '/' + page_name + '/deleted:/:id/:node?/name:?', method: module.name + '.load_delete'
            },
            load_delete: {
                route: '/' + page_name + '/deleted:/:id?/name:?', method: module.name + '.load_delete'
            },
            load_portfolios: {
                route: '/' + page_name + '/:id/:node?/name:?', method: module.name + '.load_' + page_name
            },
            load_new_portfolios: {
                route: '/' + page_name + '/:id/:node?/new:/name:?', method: module.name + '.load_new_' + page_name
            },
            load_edit_portfolios: {
                route: '/' + page_name + '/:id/:node?/edit:/name:?', method: module.name + '.load_edit_' + page_name
            }
        };
        return portfolios = {
            load: function (args) {
                check_state({args: args, conditions: [
                    {new_page: function (args) {
                        portfolios.search(args);
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
                        portfolios.load(args);
                        args.id
                            ? routes.go(routes.hash(module.rules.load_portfolios, args))
                            : routes.go(routes.hash(module.rules.load, args));
                    }}
                ]});
                delete args['filter'];
                search.filter($.extend(args, {filter: true}));
            },
            load_delete: function (args) {
                portfolios.search(args);
                portfolios.load($({name: args.name}, args));
            },
            load_new_portfolios: load_portfolios_without.partial('new'),
            load_edit_portfolios: load_portfolios_without.partial('edit'),
            load_portfolios: function (args) {
                check_state({args: args, conditions: [{new_page: portfolios.load}]});
                portfolios.details(args);
            },
            search: function (args) {search.load($.extend(options.slickgrid, {url: args}));},
            details: details_page,
            init: function () {for (var rule in module.rules) routes.add(module.rules[rule]);},
            rules: module.rules
        };
    }
});