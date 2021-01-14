package com.example.farmstore;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyCartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyCartFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyCartFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyCartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyCartFragment newInstance(String param1, String param2) {
        MyCartFragment fragment = new MyCartFragment();
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

    private RecyclerView cartItemsRecyclerView;
    private Button continueBtn;
    private Dialog loadingDialog;
    public static CartAdapter cartAdapter;
    private TextView totalAmount;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);

        //// LOADING DIALOG
        loadingDialog = new Dialog(getContext());
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getContext().getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        //// LOADING DIALOG

        cartItemsRecyclerView = view.findViewById(R.id.cart_items_recycler_view);
        continueBtn = view.findViewById(R.id.cart_continue_btn);
        totalAmount = view.findViewById(R.id.total_cart_amount);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cartItemsRecyclerView.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(DBQueries.cartItemModelList, totalAmount, true);
        cartItemsRecyclerView.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeliveryActivity.cartItemModelList = new ArrayList<>();
                DeliveryActivity.fromCart = true;

                for (int x = 0; x < DBQueries.cartItemModelList.size(); x++) {
                    CartItemModel cartItemModel = DBQueries.cartItemModelList.get(x);
                    if (cartItemModel.isInStock()) {
                        DeliveryActivity.cartItemModelList.add(cartItemModel);
                    }
                }
                DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));

                loadingDialog.show();
                if (DBQueries.addressesModelList.size() == 0) {
                    DBQueries.loadAddresses(getContext(), loadingDialog,true);
                } else {
                    loadingDialog.dismiss();
                    Intent deliveryIntent = new Intent(getContext(), DeliveryActivity.class);
                    startActivity(deliveryIntent);
                }
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        cartAdapter.notifyDataSetChanged();

        if (DBQueries.rewardModelsList.size() == 0){
            loadingDialog.show();
            DBQueries.loadRewards(getContext(),loadingDialog,false);
        }

        if (DBQueries.cartItemModelList.size() == 0) {
            DBQueries.cartList.clear();
            DBQueries.loadCartList(getContext(), loadingDialog, true, new TextView(getContext()), totalAmount);
        } else {
            if (DBQueries.cartItemModelList.get(DBQueries.cartItemModelList.size() - 1).getType() == CartItemModel.TOTAL_AMOUNT) {
                LinearLayout parent = (LinearLayout) totalAmount.getParent().getParent();
                parent.setVisibility(View.VISIBLE);
            }
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (CartItemModel cartItemModel: DBQueries.cartItemModelList) {
            if (!TextUtils.isEmpty(cartItemModel.getSelectedCoupenId())){
                for (RewardModel rewardModel : DBQueries.rewardModelsList) {
                    if (rewardModel.getCoupenId().equals(cartItemModel.getSelectedCoupenId())) {
                        rewardModel.setAlreadyUsed(false);
                    }
                }
                cartItemModel.setSelectedCoupenId(null);
                if (MyRewardsFragment.myRewardsAdapter != null) {
                    MyRewardsFragment.myRewardsAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}