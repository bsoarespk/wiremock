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
package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.AnythingPattern;
import com.github.tomakehurst.wiremock.matching.BinaryEqualToPattern;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.EqualToJsonPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.EqualToUrlEncodedFormPattern;
import com.github.tomakehurst.wiremock.matching.EqualToXmlPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import java.util.List;

import static com.github.tomakehurst.wiremock.common.ContentTypes.determineIsTextFromMimeType;

public class RequestBodyAutomaticPatternFactory implements RequestBodyPatternFactory {

    private final Boolean caseInsensitive;
    private final Boolean ignoreArrayOrder;
    private final Boolean ignoreExtraElements;
    private final List<StringValuePattern> captureFormParameters;

    @JsonCreator
    public RequestBodyAutomaticPatternFactory(
        @JsonProperty("ignoreArrayOrder") Boolean ignoreArrayOrder,
        @JsonProperty("ignoreExtraElements") Boolean ignoreExtraElements,
        @JsonProperty("caseInsensitive") Boolean caseInsensitive,
        @JsonProperty("captureFormParameters") List<StringValuePattern> captureFormParameters) {
        this.ignoreArrayOrder = ignoreArrayOrder == null ? true : ignoreArrayOrder;
        this.ignoreExtraElements = ignoreExtraElements == null ? true : ignoreExtraElements;
        this.caseInsensitive = caseInsensitive == null ? false : caseInsensitive;
        this.captureFormParameters = captureFormParameters;
    }

    private RequestBodyAutomaticPatternFactory() {
        this(null, null, null, null);
    }

    public static final RequestBodyAutomaticPatternFactory DEFAULTS = new RequestBodyAutomaticPatternFactory();

    public Boolean isIgnoreArrayOrder() {
        return ignoreArrayOrder;
    }

    public Boolean isIgnoreExtraElements() {
        return ignoreExtraElements;
    }

    public Boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * If request body was JSON or XML, use "equalToJson" or "equalToXml" (respectively) in the RequestPattern so it's
     * easier to read. Otherwise, just use "equalTo"
     */
    @Override
    public ContentPattern<?> forRequest(Request request) {
        final String mimeType = request.getHeaders().getContentTypeHeader().mimeTypePart();
        if (mimeType != null) {
            if (mimeType.contains("json")) {
                return new EqualToJsonPattern(request.getBodyAsString(), ignoreArrayOrder, ignoreExtraElements);
            } else if (mimeType.contains("xml")) {
                return new EqualToXmlPattern(request.getBodyAsString());
            } else if (mimeType.contains("form-urlencoded")) {
                return new EqualToUrlEncodedFormPattern(request.getBodyAsString(), captureFormParameters);
            } else if (mimeType.equals("multipart/form-data")) {
                // TODO: Need to add a matcher that can handle multipart data properly. For now, just always match
                return new AnythingPattern();
            } else if (!determineIsTextFromMimeType(mimeType)) {
                return new BinaryEqualToPattern(request.getBody());
            }
        }

        return new EqualToPattern(request.getBodyAsString(), caseInsensitive);
    }
}
