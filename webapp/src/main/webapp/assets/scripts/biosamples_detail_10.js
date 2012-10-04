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

// the next funtions are used for paging the samples inside a group
// ######### PAGING SAMPLES INSIDE A GROUP OF SAMPLES ############
var query = new Object();
var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
var anchor = decodeURI(window.location.hash);

var sortDefault = {
	"sampleaccession" :	"ascending",
	"1" : "ascending",
	"2" : "ascending",
	"3" : "ascending",
	"4" : "ascending",
	"5" : "ascending",
	"6" : "ascending",
	"7" : "ascending",
	"8" : "ascending",
	"9" : "ascending",
	"10" : "ascending",
	"11" : "ascending",
	"12" : "ascending",
	"13" : "ascending",
	"14" : "ascending",
	"15" : "ascending",
	"16" : "ascending",
	"17" : "ascending",
	"18" : "ascending",
	"19" : "ascending",
	"20" : "ascending",

};

var sortTitle = {
	"1" : "name",
	"2" : "name",
	"3" : "name",
	"4" : "name",
	"5" : "name"
};



//these are used when i make a new query in samples
var sortByDefault="sampleaccession";
var sortOrderDefault = sortDefault[sortByDefault];
var pageInitDefault = "1";

//these are used to mantain the current srt and order (they are reset when I make a new search)
var sortBy = "sampleaccession";
var sortOrder = sortDefault[sortBy];


var pageInit = pageInitDefault;
var pageSize = "10";
var keywords = "";



// if I;m showing the detail I will get all the samples using paging
$(function() {
	// $("#ae_results_body_inner").removeClass("ae_results_tbl_loading");

	
	// I need to initialize the sorting on the defaultfield
	var thElt = $("#bs_results_header_" + sortBy);
	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {

			var divElt = thElt.find("div.table_header_inner");
			if (null != divElt) {
				 //alert("not null");
				divElt
						.addClass("descending" == sortOrder ? "table_header_sort_desc"
								: "table_header_sort_asc");
			}
		}
	}

	/* hint to the lucene highlight */
	$("#bs_results_box").find(".ae_text_hit").attr("title",
			"This is exact string matched for input query terms");
	$("#bs_results_box")
			.find(".ae_text_syn")
			.attr(
					"title",
					"This is synonym matched from Experimental Factor Ontology e.g. neoplasia for cancer");
	$("#bs_results_box")
			.find(".ae_text_efo")
			.attr(
					"title",
					"This is matched child term from Experimental Factor Ontology e.g. brain and subparts of brain");
	/* hint to the lucene highlight */
	// if (-1 == url.indexOf("browse")) {
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "../sample/browse/" + pageName + newQuery;
	updateSamplesList(urlPage);

	var basePath = decodeURI(window.location.pathname).replace(/\/\w+\.\w+$/,
			"/");
	// alert(basePath);
	$("#bs_keywords_field").autocomplete("../" + "keywords.txt", {
		matchContains : false,
		selectFirst : false,
		scroll : true,
		max : 50,
		requestTreeUrl : "../" + "efotree.txt"
	});
	// }

});

// var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];

