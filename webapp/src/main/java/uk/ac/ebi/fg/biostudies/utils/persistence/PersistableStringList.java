package uk.ac.ebi.fg.biostudies.utils.persistence;

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

import uk.ac.ebi.fg.biostudies.utils.StringTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PersistableStringList extends ArrayList<String> implements Persistable
{
    public PersistableStringList()
    {
    }

    public PersistableStringList( List<String> listToCopy )
    {
        super(listToCopy);    
    }

    public String toPersistence()
    {
        StringBuilder sb = new StringBuilder();

        for ( String entry : this ) {
            sb.append(entry).append(StringTools.EOL);
        }

        return sb.toString();
    }

    public void fromPersistence( String str )
    {
        this.clear();
        this.addAll(Arrays.asList(str.split("" + StringTools.EOL)));
    }

    public boolean isEmpty()
    {
        return (0 == this.size());
    }
}

