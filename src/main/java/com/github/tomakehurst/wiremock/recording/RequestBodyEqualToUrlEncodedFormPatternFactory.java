package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.EqualToUrlEncodedFormPattern;

public class RequestBodyEqualToUrlEncodedFormPatternFactory implements RequestBodyPatternFactory {

    @JsonCreator
    public RequestBodyEqualToUrlEncodedFormPatternFactory() {
    }

    @Override
    public ContentPattern<?> forRequest(Request request) {
        return new EqualToUrlEncodedFormPattern(request.getBodyAsString());
    }
    
}
