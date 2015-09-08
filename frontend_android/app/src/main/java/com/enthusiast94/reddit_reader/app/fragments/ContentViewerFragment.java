package com.enthusiast94.reddit_reader.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
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
    public static final String CONTENT_TITLE_BUNDLE_KEY = "content_title_key";
    private Toolbar toolbar;
    private WebView webView;

    public static ContentViewerFragment newInstance(String contentTitle, String url) {
        Bundle bundle = new Bundle();
        bundle.putString(CONTENT_TITLE_BUNDLE_KEY, contentTitle);
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

        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        webView = (WebView) view.findViewById(R.id.webview);

        /**
         * Retrieve content info from arguments
         */

        Bundle bundle = getArguments();
        final String contentTitle = bundle.getString(CONTENT_TITLE_BUNDLE_KEY);
        final String contentUrl = bundle.getString(URL_BUNDLE_KEY);


        /**
         * Setup toolbar
         */

        toolbar.setTitle(R.string.loading);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

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

            @Override
            public void onPageFinished(WebView view, String url) {
                if (contentTitle == null) {
                    toolbar.setTitle(contentUrl);
                } else {
                    toolbar.setTitle(contentTitle);
                    toolbar.setSubtitle(contentUrl);
                }
            }
        });

        /**
         * Finally, load URL
         */

        webView.loadUrl(contentUrl);

        return view;
    }
}
