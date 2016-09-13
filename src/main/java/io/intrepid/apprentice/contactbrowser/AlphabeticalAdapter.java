package io.intrepid.apprentice.contactbrowser;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.widget.SectionIndexer;

import java.util.HashMap;
import java.util.Set;

public class AlphabeticalAdapter extends SimpleCursorAdapter implements SectionIndexer {
    private HashMap<String, Integer> alphabeticalIndexer;
    private String[] sections;

    private static final int PROJECTION_INDEX_DISPLAY_NAME = 2;



    public AlphabeticalAdapter(Context context, int layout, Cursor c, String[] from,
                               int[] to, int flags) {
        super(context, layout, c, from, to, flags);

        // Map items by their first letter
        alphabeticalIndexer = new HashMap<String, Integer>();

        if (c != null) {
            int count = c.getColumnCount();

            for (int i = 0; i < count; i++) {
                c.moveToPosition(i);
                String letter = c.getString(PROJECTION_INDEX_DISPLAY_NAME).substring(0, 1).toUpperCase();
                if (!alphabeticalIndexer.containsKey(letter)) {
                    alphabeticalIndexer.put(letter, i);
                }
            }

            // Sort letters list and transform it in String array
            Set<String> sectionLetters = alphabeticalIndexer.keySet();
            sections = (String[]) sectionLetters.toArray();
        }
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return alphabeticalIndexer.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 1;
    }
}
