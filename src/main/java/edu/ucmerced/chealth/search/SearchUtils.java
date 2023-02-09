package edu.ucmerced.chealth.search;

import java.util.Collections;
import java.util.List;

public class SearchUtils {
    public static <T> List<T> notNullList(List<T> inlist) {
        return inlist == null ? Collections.emptyList() : inlist;
    }
}
