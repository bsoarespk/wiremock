package com.github.tomakehurst.wiremock.matching;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import org.apache.commons.lang3.StringUtils;

public class EqualToUrlEncodedFormPattern extends StringValuePattern {

    private final Map<String, String> expected;
    private final String urlEncodedForm;
    private final String charset;

    public EqualToUrlEncodedFormPattern(@JsonProperty("equalToUrlEncodedForm") String urlEncodedForm) {
        super(urlEncodedForm);
        this.charset = StandardCharsets.ISO_8859_1.name();
        this.expected = parse(urlEncodedForm);
        this.urlEncodedForm = normalizedString(expected);
    }

    @JsonProperty("equalToUrlEncodedForm")
    public Object getEqualToUrlEncodedForm() {
        return urlEncodedForm;
    }

    @Override
    public MatchResult match(String value) {
        final Map<String, String> actual = parse(value);
        final String actualUrlEncodedForm = normalizedString(actual);

        return new MatchResult() {
            @Override
            public boolean isExactMatch() {
                return Objects.equal(actual, expected);
            }

            @Override
            public double getDistance() {
                return normalisedLevenshteinDistance(urlEncodedForm, actualUrlEncodedForm);
            }
        };
    }

    private Map<String, String> parse(String urlEncodedForm) {
        Map<String, String> form = new HashMap<String, String>();
	if (urlEncodedForm == null) // can happen if request method was HEAD
	    return form;
        for (String keyValuePair : urlEncodedForm.split("&")) {
            int separator = keyValuePair.indexOf("=");
            if (separator == -1 || separator == keyValuePair.length() - 1) {
                form.put(decode(keyValuePair), "");
            }
            else {
                String key = decode(keyValuePair.substring(0, separator));
                String value = decode(keyValuePair.substring(separator + 1));
                form.put(key, value);
            }
        }
        return form;
    }

    private String normalizedString(Map<String, String> form) {
        List<String> keys = new ArrayList<String>(form.keySet());
        Collections.sort(keys);

        StringBuffer stringBuffer = new StringBuffer();
        for (String key : keys) {
            String value = form.get(key);
            if (value == null) {
                value = "";
            }
            if (stringBuffer.length() > 0) {
                stringBuffer.append('&');
            }
            stringBuffer.append(encode(key));
            stringBuffer.append('=');
            stringBuffer.append(encode(value));
        }
        return stringBuffer.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, charset);
        }
        catch (UnsupportedEncodingException exception) {
            return value;
        }
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, charset);
        }
        catch (UnsupportedEncodingException exception) {
            return value;
        }
    }

    private double normalisedLevenshteinDistance(String one, String two) {
        if (one == null || two == null) {
            return 1.0;
        }

        double maxDistance = Math.max(one.length(), two.length());
        double actualDistance = StringUtils.getLevenshteinDistance(one, two);
        return (actualDistance / maxDistance);
    }
    
}
