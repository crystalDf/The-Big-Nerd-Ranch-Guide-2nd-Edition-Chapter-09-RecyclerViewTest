package com.star.recyclerviewtest;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class ImagesAdapter extends
        RecyclerView.Adapter<ImagesAdapter.ViewHolder> {

    private String[] mUrls;

    private LruCache<String, Bitmap> mMemoryCache;

    private Listener mListener;

    public ImagesAdapter(String[] urls) {
        mUrls = urls;

        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public interface Listener {
        void onClick(int position);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private CardView mCardView;

        public ViewHolder(CardView cardView) {
            super(cardView);
            mCardView = cardView;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_image, parent, false);

        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        String url = mUrls[position];

        CardView cardView = holder.mCardView;

        ImageView imageView = (ImageView) cardView.findViewById(R.id.info_image);
        imageView.setImageResource(R.drawable.empty_photo);
        cardView.setTag(url);

        Bitmap bitmap = getBitmapFromMemoryCache(url);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            BitmapWorkerTask bitmapWorkerTask = new BitmapWorkerTask(cardView);
            bitmapWorkerTask.execute(url);
        }

        imageView.setContentDescription(url);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUrls.length;
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        return mMemoryCache.get(key);
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (key != null && bitmap != null && getBitmapFromMemoryCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mImageView;
        private String mUrl;

        private CardView mCardView;

        public BitmapWorkerTask(CardView cardView) {
            mCardView = cardView;
        }

        @Override
        protected Bitmap doInBackground(String... params) {

            mUrl = params[0];

            Bitmap bitmap = downloadBitmap(mUrl);

            addBitmapToMemoryCache(mUrl, bitmap);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            if (mUrl.equals(mCardView.getTag())) {
                mImageView = (ImageView) mCardView.findViewById(R.id.info_image);

                if (mImageView != null && bitmap != null) {
                    mImageView.setImageBitmap(bitmap);
                }
            }

        }

        private Bitmap downloadBitmap(String imageUrl) {
            Bitmap bitmap = null;

            try {

                URL url = new URL(imageUrl);

                bitmap = decodeSampledBitmapFromStream(url, 300, 300);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            return bitmap;
        }

        private Bitmap decodeSampledBitmapFromStream(URL url,
                                                     int requiredWidth, int requiredHeight) {

            HttpURLConnection httpURLConnection = null;

            try {

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5 * 1000);
                httpURLConnection.setReadTimeout(10 * 1000);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                IOUtils.copy(httpURLConnection.getInputStream(), byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();

                final BitmapFactory.Options options = new BitmapFactory.Options();

                options.inJustDecodeBounds = true;

                BitmapFactory.decodeStream(new ByteArrayInputStream(bytes), null, options);

                options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);

                options.inJustDecodeBounds = false;

                return BitmapFactory.decodeStream(new ByteArrayInputStream(bytes), null, options);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }
            }

        }

        private int calculateInSampleSize(BitmapFactory.Options options,
                                          int requiredWidth, int requiredHeight) {

            final int width = options.outWidth;
            final int height = options.outHeight;

            int inSampleSize = 1;

            if (width > requiredWidth || height > requiredHeight) {
                final int widthRatio = width / requiredWidth;
                final int heightRatio = height / requiredHeight;

                inSampleSize = Math.min(widthRatio, heightRatio);
            }

            return inSampleSize;
        }

    }
}
