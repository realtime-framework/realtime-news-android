package co.realtime.realtimenews;

import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


public class WebViewActivity extends ActionBarActivity {

    private static boolean mIsInForegroundMode;
    private WebView myWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        String url = getIntent().getStringExtra("url");
        String body = getIntent().getStringExtra("body");
        String title = getIntent().getStringExtra("title");

        myWebView = (WebView) findViewById(R.id.webView);
        myWebView.setWebViewClient(new MyWebViewClient());

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (body != null) {
            myWebView.loadData(body,"text/html", null);
        }else if (url!= null){
            if (isFile(url)) {
                myWebView.loadUrl("https://docs.google.com/gview?embedded=true&url="+ url);
            }else{
                myWebView.loadUrl(url);
            }
        }

        setTitle(title);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsInForegroundMode = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsInForegroundMode = true;
    }

    private class MyWebViewClient extends WebViewClient {

        private ProgressBar mDialog;

        public MyWebViewClient(){
            mDialog = (ProgressBar)findViewById(R.id.progressDialog);
        }
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mDialog.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mDialog.setVisibility(View.GONE);
        }
    }

    public static boolean isInForeground() {
        return mIsInForegroundMode;
    }

    private boolean isFile(String url){
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
            if (type!= null && type.equalsIgnoreCase("application/pdf")){
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

}


