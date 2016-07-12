package com.brucetoo.wifitransport.HotPot.video;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.brucetoo.wifitransport.R;

/**
 * Created by Bruce Too
 * On 7/7/16.
 * At 14:32
 */
public class WebFragment extends Fragment {
    private static final String TAG = WebFragment.class.getSimpleName();

    private WebView mWebView;
    float x, y;

    public static WebFragment newInstance() {

        Bundle args = new Bundle();

        WebFragment fragment = new WebFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web, container, false);
        mWebView = (WebView) view.findViewById(R.id.web_view);
        return view;
    }

    @SuppressLint({"JavascriptInterface", "AddJavascriptInterface", "SetJavaScriptEnabled"})
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

//        mWebView.loadUrl("https://pixabay.com");
        mWebView.loadData("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<div style=\"text-align:center\">\n" +
                "  <button onclick=\"playPause()\">Play/Pause</button>\n" +
                "  <button onclick=\"makeBig()\">Big</button>\n" +
                "  <button onclick=\"makeSmall()\">Small</button>\n" +
                "  <button onclick=\"makeNormal()\">Normal</button>\n" +
                "  <br><br>\n" +
                "  <video id=\"video1\" width=\"420\">\n" +
                "    <source id=\"src\" src=\"http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4\" type=\"video/mp4\">\n" +
                "    <source src=\"http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4\" type=\"video/ogg\">\n" +
                "    Your browser does not support HTML5 video.\n" +
                "  </video>\n" +
                "</div>\n" +
                "\n" +
                "<script>\n" +
                "var myVideo = document.getElementById(\"video1\");\n" +
                "\n" +
                "function playPause() {\n" +
                "    if (myVideo.paused)\n" +
                "        myVideo.play();\n" +
                "    else\n" +
                "        myVideo.pause();\n" +
                "}\n" +
                "\n" +
                "function makeBig() {\n" +
                "    myVideo.width = 560;\n" +
                "}\n" +
                "\n" +
                "function makeSmall() {\n" +
                "    myVideo.width = 320;\n" +
                "}\n" +
                "\n" +
                "function makeNormal() {\n" +
                "    myVideo.width = 420;\n" +
                "}\n" +
                "</script>\n" +
                "\n" +
                "<p>Video courtesy of <a href=\"http://www.bigbuckbunny.org/\" target=\"_blank\">Big Buck Bunny</a>.</p>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n", "text/html", "");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.addJavascriptInterface(new JInterface(getContext()), "videoClick");

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                setVideoClickListener();
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                Log.i(TAG, "load progress -> " + newProgress);
            }
        });

    }


    private void setVideoClickListener() {

        mWebView.loadUrl("javascript:(function(){" +
                "var elms = document.getElementsByTagName(\"video\"); " +
                "for(var i=0;i<elms.length;i++)  " +
                "{"
                + "    elms[i].onclick=function()  " +
                "    {  "
                + "        window.videoClick.onVideoClick(this.children[0].src);  " +
                "    }  " +
                "}" +
                "})()");
    }


    public class JInterface {

        private Context context;

        public JInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void onVideoClick(String src) {
            Log.i(TAG, "video url:" + src);
            Intent intent = new Intent(getActivity(), VideoPlayDialogActivity.class);
            intent.putExtra("url", src);
            intent.putExtra("title", "BUCK");
            startActivity(intent);
        }
    }

    public void goBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        }
    }

    public boolean canGoBack() {
        return mWebView.canGoBack();
    }


}
