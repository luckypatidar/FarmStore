package com.example.farmstore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;


public class AddAddressActivity extends AppCompatActivity {

    private EditText city;
    private EditText locality;
    private EditText flatNo;
    private EditText pincode;
    private EditText landmark;
    private EditText name;
    private EditText mobileNo;
    private EditText alternateMobileNo;
    private Spinner stateSpinner;
    private Button saveBtn;

    private String[] stateList;
    private String selectedState;
    private Dialog loadingDialog;

    private boolean updateAddress = false;
    private AddressesModel addressesModel;
    private int position;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Add a new address");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //// LOADING DIALOG
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //// LOADING DIALOG

        stateList = getResources().getStringArray(R.array.india_states);

        city = findViewById(R.id.city);
        locality = findViewById(R.id.locality);
        flatNo = findViewById(R.id.flat_no);
        pincode = findViewById(R.id.pincode);
        landmark = findViewById(R.id.landmark);
        name = findViewById(R.id.name);
        mobileNo = findViewById(R.id.mobile_number);
        alternateMobileNo = findViewById(R.id.alternate_mobile_number);
        stateSpinner = findViewById(R.id.state_spinner);
        saveBtn = findViewById(R.id.save_btn);

        ArrayAdapter spinnerAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,stateList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        stateSpinner.setAdapter(spinnerAdapter);

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selectedState = stateList[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if (getIntent().getStringExtra("INTENT").equals("update_address")){
            updateAddress = true;
            position = getIntent().getIntExtra("index",-1) ;
            addressesModel = DBQueries.addressesModelList.get(position);

            city.setText(addressesModel.getCity());
            locality.setText(addressesModel.getLocality());
            flatNo.setText(addressesModel.getFlatNo());
            landmark.setText(addressesModel.getLandmark());
            name.setText(addressesModel.getName());
            mobileNo.setText(addressesModel.getMobileNo());
            alternateMobileNo.setText(addressesModel.getAlternateMobileNo());
            pincode.setText(addressesModel.getPincode());

            for (int i = 0; i<stateList.length;i++){
                if (stateList[i].equals(addressesModel.getState())) {
                    stateSpinner.setSelection(i);
                }
            }
            saveBtn.setText("update");
        }else {
            position = DBQueries.addressesModelList.size() ;
        }

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(city.getText())) {
                    if (!TextUtils.isEmpty(locality.getText())) {
                        if (!TextUtils.isEmpty(flatNo.getText())) {
                            if (!TextUtils.isEmpty(pincode.getText()) && pincode.getText().length() == 6) {
                                if (!TextUtils.isEmpty(name.getText())) {
                                    if (!TextUtils.isEmpty(mobileNo.getText()) && mobileNo.getText().length() == 10) {

                                        loadingDialog.show();

                                        Map<String, Object> addAddress = new HashMap();
                                        addAddress.put("city_" + String.valueOf(position + 1), city.getText().toString());
                                        addAddress.put("locality" + String.valueOf(position + 1), locality.getText().toString());
                                        addAddress.put("flat_no_" + String.valueOf(position + 1), flatNo.getText().toString());
                                        addAddress.put("pincode_" + String.valueOf(position + 1), pincode.getText().toString());
                                        addAddress.put("landmark_" + String.valueOf(position + 1), landmark.getText().toString());
                                        addAddress.put("name_" + String.valueOf(position + 1), name.getText().toString());
                                        addAddress.put("mobile_no_" + String.valueOf(position + 1), mobileNo.getText().toString());
                                        addAddress.put("alternate_mobile_no_" + String.valueOf(position + 1), alternateMobileNo.getText().toString());
                                        addAddress.put("state_" + String.valueOf(position + 1), selectedState);

                                        if (!updateAddress) {
                                            addAddress.put("list_size", (long) DBQueries.addressesModelList.size() + 1);
                                            if (getIntent().getStringExtra("INTENT").equals("manage")) {
                                                if (DBQueries.addressesModelList.size() == 0) {
                                                    addAddress.put("selected_" + String.valueOf(position + 1), true);
                                                } else {
                                                    addAddress.put("selected_" + String.valueOf(position + 1), false);
                                                }
                                            }else {
                                                addAddress.put("selected_" + String.valueOf(position + 1), true);
                                            }

                                            if (DBQueries.addressesModelList.size() > 0) {
                                                addAddress.put("selected_" + (DBQueries.selectedAddress + 1), false);
                                            }
                                        }
                                        FirebaseFirestore.getInstance().collection("USERS")
                                                .document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA")
                                                .document("MY_ADDRESSES")
                                                .update(addAddress).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    if (!updateAddress) {
                                                        if (DBQueries.addressesModelList.size() > 0) {
                                                            DBQueries.addressesModelList.get(DBQueries.selectedAddress).setSelected(false);
                                                        }
                                                        DBQueries.addressesModelList.add(new AddressesModel(true,city.getText().toString(),locality.getText().toString(),flatNo.getText().toString(),pincode.getText().toString(),landmark.getText().toString(),name.getText().toString(),mobileNo.getText().toString(),alternateMobileNo.getText().toString(),selectedState));

                                                        if (getIntent().getStringExtra("INTENT").equals("manage")) {
                                                            if (DBQueries.addressesModelList.size() == 0) {
                                                                DBQueries.selectedAddress = DBQueries.addressesModelList.size() - 1;
                                                            }
                                                        }else {
                                                            DBQueries.selectedAddress = DBQueries.addressesModelList.size() - 1;
                                                        }
                                                    }else {
                                                        DBQueries.addressesModelList.set(position,new AddressesModel(true,city.getText().toString(),locality.getText().toString(),flatNo.getText().toString(),pincode.getText().toString(),landmark.getText().toString(),name.getText().toString(),mobileNo.getText().toString(),alternateMobileNo.getText().toString(),selectedState));
                                                    }
                                                    if (getIntent().getStringExtra("INTENT").equals("deliveryIntent")) {
                                                        Intent deliveryIntent = new Intent(AddAddressActivity.this, DeliveryActivity.class);
                                                        startActivity(deliveryIntent);
                                                    }else {
                                                        MyAddressActivity.refreshItem(DBQueries.selectedAddress,DBQueries.addressesModelList.size() - 1);
                                                    }
                                                    finish();
                                                } else {
                                                    String error = task.getException().getMessage();
                                                    Toast.makeText(AddAddressActivity.this, error, Toast.LENGTH_SHORT).show();
                                                }
                                                loadingDialog.dismiss();
                                            }
                                        });

                                    } else {
                                        mobileNo.requestFocus();
                                        Toast.makeText(AddAddressActivity.this, "Please provide valid Number.", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    name.requestFocus();
                                }
                            } else {
                                pincode.requestFocus();
                                Toast.makeText(AddAddressActivity.this, "Please provide valid pincode", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            flatNo.requestFocus();
                        }
                    } else {
                        locality.requestFocus();
                    }
                } else {
                    city.requestFocus();
                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}