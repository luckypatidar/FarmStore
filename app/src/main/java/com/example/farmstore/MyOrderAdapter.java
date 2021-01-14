package com.example.farmstore;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.Viewholder> {

    private List<MyOrderItemModel> myOrderItemModelList;
    private Dialog loadingDialog;

    public MyOrderAdapter(List<MyOrderItemModel> myOrderItemModelList,Dialog loadingDialog) {
        this.myOrderItemModelList = myOrderItemModelList;
        this.loadingDialog = loadingDialog;
    }

    @NonNull
    @Override
    public MyOrderAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.my_order_item_layout,viewGroup,false);
        return new Viewholder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyOrderAdapter.Viewholder viewholder, int position) {
        String resource = myOrderItemModelList.get(position).getProductImage();
        String productID = myOrderItemModelList.get(position).getProductId();
        int rating = myOrderItemModelList.get(position).getRating();
        String title = myOrderItemModelList.get(position).getProductTitle();
        String orderStatus = myOrderItemModelList.get(position).getOrderStatus();
        Date date;
        switch (orderStatus){
            case "Ordered":
                date = myOrderItemModelList.get(position).getOrderedDate();
                break;
            case "Packed":
                date = myOrderItemModelList.get(position).getPackedDate();
                break;
            case "Shipped":
                date = myOrderItemModelList.get(position).getShippedDate();
                break;
            case "Delivered":
                date = myOrderItemModelList.get(position).getDeliveredDate();
                break;
            case "Cancelled":
                date = myOrderItemModelList.get(position).getCancelledDate();
                break;
            default:
                date = myOrderItemModelList.get(position).getCancelledDate();

        }
        viewholder.setData(resource,title,orderStatus,date,rating,productID,position);
    }

    @Override
    public int getItemCount() {
        return myOrderItemModelList.size();
    }

    class Viewholder extends RecyclerView.ViewHolder{

        private ImageView productImage;
        private ImageView orderIndicator;
        private TextView productTitle;
        private TextView deliveryStatus;
        private LinearLayout rateNowContainor;

        public Viewholder(@NonNull final View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productTitle = itemView.findViewById(R.id.product_title);
            orderIndicator = itemView.findViewById(R.id.order_indicator);
            deliveryStatus = itemView.findViewById(R.id.order_deliverd_date);
            rateNowContainor = itemView.findViewById(R.id.rate_now_container);

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void setData(String resource, String title, String orderStatus, Date date, final int rating, final String productID, final int position){
            Glide.with(itemView.getContext()).load(resource).into(productImage);
            productTitle.setText(title);
            if (orderStatus.equals("Cancelled")) {
                orderIndicator.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.successred)));
            }else{
                orderIndicator.setImageTintList(ColorStateList.valueOf(itemView.getContext().getResources().getColor(R.color.successgreen)));
            }

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE,dd MMM YYYY hh:mm:aa");
            deliveryStatus.setText(orderStatus+" "+String.valueOf(simpleDateFormat.format(date)));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent orderDetailsIntent = new Intent(itemView.getContext(),OrderDetailsActivity.class);
                    orderDetailsIntent.putExtra("Position",position);
                    itemView.getContext().startActivity(orderDetailsIntent);
                }
            });

            //// rating layout
            setRating(rating);
            for (int x=0;x<rateNowContainor.getChildCount();x++){
                final int starPosition = x;
                rateNowContainor.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onClick(View view) {
                        loadingDialog.show();
                        setRating(starPosition);
                        final DocumentReference documentReference = FirebaseFirestore.getInstance().collection("PRODUCTS").document(productID);

                        FirebaseFirestore.getInstance().runTransaction(new Transaction.Function<Object>() {
                            @Nullable
                            @Override
                            public Object apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                DocumentSnapshot documentSnapshot = transaction.get(documentReference);
                                if (rating != 0){
                                    Long increase = documentSnapshot.getLong(starPosition+1+"_star") + 1;
                                    Long decrease = documentSnapshot.getLong(rating+1+"_star") - 1;
                                    transaction.update(documentReference,starPosition+"_star",increase);
                                    transaction.update(documentReference,rating+"_star",decrease);
                                }else {
                                    Long increase = documentSnapshot.getLong(starPosition+1+"_star") + 1;
                                    transaction.update(documentReference,starPosition+1+"_star",increase);
                                }
                                return null;
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Object>() {
                            @Override
                            public void onSuccess(Object o) {

                                Map<String, Object> myRating = new HashMap<>();
                                if (DBQueries.myRatedIds.contains(productID)) {
                                    myRating.put("rating_" + DBQueries.myRatedIds.indexOf(productID), (long) starPosition + 1);
                                } else {
                                    myRating.put("list_size", (long) DBQueries.myRatedIds.size() + 1);
                                    myRating.put("product_ID_" + DBQueries.myRatedIds.size(), productID);
                                    myRating.put("rating_" + DBQueries.myRatedIds.size(), (long) starPosition + 1);
                                }
                                FirebaseFirestore.getInstance().collection("USERS").document(FirebaseAuth.getInstance().getUid()).collection("USER_DATA").document("MY_RATINGS")
                                        .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            DBQueries.myOrderItemModelList.get(position).setRating(starPosition);
                                                                                            if (DBQueries.myRatedIds.contains(productID)){
                                                                                                DBQueries.myRating.set(DBQueries.myRatedIds.indexOf(productID),Long.parseLong(String.valueOf(starPosition+1)));
                                                                                            }else {
                                                                                                DBQueries.myRatedIds.add(productID);
                                                                                                DBQueries.myRating.add(Long.parseLong(String.valueOf(starPosition+1)));
                                                                                            }

                                                                                        }else {
                                                                                            String error = task.getException().getMessage();
                                                                                            Toast.makeText(itemView.getContext(), error, Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                        loadingDialog.dismiss();
                                                                                    }
                                                                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();
                            }
                        });
                    }
                });
            }
            //// rating layout
        }
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void setRating(int starPosition) {
            for(int x=0; x<rateNowContainor.getChildCount();x++){
                ImageView starButton = (ImageView)rateNowContainor.getChildAt(x);
                starButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FA8E8B8B")));
                if(x <= starPosition){
                    starButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EFD426")));
                }
            }
        }
    }
}