function updateSamplesList(urlPage) {
	$
			.get(
					urlPage,
					function(data) {
						// alert('Load was performed.' + data);
						$("#bs_results_tbody").html(data);

						/* hint to the lucene highlight */
						$("#bs_results_tbody")
								.find(".ae_text_hit")
								.attr("title",
										"This is exact string matched for input query terms");
						$("#bs_results_tbody")
								.find(".ae_text_syn")
								.attr(
										"title",
										"This is synonym matched from Experimental Factor Ontology e.g. neoplasia for cancer");
						$("#bs_results_tbody")
								.find(".ae_text_efo")
								.attr(
										"title",
										"This is matched child term from Experimental Factor Ontology e.g. brain and subparts of brain");
						/* hint to the lucene highlight */

						// get stats from the first row
						var total = $("#bs_results_total").text();
						var from = $("#bs_results_from").text();
						var to = $("#bs_results_to").text();
						var curpage = $("#bs_results_page").text();
						var pagesize = $("#bs_results_pagesize").text();

						var totalPages = total > 0 ? Math.floor((total - 1)
								/ pagesize) + 1 : 0;

//						$("#ae_results_status")
//								.html(
//										total
//												+ " sample"
//												+ (total != 1 ? "s" : "")
//												+ " found "
//												+ (totalPages > 1 ? (", displaying samples "
//														+ from + " to " + to + ".")
//														: ""));
						$("#bs_results_pager").html("&nbsp;");
						if (total > 0) {

							var totalPages = total > 0 ? Math.floor((total - 1)
									/ pagesize) + 1 : 0;
							$("#bs_results_stats_fromto")
									.html(
											total
													+ " sample"
													+ (total != 1 ? "s" : "")
													+ " found"
													+ (totalPages > 1 ? (", displaying samples "
															+ from
															+ " to "
															+ to + ".")
															: ""));

							if (totalPages > 1) {
								var pagerHtml = "Pages: ";
								for ( var page = 1; page <= totalPages; page++) {
									if (curpage == page) {
										pagerHtml = pagerHtml
												+ "<span class=\"pager_current\">"
												+ page + "</span>";
									} else if (2 == page && curpage > 6
											&& totalPages > 11) {
										pagerHtml = pagerHtml + "..";
									} else if (totalPages - 1 == page
											&& totalPages - curpage > 5
											&& totalPages > 11) {
										pagerHtml = pagerHtml + "..";
									} else if (1 == page
											|| (curpage < 7 && page < 11)
											|| (Math.abs(page - curpage) < 5)
											|| (totalPages - curpage < 6 && totalPages
													- page < 10)
											|| totalPages == page
											|| totalPages <= 11) {
										pagerHtml = pagerHtml
												+ "<a href=\"javascript:goToPage("
												+ page + ")" + ";\">" + page
												+ "</a>";
									}
								}
								$("#bs_results_pager").html(pagerHtml);
							}

						} else {
							// Clean the paging information
							$("#bs_results_stats_fromto").html(
									"No samples found.");

						}

						// }

					});

}

function searchSamples(pKeywords) {
	// alert("par->" + pKeywords);
	keywords = pKeywords;
	//reset of status variables
	sortBy=sortByDefault;
	sortOrder=sortOrderDefault;
	pageInit=pageInitDefault;
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "../sample/browse/" + pageName + newQuery;
	updateSamplesList(urlPage);
}

function goToPage(page) {
	pageInit = page;
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();
	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "../sample/browse/" + pageName + newQuery;
	updateSamplesList(urlPage);
}

function aeSort(psortby) {
	if (psortby == sortBy) {
		sortOrder = (sortOrder == "ascending" ? "descending" : "ascending");
	} else {
		sortBy = psortby;
		sortOrder = "ascending";
	}

	for ( var key in sortDefault) {
		// do something with key and hmap[key]
		$("#bs_results_header_" + key).find("div.table_header_inner")
				.removeClass("table_header_sort_desc").removeClass(
						"table_header_sort_asc");

	}
	
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();

	var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "../sample/browse/" + pageName + newQuery;
	//alert(urlPage);
	updateSamplesList(urlPage);
	
	
	//I just put the orientation after the query return the results (before I clean all the asc and desc of all the columns, after I make the query and at the end i Put the correct one)
	var thElt = $("#bs_results_header_" + sortBy);

	if (null != thElt) {
		//alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {
			var divElt = thElt.find("div.table_header_inner");
			if (null != divElt) {
				//alert("div.table_header_inner not null");
				divElt
						.addClass("descending" == sortOrder ? "table_header_sort_desc"
								: "table_header_sort_asc");
			}
		}
	}

}

// ######### PAGING SAMPLES INSIDE A GROUP OF SAMPLES ############
