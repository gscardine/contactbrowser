package io.intrepid.apprentice.contactbrowser;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.widget.AlphabetIndexer;
import android.widget.SectionIndexer;

public class AlphabeticalAdapter extends SimpleCursorAdapter implements SectionIndexer {

    private AlphabetIndexer mAlphabeticalIndexer;

    private final static String DISPLAY_NAME_FIELD =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME;

    public AlphabeticalAdapter(Context context, int layout, Cursor c, String[] from,
                               int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public Cursor swapCursor(Cursor c) {
        // Create alphabetical indexer
        if (c != null) {
            mAlphabeticalIndexer = new AlphabetIndexer(
                    // Cursor
                    c,
                    // Column index
                    c.getColumnIndex(DISPLAY_NAME_FIELD),
                    // Alphabet
                    " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }

        return super.swapCursor(c);
    }

    @Override
    public Object[] getSections() {
        return mAlphabeticalIndexer.getSections();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return mAlphabeticalIndexer.getPositionForSection(sectionIndex);
    }

    @Override
    public int getSectionForPosition(int position) {
        return mAlphabeticalIndexer.getSectionForPosition(position);
    }
}
