package io.intrepid.apprentice.contactbrowser;

import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.widget.AdapterView;

public class ContactsPresenter
        implements LoaderManager.LoaderCallbacks<Cursor> {

    ContactsFragment contactsFragment;

    /* Cursor adapter and parameters */
    public AlphabeticalAdapter mAlphabeticalAdapter;

    private final static String DISPLAY_NAME_FIELD =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ?
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY :
                    ContactsContract.Contacts.DISPLAY_NAME;

    private final static String[] FROM_COLUMNS = {
            DISPLAY_NAME_FIELD,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private final static int[] TO_IDS = {
            R.id.text_view_contact_name,
            R.id.text_view_contact_phone
    };

    private final static String[] DATA_PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            DISPLAY_NAME_FIELD,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private static final int PROJECTION_INDEX_PHONE_NUMBER = 3;

    private static final String DATA_SELECTION =
            DISPLAY_NAME_FIELD + " LIKE ? OR "
                    + ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? ";

    private String dataSearchString = "";

    private String[] dataSelectionArgs = {dataSearchString, dataSearchString};

    private static final String DATA_ORDER = DISPLAY_NAME_FIELD + "  ASC ";

    public ContactsPresenter(ContactsFragment fragment) {
        contactsFragment = fragment;

        // Create Alphabetical Adapter
        mAlphabeticalAdapter = new AlphabeticalAdapter(
                fragment.getActivity(),
                R.layout.list_item_contacts,
                null,
                FROM_COLUMNS, TO_IDS,
                0
        );

        // Initialize cursor loader
        contactsFragment.getLoaderManager().initLoader(0, null, this);
    }

    /**
     * Get the phone number of a contact loaded from the phone's contact app
     *
     * @param parent Adapter View containing the cursor
     * @param position Position of the contact in the cursor
     */
    public void getContactPhoneNumber(AdapterView<?> parent, int position) {
        // Get data from the clicked contact
        Cursor cursor = ((AlphabeticalAdapter) parent.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String phoneNumber = cursor.getString(PROJECTION_INDEX_PHONE_NUMBER);

        contactsFragment.dialPhoneNumber(phoneNumber);
    }

    /**
     * Update adapter data with new search text
     * by restarting the loader
     *
     * @param searchText User entry to filter by name or phone
     */
    public void updateContactList(String searchText) {
        // Update search filter
        dataSearchString = !TextUtils.isEmpty(searchText) ? searchText : "";

        // Restart loader to update cursor with the new search filter
        contactsFragment.getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Update cursor with the current string
        dataSelectionArgs[0] = dataSelectionArgs[1] = "%" + dataSearchString + "%";

        return new CursorLoader(
                contactsFragment.getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                DATA_PROJECTION, DATA_SELECTION,
                dataSelectionArgs,
                DATA_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAlphabeticalAdapter.swapCursor(data);
        contactsFragment.enableListViewIndexedScroll();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAlphabeticalAdapter.swapCursor(null);
    }
}
