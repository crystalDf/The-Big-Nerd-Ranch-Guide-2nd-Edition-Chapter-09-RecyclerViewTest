package com.star.recyclerviewtest;

import android.support.v4.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return ImageMaterialFragment.newInstance();
    }
}
