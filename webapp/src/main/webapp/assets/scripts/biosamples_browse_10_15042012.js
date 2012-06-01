/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var query = new Object();
var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
var anchor = decodeURI(window.location.hash);

(function($, undefined) {
	if ($ == undefined)
		throw "jQuery not loaded";

	var sortDefault = {
		id : "ascending",
		description : "ascending",
		samples : "ascending"
	};

	var sortTitle = {
		id : "id",
		description : "description",
		samples : "samples"
	};

	$(function() {
		$("#bs_contents").show();

		// this will be executed when DOM is ready
		if ($.query == undefined)
			throw "jQuery.query not loaded";

		if ($.browser.opera && $.browser.version < 9.5) {
			onWindowResize();
			$(window).bind('resize', onWindowResize);
		} else {
			onWindowResize();
		}

		var sortby = $.query.get("sortby") || "id";
		var sortorder = $.query.get("sortorder") || "ascending";
		var page = $.query.get("page") || "1";
		var pagesize = $.query.get("pagesize") || "50";

//		var localPath = /(\/.+)$/.exec(decodeURI(window.location.pathname))[1];
		
		
		var newQuery = $.query.set( "sortby", sortby ).set( "sortorder", sortorder ).set("page", page).set("pagesize",pagesize).toString();
        //window.location.href = pageName + newQuery;
		
//		var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
		var urlPage = "biosamplesgroup/browse-table.html"  + newQuery;
		//var urlPage = "biosamplesgroup/" + pageName;
		
		QuerySampleGroup(urlPage);
		// added autocompletion
		var basePath = decodeURI(window.location.pathname).replace(
				/\/\w+\.\w+$/, "/");
//		alert(basePath);
		$("#bs_keywords").autocomplete(basePath + "keywords.txt", {
			matchContains : false,
			selectFirst : false,
			scroll : true,
			max : 50,
			requestTreeUrl : basePath + "efotree.txt"
		});
	});
	initControls();
})(window.jQuery);



