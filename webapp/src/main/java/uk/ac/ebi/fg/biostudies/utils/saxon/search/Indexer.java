package uk.ac.ebi.fg.biostudies.utils.saxon.search;

/*
 * Copyright 2009-2015 European Molecular Biology Laboratory
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
