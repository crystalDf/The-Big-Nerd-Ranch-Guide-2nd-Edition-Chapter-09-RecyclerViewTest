package com.star.recyclerviewtest;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class ImageMaterialFragment extends Fragment {

    private static final int COLUMN_NUM = 3;

    public static ImageMaterialFragment newInstance() {
        return new ImageMaterialFragment();
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RecyclerView recyclerView = (RecyclerView) inflater
                .inflate(R.layout.fragment_image_material, container, false);

        ImagesAdapter captionedImagesAdapter =
                new ImagesAdapter(Images.imageUrls);

        recyclerView.setAdapter(captionedImagesAdapter);

        StaggeredGridLayoutManager staggeredGridLayoutManager =
                new StaggeredGridLayoutManager(COLUMN_NUM, StaggeredGridLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        captionedImagesAdapter.setListener(new ImagesAdapter.Listener() {
            @Override
            public void onClick(int position) {

            }
        });

        return recyclerView;
    }

}
