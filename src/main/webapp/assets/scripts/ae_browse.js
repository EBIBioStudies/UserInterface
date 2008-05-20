//
//  AE Browse Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

// runs on page reload after rendering is done
$(document).ready( function() {

    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if ( navigator.userAgent.indexOf('MSIE') != -1 ) {
        document.getElementById('head').allowTransparency = true;
    }

    $("#ae_add_filter_select").change( onAddFilterChange );

    if ($.browser.opera && $.browser.version < 9.5) {
        $(window).resize( onWindowResize );
    } else {
        onWindowResize();
    }

    $("#ae_results_hdr th.sortable").each(addSortableHandlers);

    var query = new Object();

    if ("" != $.query.get("keywords"))
        query.keywords = $.query.get("keywords");

    if ("" != $.query.get("species"))
        query.species = $.query.get("species");

    if ("" != $.query.get("array"))
        query.array = $.query.get("array");

    if ("" != $.query.get("from"))
        query.from = $.query.get("from");

    if ("" != $.query.get("to"))
        query.to = $.query.get("to");

    if ("" != $.query.get("sortby"))
        query.sortby = $.query.get("sortby");
    else
        query.sortby = "releasedate";

    if ("" != $.query.get("sortorder"))
        query.sortorder = $.query.get("sortorder");
    else
        query.sortorder = "descending";

    showSorting(query);

    $.get( "/microarray-as/ae/servlets/query/browse-experiments/html", query, onExperimentQuery );
});

function
onAddFilterChange( eventObj )
{
    $("#ae_add_filter_select").hide();
    $("#ae_add_filter_select option:selected").removeAttr("selected");
    $("#ae_add_filter_select option:first").attr( "selected", "true" );
    $("#ae_add_filter").before("<div><label for=\"ae_filter_1\">Species</label><select class=ae_filter id=ae_filter_1><option>Select species ...</option></select>");
    $("#ae_filter_1").focus();
    $("#ae_add_filter_select").show();
}

function
onWindowResize( eventObj )
{
    var outerWidth = $("#ae_results_body").width();
    var innerWidth = $("#ae_results_body table").width();
    var padding = outerWidth - innerWidth;
    if ( padding > 0 ) {
        $("#ae_results_hdr").css( "right", padding + "px" );
    } else if ( padding == 0 && $.browser.opera && $.browser.version < 9.5 ) {
        $("#ae_results_hdr").css( "right", "-1px" );
    }
}

function
onExperimentQuery( tableHtml )
{
    // remove progress gif
    $("#ae_results_body_inner").removeAttr("class");

    // populate table with data
    $("#ae_results_tbody").html(tableHtml);

    // adjust header width to accomodate scroller (for Opera <9.5)
    if ($.browser.opera && $.browser.version < 9.5)
        onWindowResize();
}

function
showSorting( query )
{

    if ( "" != query.sortby ) {
        var thElt = $("#ae_results_header_" + query.sortby);
        if ( null != thElt ) {
            thElt.addClass("table_header_box_selected");
            thElt.removeClass("table_header_box");


            if ( "" != query.sortorder) {
                var divElt = thElt.find("div div");
                if ( null != divElt ) {
                    divElt.addClass( "descending" == query.sortorder ? "table_header_sort_desc" : "table_header_sort_asc" );
                }
            }
        }

    }
}

function
addSortableHandlers( i )
{
    var elt = $(this);
    elt.mouseover(onHeaderMouseOver).mouseout(onHeaderMouseOut).click(onHeaderClick);
}

function
onHeaderMouseOver( eventObj )
{
    $(this).addClass("table_header_box_active");
}

function
onHeaderMouseOut( eventObj )
{
    $(this).removeClass("table_header_box_active");
}

function
onHeaderClick( eventObj )
{
    var sortby = $(this).attr("id");
    sortby = sortby.substring(sortby.lastIndexOf("_") + 1, sortby.length);
    var sortorder = $(this).find("div div").attr("class");
    if ( undefined != sortorder && "" != sortorder ) {
        sortorder = sortorder == "table_header_sort_desc" ? "ascending" : "descending";
    } else {
        sortorder = "ascending";
    }
    
    var newQuery = $.query.set( "sortby", sortby ).set( "sortorder", sortorder ).toString()
    window.location.href = "browse.html" + newQuery;
}