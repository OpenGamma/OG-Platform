/*
 * Copyright 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
$.register_module({
    name: 'og.views.common.default_details',
    dependencies: [
        'og.api.text',
        'og.views.common.layout',
        'og.common.util.ui.toolbar',
        'og.common.util.history'
    ],
    obj: function () {
        return function (page_name, title, options, handler) {
            og.api.text({module: 'og.views.default', handler: function (template) {
                var layout = og.views.common.layout,
                    $html = $.tmpl(template, {
                        name: title,
                        recent_list: og.common.util.history.get_html('history.' + page_name + '.recent') ||
                            'no recently viewed ' + page_name
                    });
                $('.OG-layout-admin-details-center .ui-layout-header').html($html.find('> header'));
                $('.OG-layout-admin-details-center .ui-layout-content').html($html.find('> section'));
                layout.inner.options.south.onclose = null;
                layout.inner.close('north'), $('.OG-layout-admin-details-north').empty();
                layout.inner.close('south'), $('.OG-layout-admin-details-south').empty();
                if (!og.views.common.layout.inner.state.south.isClosed) {og.views.common.versions.clear()}
                // if options are not passed in, do not create a toolbar
                if (options) og.common.util.ui.toolbar(options.toolbar['default']);
                setTimeout(layout.inner.resizeAll);
                if (handler) handler();
            }});
        }
    }
});