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

public class GraalStats {

    @JsonProperty("general_info")
    private GeneralInfo genInfo;

    @JsonProperty("analysis_results")
    private AnalysisResults analysisResults;

    @JsonProperty("image_details")
    private ImageDetails imageDetails;

    @JsonProperty("resource_usage")
    private ResourceUsage resourceUsage;

    public GeneralInfo getGenInfo() {
        return genInfo;
    }

    public void setGenInfo(GeneralInfo genInfo) {
        this.genInfo = genInfo;
    }

    public AnalysisResults getAnalysisResults() {
        return analysisResults;
    }

    public void setAnalysisResults(AnalysisResults analysisResults) {
        this.analysisResults = analysisResults;
    }

    public ImageDetails getImageDetails() {
        return imageDetails;
    }

    public void setImageDetails(ImageDetails imageDetails) {
        this.imageDetails = imageDetails;
    }

    public ResourceUsage getResourceUsage() {
        return resourceUsage;
    }

    public void setResourceUsage(ResourceUsage resourceUsage) {
        this.resourceUsage = resourceUsage;
    }
}
