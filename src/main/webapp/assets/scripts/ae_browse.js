//
//  AE Browse Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// runs on page reload after rendering is done

var query = new Object();

$(document).ready( function() {

    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if ( navigator.userAgent.indexOf('MSIE') != -1 ) {
        document.getElementById('head').allowTransparency = true;
    }

    $("#ae_add_filter_select").change( onAddFilterChange );

    if ($.browser.opera && $.browser.version < 9.5) {
        onWindowResize();
        $(window).resize( onWindowResize );
    } else {
        onWindowResize();
    }

    $("#ae_results_hdr th.sortable").each(addSortableHandlers);

    if ("" != $.query.get("keywords"))
        query.keywords = $.query.get("keywords");

    if ("" != $.query.get("wholewords"))
        query.wholewords = true;

    query.species = $.query.get("species");
    query.array = $.query.get("array");

    if ("" != $.query.get("page"))
        query.page = $.query.get("page");

    if ("" != $.query.get("pagesize"))
        query.pagesize = $.query.get("pagesize");
    else
        query.pagesize = "25";

    if ("" != $.query.get("sortby"))
        query.sortby = $.query.get("sortby");
    else
        query.sortby = "releasedate";

    if ("" != $.query.get("sortorder"))
        query.sortorder = $.query.get("sortorder");
    else
        query.sortorder = "descending";

    if ("" != $.query.get("detailedview"))
        query.detailedview = true;

    initControls();

    $("#ae_results_body_inner").ajaxError(onQueryError);
    $.get( "servlets/query/browse-experiments/html", query ).next(onExperimentQuery);
});

function
onAddFilterChange()
{
    $("#ae_add_filter_select").hide();
    $("#ae_add_filter_select option:selected").removeAttr("selected");
    $("#ae_add_filter_select option:first").attr( "selected", "true" );
    $("#ae_add_filter").before("<div><label for=\"ae_filter_1\">Species</label><select class=ae_filter id=ae_filter_1><option>Select species ...</option></select>");
    $("#ae_filter_1").focus();
    $("#ae_add_filter_select").show();
}

function
onWindowResize()
{
    var outerWidth = $("#ae_results_body").width();
    var innerWidth = $("#ae_results_body table").width();
    var padding = outerWidth - innerWidth;
    if ( padding > 0 && padding < 30 ) {
        $("#ae_results_hdr").css( "right", padding + "px" );
    } else if ( padding == 0 && $.browser.opera && $.browser.version < 9.5 ) {
        $("#ae_results_hdr").css( "right", "-1px" );
    }
}

function
onExperimentQuery( tableHtml )
{
    // remove progress gif
    $("#ae_results_body_inner").removeClass("ae_results_table_loading");

    // populate table with data
    $("#ae_results_tbody").html(tableHtml);

    // adjust header width to accomodate scroller (for Opera <9.5)
    if ($.browser.opera && $.browser.version < 9.5)
        onWindowResize();

    // get stats from the first row
    var total = $("#ae_results_total").text();
    var totalHybs = $("#ae_results_total_hybs").text();
    var from = $("#ae_results_from").text();
    var to = $("#ae_results_to").text();
    var curpage = $("#ae_results_page").text();
    var pagesize = $("#ae_results_pagesize").text();


    $("#ae_results_status").html( total + " experiments, " + totalHybs + " hybridizations. Displaying experiments " + from + " to " + to + "." );

    var totalPages = total > 0 ? Math.floor( total / pagesize ) + 1 : 0;
    var pagesAround = 10;
    if ( totalPages > 1 ) {
        var pagerHtml = "Pages: ";
        for ( var page = 1; page <= totalPages; page++ ) {
            if ( curpage == page ) {
                pagerHtml = pagerHtml + "" + page + "";
            } else if ( 2 == page && curpage > pagesAround && totalPages > 20 ) {
                pagerHtml = pagerHtml + "..";
            } else if ( totalPages - 1 == page && totalPages - curpage > pagesAround && totalPages > 20 ) {
                pagerHtml = pagerHtml + "..";
            } else if ( 1 == page || ( Math.abs( curpage - page ) <= pagesAround ) || totalPages == page || totalPages <= 20 ) {
                var newQuery = $.query.set( "page", page ).set( "pagesize", pagesize ).toString()
                pagerHtml = pagerHtml + "<a href=\"browse.html" + newQuery + "\">" + page + "</a>";
            }
            if ( page < totalPages ) {
                pagerHtml = pagerHtml + " ";
            }
        }
        $("#ae_results_pager").html( pagerHtml );
    }
    // attach handlers
    $(".ae_results_tr_main").each(addExpansionHandlers);

}

