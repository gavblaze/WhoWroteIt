package com.gavblaze.android.whowroteit;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private EditText mBookQuery;
    private TextView mTitleText;
    private TextView mAuthorText;

    private static final String BOOK_BASE_URL =  "https://www.googleapis.com/books/v1/volumes?";
    // Parameter for the search string.
    private static final String QUERY_PARAM = "q";
    // Parameter that limits search results.
    private static final String MAX_RESULTS = "maxResults";
    // Parameter to filter by print type.
    private static final String PRINT_TYPE = "printType";

    private LoaderManager mLoaderManager;
    private static final int LOADER_ID = 111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBookQuery = findViewById(R.id.bookInput);
        mTitleText = findViewById(R.id.titleText);
        mAuthorText = findViewById(R.id.authorText);

        mLoaderManager = getSupportLoaderManager();
        if (mLoaderManager.getLoader(LOADER_ID)!= null) {
            mLoaderManager.initLoader(LOADER_ID, null, this);
        }
    }

    public void searchBooks(View view) {

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        //get the value from the editTextField
        String queryString = mBookQuery.getText().toString().trim();

        /*programmatically hide the keyboard and update one of the
        result text views to read "Loading..." while the query is performed.*/
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager != null ) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        mTitleText.setText(R.string.loading);
        mAuthorText.setText("");

        if (networkInfo != null && networkInfo.isConnected() && queryString.length() != 0) {

            //save the value of queryString to a bundle & restart loader
            Bundle bundle = new Bundle();
            bundle.putString("query_key", queryString);
            mLoaderManager.restartLoader(LOADER_ID, bundle, this);
            mAuthorText.setText("");
            mTitleText.setText(R.string.loading);
            
        } else {
            if (queryString.length() == 0) {
                mTitleText.setText(R.string.no_search_term);
                mAuthorText.setText("");
            } else {
                mTitleText.setText(R.string.no_network);
                mAuthorText.setText("");
            }
        }
    }

    private String buildUrl(String editTextQuery) {
        Uri.Builder builder = Uri.parse(BOOK_BASE_URL).buildUpon()
                .appendQueryParameter(QUERY_PARAM, editTextQuery)
                .appendQueryParameter(MAX_RESULTS, "10")
                .appendQueryParameter(PRINT_TYPE, "books");
        return String.valueOf(builder.build());
    }


    @NonNull
    @Override
    public Loader<String> onCreateLoader(int i, @Nullable Bundle bundle) {
        String queryString = null;
        if (bundle != null) {
            queryString = bundle.getString("query_key");
        }
        return new BookLoader(this, buildUrl(queryString));
    }


    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String s) {
        //update the UI
        JSONObject root;
        try {
            root = new JSONObject(s);
            JSONArray itemsArray = root.getJSONArray("items");
            JSONObject itemsObject = itemsArray.getJSONObject(0);
            JSONObject volumeInfoObject = itemsObject.getJSONObject("volumeInfo");
            JSONArray authorInfo = volumeInfoObject.getJSONArray("authors");

            String title = volumeInfoObject.getString("title");
            String author = authorInfo.getString(0);
            mTitleText.setText(title);
            mAuthorText.setText(author);

        } catch (JSONException e) {
            e.printStackTrace();
            mTitleText.setText(R.string.no_result);
            mAuthorText.setText("");
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {
        // Do nothing.  Required by interface.
    }
}
