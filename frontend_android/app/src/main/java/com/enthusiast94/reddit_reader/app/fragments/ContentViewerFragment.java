package com.enthusiast94.reddit_reader.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.enthusiast94.reddit_reader.app.R;

/**
 * Created by manas on 05-09-2015.
 */
public class ContentViewerFragment extends Fragment {

    public static final String URL_BUNDLE_KEY = "url_key";
    private WebView webView;

    public static ContentViewerFragment newInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(URL_BUNDLE_KEY, url);
        ContentViewerFragment contentViewerFragment = new ContentViewerFragment();
        contentViewerFragment.setArguments(bundle);

        return contentViewerFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_content_viewer, container, false);

        /**
         * Find views
         */

        webView = (WebView) view.findViewById(R.id.webview);

        /**
         * Configure browser settings
         */

        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        // fit content to screen
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        // enable local storage
        webView.getSettings().setDomStorageEnabled(true);
        // force to load url in the webview itself instead of opening default browser
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        /**
         * Retrieve URL from arguments and load it
         */

        String url = getArguments().getString(URL_BUNDLE_KEY);
        webView.loadUrl(url);

        return view;
    }
}
