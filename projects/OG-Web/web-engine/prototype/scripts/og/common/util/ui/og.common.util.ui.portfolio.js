/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Portfolio',
    dependencies: ['og.common.util.ui.Block'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var level = function (selector, path) {};
        var root_template = Handlebars.compile('<div id="{{id}}" class="OG-portfolio root">{{{html}}}</div>');
        var row_template = Handlebars.compile('\
            <div class="holder">\
                <div class="name">\
                    {{prefix}}<span class="node{{#collapse}} collapse{{/collapse}}{{#expand}} expand{{/expand}}\
                    " data-root="{{root}}" data-node="{{node}}" data-indent="{{indent}}"></span>\
                    &nbsp;&nbsp;&nbsp;&nbsp;{{name}}\
                </div>\
                <div class="children"></div>\
            </div>\
        ');
        var rep = (function (rep_memo, rep_str) {
            return function (times, lcv, result) {
                if (times in rep_memo) return rep_memo[times];
                if ((result = '') || (lcv = times)) while (lcv--) result += rep_str;
                return rep_memo[times] = result;
            }
        })({}, '&nbsp;&nbsp;&nbsp;');
        var Portfolio = function (config) {
            var block = this, id = og.common.id('portfolio'),
                form = config.form, root = config.root, node = config.node;
            form.Block.call(block, {generator: function (handler) {
                og.api.rest.portfolios.get({
                    id: root, node: node, name: root ? void 0 : '*', page: root ? void 0 : '*'
                }).pipe(function (result) {
                    var folders = result.data.data ? result.data.data.map(function (val) {
                        var split = val.split('|');
                        return {root: split[0], node: split[1], name: split[2], expand: !!+split.pop(), indent: 0}
                    }) : result.data.portfolios.map(function (val) {
                        return {root: root, node: val.id, name: val.name, expand: !!val.nodeCount, indent: 0};
                    });
                    handler(root_template({
                        id: id, html: folders.reduce(function (acc, val) {return acc + row_template(val);}, '')
                    }));
                });
            }});
            block.id = id;
            block.on('click', '#' + block.id + ' .node', function (event) {
                var $target = $(event.target), $parent = $target.parents('div.holder:first'),
                    indent = +$target.attr('data-indent'), root = $target.attr('data-root'),
                    node = $target.attr('data-node');
                if ($target.is('.collapse')) {
                    $parent.find('.children:first').empty();
                    $target.removeClass('collapse').addClass('expand');
                    return false;
                }
                $target.removeClass('expand');
                og.api.rest.portfolios.get({id: root, node: node}).pipe(function (result) {
                    var folders = result.data.portfolios.map(function (val) {
                        return {
                            root: root, node: val.id, name: val.name, expand: !!val.nodeCount,
                            indent: indent + 1, prefix: rep(indent + 1)
                        };
                    });
                    $parent.find('.children:first').empty()
                        .html(folders.reduce(function (acc, val) {return acc + row_template(val);}, ''));
                    $target.addClass('collapse');
                });
                return false;
            });
        };
        Portfolio.prototype = new Block; // inherit Block prototype
        return Portfolio;
    }
});