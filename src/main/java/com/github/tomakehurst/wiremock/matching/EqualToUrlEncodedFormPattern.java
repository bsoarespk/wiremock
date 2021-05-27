package com.github.tomakehurst.wiremock.matching;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.FluentIterable.from;

public class EqualToUrlEncodedFormPattern extends StringValuePattern {

    private final Map<String, String> expected;
    private final String urlEncodedForm;
    private final String charset;
    private final List<StringValuePattern> captureFormParameters;

    public EqualToUrlEncodedFormPattern(@JsonProperty("equalToUrlEncodedForm") String urlEncodedForm,
                                        @JsonProperty("captureFormParameters") List<StringValuePattern> captureFormParameters) {
        super(urlEncodedForm);
        if (captureFormParameters == null)
            captureFormParameters = Collections.<StringValuePattern>singletonList(new AnythingPattern());
        this.charset = StandardCharsets.ISO_8859_1.name();
        this.expected = parse(urlEncodedForm, captureFormParameters);
        this.urlEncodedForm = normalizedString(expected);
        this.captureFormParameters = captureFormParameters;
    }

    @JsonProperty("equalToUrlEncodedForm")
    public Object getEqualToUrlEncodedForm() {
        return urlEncodedForm;
    }

    @JsonProperty("captureFormParameters")
    public Object getCaptureFormParameters() {
        return this.captureFormParameters;
    }

    @Override
    public MatchResult match(String value) {
        final Map<String, String> actual = parse(value, captureFormParameters);
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

    private Map<String, String> parse(String urlEncodedForm, List<StringValuePattern> formParametersToMatch) {
        Map<String, String> form = new HashMap<String, String>();
	if (urlEncodedForm == null) // can happen if request method was HEAD
	    return form;
        for (String keyValuePair : urlEncodedForm.split("&")) {
            int separator = keyValuePair.indexOf("=");
            if (separator == -1 || separator == keyValuePair.length() - 1) {
                putIfKeyMatches(form, formParametersToMatch, decode(keyValuePair), "");
            }
            else {
                String key = decode(keyValuePair.substring(0, separator));
                String value = decode(keyValuePair.substring(separator + 1));
                putIfKeyMatches(form, formParametersToMatch, key, value);
            }
        }
        return form;
    }

    private void putIfKeyMatches(Map<String, String> map, List<StringValuePattern> patternsToMatch, final String key, String value) {
        final Predicate<StringValuePattern> predicate = new Predicate<StringValuePattern>() {
            @Override
            public boolean apply(StringValuePattern pattern) {
                return pattern != null && pattern.match(key).isExactMatch();
            }
        };
        if (from(patternsToMatch).anyMatch(predicate))
            map.put(key, value);
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
