/*
 * @copyright 2009 - present by OpenGamma Inc
 * @license See distribution for license
 */
$.register_module({
    name: 'og.views.common.layout',
    dependencies: [],
    obj: function () {
        var main_layout_settings = {
            defaults: {
                enableCursorHotkey: false
            },
            north: {
                spacing_open: 0,
                paneClass: 'ui-layout-masthead',
                size: 43
            },
            south: {
                spacing_open: 0,
                paneClass: 'ui-layout-footer',
                size: 36
            },
            east: {
                initClosed: true,
                spacing_closed: 0
            },
            west: {
              size: '33%',
              paneClass: 'ui-layout-search',
              contentSelector: '.ui-layout-content'
            },
            center: {
              paneClass: 'ui-layout-details'
            }
        },
        details_layout_settings = {
            defaults: {
                enableCursorHotkey: false
            },
            center: {
                paneSelector: '.ui-layout-inner-center',
                contentSelector: '.ui-layout-content',
                paneClass: 'ui-layout-inner-center'
            },
            north: {
                paneSelector: '.ui-layout-inner-north',
                paneClass: 'ui-layout-inner-north',
                size: 50,
                initClosed: true,
                spacing_closed: 0,
                spacing_open: 0
            },
            south: {
                paneSelector: '.ui-layout-inner-south',
                paneClass: 'ui-layout-inner-south',
                size: '50%',
                initClosed: true,
                spacing_closed: 0,
                onopen_start: function () {$('.ui-layout-inner-south').empty()}
            }
        },
        analytics = {
            defaults: {
                enableCursorHotkey: false
            },
            north: {
                spacing_open: 0,
                paneClass: 'ui-layout-masthead',
                size: 43
            },
            south: {
                spacing_open: 0,
                paneClass: 'ui-layout-footer',
                size: 36
            },
            east: {
                initClosed: true,
                spacing_closed: 0
            },
            west: {
                size: '33%',
                initClosed: true,
                spacing_closed: 0,
                spacing_open: 0
            },
            center: {
                paneClass: 'ui-layout-analytics',
                contentSelector: '.ui-layout-content'
            }
        },
        default_view = function () {
            return {
                main: $('.ui-layout-container').layout(main_layout_settings),
                inner: $('.ui-layout-center').layout(details_layout_settings)
            }
        },
        analytics_view = function () {
            return {
                main: $('.ui-layout-container').layout(analytics)
            }
        };
        return {
            'default': default_view,
            'analytics': analytics_view
        }
    }
});