package com.github.volfor.notes.login;

import android.content.Context;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.github.volfor.notes.BaseViewModel;
import com.github.volfor.notes.R;
import com.github.volfor.notes.model.User;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import timber.log.Timber;

public class LoginViewModel extends BaseViewModel implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private GoogleApiClient googleApiClient;

    private LoginView view;

    public LoginViewModel(FragmentActivity activity) {
        this.view = (LoginView) activity;

        init(activity);
    }

    private void init(final FragmentActivity activity) {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleApiClient = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        auth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Timber.d("onAuthStateChanged:signed_in:%s", user.getUid());

                    saveUser(user);

                    view.startNoteListActivity();
                } else {
                    // User is signed out
                    Timber.d("onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void start(Context context) {
        auth.addAuthStateListener(authListener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                view.hideProgressDialog();
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    @Override
    public void stop() {
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Timber.d("onConnectionFailed:%s", connectionResult);
        view.showMessage("Google Play Services error.");

        view.hideProgressDialog();
    }

    public void signIn() {
        view.showProgressDialog();
        view.startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), RC_SIGN_IN);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Timber.d("firebaseAuthWithGoogle:%s", acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Timber.d("signInWithCredential:onComplete:%s", task.isSuccessful());

                        view.hideProgressDialog();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Timber.w(task.getException(), "signInWithCredential");
                            view.showMessage("Authentication failed");
                        }
                    }
                });
    }

    private void saveUser(FirebaseUser firebaseUser) {
        User user = User.castToUser(firebaseUser);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        database.child("users").child(user.id).setValue(user);
    }

    @BindingAdapter({"onSignInClick"})
    public static void onSignInButtonClick(SignInButton button, View.OnClickListener listener) {
        button.setOnClickListener(listener);
    }

}
