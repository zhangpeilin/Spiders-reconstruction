/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.commons.compress.changes;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.InputStream;
import java.util.Objects;

/**
 * Change holds meta information about a change.
 *
 * @Immutable
 */
class Change {
    private final String targetFile; // entry name to delete
    private final ArchiveEntry entry; // new entry to add
    private final InputStream input; // source for new entry
    private final boolean replaceMode; // change should replaceMode existing entries

    // Type of change
    private final int type;
    // Possible type values
    static final int TYPE_DELETE = 1;
    static final int TYPE_ADD = 2;
    static final int TYPE_MOVE = 3; // NOT USED
    static final int TYPE_DELETE_DIR = 4;

    /**
     * Constructor. Takes the file name of the file to be deleted
     * from the stream as argument.
     * @param fileName the file name of the file to delete
     */
    Change(final String fileName, final int type) {
        Objects.requireNonNull(fileName, "fileName");
        this.targetFile = fileName;
        this.type = type;
        this.input = null;
        this.entry = null;
        this.replaceMode = true;
    }

    /**
     * Construct a change which adds an entry.
     *
     * @param archiveEntry the entry details
     * @param inputStream the InputStream for the entry data
     */
    Change(final ArchiveEntry archiveEntry, final InputStream inputStream, final boolean replace) {
        Objects.requireNonNull(archiveEntry, "archiveEntry");
        Objects.requireNonNull(inputStream, "inputStream");
        this.entry = archiveEntry;
        this.input = inputStream;
        type = TYPE_ADD;
        targetFile = null;
        this.replaceMode = replace;
    }

    Change(final ArchiveEntry archiveEntry, final boolean replace) {
        Objects.requireNonNull(archiveEntry, "archiveEntry");
        this.entry = archiveEntry;
        this.input = null;
        type = TYPE_ADD;
        targetFile = null;
        this.replaceMode = replace;
    }

    ArchiveEntry getEntry() {
        return entry;
    }

    InputStream getInput() {
        return input;
    }

    String targetFile() {
        return targetFile;
    }

    int type() {
        return type;
    }

    boolean isReplaceMode() {
        return replaceMode;
    }
}
