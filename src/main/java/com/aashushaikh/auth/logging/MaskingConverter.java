package com.aashushaikh.auth.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

// Replaces sensitive field values in log messages with [REDACTED].
// Covers JSON payloads, query params, and plain text patterns.
public class MaskingConverter extends MessageConverter {

    private static final Pattern SENSITIVE = Pattern.compile(
        "(?i)(\"?(password|token|secret|authorization|accessToken|refreshToken)\"?\\s*[:=]\\s*)[\"']?([^\"',\\s}]+)[\"']?",
        Pattern.CASE_INSENSITIVE
    );

    @Override
    public String convert(ILoggingEvent event) {
        String message = event.getFormattedMessage();
        return SENSITIVE.matcher(message).replaceAll("$1[REDACTED]");
    }
}
