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

package tw.gov.cdc.exposurenotifications.keyupload;


import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.io.BaseEncoding;

import org.threeten.bp.LocalDate;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;

import tw.gov.cdc.exposurenotifications.activity.UploadActivity;
import tw.gov.cdc.exposurenotifications.network.DiagnosisKey;

/**
 * A value class to carry data through the diagnosis verification and key upload flow, from start to
 * finish.
 *
 * <p>We use this class for both inputs and outputs of the {@link UploadActivity} because the
 * verification+upload flow and its parameters may change frequently, and this request/response
 * object makes it easy to alter/extend the parameters and return values without changing method
 * signatures.
 */
//@AutoValue
public abstract class Upload {
    private static final SecureRandom RAND = new SecureRandom();
    // "The key should be at least 128 bits of random data generated on the device"
    // https://github.com/google/exposure-notifications-server/blob/main/docs/design/verification_protocol.md
    private static final int HMAC_KEY_LEN_BYTES = 128 / 8;
    private static final BaseEncoding BASE64 = BaseEncoding.base64();

    /**
     * Every {@link Upload} starts with these two givens.
     */
    public static Upload.Builder newBuilder(
            List<DiagnosisKey> keys,
            String verificationCode) {
        return new AutoValue_Upload.Builder()
                .setVerificationCode(verificationCode)
                .setKeys(ImmutableList.copyOf(keys))
                .setHmacKeyBase64(newHmacKey())
                .setIsCoverTraffic(false)
                .setNumKeysAffected(0)
                .setRegions(ImmutableList.of())
                .setHasTraveled(false);
    }

    /**
     * Every {@link Upload} starts with these two givens.
     */
    public static Upload.Builder newBuilder(
            String verificationCode) {
        return newBuilder(ImmutableList.of(), verificationCode);
    }

    public static String newHmacKey() {
        byte[] bytes = new byte[HMAC_KEY_LEN_BYTES];
        RAND.nextBytes(bytes);
        return BASE64.encode(bytes);
    }

    public abstract String verificationCode();

    @Nullable
    public abstract ImmutableList<DiagnosisKey> keys();

    @Nullable
    public abstract String homeRegion();

    @Nullable
    public abstract ImmutableList<String> regions();

    @Nullable
    public abstract String longTermToken();

    @Nullable
    public abstract String testType();

    @Nullable
    public abstract String hmacKeyBase64();

    @Nullable
    public abstract String certificate();

    @Nullable
    public abstract LocalDate symptomOnset();

    @Nullable
    public abstract LocalDate diagnosisDate();

    @Nullable
    public abstract String revisionToken();

    public abstract boolean hasTraveled();

    /**
     * The keyserver reports back how many of the keys we uploaded result in a change, either adding
     * a new key or revising one it already had.
     */
    public abstract int numKeysAffected();

    public abstract boolean isCoverTraffic();

    public abstract Upload.Builder toBuilder();

//    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Upload.Builder setVerificationCode(String code);

        public abstract Upload.Builder setKeys(Collection<DiagnosisKey> keys);

        public abstract Upload.Builder setHomeRegion(String region);

        public abstract Upload.Builder setRegions(Collection<String> regions);

        public abstract Upload.Builder setTestType(String type);

        public abstract Upload.Builder setLongTermToken(String token);

        public abstract Upload.Builder setHmacKeyBase64(String key);

        public abstract Upload.Builder setCertificate(String cert);

        public abstract Upload.Builder setSymptomOnset(LocalDate date);

        public abstract Upload.Builder setDiagnosisDate(LocalDate date);

        public abstract Upload.Builder setIsCoverTraffic(boolean isFake);

        public abstract Upload.Builder setRevisionToken(String revisionToken);

        public abstract Upload.Builder setNumKeysAffected(int numKeysAffected);

        public abstract Upload.Builder setHasTraveled(boolean hasTraveled);

        public abstract Upload build();
    }

}
