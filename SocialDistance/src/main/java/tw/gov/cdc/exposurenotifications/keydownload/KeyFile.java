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

package tw.gov.cdc.exposurenotifications.keydownload;


import androidx.annotation.Nullable;

import java.io.File;

/**
 * A carrier for the keyfile's URI and some other metadata that helps us remember the latest
 * successful download, so we can skip it next time.
 */
//@AutoValue
public abstract class KeyFile {
    /**
     * Creates a {@link KeyFile} for the usual production use case.
     */
    public static KeyFile create(String indexEntry, boolean isMostRecent) {
        return new AutoValue_KeyFile(indexEntry, null, isMostRecent);
    }

    /**
     * Creates a {@link KeyFile} not for use in production, rather in testing and faked environments
     * (it doesn't track its source server the way a real one does).
     */
    public static KeyFile createNonProd(File f) {
        return new AutoValue_KeyFile("indexEntry", f, false);
    }

    public abstract String indexEntry();

    @Nullable
    public abstract File file();

    public abstract boolean isMostRecent();

    public KeyFile with(File f) {
        return new AutoValue_KeyFile(indexEntry(), f, isMostRecent());
    }
}