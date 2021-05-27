package com.github.tomakehurst.wiremock.recording;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.EqualToUrlEncodedFormPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import java.util.List;

public class RequestBodyEqualToUrlEncodedFormPatternFactory implements RequestBodyPatternFactory {

    private final List<StringValuePattern> captureFormParameters;

    @JsonCreator
    public RequestBodyEqualToUrlEncodedFormPatternFactory(@JsonProperty("captureFormParameters") List<StringValuePattern> captureFormParameters) {
        this.captureFormParameters = captureFormParameters;
    }

    @Override
    public ContentPattern<?> forRequest(Request request) {
        return new EqualToUrlEncodedFormPattern(request.getBodyAsString(), captureFormParameters);
    }
    
}
