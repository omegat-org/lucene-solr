/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.LuceneTestCase;

public class TestUsageTrackingFilterCachingPolicy extends LuceneTestCase {

  public void testCostlyFilter() {
    assertTrue(UsageTrackingQueryCachingPolicy.isCostly(new PrefixQuery(new Term("field", "prefix"))));
    assertTrue(UsageTrackingQueryCachingPolicy.isCostly(IntPoint.newRangeQuery("intField", 1, 1000)));
    assertFalse(UsageTrackingQueryCachingPolicy.isCostly(new TermQuery(new Term("field", "value"))));
  }

  public void testNeverCacheMatchAll() throws Exception {
    Query q = new MatchAllDocsQuery();
    UsageTrackingQueryCachingPolicy policy = new UsageTrackingQueryCachingPolicy();
    for (int i = 0; i < 1000; ++i) {
      policy.onUse(q);
    }
    Directory dir = newDirectory();
    IndexWriter w = new IndexWriter(dir, new IndexWriterConfig(null));
    w.addDocument(new Document());
    IndexReader r = DirectoryReader.open(w);
    assertFalse(policy.shouldCache(q, getOnlyLeafReader(r).getContext()));
    
    r.close();
    w.close();
    dir.close();
  }

}