function
initControls()
{
    // keywords
    $("#bs_keywords").val(query.keywords);
 
    $("#bs_sortby").val(query.sortby);
    $("#bs_sortorder").val(query.sortorder);
    $("#bs_pagesize").val(query.pagesize);
 


    if ( "" != query.sortby ) {
        var thElt = $("#bs_results_header_" + query.sortby);
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

function QuerySampleGroup(url) {
//	alert("QuerySampleGroup2->" + url);
	$.get(
			url,
			function(tableHtml) {
//				alert("Data->"+tableHtml);
				
				 $("#bs_results_body_inner").removeClass("bs_results_tbl_loading");

				    // populate table with data
				    $("#bs_results_tbody").html(tableHtml);

				    // attach titles to highlight classes
				    $("#bs_results_tbody").find(".ae_text_hit").attr("title", "This is exact string matched for input query terms");
				    $("#bs_results_tbody").find(".ae_text_syn").attr("title", "This is synonym matched from Experimental Factor Ontology e.g. neoplasia for cancer");
				    $("#bs_results_tbody").find(".ae_text_efo").attr("title", "This is matched child term from Experimental Factor Ontology e.g. brain and subparts of brain");

		

				    $("#bs_results_body_inner").scroll(
				            function (e) {
				                updateAppStateScroll($(this).scrollTop());
				            }
				        );

				    // adjust header width to accomodate scroller (for Opera <9.5)
				    if ($.browser.opera && $.browser.version < 9.5)
				        onWindowResize();

				    // get stats from the first row
				    var total = $("#ae_results_total").text();
				    var from = $("#ae_results_from").text();
				    var to = $("#ae_results_to").text();
				    var curpage = $("#ae_results_page").text();
				    var pagesize = $("#ae_results_pagesize").text();


				    if ( total > 0 ) {

				       
				        var totalPages = total > 0 ? Math.floor( ( total - 1 ) / pagesize ) + 1 : 0;
				        $("#bs_results_status").html(
				            total + " group" + (total != 1 ? "s" : "" ) + ", " +
				            ( totalPages > 1 ? (" Displaying groups " + from + " to " + to + ".") : "" )
				            );

				        if ( totalPages > 1 ) {
				            var pagerHtml = "Pages: ";
				            for ( var page = 1; page <= totalPages; page++ ) {
				                if ( curpage == page ) {
				                    pagerHtml = pagerHtml + "<span class=\"pager_current\">" + page + "</span>";
				                } else if ( 2 == page && curpage > 6 && totalPages > 11 ) {
				                    pagerHtml = pagerHtml + "..";
				                } else if ( totalPages - 1 == page && totalPages - curpage > 5 && totalPages > 11 ) {
				                    pagerHtml = pagerHtml + "..";
				                } else if ( 1 == page || ( curpage < 7 && page < 11 ) || ( Math.abs( page - curpage ) < 5 ) || ( totalPages - curpage < 6 && totalPages - page < 10 ) || totalPages == page || totalPages <= 11 ) {
				                    var newQuery = $.query.set( "page", page ).set( "pagesize", pagesize ).toString();
				                    pagerHtml = pagerHtml + "<a href=\"" + pageName + newQuery + "\">" + page + "</a>";
				                }
				            }
				            $("#bs_results_pager").html( pagerHtml );
				        }
				
				    }
			});


}

function onQueryError() {
	// remove progress gif
	$("#bs_results_body_inner").removeClass("bs_results_tbl_loading");
	// report problem to the user
	$("#bs_results_tbody")
			.html(
					"<tr class=\"bs_results_tr_error\"><td colspan=\"9\">There was an error processing the query. Please try again later.</td></tr>");
}

function onWindowResize() {
	//alert("onWindowResize");
	var outerWidth = $("#bs_results_body").width();
	var innerWidth = $("#bs_results_body table").width();
	var padding = outerWidth - innerWidth - 1;
	if (padding >= 0 && padding < 30) {
		$("#bs_results_hdr").css("right", padding + "px");
	} else if (padding <= 0 && $.browser.opera && $.browser.version < 9.5) {
		$("#bs_results_hdr").css("right", "-1px");
	} else if (-1 == padding && $.browser.safari
			&& String($.browser.version).replace(/(\d+).*/i, "$1") >= 534) {
		$("#bs_results_hdr").css("right", "-1px");
	}
}



function goToPage(pPage) {
	page = pPage;
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	pageName = "../biosamplessample/" + pageName + "?page=" + page + "&sortby="
			+ sortBy + "&sortorder=" + sortOrder;
	// alert("url->" + pageName);
	updateSamplesList(pageName);
}

function searchSamples(pKeywords) {
	// alert("par->" + pKeywords);
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	pageName = "../biosamplessample/" + pageName + "?keywords=" + pKeywords
			+ "&page=" + page + "&sortby=" + sortBy + "&sortorder=" + sortOrder;
	// alert("url->" + pageName);
	updateSamplesList(pageName);
}

function getQueryStringParam(paramName, defaultValue) {
	var param = $.query.get(paramName);
	if ("" !== param) {
		return param;
	} else {
		return defaultValue;
	}
}

function getQueryArrayParam(paramName) {
	var param = $.query.get(paramName);
	if (!jQuery.isArray(param)) {
		return new Array(param);
	} else {
		return param;
	}
}

function getQueryBooleanParam(paramName) {
	var param = $.query.get(paramName);
	return (true === param || "" != param) ? true : undefined;
}




function
aeSort( sortby )
{
	//alert("sortby->" + sortby);
    if ( -1 != String("accession description samples").indexOf(sortby) ) {
        var innerElt = $( "#bs_results_header_" + sortby ).find("div.table_header_inner");
        var sortorder = "ascending";
 /*       if ( -1 != String("accession name species").indexOf(sortby)) {
            if ( undefined != innerElt && innerElt.hasClass("table_header_sort_asc") )
                sortorder = "descending";
        } else {*/
            sortorder = "descending";
            if ( undefined != innerElt && innerElt.hasClass("table_header_sort_desc") )
                sortorder = "ascending";
        //}
  
		var page = $.query.get("page") || "1";
		var pagesize = $.query.get("pagesize") || "50";

//		var localPath = /(\/.+)$/.exec(decodeURI(window.location.pathname))[1];
				
		var newQuery = $.query.set( "sortby", sortby ).set( "sortorder", sortorder ).set("page", page).set("pagesize",pagesize).toString();
        //window.location.href = pageName + newQuery;
		
		var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
		var urlPage =  pageName  + newQuery;
		window.location.href = urlPage;
    }
}



function
updateAppStateScroll( scrollValue )
{
    var appStateData = $.cookie("AeAppStateData");
    if (null != appStateData) {
        appStateData = appStateData.replace(/^\d+/, scrollValue);
        $.cookie("AeAppStateData", appStateData);
    }
}
