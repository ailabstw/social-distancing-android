package tw.gov.cdc.exposurenotifications.network;

// Generated by com.google.auto.value.processor.AutoValueProcessor
final class AutoValue_DiagnosisKey extends DiagnosisKey {

    private final DiagnosisKey.ByteArrayValue key;

    private final int intervalNumber;

    private final int rollingPeriod;

    private final int transmissionRisk;

    private AutoValue_DiagnosisKey(
            DiagnosisKey.ByteArrayValue key,
            int intervalNumber,
            int rollingPeriod,
            int transmissionRisk) {
        this.key = key;
        this.intervalNumber = intervalNumber;
        this.rollingPeriod = rollingPeriod;
        this.transmissionRisk = transmissionRisk;
    }

    @Override
    public DiagnosisKey.ByteArrayValue getKey() {
        return key;
    }

    @Override
    public int getIntervalNumber() {
        return intervalNumber;
    }

    @Override
    public int getRollingPeriod() {
        return rollingPeriod;
    }

    @Override
    public int getTransmissionRisk() {
        return transmissionRisk;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof DiagnosisKey) {
            DiagnosisKey that = (DiagnosisKey) o;
            return this.key.equals(that.getKey())
                    && this.intervalNumber == that.getIntervalNumber()
                    && this.rollingPeriod == that.getRollingPeriod()
                    && this.transmissionRisk == that.getTransmissionRisk();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h$ = 1;
        h$ *= 1000003;
        h$ ^= key.hashCode();
        h$ *= 1000003;
        h$ ^= intervalNumber;
        h$ *= 1000003;
        h$ ^= rollingPeriod;
        h$ *= 1000003;
        h$ ^= transmissionRisk;
        return h$;
    }

    static final class Builder extends DiagnosisKey.Builder {
        private DiagnosisKey.ByteArrayValue key;
        private Integer intervalNumber;
        private Integer rollingPeriod;
        private Integer transmissionRisk;

        Builder() {
        }

        @Override
        public DiagnosisKey.Builder setKey(DiagnosisKey.ByteArrayValue key) {
            if (key == null) {
                throw new NullPointerException("Null key");
            }
            this.key = key;
            return this;
        }

        @Override
        public DiagnosisKey.Builder setIntervalNumber(int intervalNumber) {
            this.intervalNumber = intervalNumber;
            return this;
        }

        @Override
        public DiagnosisKey.Builder setRollingPeriod(int rollingPeriod) {
            this.rollingPeriod = rollingPeriod;
            return this;
        }

        @Override
        int getRollingPeriod() {
            if (rollingPeriod == null) {
                throw new IllegalStateException("Property \"rollingPeriod\" has not been set");
            }
            return rollingPeriod;
        }

        @Override
        public DiagnosisKey.Builder setTransmissionRisk(int transmissionRisk) {
            this.transmissionRisk = transmissionRisk;
            return this;
        }

        @Override
        int getTransmissionRisk() {
            if (transmissionRisk == null) {
                throw new IllegalStateException("Property \"transmissionRisk\" has not been set");
            }
            return transmissionRisk;
        }

        @Override
        DiagnosisKey autoBuild() {
            String missing = "";
            if (this.key == null) {
                missing += " key";
            }
            if (this.intervalNumber == null) {
                missing += " intervalNumber";
            }
            if (this.rollingPeriod == null) {
                missing += " rollingPeriod";
            }
            if (this.transmissionRisk == null) {
                missing += " transmissionRisk";
            }
            if (!missing.isEmpty()) {
                throw new IllegalStateException("Missing required properties:" + missing);
            }
            return new AutoValue_DiagnosisKey(
                    this.key,
                    this.intervalNumber,
                    this.rollingPeriod,
                    this.transmissionRisk);
        }
    }

}
