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

import java.io.IOException;
import java.util.List;

/**
 * Combines scores of subscorers. If a subscorer does not contain the docId, a smoothing score is
 * calculated for that document/subscorer combination.
 */
public class IndriWeightedSumScorer extends IndriDisjunctionScorer {

  protected IndriWeightedSumScorer(
      Weight weight, List<Scorer> subScorers, ScoreMode scoreMode, float boost) throws IOException {
    super(weight, subScorers, scoreMode, boost);
  }

  @Override
  public float score(List<Scorer> subScorers) throws IOException {
    int docId = this.docID();
    return scoreDoc(subScorers, docId);
  }

  @Override
  public float smoothingScore(List<Scorer> subScorers, int docId) throws IOException {
    return scoreDoc(subScorers, docId);
  }

  private float scoreDoc(List<Scorer> subScorers, int docId) throws IOException {
    double score = 0;
    for (Scorer scorer : subScorers) {
      int scorerDocId = scorer.docID();
      double subScore = 0;
      if (docId == scorerDocId) {
        subScore += Math.exp(scorer.score());
      } else {
        subScore += Math.exp(scorer.smoothingScore(docId));
      }
      score += subScore;
    }
    return (float) Math.log(score);
  }
}
