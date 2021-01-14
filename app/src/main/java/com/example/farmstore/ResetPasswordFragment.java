package com.example.farmstore;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ResetPasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResetPasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ResetPasswordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResetPasswordFragment newInstance(String param1, String param2) {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
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
    }

    private EditText registerEmail;
    private Button resetPasswordbtn;
    private TextView goBack;

    private FrameLayout parentFrameLayout;

    private ViewGroup emailIconContainer;

    private TextView emailIconText;
    private ProgressBar emailIconProgressbar;

    private FirebaseAuth firebaseAuth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);

        registerEmail = view.findViewById(R.id.forgot_password_email);
        resetPasswordbtn = view.findViewById(R.id.reset_password_btn);
        goBack = view.findViewById(R.id.forgot_password_goback);

        emailIconContainer = view.findViewById(R.id.forgotpassword_email_icon_containor);

        emailIconText = view.findViewById(R.id.forgot_password_email_icontext);
        emailIconProgressbar = view.findViewById(R.id.forgot_password_progressbar);

        parentFrameLayout = getActivity().findViewById(R.id.register_framelayout);

        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        registerEmail.addTextChangedListener(new TextWatcher() {
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

        resetPasswordbtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {

                TransitionManager.beginDelayedTransition(emailIconContainer);
                emailIconText.setVisibility(View.GONE);

                TransitionManager.beginDelayedTransition(emailIconContainer);

                emailIconProgressbar.setVisibility(getView().VISIBLE);

                resetPasswordbtn.setEnabled(false);
                resetPasswordbtn.setTextColor(Color.argb(80,255,255,255));

                firebaseAuth.sendPasswordResetEmail(registerEmail.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    emailIconText.setText("Email is sent Successfully ! Check your Inbox");
                                    emailIconText.setVisibility(View.VISIBLE);

                                }else{
                                    String error = task.getException().getMessage();
                                    emailIconProgressbar.setVisibility(getView().INVISIBLE);
                                    resetPasswordbtn.setEnabled(true);
                                    resetPasswordbtn.setTextColor(Color.argb(255,255,255,255));

                                    emailIconText.setText(error);
                                    emailIconText.setTextColor(getResources().getColor(R.color.successred));
                                    TransitionManager.beginDelayedTransition(emailIconContainer);
                                    emailIconText.setVisibility(View.VISIBLE);
                                }
                                emailIconProgressbar.setVisibility(View.GONE);

                            }
                        });

            }
        });

        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragment(new SignInFragment());
            }
        });
    }
    private void checkInputs(){
        if(TextUtils.isEmpty(registerEmail.getText())){
           resetPasswordbtn.setEnabled(false);
           resetPasswordbtn.setTextColor(Color.argb(80,255,255,255));
        }else{
            resetPasswordbtn.setEnabled(true);
            resetPasswordbtn.setTextColor(Color.argb(255,255,255,255));
        }
    }
    public void setFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction =getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_from_left,R.anim.slideout_from_right);
        fragmentTransaction.replace(parentFrameLayout.getId(),fragment);
        fragmentTransaction.commit();
    }
}