package edu.gatech.wguo64.lostandfoundandroidapp.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;

import butterknife.ButterKnife;
import edu.gatech.wguo64.lostandfoundandroidapp.R;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Credentials;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.Preferences;
import edu.gatech.wguo64.lostandfoundandroidapp.constants.RequestCodes;
import edu.gatech.wguo64.lostandfoundandroidapp.network.Api;


public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    public final static String TAG = LoginActivity.class.getName();

    public View rootView;
    public SignInButton signInButton;

    public SharedPreferences preferences;

    public GoogleApiClient googleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        inflateViews();
        /**
         * Use ButterKnife
         */
        ButterKnife.inject(this);

        preferences = getSharedPreferences(getPackageName(), MODE_PRIVATE);

        initSignIn();
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
        Snackbar.make(rootView, getString(R.string.google_api_client_unavailable),
                Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent
            data) {
        switch (requestCode) {
            case RequestCodes.SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                break;
            default:
                Log.d(TAG, "onActivityResult: Unknown request code.");
        }
    }

    /**
     * Handle sign in result.
     * @param result
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.i(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            /**
             * Save email and photo url in preferences.
             */
            GoogleSignInAccount account = result.getSignInAccount();
            preferences.edit()
                    .putString(Preferences.ACCOUNT_NAME, account.getEmail())
                    .putString(Preferences.ACCOUNT_PHOTO_URL, account.getPhotoUrl() == null ?
                            null : account.getPhotoUrl().toString()).apply();

            onLoginSuccess();
        } else {
            onLoginFailed();
        }
    }
    /**
     * Start MainActivity.
     */
    private void onLoginSuccess() {
        setBackendApi();
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * Notify user that sign in failed.
     */
    private void onLoginFailed() {
        Log.d(TAG, "handleSignInResult: Sign in failed.");
        Snackbar.make(rootView,
                getString(R.string.common_google_play_services_sign_in_failed_text),
                Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Inflate views from xml files.
     */
    private void inflateViews() {
        rootView =  findViewById(R.id.rootView);
        signInButton = (SignInButton) findViewById(R.id.signInBtn);
    }

    /**
     * Google Sign in
     * Request both email and id
     */
    private void initSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestId()
                .requestProfile()
                .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, RequestCodes.SIGN_IN);
            }
        });
    }

    private void setBackendApi() {
        GoogleAccountCredential credential = GoogleAccountCredential
                .usingAudience(this,
                        Credentials.AUDIENCE);
        credential.setSelectedAccountName(preferences.getString(Preferences.ACCOUNT_NAME, null));
        Log.i(TAG, "setBackendApi: Credential account name: " +
                credential.getSelectedAccountName());
        Api.initialize(credential);
    }

}
