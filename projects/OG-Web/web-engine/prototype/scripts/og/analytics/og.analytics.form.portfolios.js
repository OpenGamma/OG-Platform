/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.Portfolios',
    dependencies: ['og.common.util.ui.Dropdown'],
    obj: function () {
        var module = this, menu, Block = og.common.util.ui.Block;
        var Portfolios = function (config) {
            var block = this, form = config.form, index = 'portfolio', title = 'portfolio';
            form.Block.call(block, {
                template: '<div class="og-option-title">' +
                    '<header class="OG-background-05">' + title + ':</header>{{{children}}}</div>',
                children: [new og.common.util.ui.Dropdown({
                    form: form, resource: 'portfolios', index: index, value: config.val,
                    rest_options: {page: '*'}, placeholder: 'Select...', fields: [0, 2]
                })],
                processor: function (data) {
                    if (!data[index]) // hack to get the value of a searchable dropdown
                        data[index] = $('#' + form.id + ' select[name=' + index + ']').siblings('select').val();
                }
            });
            block.on('form:load', function () {$('#' + form.id + ' select[name=' + index + ']').searchable().focus();});
        };
        Portfolios.prototype = new Block;
        return Portfolios;
    }
});