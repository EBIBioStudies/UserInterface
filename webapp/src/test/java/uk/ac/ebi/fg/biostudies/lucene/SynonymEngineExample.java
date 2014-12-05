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

package uk.ac.ebi.fg.biostudies.lucene;

import java.util.HashMap;

public class SynonymEngineExample implements SynonymEngine {
    private static HashMap map = new HashMap();
    static {
      map.put("quick", new String[] {"fast", "speedy"});
      map.put("jumps", new String[] {"leaps", "hops"});
      map.put("over", new String[] {"above"});
      map.put("lazy", new String[] {"apathetic", "sluggish"});
      map.put("dogs", new String[] {"canines", "pooches"});
}
    public String[] getSynonyms(String s) {
      return (String[]) map.get(s);
} }
