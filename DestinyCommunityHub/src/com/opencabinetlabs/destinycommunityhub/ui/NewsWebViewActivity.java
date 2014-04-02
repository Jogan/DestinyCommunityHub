package com.opencabinetlabs.destinycommunityhub.ui;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.opencabinetlabs.destinycommunityhub.R;
import com.opencabinetlabs.destinycommunityhub.ui.view.HTML5WebView;

import java.lang.reflect.InvocationTargetException;

public class NewsWebViewActivity extends Activity {

    private HTML5WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminateVisibility(true);
        mWebView = new HTML5WebView(this);

        if (savedInstanceState != null) {
            mWebView.restoreState(savedInstanceState);
        } else {
            // Retrieve the data we need for the webview to display
            Bundle bundle = getIntent().getExtras();
            String url = bundle.getString("url");
            mWebView.loadUrl(url);
        }
        setContentView(mWebView.getLayout());

        getActionBar().setIcon(R.drawable.action_bar_icon);
        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#222222")));

    }


    /**
     * Overriding this method allows us to run our exit animation first, then exiting
     * the activity when it is complete.
     */
    @Override
    public void onBackPressed()
    {
        if(mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_with_cancel_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_cancel:
                this.finish();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        mWebView.stopLoading();
        mWebView.clearCache(true);
        mWebView.clearHistory();
    }

    @Override
    public void onPause() {
        super.onPause();

        try {
            Class.forName("android.webkit.WebView")
                    .getMethod("onPause", (Class[]) null)
                    .invoke(mWebView, (Object[]) null);

        } catch(ClassNotFoundException cnfe) {

        } catch(NoSuchMethodException nsme) {

        } catch(InvocationTargetException ite) {

        } catch (IllegalAccessException iae) {

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWebView.inCustomView()) {
                mWebView.hideCustomView();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
