/*
 * Copyright 2015 fnoorala.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.

 */
package fr.inria.wimmics.smilk.localsimilarity;


/**
 * based on  https://github.com/Simmetrics/simmetrics
 */
import java.util.Locale;
 
import static org.simmetrics.StringMetricBuilder.with;
import org.simmetrics.metrics.CosineSimilarity;
import org.simmetrics.StringMetric;
import org.simmetrics.simplifiers.Case;
import org.simmetrics.simplifiers.WordCharacters;
import org.simmetrics.tokenizers.QGram;
import org.simmetrics.tokenizers.Whitespace;

/**
 *
 * @author fnoorala
 */
public class SimilarityMetrics {
    
    
    
    StringMetric metric;

    public SimilarityMetrics() {
        metric=
            with(new CosineSimilarity<String>())
            .simplify(new Case.Lower(Locale.ENGLISH))
            .simplify(new WordCharacters())
            .tokenize(new Whitespace())
            .tokenize(new QGram(3))
            .build();
    }
    
    public float score(String x, String y){
         return metric.compare(x, y);
    }
}
