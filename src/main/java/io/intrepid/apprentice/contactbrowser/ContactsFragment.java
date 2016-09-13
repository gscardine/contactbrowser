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
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
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

    /* UI elements */
    ListView mContactsList;

    /* Cursor adapter and parameters */
    private AlphabeticalAdapter mCursorAdapter;
    private AlphabeticalAdapter mAlphabeticalAdapter;

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

    private final static String[] PROJECTION = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY,
            DISPLAY_NAME_FIELD,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    private static final int PROJECTION_INDEX_PHONE_NUMBER = 3;

    private static final String SELECTION =
            DISPLAY_NAME_FIELD + " LIKE ? OR "
            + ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? ";

    private String mSearchString = "";

    private String[] mSelectionArgs = { mSearchString, mSearchString };




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Has menu (search)
        setHasOptionsMenu(true);

        // List View with contacts
        mContactsList = (ListView) getActivity().findViewById(R.id.list_view_contacts);

        // mAlphabeticalAdapter(getActivity(), )

//        mCursorAdapter = new SimpleCursorAdapter(
        mCursorAdapter = new AlphabeticalAdapter(
                getActivity(),
                R.layout.list_item_contacts,
                null,
                FROM_COLUMNS, TO_IDS,
                0
        );

        mContactsList.setAdapter(mCursorAdapter);

        // Set the item click listener to be the current fragment
        mContactsList.setOnItemClickListener(this);

        // Initialize the loader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Update cursor with the current string
        mSelectionArgs[0] = mSelectionArgs[1] = "%" + mSearchString + "%";

        return new CursorLoader(
                getActivity(),
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                PROJECTION, SELECTION,
                mSelectionArgs,
                DISPLAY_NAME_FIELD + "  ASC "
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Get data from the clicked contact
        Cursor cursor = ((AlphabeticalAdapter) parent.getAdapter()).getCursor();
        cursor.moveToPosition(position);
        String phoneNumber = cursor.getString(PROJECTION_INDEX_PHONE_NUMBER);

        // Open phone app with number prefilled
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
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
        mSearchString = !TextUtils.isEmpty(newText) ? newText : "";

        // Restart loader to update cursor with the new search filter
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }
}
