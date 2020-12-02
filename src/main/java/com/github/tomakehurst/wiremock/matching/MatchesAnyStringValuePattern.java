/*
 * Copyright (C) 2011 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.matching;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class MatchesAnyStringValuePattern extends StringValuePattern {

    private final List<StringValuePattern> patterns;

    public MatchesAnyStringValuePattern(@JsonProperty("matchesAny") List<StringValuePattern> patterns) {
        super("[" + String.join(" or ", Lists.transform(patterns, new Function<StringValuePattern, String>() {
            @Override
            public String apply(StringValuePattern pattern) {
                return pattern.expectedValue;
            }
        })) + "]");
        this.patterns = patterns;
    }

    @JsonProperty("matchesAny")
    public List<StringValuePattern> getPatterns() {
        return patterns;
    }

    @Override
    public MatchResult match(String value) {
        MatchResult bestMatch = MatchResult.noMatch();
        for (StringValuePattern pattern : patterns) {
            bestMatch = pattern.match(value);
            if (bestMatch.isExactMatch()) {
                break;
            }
        }
        return bestMatch;
    }

}
