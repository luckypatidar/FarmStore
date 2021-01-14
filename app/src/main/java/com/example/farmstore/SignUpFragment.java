package com.example.farmstore;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SignUpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SignUpFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SignUpFragment() {
        // Required empty public constructor
    }


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SignUpFragement.
     */
    // TODO: Rename and change types and number of parameters
    public static SignUpFragment newInstance(String param1, String param2) {
        SignUpFragment fragment = new SignUpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                setFragment(new SignInFragment());
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

        // The callback can be enabled or disabled here or in handleOnBackPressed()
    }


    private TextView alreadyHaveAnAccount;
    private FrameLayout parentFrameLayout;

    private EditText email;
    private EditText fullname;
    private EditText password;
    private EditText confirmPassword;

    private ImageView closeBtn;
    private Button signUpBtn;

    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    public static boolean disableCloseBtn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up_fragement, container, false);

        parentFrameLayout = getActivity().findViewById(R.id.register_framelayout);

        alreadyHaveAnAccount = view.findViewById(R.id.already_have_an_account);

        email = view.findViewById(R.id.signup_email);
        fullname = view.findViewById(R.id.signup_name);
        password = view.findViewById(R.id.signup_password);
        confirmPassword = view.findViewById(R.id.signup_confirmpassword);

        closeBtn = view.findViewById(R.id.signup_close_btn);
        signUpBtn = view.findViewById(R.id.signup_button);

        progressBar = view.findViewById(R.id.signup_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (disableCloseBtn){
            closeBtn.setVisibility(View.GONE);
        }else{
            closeBtn.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 setFragment(new SignInFragment());
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainIntent();
            }
        });

        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkInputs();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        fullname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
               checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                  checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                 checkInputs();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkEmailAndPassword();
            }
        });
    }
    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_left,R.anim.slideout_from_right);
        fragmentTransaction.replace(parentFrameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }

    public void checkInputs(){
        if(!TextUtils.isEmpty(email.getText())){
            if(!TextUtils.isEmpty((fullname.getText()))){
                if(!TextUtils.isEmpty(password.getText()) && password.length()>=8){
                    if(!TextUtils.isEmpty(confirmPassword.getText())){
                        signUpBtn.setEnabled(true);
                        signUpBtn.setTextColor(Color.argb(255,255,255,255));
                    }else{
                        signUpBtn.setEnabled(false);
                        signUpBtn.setTextColor(Color.argb(80,255,255,255));
                    }
                }else{
                    signUpBtn.setEnabled(false);
                    signUpBtn.setTextColor(Color.argb(80,255,255,255));
                }
            }else{
                signUpBtn.setEnabled(false);
                signUpBtn.setTextColor(Color.argb(80,255,255,255));
            }
        }else{
              signUpBtn.setEnabled(false);
              signUpBtn.setTextColor(Color.argb(80,255,255,255));
        }
    }

    public void checkEmailAndPassword(){
          if(email.getText().toString().matches(emailPattern)){
              if(password.getText().toString().equals(confirmPassword.getText().toString())){

                  progressBar.setVisibility(View.VISIBLE);
                  signUpBtn.setEnabled(false);
                  signUpBtn.setTextColor(Color.argb(80,255,255,255));

                  firebaseAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                          .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                              @Override
                              public void onComplete(@NonNull Task<AuthResult> task) {
                                  if(task.isSuccessful()){

                                      Map <String,Object > userdata = new HashMap<>();
                                      userdata.put("fullname",fullname.getText().toString());
                                      userdata.put("email",email.getText().toString());
                                      userdata.put("profile","");

                                      firebaseFirestore.collection("USERS").document(firebaseAuth.getUid())
                                              .set(userdata)
                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      if(task.isSuccessful()){

                                                          CollectionReference userDataReference = firebaseFirestore.collection("USERS").document(firebaseAuth.getUid()).collection("USER_DATA");

                                                          ///// MAPS
                                                          Map <String,Object > wishlistMap = new HashMap<>();
                                                          wishlistMap.put("list_size",(long)0);

                                                          Map <String,Object > ratingsMap = new HashMap<>();
                                                          ratingsMap.put("list_size",(long)0);

                                                          Map <String,Object > cartMap = new HashMap<>();
                                                          cartMap.put("list_size",(long)0);

                                                          Map <String,Object > myAddressesMap = new HashMap<>();
                                                          myAddressesMap.put("list_size",(long)0);

                                                          Map <String,Object > notificationMap = new HashMap<>();
                                                          notificationMap.put("list_size",(long)0);

                                                          ////// MAPS

                                                          final List<String> documentNames = new ArrayList<>();
                                                          documentNames.add("MY_WISHLIST");
                                                          documentNames.add("MY_RATINGS");
                                                          documentNames.add("MY_CART");
                                                          documentNames.add("MY_ADDRESSES");
                                                          documentNames.add("MY_NOTIFICATIONS");

                                                          List<Map <String,Object >> documentFields = new ArrayList<>();
                                                          documentFields.add(wishlistMap);
                                                          documentFields.add(ratingsMap);
                                                          documentFields.add(cartMap);
                                                          documentFields.add(myAddressesMap);
                                                          documentFields.add(notificationMap);

                                                          for (int x = 0;x<documentNames.size();x++){
                                                              final int finalX = x;
                                                              userDataReference.document(documentNames.get(x))
                                                                      .set(documentFields.get(x)).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                      if(task.isSuccessful()){
                                                                          if (finalX == documentNames.size() -1) {
                                                                              mainIntent();
                                                                          }
                                                                      }else {
                                                                          progressBar.setVisibility(View.INVISIBLE);
                                                                          signUpBtn.setEnabled(true);
                                                                          signUpBtn.setTextColor(Color.argb(255,255,255,255));
                                                                          String error = task.getException().getMessage();
                                                                          Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                                                                      }
                                                                  }
                                                              });
                                                          }

                                                      }else{
                                                          String error = task.getException().getMessage();
                                                          Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();

                                                      }
                                                  }
                                              });
                                  }else{
                                      progressBar.setVisibility(View.INVISIBLE);
                                      signUpBtn.setEnabled(true);
                                      signUpBtn.setTextColor(Color.argb(255,255,255,255));
                                      String error = task.getException().getMessage();
                                      Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
                                  }
                              }
                          });
              }else{
                  confirmPassword.setError("Password doesn't matched!");
              }
          }else{
              email.setError("Invalid Email!");
          }
    }
    private void mainIntent(){
        if (disableCloseBtn) {
            disableCloseBtn = false;
        }else {
            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
            startActivity(mainIntent);
        }
        getActivity().finish();
     }
}



