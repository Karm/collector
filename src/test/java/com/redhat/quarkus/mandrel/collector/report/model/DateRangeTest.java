/*
 * Copyright (c) 2022, 2024 Contributors to the Collector project
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

package com.redhat.quarkus.mandrel.collector.report.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DateRangeTest {

    public static final String START = "2020-01-01 00:00:00.000";
    public static final String END = "2021-01-02 00:00:00.000";

    @Test
    void checkDateRangeNullStart() {
        DateRange dateRange = DateRange.fromString(null, END);
        String start = ImageStatsCollection.CREATED_DATE_FORMATTER.format(
                dateRange.start.toInstant().atOffset(ZoneOffset.UTC));
        assertEquals("1970-01-01 00:00:00.000", start);
    }

    @Test
    void checkDateRangeNullEnd() {
        DateRange dateRange = DateRange.fromString(START, null);
        String currentDate = LocalDateTime.now().format(ImageStatsCollection.CREATED_DATE_FORMATTER).split(" ")[0];
        String end = ImageStatsCollection.CREATED_DATE_FORMATTER.format(
                dateRange.end.toInstant().atOffset(ZoneOffset.UTC));
        assertTrue(end.startsWith(currentDate));
    }

    @Test
    void checkDateRange() {
        DateRange dateRange = DateRange.fromString(START, END);
        String start = ImageStatsCollection.CREATED_DATE_FORMATTER.format(
                dateRange.start.toInstant().atOffset(ZoneOffset.UTC));
        String end = ImageStatsCollection.CREATED_DATE_FORMATTER.format(
                dateRange.end.toInstant().atOffset(ZoneOffset.UTC));
        assertEquals(START, start);
        assertEquals(END, end);
    }
}
