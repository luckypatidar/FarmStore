package com.example.farmstore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.dynamic.IFragmentWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class DeliveryActivity extends AppCompatActivity {

    public static List<CartItemModel> cartItemModelList;
    private RecyclerView deliveryRecyclerview;
    public static CartAdapter cartAdapter;
    private Button changeOrAddNewAddressBtn;
    public static final int SELECT_ADDRESS = 0;
    private TextView totalAmount;
    private TextView fullname;
    private String name, mobileNo;
    private TextView fullAddress;
    private TextView pincode;
    private Button continueBtn;
    public static Dialog loadingDialog;
    private Dialog paymentMethodDialog;
    private TextView codTitle;
    private View divider;
    private ImageButton paytm, cod;
    private String paymentMethod = "PAYTM";
    private ConstraintLayout orderConfirmationLayout;
    private ImageButton continueShoppingBtn;
    private TextView orderId;
    private boolean successResponse = false;
    public static boolean fromCart;
    private String order_id;
    public static boolean codOrderConfirmed = false;

    private FirebaseFirestore firebaseFirestore;
    public static boolean getQtyIDs = true;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Delivery");

        deliveryRecyclerview = findViewById(R.id.delivery_recycler_view);
        changeOrAddNewAddressBtn = findViewById(R.id.change_or_add_address_btn);
        totalAmount = findViewById(R.id.total_cart_amount);
        fullname = findViewById(R.id.full_name);
        fullAddress = findViewById(R.id.address);
        pincode = findViewById(R.id.pincode);
        continueBtn = findViewById(R.id.cart_continue_btn);
        orderConfirmationLayout = findViewById(R.id.order_confirmation_layout);
        continueShoppingBtn = findViewById(R.id.continue_shopping_btn);
        orderId = findViewById(R.id.order_id);

        ///loading dialog
        loadingDialog = new Dialog(DeliveryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ///loading dialog

        ///paymentMethodDialog dialog
        paymentMethodDialog = new Dialog(DeliveryActivity.this);
        paymentMethodDialog.setContentView(R.layout.payment_method);
        paymentMethodDialog.setCancelable(true);
        paymentMethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        paymentMethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        paytm = paymentMethodDialog.findViewById(R.id.paytm);
        cod = paymentMethodDialog.findViewById(R.id.cod_btn);
        codTitle = paymentMethodDialog.findViewById(R.id.cod_btn_title);
        divider = paymentMethodDialog.findViewById(R.id.divider);
        ///paymentMethodDialog dialog
        firebaseFirestore = FirebaseFirestore.getInstance();
        getQtyIDs = true;

        order_id = UUID.randomUUID().toString().substring(0, 28);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        deliveryRecyclerview.setLayoutManager(layoutManager);

        cartAdapter = new CartAdapter(cartItemModelList, totalAmount, false);
        deliveryRecyclerview.setAdapter(cartAdapter);
        cartAdapter.notifyDataSetChanged();

        changeOrAddNewAddressBtn.setVisibility(View.VISIBLE);
        changeOrAddNewAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getQtyIDs = false;
                Intent myAddressesIntent = new Intent(DeliveryActivity.this, MyAddressActivity.class);
                myAddressesIntent.putExtra("MODE", SELECT_ADDRESS);
                startActivity(myAddressesIntent);
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean allProductsAvailable = true;
                for (CartItemModel cartItemModel : cartItemModelList) {
                    if (cartItemModel.isQtyError()) {
                        allProductsAvailable = false;
                        break;
                    }
                    if (cartItemModel.getType() == CartItemModel.CART_ITEM) {
                        if (!cartItemModel.isCOD()) {
                            cod.setEnabled(false);
                            cod.setAlpha(0.5f);
                            codTitle.setAlpha(0.5f);
                            divider.setVisibility(View.VISIBLE);
                            break;
                        } else {
                            cod.setEnabled(true);
//                            cod.setAlpha(100);
                            codTitle.setAlpha(1);
                            cod.setVisibility(View.VISIBLE);
                            divider.setVisibility(View.VISIBLE);
                        }
                    }
                }
                if (allProductsAvailable) {
                    paymentMethodDialog.show();
                }
            }
        });

        cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               paymentMethod = "COD";
               placeOrderDetails();
            }
        });

        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentMethod = "PAYTM";
                placeOrderDetails();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        /// accessing quantity
        if (getQtyIDs) {
            loadingDialog.show();
            for (int x = 0; x < cartItemModelList.size() - 1; x++) {

                for (int y = 0; y < cartItemModelList.get(x).getProductQuantity(); y++) {
                    final String quantityDocumentName = UUID.randomUUID().toString().substring(0, 20);

                    Map<String, Object> timeStamp = new HashMap<>();
                    timeStamp.put("time", FieldValue.serverTimestamp());
                    final int finalX = x;
                    final int finalY = y;
                    firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(quantityDocumentName).set(timeStamp)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if (task.isSuccessful()) {

                                        cartItemModelList.get(finalX).getQtyIDs().add(quantityDocumentName);

                                        if (finalY + 1 == cartItemModelList.get(finalX).getProductQuantity()) {

                                            firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(finalX).getProductID()).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).limit(cartItemModelList.get(finalX).getStockQuantity()).get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                List<String> serverQuantity = new ArrayList<>();

                                                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                                                    serverQuantity.add(queryDocumentSnapshot.getId());
                                                                }

                                                                long availableQty = 0;
                                                                boolean noLongerAvailable = true;
                                                                for (String qtyId : cartItemModelList.get(finalX).getQtyIDs()) {
                                                                    cartItemModelList.get(finalX).setQtyError(false);
                                                                    if (!serverQuantity.contains(qtyId)) {
                                                                        if (noLongerAvailable) {
                                                                            cartItemModelList.get(finalX).setInStock(false);
                                                                        } else {
                                                                            cartItemModelList.get(finalX).setQtyError(true);
                                                                            cartItemModelList.get(finalX).setMaxQuantity(availableQty);
                                                                            Toast.makeText(DeliveryActivity.this, "Sorry ! All products may not be available in required quantity...", Toast.LENGTH_SHORT).show();
                                                                        }

                                                                    } else {
                                                                        availableQty++;
                                                                        noLongerAvailable = false;
                                                                    }
                                                                }
                                                                cartAdapter.notifyDataSetChanged();
                                                            } else {
                                                                String error = task.getException().getMessage();
                                                                Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                                            }
                                                            loadingDialog.dismiss();
                                                        }
                                                    });
                                        }
                                    } else {
                                        loadingDialog.dismiss();
                                        String error = task.getException().getMessage();
                                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }

            }
        } else {
            getQtyIDs = true;
        }
        /// accessing quantity

        name = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getName();
        mobileNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getMobileNo();
        if (DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo().equals("")) {
            fullname.setText(name + " - " + mobileNo);
        }else {
            fullname.setText(name + " - " + mobileNo + " or " + DBQueries.addressesModelList.get(DBQueries.selectedAddress).getAlternateMobileNo());
        }
        String flatNo = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getFlatNo();
        String locality = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLocality();
        String landmark = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getLandmark();
        String city = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getCity();
        String state = DBQueries.addressesModelList.get(DBQueries.selectedAddress).getState();

        if (landmark.equals("")){
            fullAddress.setText(flatNo +" "+ locality +" "+ city +" "+ state);
        }else {
            fullAddress.setText(flatNo + " " + locality + " " + landmark + " " + city + " " + state);
        }
        pincode.setText(DBQueries.addressesModelList.get(DBQueries.selectedAddress).getPincode());

        if (codOrderConfirmed) {
            showConfirmationLayout();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        loadingDialog.dismiss();

        if (getQtyIDs) {

            for (int x = 0; x < cartItemModelList.size() - 1; x++) {
                if (!successResponse) {
                    for (final String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                        final int finalX = x;
                        firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        if (qtyID.equals(cartItemModelList.get(finalX).getQtyIDs().get(cartItemModelList.get(finalX).getQtyIDs().size() - 1))) {
                                            cartItemModelList.get(finalX).getQtyIDs().clear();
                                        }
                                    }
                                });
                    }
                } else {
                    cartItemModelList.get(x).getQtyIDs().clear();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (successResponse) {
            finish();
            return;
        }
        super.onBackPressed();
    }

    private void showConfirmationLayout() {
        successResponse = true;
        codOrderConfirmed = false;
        getQtyIDs = false;
        for (int x = 0; x < cartItemModelList.size() - 1; x++) {

            for (String qtyID : cartItemModelList.get(x).getQtyIDs()) {
                firebaseFirestore.collection("PRODUCTS").document(cartItemModelList.get(x).getProductID()).collection("QUANTITY").document(qtyID).update("user_ID", FirebaseAuth.getInstance().getUid());
            }
        }

        if (MainActivity.mainActivity != null) {
            MainActivity.mainActivity.finish();
            MainActivity.mainActivity = null;
            MainActivity.showCart = false;
        } else {
            MainActivity.resetMainActivity = true;
        }
        if (ProductDetailActivity.productDetailsActivity != null) {
            ProductDetailActivity.productDetailsActivity.finish();
            ProductDetailActivity.productDetailsActivity = null;
        }
        //// send confirmation sms
        String SMS_API = " https://www.fast2sms.com/dev/bulk";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, SMS_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ////// nothing to show
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /////// nothing to show
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("authorization", "xbKWeJLXdz0pfV9kEwsSCAyjogU1YP25ui8lQDIZvHN3R6cBtmw2eoAmq6dXPsUEbFLV78iaYknQSBj1");
                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> body = new HashMap<>();
                body.put("sender_id", "SMSIND");
                body.put("language", "english");
                body.put("route", "qt");
                body.put("numbers", mobileNo);
                body.put("message", "37940");
                body.put("variables", "{#FF#}");
                body.put("variables_values", order_id);
                return body;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                5000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
        requestQueue.add(stringRequest);
        //// send confirmation sms


        if (fromCart) {
            loadingDialog.show();

            Map<String, Object> updateCartlist = new HashMap<>();
            long cartListSize = 0;
            final List<Integer> indexList = new ArrayList<>();
            for (int x = 0; x < DBQueries.cartList.size(); x++) {
                if (!cartItemModelList.get(x).isInStock()) {
                    updateCartlist.put("product_ID_" + cartListSize, cartItemModelList.get(x).getProductID());
                    cartListSize++;
                } else {
                    indexList.add(x);
                }
            }
            updateCartlist.put("list_size", cartListSize);

            FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_CART")
                    .set(updateCartlist).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        for (int x = 0; x < indexList.size(); x++) {
                            DBQueries.cartList.remove(indexList.get(x).intValue());
                            DBQueries.cartItemModelList.remove(indexList.get(x).intValue());
                            DBQueries.cartItemModelList.remove(DBQueries.cartItemModelList.size() - 1);
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                    loadingDialog.dismiss();
                }
            });

        }
        continueBtn.setEnabled(false);
        changeOrAddNewAddressBtn.setEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        orderId.setText("Order_ID - " + order_id);
        orderConfirmationLayout.setVisibility(View.VISIBLE);
        continueShoppingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void placeOrderDetails() {

        String userID = FirebaseAuth.getInstance().getUid();
        loadingDialog.show();
        for (CartItemModel cartItemModel : cartItemModelList) {
            if (cartItemModel.getType() == CartItemModel.CART_ITEM) {

                Map<String,Object> orderDetails = new HashMap<>();
                orderDetails.put("ORDER_ID",order_id);
                orderDetails.put("Product Id",cartItemModel.getProductID());
                orderDetails.put("Product Image",cartItemModel.getProductImage());
                orderDetails.put("Product Title",cartItemModel.getProductTitle());
                orderDetails.put("User Id",userID);
                orderDetails.put("Product Quantity",cartItemModel.getProductQuantity());
                if (cartItemModel.getCuttedPrice() != (null)) {
                    orderDetails.put("Cutted Price", cartItemModel.getCuttedPrice());
                }else{
                    orderDetails.put("Cutted Price", "");
                }
                orderDetails.put("Product Price",cartItemModel.getProductPrice());
                if (cartItemModel.getSelectedCoupenId() != null) {
                    orderDetails.put("Coupen Id", cartItemModel.getSelectedCoupenId());
                }else {
                    orderDetails.put("Coupen Id", "");
                }
                if (cartItemModel.getDiscountedPrice() != null) {
                    orderDetails.put("Discounted Price",cartItemModel.getDiscountedPrice());
                }else {
                    orderDetails.put("Discounted Price","");
                }
                orderDetails.put("Ordered date",FieldValue.serverTimestamp());
                orderDetails.put("Packed date",FieldValue.serverTimestamp());
                orderDetails.put("Shipped date",FieldValue.serverTimestamp());
                orderDetails.put("Delivered date",FieldValue.serverTimestamp());
                orderDetails.put("Cancelled date",FieldValue.serverTimestamp());
                orderDetails.put("Order Status","Ordered");
                orderDetails.put("Payment Method",paymentMethod);
                orderDetails.put("Address",fullAddress.getText());
                orderDetails.put("FullName",fullname.getText());
                orderDetails.put("Pincode",pincode.getText());
                orderDetails.put("Free Coupens",cartItemModel.getFreeCoupons());
                orderDetails.put("Delivery Price",cartItemModelList.get(cartItemModelList.size()-1).getDeliveryPrice());
                orderDetails.put("Cancellation request",false);

                firebaseFirestore.collection("ORDERS").document(order_id).collection("OrderItems").document(cartItemModel.getProductID())
                        .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()){
                            String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }else {
                Map<String,Object> orderDetails = new HashMap<>();
                orderDetails.put("Total Items",cartItemModel.getTotalItems());
                orderDetails.put("Total Items Price",cartItemModel.getTotalItemsPrice());
                orderDetails.put("Delivery Price",cartItemModel.getDeliveryPrice());
                orderDetails.put("Total Amount",cartItemModel.getTotalAmount());
                orderDetails.put("Saved Amount",cartItemModel.getSavedAmount());
                orderDetails.put("Payment Status","not Paid");
                orderDetails.put("Order Status","Cancelled");

                firebaseFirestore.collection("ORDERS").document(order_id)
                        .set(orderDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            if (paymentMethod.equals("PAYTM")){
                                paytm();
                            }else {
                                cod();
                            }
                        }else {
                            String error = task.getException().getMessage();
                            Toast.makeText(DeliveryActivity.this, error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    }

    private void paytm(){
            getQtyIDs = false;
            paymentMethodDialog.dismiss();
            loadingDialog.show();
            if (ContextCompat.checkSelfPermission(DeliveryActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeliveryActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
            }

            final String M_id = "ufUhRW51809297881985";
            final String customer_id = FirebaseAuth.getInstance().getUid();

            final String url = "https://farmstore1.000webhostapp.com/paytm/generateChecksum.php";
            final String callBackUrl = "https://securegw-stage.paytm.in/theia/paytmCallback?ORDER_ID=<order_id>";

            RequestQueue requestQueue = Volley.newRequestQueue(DeliveryActivity.this);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.has("CHECKSUMHASH")) {
                            String CHECKSUMHASH = jsonObject.getString("CHECKSUMHASH");

                            PaytmPGService paytmPGService = PaytmPGService.getStagingService(callBackUrl);
                            HashMap<String, String> paramMap = new HashMap<>();
                            paramMap.put("MID", M_id);
                            paramMap.put("ORDER_ID", order_id);
                            paramMap.put("CUST_ID", customer_id);
                            paramMap.put("CHANNEL_ID", "WAP");
                            paramMap.put("TXN_AMOUNT", totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
                            paramMap.put("WEBSITE", "WEBSTAGING");
                            paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                            paramMap.put("CALLBACK_URL", callBackUrl);
                            paramMap.put("CHECKSUMHASH", CHECKSUMHASH);

                            PaytmOrder order = new PaytmOrder(paramMap);

                            paytmPGService.initialize(order, null);
                            paytmPGService.startPaymentTransaction(DeliveryActivity.this, true, true, new PaytmPaymentTransactionCallback() {
                                @Override
                                public void onTransactionResponse(Bundle inResponse) {
//                                        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();

                                    if (inResponse.getString("STATUS").equals("TXN_SUCCESS")) {
                                            Map<String,Object> updateStatus = new HashMap<>();
                                            updateStatus.put("Payment Status","Paid");
                                            updateStatus.put("Order Status","Ordered");
                                        firebaseFirestore.collection("ORDERS").document(order_id).update(updateStatus)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                Map<String,Object> userOrder = new HashMap<>();
                                                                userOrder.put("order_id",order_id);
                                                                userOrder.put("time",FieldValue.serverTimestamp());
                                                                firebaseFirestore.collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_ORDERS").document(order_id)
                                                                        .set(userOrder).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()){
                                                                            showConfirmationLayout();
                                                                        }else {
                                                                            Toast.makeText(DeliveryActivity.this, "Failed to update user's order list.", Toast.LENGTH_LONG).show();
                                                                        }
                                                                    }
                                                                });

                                                            }else {
                                                                Toast.makeText(DeliveryActivity.this, "Order Cancelled", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });

                                    }
                                }

                                @Override
                                public void networkNotAvailable() {
                                    Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void clientAuthenticationFailed(String inErrorMessage) {
                                    Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void someUIErrorOccurred(String inErrorMessage) {
                                    Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {
                                    Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage, Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onBackPressedCancelTransaction() {
                                    Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                                    Toast.makeText(getApplicationContext(), "Transaction Cancelled" + inErrorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    loadingDialog.dismiss();
                    Toast.makeText(DeliveryActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> paramMap = new HashMap<String, String>();
                    paramMap.put("MID", M_id);
                    paramMap.put("ORDER_ID", order_id);
                    paramMap.put("CUST_ID", customer_id);
                    paramMap.put("CHANNEL_ID", "WAP");
                    paramMap.put("TXN_AMOUNT", totalAmount.getText().toString().substring(3, totalAmount.getText().length() - 2));
                    paramMap.put("WEBSITE", "WEBSTAGING");
                    paramMap.put("INDUSTRY_TYPE_ID", "Retail");
                    paramMap.put("CALLBACK_URL", callBackUrl);
                    return paramMap;
                }
            };
            requestQueue.add(stringRequest);

    }

    private void cod(){
        getQtyIDs = false;
        paymentMethodDialog.dismiss();
        Intent otpIntent = new Intent(DeliveryActivity.this, OTPverificationActivity.class);
        otpIntent.putExtra("mobileNo", mobileNo.substring(0, 10));
        otpIntent.putExtra("OrderID", order_id);
        startActivity(otpIntent);
    }
}