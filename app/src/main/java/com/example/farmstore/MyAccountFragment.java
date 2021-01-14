package com.example.farmstore;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyAccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyAccountFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyAccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyAccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyAccountFragment newInstance(String param1, String param2) {
        MyAccountFragment fragment = new MyAccountFragment();
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

    private FloatingActionButton settingsBtn;
    private Button viewAllAddressButton,signOutBtn;
    public static final int MANAGE_ADDRESS = 1;
    private CircleImageView profileView, currentOrderImage;
    private TextView name, email, tvCurrentOrderStatus;
    private LinearLayout layoutContainer, recentOrdersContainer;
    private Dialog loadingDialog;
    private ImageView orderIndicator, packedIndicator, shippedIndicator, deliveredIndicator;
    private ProgressBar O_P_progress, P_S_progress, S_D_progress;
    private TextView yourRecentOrdersTitle;
    private TextView addressName,address,pincode;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);

        ///loading dialog
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        ///loading dialog

        profileView = view.findViewById(R.id.profile_image);
        name = view.findViewById(R.id.username);
        email = view.findViewById(R.id.user_email);
        layoutContainer = view.findViewById(R.id.layout_container);
        currentOrderImage = view.findViewById(R.id.current_order_image);
        tvCurrentOrderStatus = view.findViewById(R.id.tv_current_order_status);

        orderIndicator = view.findViewById(R.id.ordered_indicator);
        packedIndicator = view.findViewById(R.id.packed_indicator);
        shippedIndicator = view.findViewById(R.id.shipped_indicator);
        deliveredIndicator = view.findViewById(R.id.delivered_indicator);

        O_P_progress = view.findViewById(R.id.order_packed_progress);
        P_S_progress = view.findViewById(R.id.packed_shipped_progress);
        S_D_progress = view.findViewById(R.id.shipped_deliverd_progress);
        yourRecentOrdersTitle = view.findViewById(R.id.your_recent_orders_title);
        recentOrdersContainer = view.findViewById(R.id.recent_orders_container);
        addressName = view.findViewById(R.id.address_full_name);
        address = view.findViewById(R.id.address);
        pincode = view.findViewById(R.id.address_pincode);
        signOutBtn = view.findViewById(R.id.sign_out_btn);
        settingsBtn = view.findViewById(R.id.settings_btn);

        layoutContainer.getChildAt(1).setVisibility(View.GONE);
        loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                for (MyOrderItemModel orderItemModel : DBQueries.myOrderItemModelList) {
                    if (!orderItemModel.isCancellationRequested()) {
                        if (!orderItemModel.getOrderStatus().equals("Delivered") && !orderItemModel.getOrderStatus().equals("Cancelled")) {
                            layoutContainer.getChildAt(1).setVisibility(View.VISIBLE);
                            Glide.with(getContext()).load(orderItemModel.getProductImage()).apply(new RequestOptions().placeholder(R.drawable.place_holder_1)).into(currentOrderImage);
                            tvCurrentOrderStatus.setText(orderItemModel.getOrderStatus());

                            switch (orderItemModel.getOrderStatus()) {
                                case "Ordered":
                                    orderIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    break;
                                case "Packed":
                                    orderIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    O_P_progress.setProgress(100);
                                    break;
                                case "Shipped":
                                    orderIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));

                                    O_P_progress.setProgress(100);
                                    P_S_progress.setProgress(100);
                                    break;
                                case "Out for Delivery":
                                    orderIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    packedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    shippedIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));
                                    deliveredIndicator.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.successgreen)));

                                    O_P_progress.setProgress(100);
                                    P_S_progress.setProgress(100);
                                    S_D_progress.setProgress(100);
                                    break;

                            }
                        }
                    }
                }
                int i = 0;
                for (MyOrderItemModel myOrderItemModel : DBQueries.myOrderItemModelList) {
                    if (i<4) {
                        if (myOrderItemModel.getOrderStatus().equals("Delivered")) {
                            Glide.with(getContext()).load(myOrderItemModel.getProductImage()).apply(new RequestOptions().placeholder(R.drawable.place_holder_1)).into((CircleImageView) recentOrdersContainer.getChildAt(i));
                            i++;
                        }
                    }else {
                        break;
                    }
                }
                if (i == 0){
                    yourRecentOrdersTitle.setText("No recent Orders.");
                }
                if (i < 3){
                    for (int x = i; x<4;x++){
                        recentOrdersContainer.getChildAt(x).setVisibility(View.GONE);
                    }
                }
                loadingDialog.show();
                loadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        loadingDialog.setOnDismissListener(null);
                        if (DBQueries.addressesModelList.size() == 0){
                            addressName.setText("No Address is saved");
                            address.setText("-");
                            pincode.setText("-");
                        }else {
                           setAddress();
                        }
                    }
                });
                DBQueries.loadAddresses(getContext(),loadingDialog,false);
            }
        });
        DBQueries.loadOrders(getContext(), null, loadingDialog);

        viewAllAddressButton = view.findViewById(R.id.view_all_address_btn);
        viewAllAddressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myAddressesIntent = new Intent(getContext(), MyAddressActivity.class);
                myAddressesIntent.putExtra("MODE", MANAGE_ADDRESS);
                startActivity(myAddressesIntent);
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                DBQueries.clearData();
                Intent registerIntent = new Intent(getContext(), RegisterActivity.class);
                startActivity(registerIntent);
                getActivity().finish();
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent updateUserInfo = new Intent(getContext(),UpdateUserInfoActivity.class);
                updateUserInfo.putExtra("Name",name.getText());
                updateUserInfo.putExtra("Email",email.getText());
                updateUserInfo.putExtra("Photo",DBQueries.profile);
                startActivity(updateUserInfo);
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        name.setText(DBQueries.fullname);
        email.setText(DBQueries.email);
        if (!DBQueries.profile.equals("")) {
            Glide.with(getContext()).load(DBQueries.profile).apply(new RequestOptions().placeholder(R.drawable.finaluser_icon)).into(profileView);
        }else {
            profileView.setImageResource(R.drawable.user_placeholder);
        }
        if (!loadingDialog.isShowing()){
            if (DBQueries.addressesModelList.size() == 0){
                addressName.setText("No Address is saved");
                address.setText("-");
                pincode.setText("-");
            }else {
                setAddress();
            }
        }
    }

    private void setAddress(){
        String nameText,mobileNo;
        nameText = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getName();
        mobileNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getMobileNo();
        if (DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo().equals("")) {
            addressName.setText(nameText + " - " + mobileNo);
        }else {
            addressName.setText(nameText + " - " + mobileNo + " or " + DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo());
        }
        String flatNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getFlatNo();
        String locality = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLocality();
        String landmark = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLandmark();
        String city = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getCity();
        String state = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getState();

        if (landmark.equals("")){
            address.setText(flatNo +" "+ locality +" "+ city +" "+ state);
        }else {
            address.setText(flatNo + " " + locality + " " + landmark + " " + city + " " + state);
        }
        pincode.setText(DBQueries.addressesModelList.get(DBQueries.selectedAddress).getPincode());
    }
}