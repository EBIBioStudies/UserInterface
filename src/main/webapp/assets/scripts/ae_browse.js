//
//  AE Browse Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// query object is a global variable
var query = new Object();

function
aeClearKeywords()
{
    $("#ae_keywords").val("");
    $("#ae_wholewords").removeAttr("checked");
}

function
aeResetFilters()
{
    $("#ae_species").val("");
    $("#ae_array").val("");
}

function
aeResetOptions()
{
    $("#ae_pagesize").val("25");
    $("#ae_detailedview").removeAttr("checked");
}

function
aeSort( sortby )
{
    if ( -1 != String("accession name assays species releasedate fgem raw").indexOf(sortby) ) {
        var innerElt = $( "#ae_results_header_" + sortby ).find("div.table_header_inner");
        var sortorder = "ascending";
        if ( undefined != innerElt && innerElt.hasClass("table_header_sort_asc") )
            sortorder = "descending";

        var newQuery = $.query.set( "sortby", sortby ).set( "sortorder", sortorder ).toString()
        window.location.href = "browse.html" + newQuery;
    }
}
///////////////////////////////////////////////////////////////////////////////////////////////////

$(document).ready( function() {

    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if ( navigator.userAgent.indexOf('MSIE') != -1 ) {
        document.getElementById('head').allowTransparency = true;
    }

    if ($.browser.opera && $.browser.version < 9.5) {
        onWindowResize();
        $(window).resize( onWindowResize );
    } else {
        onWindowResize();
    }

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
    var totalAssays = $("#ae_results_total_assays").text();
    var from = $("#ae_results_from").text();
    var to = $("#ae_results_to").text();
    var curpage = $("#ae_results_page").text();
    var pagesize = $("#ae_results_pagesize").text();


    if ( total > 0 ) {

        // assign valid hrefs to save to tab/xls
        $("#ae_results_save a").attr("href", "ArrayExpress-Experiments.txt" + $.query.toString());
        $("#ae_results_save_xls a").attr("href", "ArrayExpress-Experiments.xls" + $.query.toString());
        // show controls
        $("#ae_results_save").show();
        $("#ae_results_save_xls").show();

        var totalPages = total > 0 ? Math.floor( total / pagesize ) + 1 : 0;
        $("#ae_results_status").html(
            total + " experiment" + (total != 1 ? "s" : "" ) + ", " +
            totalAssays + " assay" + (totalAssays != 1 ? "s" : "" ) + "." +
            ( totalPages > 1 ? (" Displaying experiments " + from + " to " + to + ".") : "" )
            );

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
        $("#ae_species").html(data).removeAttr("disabled").val(query.species);
        
    });

    $.get("servlets/query/arrays-select/html").next( function(data) {
        addHtmlToSelect("ae_array", data);
        $("#ae_array").removeAttr("disabled").val(query.array);
    });

    if ( "" != query.sortby ) {
        var thElt = $("#ae_results_header_" + query.sortby);
        if ( null != thElt ) {
            thElt.addClass("table_header_box_selected").removeClass("table_header_box").removeClass("sortable");

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

function
addHtmlToSelect( selectEltId, html )
{
    if ( $.browser.opera ) {
        var htmlParsed = $.clean( new Array(html) );
        var select = $( "#" + selectEltId ).empty();
        for ( var i = 0; i < htmlParsed.length; i++ ) {
            select[0].appendChild(htmlParsed[i].cloneNode(true));
        }
    } else {
        $( "#" + selectEltId ).html(html);
    }
}