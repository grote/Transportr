package de.grobox.liberario.utils;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import de.grobox.liberario.WrapLocation;
import de.schildbach.pte.dto.Location;

/**
 * Created by kgreshake on 14.11.16.
 */

public abstract class LocationFilter extends Filter {
    private boolean onlyIDs;

    public LocationFilter(boolean onlyIDs) {
        this.onlyIDs = onlyIDs;
    }

    protected FilterResults performFiltering(final CharSequence constraint,
                                             List<Location> suggestedLocations,
                                             List<WrapLocation> defaultLocations) {
        FilterResults filterResults = new FilterResults();
        final HashMap<Location, Double> resultScores = new HashMap<>();

        if (constraint != null) {
            List<WrapLocation> result = new ArrayList<>();

            // add fav locations that fulfill constraint
            if (defaultLocations != null) {
                for (WrapLocation l : defaultLocations) {
                    // if we only want locations with ID, make sure the location has one
                    if (!onlyIDs || l.getLocation().hasId()) {
                        // case-insensitive match of location name and location not already included
                        if (l.getLocation().name != null && l.getLocation().name.toLowerCase().contains(constraint.toString().toLowerCase()) && !result.contains(l)) {
                            result.add(l);
                            // Favorites are most important, they have to stay at the top
                            resultScores.put(l.getLocation(), Double.MAX_VALUE);
                        }
                    }
                }
            }

            // add suggested locations (from network provider) without filtering if not already included
            if (suggestedLocations != null) {
                for (Location l : suggestedLocations) {
                    WrapLocation loc = new WrapLocation(l);
                    // prevent duplicates and if we only want locations with ID, make sure the location has one
                    if (!result.contains(loc) && (!onlyIDs || l.hasId())) {
                        result.add(loc);
                        String lName = loc.getLocation().name + " ";
                        if (loc.getLocation().place != null)
                            lName += loc.getLocation().place;
                        resultScores.put(loc.getLocation(), searchWordScore(lName, constraint.toString()));
                    }
                }
            }
            Collections.sort(result, new Comparator<WrapLocation>() {
                @Override
                public int compare(WrapLocation l1, WrapLocation l2) {
                    return resultScores.get(l2.getLocation()).compareTo(resultScores.get(l1.getLocation()));
                }
            });

            // Assign the data to the FilterResults
            filterResults.values = result;
            filterResults.count = result.size();
        }
        return filterResults;
    }

    private static String prepareString(String s) {
        return s.toLowerCase()
                .trim()
                .replace(",", "")
                .replace(".", "")
                .replace("(", "")
                .replace("-", "")
                .replace(")", "");
    }

    private static double searchWordScore(String s, String searchTerm) {
        s = prepareString(s);
        searchTerm = prepareString(searchTerm);

        // Looking for occurences of search words in no order first
        String[] searchWords = searchTerm.split(" ");

        double score = 0;

        if (searchWords.length > 1) {
            for (String w : searchWords) {
                // does it occur literally? If so, make the partial score one
                if (s.contains(w))
                    score += 1;
                else
                    score += stringSimilarity(s, w) - 0.2; // Penalty for
                // non-literal matches
            }
        }

        if (s.contains(searchTerm))
            score += 1;
        else
            score += stringSimilarity(s, searchTerm) - 0.2;

        int diff = Math.min(20, Math.abs(searchTerm.length() - s.length())) + 1;
        score += 1 / diff;

        // Average score, divided by number of criteria
        return score / (searchWords.length + 2);
    }

    // People make mistakes writing search terms.
    // That means we have to use approximation to score
    // stations based on their similarity to the searched term

    // This gives us an approximation between 1 and 0, using the Levenshtein distance
    private static double stringSimilarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
        return (longerLength - levenshteinDistance(longer, shorter)) / (double) longerLength;
    }

    // Example implementation of the Levenshtein Edit Distance
    // In the future, this may be replaced by a distance better suited
    // to find partial matches/similarities, and spelling errors
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    private static int levenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
