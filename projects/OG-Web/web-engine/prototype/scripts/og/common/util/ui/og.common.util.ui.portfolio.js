/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.common.util.ui.Portfolio',
    dependencies: ['og.common.util.ui.Block'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var level = function (element, path) {};
        var Portfolio = function (config) {
            var block = this, id = og.common.id('portfolio'),
                form = config.form, root = config.root, node = config.node;
            form.Block.call(block, {extras: {id: id}});
            block.id = id;
            console.log(block.id);
        };
        Portfolio.prototype = new Block(null, {module: 'og.views.forms.portfolio_tash'}); // inherit Block prototype
        Portfolio.prototype.off = function () {
            var block = this, args = Array.prototype.slice.call(arguments, 1), type = arguments[0];
            return Block.prototype.off.apply(block, [type, '#' + block.id].concat(args));
        };
        Portfolio.prototype.on = function () {
            var block = this, args = Array.prototype.slice.call(arguments, 1), type = arguments[0];
            return Block.prototype.on.apply(block, [type, '#' + block.id].concat(args));
        };
        return Portfolio;
    }
});