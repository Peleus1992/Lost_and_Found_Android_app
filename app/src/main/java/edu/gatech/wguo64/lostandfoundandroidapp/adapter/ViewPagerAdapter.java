package edu.gatech.wguo64.lostandfoundandroidapp.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import edu.gatech.wguo64.lostandfoundandroidapp.fragment.FoundFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.LostFragment;
import edu.gatech.wguo64.lostandfoundandroidapp.fragment.MyPostFragment;


/**
 * Created by guoweidong on 10/24/15.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;
    SparseArray<Fragment> registeredFragments = new SparseArray<Fragment>();

    public ViewPagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                LostFragment lostFragment = new LostFragment();
                return lostFragment;
            case 1:
                FoundFragment foundFragment = new FoundFragment();
                return foundFragment;
            case 2:
                MyPostFragment myPostFragment = new MyPostFragment();
                return myPostFragment;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
