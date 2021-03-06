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

package tw.gov.cdc.exposurenotifications.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;

import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * A carrier of diagnosis key into and out of the exposurenotifications.network operations.
 */
//@AutoValue
public abstract class DiagnosisKey {
    private static final BaseEncoding BASE16 = BaseEncoding.base16().lowerCase();
    private static final BaseEncoding BASE64 = BaseEncoding.base64();
    // The number of 10-minute intervals the key is valid for, by default.
    private static final int DEFAULT_PERIOD = 144;
    private static final int DEFAULT_TRANSMISSION_RISK = 1;
    // EN time is measured in ten minute intervals since epoch.
    private static final long INTERVAL_LEN_MS = TimeUnit.MINUTES.toMillis(10);

    public static Builder newBuilder() {
        return new AutoValue_DiagnosisKey.Builder()
                .setRollingPeriod(DEFAULT_PERIOD)
                .setTransmissionRisk(DEFAULT_TRANSMISSION_RISK);
    }

    public static Instant intervalToInstant(int interval) {
        return Instant.ofEpochMilli(((long) interval) * INTERVAL_LEN_MS);
    }

    public static int instantToInterval(Instant instant) {
        return (int) (instant.toEpochMilli() / INTERVAL_LEN_MS);
    }

    public abstract ByteArrayValue getKey();

    public abstract int getIntervalNumber();

    public abstract int getRollingPeriod();

    public abstract int getTransmissionRisk();

    public byte[] getKeyBytes() {
        return getKey().getBytes();
    }

    @NonNull
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("key:hex", "[" + BASE16.encode(getKeyBytes()) + "]")
                .add("key:base64", "[" + BASE64.encode(getKeyBytes()) + "]")
                .add("interval_number", getIntervalNumber())
                .add("rolling_period", getRollingPeriod())
                .add("transmission_risk", getTransmissionRisk())
                .toString();
    }

    /**
     * Builder for {@link DiagnosisKey}.
     */
//    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder setKey(ByteArrayValue key);

        public abstract Builder setIntervalNumber(int intervalNumber);

        abstract int getRollingPeriod();

        public abstract Builder setRollingPeriod(int rollingPeriod);

        abstract int getTransmissionRisk();

        public abstract Builder setTransmissionRisk(int risk);

        abstract DiagnosisKey autoBuild();

        public DiagnosisKey build() {
            // Both transmission risk and rolling period need to be positive ints. If they're zero, they
            // probably were unset in the source data, so we use defaults.
            setTransmissionRisk(
                    getTransmissionRisk() > 0 ? getTransmissionRisk() : DEFAULT_TRANSMISSION_RISK);
            setRollingPeriod(getRollingPeriod() > 0 ? getRollingPeriod() : DEFAULT_PERIOD);
            return autoBuild();
        }

        public Builder setKeyBytes(byte[] keyBytes) {
            setKey(new ByteArrayValue(keyBytes));
            return this;
        }
    }

    /**
     * Wrapper class which makes a {@code byte[]} value immutable.
     */
    public static class ByteArrayValue {
        private final byte[] bytes;

        public ByteArrayValue(byte[] bytes) {
            this.bytes = bytes.clone();
        }

        public byte[] getBytes() {
            return bytes.clone();
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof ByteArrayValue)) {
                return false;
            }
            ByteArrayValue that = (ByteArrayValue) other;
            return Arrays.equals(bytes, that.bytes);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(bytes);
        }

        @NonNull
        @Override
        public String toString() {
            return Arrays.toString(bytes);
        }
    }
}