function
onQueryError()
{
    $(this).removeClass("ae_results_table_loading");
    $("#ae_results_tbody").html("<tr class=\"ae_results_tr_error\"><td colspan=\"8\">There was an error processing the query. Please try again later.</td></tr>");
}

function
initControls()
{
    // keywords
    $("#ae_keywords").val(query.keywords);
    if (query.wholewords)
        $("#ae_wholewords").attr("checked","true");
    $("#ae_sortby").val(query.sortby);
    $("#ae_sortorder").val(query.sortorder);
    $("#ae_pagesize").val(query.pagesize);
    if (query.detailedview)
        $("#ae_detailedview").attr("checked","true");

    $.get("servlets/query/species-select/html").next( function(data) {
        $("#ae_species").html(data).val(query.species).removeAttr("disabled");
        
    });

    $.get("servlets/query/arrays-select/html").next( function(data) {
        $("#ae_array").html(data).val(query.array).removeAttr("disabled");
    });

    if ( "" != query.sortby ) {
        var thElt = $("#ae_results_header_" + query.sortby);
        if ( null != thElt ) {
            thElt.addClass("table_header_box_selected");
            thElt.removeClass("table_header_box");


            if ( "" != query.sortorder) {
                var divElt = thElt.find("div.table_header_inner");
                if ( null != divElt ) {
                    divElt.addClass( "descending" == query.sortorder ? "table_header_sort_desc" : "table_header_sort_asc" );
                }
            }
        }

    }
}

function
addSortableHandlers()
{
    var elt = $(this);
    elt.mouseover(onHeaderMouseOver).mouseout(onHeaderMouseOut).click(onHeaderClick);
}

function
onHeaderMouseOver()
{
    $(this).addClass("table_header_box_active");
}

function
onHeaderMouseOut()
{
    $(this).removeClass("table_header_box_active");
}

function
onHeaderClick()
{
    var sortby = $(this).attr("id");
    sortby = sortby.substring(sortby.lastIndexOf("_") + 1, sortby.length);
    var innerElt = $(this).find("div.table_header_inner");
    var sortorder = "ascending";
    if ( undefined != innerElt && innerElt.hasClass("table_header_sort_asc") )
        sortorder = "descending";

    var newQuery = $.query.set( "sortby", sortby ).set( "sortorder", sortorder ).toString()
    window.location.href = "browse.html" + newQuery;
}

function
addExpansionHandlers()
{
    var elt = $(this);
    elt.mouseover(onTableRowMouseOver).mouseout(onTableRowMouseOut).click(onTableRowClick);
}

function
onTableRowMouseOver()
{
    $(this).addClass("tr_main_active");
}

function
onTableRowMouseOut()
{
    $(this).removeClass("tr_main_active");
}

function
onTableRowClick( eventObj )
{
    if (eventObj) {
        var target = String(eventObj.target.tagName).toLowerCase();
        if ( "img" == target || "a" == target )
            return;
    }
    var mainElt = $(this);
    var extElt = $("#" + mainElt.attr("id").replace("_main", "_ext"));
    if ( mainElt.hasClass("tr_main_expanded")) {
        // collapse now
        mainElt.removeClass("tr_main_expanded");
        extElt.hide();
    } else {
        mainElt.addClass("tr_main_expanded");
        extElt.show();
    }
    onWindowResize();
}
