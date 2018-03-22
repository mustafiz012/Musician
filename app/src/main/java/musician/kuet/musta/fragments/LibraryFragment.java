package musician.kuet.musta.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import musician.kuet.musta.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraryFragment extends Fragment {

    private ViewPager viewPager;
    private Context mContext;

    public LibraryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_music_library, container, false);
        mContext = getContext();
        /*Toolbar toolbar = rootView.findViewById(R.id.toolbar_music_library);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        ab.setHomeAsUpIndicator(R.drawable.ic_nav_menu);
        ab.setDisplayHomeAsUpEnabled(true);*/

        viewPager = rootView.findViewById(R.id.viewpager_music_library);
        if (viewPager != null) {
            setupViewPager(viewPager);
            viewPager.setOffscreenPageLimit(2);
        }

        TabLayout tabLayout = rootView.findViewById(R.id.tabs_music_library);
        tabLayout.setupWithViewPager(viewPager);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_fragment_library, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_go_to_artist:
                Toast.makeText(mContext, "Go to artist", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_sort_by:
                Toast.makeText(mContext, "Sort list", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getChildFragmentManager());
        tabPagerAdapter.addFragment(new SongsFragment(), this.getString(R.string.songs));
        tabPagerAdapter.addFragment(new AlbumsFragment(), this.getString(R.string.albums));
        tabPagerAdapter.addFragment(new ArtistsFragment(), this.getString(R.string.artists));
        viewPager.setAdapter(tabPagerAdapter);
    }

    private static class TabPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public TabPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }

}
