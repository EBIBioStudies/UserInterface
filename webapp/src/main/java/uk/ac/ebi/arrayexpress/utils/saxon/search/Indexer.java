package uk.ac.ebi.arrayexpress.utils.saxon.search;

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

import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.DocumentInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.xpath.XPathEvaluator;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.facet.index.CategoryDocumentBuilder;
import org.apache.lucene.facet.taxonomy.CategoryPath;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmldb.api.DatabaseManager;
import org.xmldb.api.base.Collection;
import org.xmldb.api.base.Database;
import org.xmldb.api.base.ResourceIterator;
import org.xmldb.api.base.ResourceSet;
import org.xmldb.api.base.XMLDBException;
import org.xmldb.api.modules.XPathQueryService;

import uk.ac.ebi.arrayexpress.app.Application;
import uk.ac.ebi.arrayexpress.components.SaxonEngine;
import uk.ac.ebi.arrayexpress.components.XmlDbConnectionPool;
import uk.ac.ebi.arrayexpress.utils.StringTools;
import uk.ac.ebi.arrayexpress.utils.saxon.PrintUtils;

public class Indexer {
	// logging machinery
	private final Logger logger = LoggerFactory.getLogger(getClass());

	private AbstractIndexEnvironment env;

	public Indexer(AbstractIndexEnvironment env) {
		this.env = env;
	}

	// TODO: change this enum from here
	public enum RebuildCategories {
		NOTREBUILD(0), REBUILD(1), INCREMENTALREBUILD(2);
		private int value;

		private RebuildCategories(int value) {
			this.value = value;
		}

		public int getRebuildCategory() {
			return value;
		}
	}

	// no parameters means that i will do all the work in the default database
	// (the one that is configured)
	public void indexFromXmlDB() throws Exception {
		this.env.indexFromXmlDB();
	}

	// TODO: I'm assuming that there is always an attribute @id in each element
	public void indexFromXmlDB(String indexLocationDirectory, String dbHost,
			int dbPort, String dbPassword, String dbName) throws Exception {
		this.env.indexFromXmlDB(indexLocationDirectory, dbHost, dbPort,
				dbPassword, dbName);
	}

	// no parameters means that i will do all the work in the default database
	// (the one that is configured)
	public void indexIncrementalFromXmlDB() throws Exception {
		this.env.indexIncrementalFromXmlDB();
	}

	// TODO: I'm assuming that there is always an attribute @id in each element
	public void indexIncrementalFromXmlDB(String indexLocationDirectory, String dbHost,
			int dbPort, String dbPassword, String dbName) throws Exception {
		this.env.indexIncrementalFromXmlDB(indexLocationDirectory, dbHost, dbPort,
				dbPassword, dbName);
	}

	public void indexReader() {
		env.indexReader();
	}

}
