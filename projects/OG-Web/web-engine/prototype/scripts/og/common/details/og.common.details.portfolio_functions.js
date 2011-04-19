/**
 * @copyright 2009 - 2010 by OpenGamma Inc
 * @license See distribution for license
 */

$.register_module({
    name: 'og.common.details.portfolio_functions',
    dependencies: ['og.common.routes', 'og.common.util.ui.dialog'],
    obj: function () {
        var routes = og.common.routes, ui = og.common.util.ui, api = og.api.rest,
        /**
         *
         * @param selector
         * @param json
         */
        render_breadcrumb = function (selector, json) {
            var b = [];
            if (json.portfolioName) b.push(portfolioName);
            b.push(json.name);

        },
        /**
         *
         * @param selector
         * @param json
         */
        render_portfolio_rows = function (selector, json, handler) {
            var $parent = $(selector), id = json.templateData.id, portfolios = json.portfolios,
                rule = og.views.portfolios.rules['load_portfolios'], length = portfolios.length,
                render, iterator, CHUNK = 500;
            if (!portfolios[0]) return $parent.html('<tr><td>No Portfolios</td></tr>'), handler();
            $parent.empty();
            iterator = function (acc, val) {
                acc.push(
                    '<tr><td><a href="#', routes.hash(rule, {id: id, node: val.id}), '">', val.name, '</a></td></tr>'
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
        /**
         *
         * @param selector
         * @param json
         */
        render_position_rows = function (selector, json, handler) {
            var $parent = $(selector), positions = json.positions, length = positions.length, render, iterator,
                rule = og.views.positions.rules['load_positions'], CHUNK = 500;
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
        },
        /**
         *
         * @param json
         */
        hook_up_portfolio_button = function (json) {
            var $input = $('.OG-portfolio .og-js-create-portfolio-node'),
                $button = $input.find('+ button');
            $button.unbind('click').bind('click', function (e) {
                e.stopPropagation();
                if ($input.val() === ('' || 'name')) return;
                api.portfolios.put({
                    handler: function (r) {
                        if (r.error) {
                            ui.dialog({type: 'error', message: r.message});
                            return
                        }
                        routes.go(routes.hash(og.views.portfolios.rules.load_new_portfolios,
                                $.extend({},routes.current().args, {'new': true})
                        ));
                    },
                    name: $input.val(),
                    id: json.templateData.id,
                    node: json.templateData.node,
                    'new': true
                });
            });
        },
        /**
         *
         */
        hook_up_position_add = function () {
            $('.OG-portfolio .og-js-add-position input').autocomplete({
                source: function (obj, callback) {
                    og.api.rest.positions.get({
                        handler: function (r) {
                            callback(
                                r.data.data.map(function (val) {
                                    var arr = val.split('|');
                                    return {value: arr[0], label: arr[1], id: arr[0], node: arr[1]};
                                })
                            );
                        },
                        loading: '',
                        page_size: 10,
                        page: 1,
                        identifier: '*' + obj.term.replace(/\s/g, '*') + '*'
                    });
                },
                minLength: 1,
                select: function (e, ui) {
                        // TODO: API not implemented yet
//                        api.portfolios.put({
//                            handler: function (r) {
//                                if (r.error) return og.common.util.ui.dialog({type: 'error', message: r.message});
//                                routes.go(routes.hash(module.rules.load_new_portfolios,
//                                        $.extend({}, routes.last().args, {id: r.meta.id, 'new': true})
//                                ));
//                            },
//                            position_id: ui.item.value, id: 'DbPos~97338', node: 'DbPrt~97339'
//                        });
                }
            });
        };

        return {
            render_portfolio_rows: render_portfolio_rows,
            render_position_rows: render_position_rows,
            hook_up_portfolio_button: hook_up_portfolio_button,
            hook_up_position_add: hook_up_position_add,
            render_breadcrumb: render_breadcrumb
        };

    }
});