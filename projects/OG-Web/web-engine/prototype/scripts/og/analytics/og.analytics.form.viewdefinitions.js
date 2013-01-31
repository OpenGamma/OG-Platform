/*
 * Copyright 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.ViewDefinitions',
    dependencies: ['og.common.util.ui.Dropdown'],
    obj: function () {
        var module = this, menu, Block = og.common.util.ui.Block;
        var ViewDefinitions = function (config) {
            var block = this, form = config.form, index = 'viewdefinition', title = 'calculations';
            form.Block.call(block, {
                template: '<div class="og-option-title">' +
                    '<header class="OG-background-05">' + title + ':</header>{{{children}}}</div>',
                children: [new og.common.util.ui.Dropdown({
                    form: form, index: index, resource: 'viewdefinitions', fields: ['id', 'name'],
                    style: 'width: 100%', value: config.val, placeholder: 'Select...'
                })],
                processor: function (data) {
                    if (!data[index]) // hack to get the value of a searchable dropdown
                        data[index] = $('#' + form.id + ' select[name=' + index + ']').siblings('select').val();
                }
            });
            block.on('form:load', function () {$('#' + form.id + ' select[name=' + index + ']').searchable();});
        };
        ViewDefinitions.prototype = new Block;
        return ViewDefinitions;
    }
});