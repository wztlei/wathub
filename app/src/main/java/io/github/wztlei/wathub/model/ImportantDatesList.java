package io.github.wztlei.wathub.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import io.github.wztlei.wathub.utils.DateTimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImportantDatesList extends ArrayList<ImportantDatesDisplay> {
    /**
     * Sorts the Important Dates List by start date
     */
    /*
    public void sort() {
        Collections.sort(this, (id1, id2) -> {
            try {
                Date start1 = new SimpleDateFormat().parse(id1.getStartDate());
                Date start2 = new SimpleDateFormat().parse(id2.getStartDate());

                if (!start1.equals(start2)) {
                    return start1.compareTo(start2);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }
    */
}
