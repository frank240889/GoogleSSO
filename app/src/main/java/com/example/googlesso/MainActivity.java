package com.example.googlesso;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.googlesso.databinding.ActivityMainBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

  private static final int RC_SIGN_IN = 25;
  GoogleSignInClient mGoogleSignInClient;
  ActivityMainBinding mActivityMainBinding;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    mActivityMainBinding.setLifecycleOwner(this);
    mActivityMainBinding.progressBar.setVisibility(View.VISIBLE);

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestProfile()
        .requestIdToken("1077650617219-gshpl4c5u88f1m710g2p5hbo27nsgngk.apps.googleusercontent.com")
        .build();

    mGoogleSignInClient = GoogleSignIn.getClient(getApplicationContext(), gso);

    GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
    updateUi(googleSignInAccount);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mActivityMainBinding.progressBar.setVisibility(View.GONE);
    if(resultCode == Activity.RESULT_OK && requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> googleSignInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        GoogleSignInAccount account = googleSignInAccountTask.getResult(ApiException.class);
        updateUi(account);
      } catch (ApiException e) {
        e.printStackTrace();
        Log.w("GSSO", e.getStatusCode()+"");
      }
    }
  }

  private void updateUi(GoogleSignInAccount googleSignInAccount) {
    if(googleSignInAccount == null) {
      mActivityMainBinding.displayName.setText("");
      mActivityMainBinding.email.setText("");
      mActivityMainBinding.imageProfile.setVisibility(View.GONE);
      mActivityMainBinding.imageProfile.setImageURI(null);
      mActivityMainBinding.signOutButton.setVisibility(View.GONE);
      mActivityMainBinding.signOutButton.setOnClickListener(null);
      mActivityMainBinding.signInButton.setVisibility(View.VISIBLE);
      mActivityMainBinding.signInButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          mActivityMainBinding.progressBar.setVisibility(View.VISIBLE);
          Intent signIntent = mGoogleSignInClient.getSignInIntent();
          startActivityForResult(signIntent, RC_SIGN_IN);
        }
      });
    }
    else {
      mActivityMainBinding.signOutButton.setVisibility(View.VISIBLE);
      mActivityMainBinding.signOutButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              mGoogleSignInClient.revokeAccess().addOnCompleteListener(
                  new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      updateUi(null);
                    }
                  });
            }
          });
        }
      });
      mActivityMainBinding.displayName.setText(googleSignInAccount.getDisplayName());
      mActivityMainBinding.email.setText(googleSignInAccount.getEmail());
      mActivityMainBinding.imageProfile.setVisibility(View.VISIBLE);
      Glide.with(mActivityMainBinding.imageProfile)
          .load(googleSignInAccount.getPhotoUrl())
          .apply(RequestOptions.circleCropTransform())
          .into(mActivityMainBinding.imageProfile);
      mActivityMainBinding.signInButton.setVisibility(View.GONE);
      mActivityMainBinding.signInButton.setOnClickListener(null);
    }
    mActivityMainBinding.progressBar.setVisibility(View.GONE);
  }
}
