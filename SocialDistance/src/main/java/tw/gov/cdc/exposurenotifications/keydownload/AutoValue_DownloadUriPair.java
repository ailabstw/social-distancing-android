package tw.gov.cdc.exposurenotifications.keydownload;

import android.net.Uri;

// Generated by com.google.auto.value.processor.AutoValueProcessor
final class AutoValue_DownloadUriPair extends DownloadUriPair {

    private final Uri indexUri;

    private final Uri fileBaseUri;

    AutoValue_DownloadUriPair(
            Uri indexUri,
            Uri fileBaseUri) {
        if (indexUri == null) {
            throw new NullPointerException("Null indexUri");
        }
        this.indexUri = indexUri;
        if (fileBaseUri == null) {
            throw new NullPointerException("Null fileBaseUri");
        }
        this.fileBaseUri = fileBaseUri;
    }

    @Override
    public Uri indexUri() {
        return indexUri;
    }

    @Override
    public Uri fileBaseUri() {
        return fileBaseUri;
    }

    @Override
    public String toString() {
        return "DownloadUriPair{"
                + "indexUri=" + indexUri + ", "
                + "fileBaseUri=" + fileBaseUri
                + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof DownloadUriPair) {
            DownloadUriPair that = (DownloadUriPair) o;
            return this.indexUri.equals(that.indexUri())
                    && this.fileBaseUri.equals(that.fileBaseUri());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h$ = 1;
        h$ *= 1000003;
        h$ ^= indexUri.hashCode();
        h$ *= 1000003;
        h$ ^= fileBaseUri.hashCode();
        return h$;
    }

}
