package com.example.farmstore;

import android.app.Dialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.farmstore.DeliveryActivity.SELECT_ADDRESS;
import static com.example.farmstore.MyAccountFragment.MANAGE_ADDRESS;
import static com.example.farmstore.MyAddressActivity.refreshItem;

public class AddressesAdapter extends RecyclerView.Adapter<AddressesAdapter.Viewholder> {

    private List<AddressesModel> addressesModelList;
    private int MODE;
    private int preSelectedPosition;
    private boolean refresh = false;
    private Dialog loadingDialog;

    public AddressesAdapter(List<AddressesModel> addressesModelList, int MODE, Dialog loadingDialog) {
        this.addressesModelList = addressesModelList;
        this.MODE = MODE;
        preSelectedPosition = DBQueries.selectedAddress;
        this.loadingDialog = loadingDialog;

    }

    @NonNull
    @Override
    public AddressesAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.addresses_item_layout, viewGroup, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AddressesAdapter.Viewholder viewholder, int position) {
        String city = addressesModelList.get(position).getCity();
        String locality = addressesModelList.get(position).getLocality();
        String flatNo = addressesModelList.get(position).getFlatNo();
        String pincode = addressesModelList.get(position).getPincode();
        String landmark = addressesModelList.get(position).getLandmark();
        String name = addressesModelList.get(position).getName();
        String mobileNo = addressesModelList.get(position).getMobileNo();
        String alternateMobileNo = addressesModelList.get(position).getAlternateMobileNo();
        String state = addressesModelList.get(position).getState();
        boolean selected = addressesModelList.get(position).getSelected();

        viewholder.setData(name, city, pincode, selected, position, mobileNo, alternateMobileNo, flatNo, locality, state, landmark);
    }

    @Override
    public int getItemCount() {
        return addressesModelList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {

        private TextView fullname;
        private TextView address;
        private TextView pincode;
        private ImageView icon;
        private LinearLayout optionContainer;

        public Viewholder(@NonNull View itemView) {
            super(itemView);

            fullname = itemView.findViewById(R.id.name);
            address = itemView.findViewById(R.id.address);
            pincode = itemView.findViewById(R.id.pincode);
            icon = itemView.findViewById(R.id.icon_view);
            optionContainer = itemView.findViewById(R.id.option_container);
        }

        private void setData(String userName, String city, String userPincode, final Boolean selected, final int position, String mobileNo, String alternateMobileNo, String flatNo, String locality, String state, String landmark) {
            if (alternateMobileNo.equals("")) {
                fullname.setText(userName + " - " + mobileNo);
            } else {
                fullname.setText(userName + " - " + mobileNo + " or " + alternateMobileNo);
            }
            if (landmark.equals("")) {
                address.setText(flatNo + " " + locality + " " + city + " " + state);
            } else {
                address.setText(flatNo + " " + locality + " " + landmark + " " + city + " " + state);
            }
            pincode.setText(userPincode);

            if (MODE == SELECT_ADDRESS) {
                icon.setImageResource(R.mipmap.right_icon);
                if (selected) {
                    icon.setVisibility(View.VISIBLE);
                    preSelectedPosition = position;
                } else {
                    icon.setVisibility(View.GONE);
                }

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (preSelectedPosition != position) {
                            addressesModelList.get(position).setSelected(true);
                            addressesModelList.get(preSelectedPosition).setSelected(false);
                            refreshItem(preSelectedPosition, position);
                            preSelectedPosition = position;
                            DBQueries.selectedAddress = position;
                        }
                    }
                });

            } else if (MODE == MANAGE_ADDRESS) {
                optionContainer.setVisibility(View.GONE);
                optionContainer.getChildAt(0).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ///edit address
                        Intent addAddressIntent = new Intent(itemView.getContext(), AddAddressActivity.class);
                        addAddressIntent.putExtra("INTENT","update_address");
                        addAddressIntent.putExtra("index",position);
                        itemView.getContext().startActivity(addAddressIntent);
                        refresh = false;
                    }
                });

                optionContainer.getChildAt(1).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ///remove address
                        loadingDialog.show();
                        Map<String,Object> addresses = new HashMap<>();
                        int x = 0;
                        int selected = -1;
                        for(int i = 0 ; i < addressesModelList.size() ;i++ ){
                            if (i != position){
                                x++;
                                addresses.put("city_" + x, addressesModelList.get(i).getCity());
                                addresses.put("locality_" + x,addressesModelList.get(i).getLocality());
                                addresses.put("flat_no_" + x,addressesModelList.get(i).getFlatNo());
                                addresses.put("pincode_" + x,addressesModelList.get(i).getPincode());
                                addresses.put("landmark_" + x,addressesModelList.get(i).getLandmark());
                                addresses.put("name_"  + x,addressesModelList.get(i).getName());
                                addresses.put("mobile_no_"+ x,addressesModelList.get(i).getMobileNo());
                                addresses.put("alternate_mobile_no_" + x,addressesModelList.get(i).getAlternateMobileNo());
                                addresses.put("state_" + x,addressesModelList.get(i).getState());
                               if (addressesModelList.get(position).getSelected()){
                                   if (position -1 >= 0){
                                       if (x == position ){
                                           addresses.put("selected_" + x,true);
                                           selected = x;
                                       }else {
                                           addresses.put("selected_" + x,addressesModelList.get(i).getSelected());
                                       }
                                   }else{
                                       if (x ==1 ){
                                           selected = x;
                                           addresses.put("selected_" + x,true);
                                       }else {
                                           addresses.put("selected_" + x,addressesModelList.get(i).getSelected());
                                       }
                                   }
                               }else {
                                   addresses.put("selected_" + x,addressesModelList.get(i).getSelected());
                                   if (addressesModelList.get(i).getSelected()){
                                       selected = x;
                                   }
                               }
                            }
                        }
                        addresses.put("list_size",x);

                        final int finalSelected = selected;
                        FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_ADDRESSES")
                                .set(addresses).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    DBQueries.addressesModelList.remove(position);
                                    if (finalSelected != -1) {
                                        DBQueries.selectedAddress = finalSelected - 1;
                                        DBQueries.addressesModelList.get(finalSelected - 1).setSelected(true);
                                    }else if (DBQueries.addressesModelList.size()==0){
                                        DBQueries.selectedAddress = -1;
                                    }
                                    notifyDataSetChanged();
                                }else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                                }
                                loadingDialog.dismiss();
                            }
                        });

                        refresh = false;
                    }
                });
                icon.setImageResource(R.mipmap.three_dots_icon);
                icon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        optionContainer.setVisibility(View.VISIBLE);
                        if (refresh) {
                            refreshItem(preSelectedPosition, preSelectedPosition);
                        } else {
                            refresh = true;
                        }
                        preSelectedPosition = position;
                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        refreshItem(preSelectedPosition, preSelectedPosition);
                        preSelectedPosition = -1;
                    }
                });
            }
        }
    }
}
