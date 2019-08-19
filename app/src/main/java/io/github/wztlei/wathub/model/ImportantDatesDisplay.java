package io.github.wztlei.wathub.model;

import java.util.Calendar;

import io.github.wztlei.wathub.utils.DateTimeUtils;

public class ImportantDatesDisplay {
    private String importantDate;
    private String description;
    private String startDate;
    private String endDate;

    public ImportantDatesDisplay(String importantDate, String description, String startDate, String endDate) {
        this.importantDate = importantDate;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getImportantDate() { return importantDate; }
    public String getDescription() { return description; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}
