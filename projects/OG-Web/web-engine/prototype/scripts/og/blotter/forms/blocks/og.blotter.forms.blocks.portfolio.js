/**
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.blotter.forms.blocks.Portfolio',
    dependencies: ['og.common.util.ui.Form'],
    obj: function () {
        var module = this, Block = og.common.util.ui.Block;
        var Portfolio = function (config) {
            var block = this, id = og.common.id('attributes'), form = config.form;
            form.Block.call(block, {module: 'og.blotter.forms.blocks.portfolio_tash', extras: {id: id}});
        };
        Portfolio.prototype = new Block(); // inherit Block prototype
        return Portfolio;
    }
});