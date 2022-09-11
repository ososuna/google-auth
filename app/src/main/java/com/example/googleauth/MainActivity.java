package com.example.googleauth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

  private TextView userNombre,userEmail,userID;
  private CircleImageView userImg;
  Button btnCerrarSesion, btnEliminarCta;
  //Variable para gestionar FirebaseAuth
  private FirebaseAuth mAuth;
  //Variables opcionales para desloguear de google tambien
  private GoogleSignInClient mGoogleSignInClient;
  private GoogleSignInOptions gso;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    userNombre = findViewById(R.id.userNombre);
    userEmail = findViewById(R.id.userEmail);
    userID = findViewById(R.id.userId);
    userImg = findViewById(R.id.userImagen);
    btnEliminarCta = findViewById(R.id.btnEliminarCta);

    mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();

    userNombre.setText(currentUser.getDisplayName());
    userEmail.setText(currentUser.getEmail());
    userID.setText(currentUser.getUid());
    //cargar imágen con glide:
    Glide.with(this).load(currentUser.getPhotoUrl()).into(userImg);

    //Configurar las gso para google signIn con el fin de luego desloguear de google
    gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();
    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    //
    btnEliminarCta.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        //obtener el usuario actual
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get the account
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (signInAccount != null) {
          AuthCredential credential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);
          //Re-autenticar el usuario para eliminarlo
          user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              if (task.isSuccessful()) {
                //Eliminar el usuario
                user.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                  @Override
                  public void onSuccess(Void aVoid) {
                    Log.d("MainActivity", "onSuccess:Usuario Eliminado");
                    //llamar al metodo signOut para salir de aqui
                    signOut();
                  }
                });
              } else {
                Log.e("MainActivity", "onComplete: Error al eliminar el usuario",
                        task.getException());
              }
            }
          });
        } else {
          Log.d("MainActivity", "Error: reAuthenticateUser: user account is null");
        }
      }
    });//fin onClic
  }
  //
  private void signOut() {
    //sign out de firebase
    FirebaseAuth.getInstance().signOut();
    //sign out de "google sign in"
    mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        //regresar al login screen o MainActivity
        //Abrir mainActivity para que inicie sesión o sign in otra vez.
        Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginActivity);
        MainActivity.this.finish();
      }
    });
  }

}