/*
 * Copyright 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.analytics.form.Portfolios',
    dependencies: ['og.common.util.ui.Dropdown'],
    obj: function () {
        var Block = og.common.util.ui.Block;
        var Portfolios = function (config) {
            var block = this, form = config.form, index = 'portfolio', selectedIndex;
            form.Block.call(block, {
                template: '{{{children}}}<div class="OG-icon og-icon-down"></div>',
                children: [new og.common.util.ui.Dropdown({
                    form: form, resource: 'portfolios', index: index, value: config.val,
                    rest_options: {page: '*'}, placeholder: 'Select portfolio', fields: [0, 2]
                })],
                processor: function (data) {
                    if (!data[index]) // hack to get the value of a searchable dropdown
                        data[index] = $('#' + form.id + ' select[name=' + index + ']').siblings('select').val();
                }
            });
            block.on('form:load', function () {
                var selectedIndex, selector = '#' + form.id + ' select[name=' + index + ']';
                    select = $(selector).searchable().hide(),
                    list = select.siblings('select').addClass('dropdown-list'),
                    input = select.siblings('input').attr('placeholder', 'Select portfolio').select(),
                    toggle = select.parent().siblings('.og-icon-down');

                if (select.val() !== '') input.val($('option:selected', select).text());

                input.removeAttr('style').blur(function () {
                    list.hide();
                }).keydown(function (event) {
                    if (event.keyCode === $.ui.keyCode.ESCAPE) list.hide();
                    if (event.keyCode === $.ui.keyCode.UP || event.keyCode === $.ui.keyCode.DOWN) {
                        if (input.val() === '') input.focus(0).click();
                        list.show();
                    }
                }).click(function (event) {
                    list.show().prop('selectedIndex', selectedIndex);
                    select.prop('selectedIndex', selectedIndex);
                });

                toggle.click(function (event) {
                    selectedIndex = list.prop('selectedIndex');
                    if (!selectedIndex) {
                        select.prop('selectedIndex', selectedIndex);
                        list.show().prop('selectedIndex', selectedIndex);
                    }
                    input.focus(0).click();
                });

                list.on('mousedown', function (event) {
                    selectedIndex = list.get(0).selectedIndex;
                    select.get(0).selectedIndex = selectedIndex+1;
                    input.val(list.find("option:selected").text()).focus(0);
                });
            });
        };
        Portfolios.prototype = new Block;
        return Portfolios;
    }
});