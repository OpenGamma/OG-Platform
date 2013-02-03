/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Portfolio',
    dependencies: ['og.common.util.ui.Block'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var get_folders = function (list, portfolio, indent) {
            return list.map(function (val) {
                return {
                    portfolio: portfolio, node: val.id, name: val.name,
                    state: val.nodeCount + val.positionCount ? ' expand' : '', indent: indent, prefix: rep(indent)
                };
            });
        };
        var get_files = function (list, portfolio, indent) {
            return list.map(function (val) {
                return {portfolio: portfolio, node: val.id, name: val.name, indent: indent, prefix: rep(indent)};
            });
        };
        var root_template = Handlebars.compile('<div id="{{id}}" class="OG-portfolio root">{{{html}}}</div>');
        var rep = (function (rep_memo, rep_str) {
            return function (times, lcv, result) {
                if (times in rep_memo) return rep_memo[times];
                if ((result = '') || (lcv = times)) while (lcv--) result += rep_str;
                return rep_memo[times] = result;
            }
        })({}, '&nbsp;&nbsp;&nbsp;&nbsp;');
        var Portfolio = function (config) {
            var block = this, id = og.common.id('portfolio'), value = config.value || {}, index = config.index,
                form = config.form, root = value.root;
            form.Block.call(block, {generator: function (handler) {
                og.api.rest.portfolios.get({
                    id: root, node: value.node, name: root ? void 0 : '*', page: root ? void 0 : '*'
                }).pipe(function (result) {
                    var main = folders = result.data.data ? result.data.data.map(function (val) {
                        var split = val.split('|');
                        return {
                            portfolio: split[0], node: split[1], name: split[2],
                            state: +split.pop() + +split.pop() ? ' expand' : '', indent: 0
                        };
                    }) : get_folders(result.data.portfolios, portfolio, 0),
                    files = result.data.data ? [] : get_files(result.data.positions, portfolio, 0);
                    main = new form.Block({
                        module: 'og.views.forms.portfolio_tash', extras: {folders: folders, files: files}
                    });
                    main.html(function (html) {handler(root_template({id: id, html: html}));});
                });
            }});
            block.id = id;
            block.on('click', '#' + block.id + ' .folder', function (event) {
                var $target = $(event.target), $parent = $target.parents('div.holder:first'),
                    indent = +$target.attr('data-indent'), portfolio = $target.attr('data-portfolio'),
                    node = $target.attr('data-node');
                if ($target.is('.collapse')) {
                    $parent.find('.children:first').empty();
                    $target.removeClass('collapse').addClass('expand');
                    return false;
                }
                $target.removeClass('expand');
                og.api.rest.portfolios.get({id: portfolio, node: node}).pipe(function (result) {
                    var folders = get_folders(result.data.portfolios, portfolio, indent + 1),
                        files = get_files(result.data.positions, portfolio, indent + 1);
                    new form.Block({
                        module: 'og.views.forms.portfolio_tash', extras: {folders: folders, files: files}
                    }).html(function (html) {
                        $parent.find('.children:first').empty().html(html);
                        $target.addClass('collapse');
                    });
                });
                return false;
            });
        };
        Portfolio.prototype = new Block; // inherit Block prototype
        return Portfolio;
    }
});