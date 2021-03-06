/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package tw.gov.cdc.exposurenotifications.common;

import com.google.common.io.BaseEncoding;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Simple util class for manipulating strings.
 */
public final class StringUtils {

    public static final String ELLIPSIS = "\u2026";
    private static final SecureRandom RAND = new SecureRandom();
    private static final BaseEncoding BASE64 = BaseEncoding.base64();
    private static final DateTimeFormatter LONG_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm:ss z").withZone(ZoneId.of("UTC"));

    private StringUtils() {
        // Prevent instantiation.
    }

    /**
     * Converts an epoch UTC timestamp to a formatted UTC date for a given locale.
     *
     * @param timestampMs the epoch timestamp to convert
     * @param locale      the locale to represent the text in
     * @return standard date format includes month and year of the timestamp as a UTC date
     */
    public static String epochTimestampToMediumUTCDateString(long timestampMs, Locale locale) {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, locale);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(timestampMs);
    }

    /**
     * Converts an epoch UTC timestamp in days to a formatted UTC date for a given locale.
     *
     * @param epochDays the epoch timestamp to convert in days since epoch
     * @param locale    the locale to represent the text in
     * @return the MMMM dd, YYYY representation of the timestamp as a UTC date
     */
    public static String epochDaysTimestampToMediumUTCDateString(long epochDays, Locale locale) {
        return epochTimestampToMediumUTCDateString(TimeUnit.DAYS.toMillis(epochDays),
                locale);
    }

    /**
     * Converts an epoch UTC timestamp to a formatted UTC date for a given locale.
     *
     * @param timestampMs the epoch timestamp to convert
     * @param locale      the locale to represent the text in
     * @return the yyyy-MM-dd, HH:mm:ss z representation of the timestamp as a UTC date
     */
    public static String epochTimestampToLongUTCDateTimeString(long timestampMs, Locale locale) {
        return LONG_FORMAT.withLocale(locale).format(Instant.ofEpochMilli(timestampMs));
    }

    /**
     * Truncates string and appends ellipses char at the end of it if string is longer than len
     *
     * @param text string to truncate
     * @param len  desired length of the string
     * @return truncated string
     */
    public static String truncateWithEllipsis(String text, int len) {
        return text.length() <= len ? text : text.substring(0, Math.max(0, len - 1)) + ELLIPSIS;
    }

    public static String randomBase64Data(int numBytes) {
        byte[] bytes = new byte[numBytes];
        RAND.nextBytes(bytes);
        return BASE64.encode(bytes);
    }

}
