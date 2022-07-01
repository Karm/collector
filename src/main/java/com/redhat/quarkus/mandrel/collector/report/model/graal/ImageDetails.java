/*
 * Copyright (c) 2022 Contributors to the Collector project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.redhat.quarkus.mandrel.collector.report.model.graal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageDetails {

    @JsonProperty("total_bytes")
    private long sizeBytes;
    @JsonProperty("debug_info")
    private DebugInfo debugInfo;
    @JsonProperty("code_area")
    private CodeArea codeArea;
    @JsonProperty("image_heap")
    private ImageHeap imageHeap;
    
    public long getSizeBytes() {
        return sizeBytes;
    }
    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }
    public DebugInfo getDebugInfo() {
        return debugInfo;
    }
    public void setDebugInfo(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }
    public CodeArea getCodeArea() {
        return codeArea;
    }
    public void setCodeArea(CodeArea codeArea) {
        this.codeArea = codeArea;
    }
    public ImageHeap getImageHeap() {
        return imageHeap;
    }
    public void setImageHeap(ImageHeap imageHeap) {
        this.imageHeap = imageHeap;
    }
}
