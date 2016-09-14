package io.intrepid.apprentice.contactbrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ContactsFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener {

    ContactsPresenter presenter;

    /* UI elements */
    ListView mContactsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        presenter = new ContactsPresenter(this);

        // Has menu (search)
        setHasOptionsMenu(true);

        // List View with contacts
        mContactsList = (ListView) getActivity().findViewById(R.id.list_view_contacts);

        mContactsList.setAdapter(presenter.mAlphabeticalAdapter);

        // Set the item click listener to be the current fragment
        mContactsList.setOnItemClickListener(this);
    }

    /**
     * Enable fast scroll, so that alphabetical index is displayed
     */
    public void enableListViewIndexedScroll() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mContactsList.setFastScrollAlwaysVisible(true);
        }
        mContactsList.setFastScrollEnabled(true);
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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        presenter.getContactPhoneNumber(parent, position);
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
    /**
     * Mandatory method that will not be called in this case
     * (no submit button, but real-time result)
     */
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        presenter.updateContactList(newText);
        return true;
    }
}
