package io.intrepid.apprentice.contactbrowser;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ContactsFragment extends Fragment implements
        LoaderCallbacks<Cursor>,
        AdapterView.OnItemClickListener, SearchView.OnQueryTextListener {

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

    private final static int PROJECTION_INDEX_PHONE_NUMBER = 3;

    private final static String DATA_SELECTION =
            DISPLAY_NAME_FIELD + " LIKE ? OR "
            + ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? ";

    private final static String DATA_ORDER = DISPLAY_NAME_FIELD + "  ASC ";

    ListView ContactsListView;

    private AlphabeticalAdapter alphabeticalAdapter;

    private String dataSearchString = "";

    private String[] dataSelectionArgs = { dataSearchString, dataSearchString };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Has menu (search)
        setHasOptionsMenu(true);

        // List View with contacts
        ContactsListView = (ListView) getActivity().findViewById(R.id.list_view_contacts);

        // Alphabetical Adapter with indexer
        alphabeticalAdapter = new AlphabeticalAdapter(
                getActivity(),
                R.layout.list_item_contacts,
                null,
                FROM_COLUMNS, TO_IDS,
                0
        );

        ContactsListView.setAdapter(alphabeticalAdapter);

        // Set the item click listener to be the current fragment
        ContactsListView.setOnItemClickListener(this);

        // Initialize the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Update cursor with the current string
        dataSelectionArgs[0] = dataSelectionArgs[1] = "%" + dataSearchString + "%";

        return new CursorLoader(
                getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                DATA_PROJECTION, DATA_SELECTION,
                dataSelectionArgs,
                DATA_ORDER
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateCursor(data);
        enableListViewIndexedScroll();
    }

    /**
     * Replace cursor with new data
     *
     * @param newCursor New cursor
     */
    public void updateCursor(Cursor newCursor) {
        alphabeticalAdapter.swapCursor(newCursor);
    }

    /**
     * Open phone app with number pre-filled
     *
     * @param phoneNumber
     */
    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * Enable fast scroll, so that alphabetical index is displayed
     */
    public void enableListViewIndexedScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContactsListView.setFastScrollAlwaysVisible(true);
        }
        ContactsListView.setFastScrollEnabled(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        alphabeticalAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = ((AlphabeticalAdapter) parent.getAdapter()).getCursor();

        String phoneNumber = getContactPhoneNumber(cursor, position);
        dialPhoneNumber(phoneNumber);
    }

    /**
     * Get data from current contact
     *
     * @param cursor Current cursor
     * @param position Position of currently selected contact
     */
    public String getContactPhoneNumber(Cursor cursor, int position) {
        cursor.moveToPosition(position);
        return cursor.getString(PROJECTION_INDEX_PHONE_NUMBER);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Create a search menu item (bar)
        MenuItem searchItem = menu.add(R.string.search_menu_item);
        searchItem.setIcon(android.R.drawable.ic_menu_search);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        // Set query listener to search view
        SearchView searchView = new SearchView(getActivity());
        searchView.setOnQueryTextListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchItem.setActionView(searchView);
        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // No submit button (realtime search)
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Update search filter
        dataSearchString = !TextUtils.isEmpty(newText) ? newText : "";

        // Restart loader to update cursor with the new search filter
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }
}
