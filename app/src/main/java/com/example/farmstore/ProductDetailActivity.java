package com.example.farmstore;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.farmstore.MainActivity.showCart;
import static com.example.farmstore.RegisterActivity.setSignUpFragment;

public class ProductDetailActivity extends AppCompatActivity {

    public static boolean running_wishlist_query = false;
    public static boolean running_rating_query = false;
    public static boolean running_cart_query = false;
    public static Activity productDetailsActivity;

    public static boolean fromSearch = false;

    private ViewPager productImagesViewPager;
    private TextView productTitle;
    private TextView averageRatingMiniView;
    private TextView totalRatingsMiniView;
    private TextView productPrice;
    private String productOriginalPrice;
    private TextView cuttedPrice;
    private ImageView codIndicator;
    private TextView tvCodIndicator;
    private TabLayout viewpagerIndicator;

    private LinearLayout coupenRedeemptionLayout;
    private Button coupenRedeemBtn;
    private TextView rewardTitle;
    private TextView rewardBody;

    //////////////product description
    private ConstraintLayout productDetailsOnlyContainer;
    private ConstraintLayout productDetailsTabsContainer;
    private TabLayout productDetailsTabLayout;
    private ViewPager productDetailsViewPager;
    private TextView productOnlyDescriptionBody;

    private List<ProductSpecificationModel> productSpecificationModelList = new ArrayList<>();
    private String productDescription;
    private String productOtherDetails;

    /////////////// productdescription
    ///////// rating layout
    public static int initialRating;
    public static LinearLayout rateNowContainor;
    private TextView totalRatings;
    private LinearLayout ratingsNoContainer;
    private TextView totalRatingsFigure;
    private LinearLayout ratingsProgressbarContainer;
    private TextView averageRating;
    //////////// rating layout
    private Button buyNowBtn;
    private LinearLayout addToCartBtn;
    public static MenuItem cartItem;

    public static boolean ALREADY_ADDED_TO_WISHLIST = false;
    public static boolean ALREADY_ADDED_TO_CART = false;
    public static FloatingActionButton addToWishlistBtn;

    private FirebaseFirestore firebaseFirestore;
    ///////coupenDialog
    private TextView coupenTitle;
    private TextView coupenBody;
    private TextView coupenExpiryDate;
    private RecyclerView coupensRecyclerView;
    private LinearLayout selectedCoupen;
    private TextView discountedPrice;
    private TextView originalPrice;
    //////coupenDialog

    private Dialog signInDialog;
    private Dialog loadingDialog;
    private FirebaseUser currentUser;
    public static String productID;
    private TextView badgeCount;
    private boolean inStock = false;

    private DocumentSnapshot documentSnapshot;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        productImagesViewPager = findViewById(R.id.product_images_viewpager);
        viewpagerIndicator = findViewById(R.id.viewpager_indicator);
        addToWishlistBtn = findViewById(R.id.add_to_wishlist_btn);
        productDetailsViewPager = findViewById(R.id.product_details_viewpager);
        productDetailsTabLayout = findViewById(R.id.product_details_tablayout);
        buyNowBtn = findViewById(R.id.buy_now_btn);
        coupenRedeemBtn = findViewById(R.id.coupon_redemption_button);
        productTitle = findViewById(R.id.product_title);
        averageRatingMiniView = findViewById(R.id.tv_product_rating_miniview);
        totalRatingsMiniView = findViewById(R.id.total_ratings_miniview);
        productPrice = findViewById(R.id.product_price);
        cuttedPrice = findViewById(R.id.cutted_price);
        codIndicator = findViewById(R.id.cod_indicator_imageview);
        tvCodIndicator = findViewById(R.id.tv_cod_indicator);
        rewardTitle = findViewById(R.id.reward_title);
        rewardBody = findViewById(R.id.reward_body);
        productDetailsTabsContainer = findViewById(R.id.product_details_tab_container);
        productDetailsOnlyContainer = findViewById(R.id.product_details_containor);
        productOnlyDescriptionBody = findViewById(R.id.product_details_body);
        totalRatings = findViewById(R.id.total_ratings);
        ratingsNoContainer = findViewById(R.id.ratings_numbers_containors);
        totalRatingsFigure = findViewById(R.id.total_ratings_figure);
        ratingsProgressbarContainer = findViewById(R.id.ratings_progressbar_containor);
        averageRating = findViewById(R.id.average_rating);
        addToCartBtn = findViewById(R.id.add_to_cart_btn);
        coupenRedeemptionLayout = findViewById(R.id.coupon_redemption_layout);

        initialRating = -1;

        //// LOADING DIALOG
        loadingDialog = new Dialog(ProductDetailActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadingDialog.show();
        //// LOADING DIALOG

        //////// coupen dialog
        final Dialog checkCoupenPriceDialog = new Dialog(ProductDetailActivity.this);
        checkCoupenPriceDialog.setContentView(R.layout.coupen_reedem_dialog);
        checkCoupenPriceDialog.setCancelable(true);
        checkCoupenPriceDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView toggleRecyclerView = checkCoupenPriceDialog.findViewById(R.id.toggle_recycler_view);
        coupensRecyclerView = checkCoupenPriceDialog.findViewById(R.id.coupens_recycler_view);
        selectedCoupen = checkCoupenPriceDialog.findViewById(R.id.selected_coupen);
        coupenTitle = checkCoupenPriceDialog.findViewById(R.id.coupen_title);
        coupenExpiryDate = checkCoupenPriceDialog.findViewById(R.id.coupen_validity);
        coupenBody = checkCoupenPriceDialog.findViewById(R.id.coupen_body);

        originalPrice = checkCoupenPriceDialog.findViewById(R.id.original_price);
        discountedPrice = checkCoupenPriceDialog.findViewById(R.id.discounted_price);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ProductDetailActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        coupensRecyclerView.setLayoutManager(layoutManager);


        toggleRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogRecyclerView();
            }
        });
        /////////coupen dialog

        firebaseFirestore = FirebaseFirestore.getInstance();

        final List<String> productImages = new ArrayList<>();
        productID = getIntent().getStringExtra("PRODUCT_ID");
        firebaseFirestore.collection("PRODUCTS").document(productID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    documentSnapshot = task.getResult();

                    firebaseFirestore.collection("PRODUCTS").document(productID).collection("QUANTITY").orderBy("time", Query.Direction.ASCENDING).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()){

                                        for (long x = 1; x < (long) documentSnapshot.get("no_of_product_images") + 1; x++) {
                                            productImages.add(documentSnapshot.get("product_image_" + x).toString());
                                        }
                                        ProductImagesAdaptar productImagesAdaptar = new ProductImagesAdaptar(productImages);
                                        productImagesViewPager.setAdapter(productImagesAdaptar);

                                        productTitle.setText(documentSnapshot.get("product_title").toString());
                                        averageRatingMiniView.setText(documentSnapshot.get("average_rating").toString());
                                        totalRatingsMiniView.setText("(" + (long) documentSnapshot.get("total_ratings") + ") ratings");
                                        productPrice.setText("Rs." + documentSnapshot.get("product_price").toString() + "/-");
                                        productOriginalPrice = documentSnapshot.get("product_price").toString();
                                        ///// for coupen dialog
                                        originalPrice.setText(productPrice.getText());
                                        productOriginalPrice = documentSnapshot.get("product_price").toString();
                                        MyRewardsAdapter myRewardsAdapter = new MyRewardsAdapter(DBQueries.rewardModelsList, true,coupensRecyclerView,selectedCoupen,productOriginalPrice,coupenTitle,coupenExpiryDate,coupenBody,discountedPrice);
                                        coupensRecyclerView.setAdapter(myRewardsAdapter);
                                        myRewardsAdapter.notifyDataSetChanged();
                                        ////// for coupen dialog
                                        cuttedPrice.setText("Rs." + documentSnapshot.get("cutted_price").toString() + "/-");

                                        if ((boolean) documentSnapshot.get("COD")) {
                                            codIndicator.setVisibility(View.VISIBLE);
                                            tvCodIndicator.setVisibility(View.VISIBLE);
                                        } else {
                                            codIndicator.setVisibility(View.INVISIBLE);
                                            tvCodIndicator.setVisibility(View.INVISIBLE);
                                        }
                                        rewardTitle.setText((long) documentSnapshot.get("free_coupens") + documentSnapshot.get("free_coupen_title").toString());
                                        rewardBody.setText(documentSnapshot.get("free_coupen_body").toString());

                                        if ((boolean) documentSnapshot.get("use_tab_layout")) {
                                            productDetailsTabsContainer.setVisibility(View.VISIBLE);
                                            productDetailsOnlyContainer.setVisibility(View.GONE);
                                            productDescription = documentSnapshot.get("product_description").toString();

                                            productOtherDetails = documentSnapshot.get("product_other_details").toString();

                                            for (long x = 1; x < (long) documentSnapshot.get("total_spec_titles") + 1; x++) {

                                                productSpecificationModelList.add(new ProductSpecificationModel(0, documentSnapshot.get("spec_title_" + x).toString()));
                                                for (long y = 1; y < (long) documentSnapshot.get("spec_title_" + x + "_total_fields") + 1; y++) {
                                                    productSpecificationModelList.add(new ProductSpecificationModel(1, documentSnapshot.get("spec_title_" + x + "_field_" + y + "_name").toString(), documentSnapshot.get("spec_title_" + x + "_field_" + y + "_value").toString()));
                                                }
                                            }

                                        } else {
                                            productDetailsTabsContainer.setVisibility(View.GONE);
                                            productDetailsOnlyContainer.setVisibility(View.VISIBLE);
                                            productOnlyDescriptionBody.setText(documentSnapshot.get("product_description").toString());
                                        }

                                        totalRatings.setText((long) documentSnapshot.get("total_ratings") + " ratings");

                                        for (int x = 0; x < 5; x++) {
                                            TextView rating = (TextView) ratingsNoContainer.getChildAt(x);
                                            rating.setText(String.valueOf((long) documentSnapshot.get((5 - x) + "_star")));

                                            ProgressBar progressBar = (ProgressBar) ratingsProgressbarContainer.getChildAt(x);
                                            int maxProgress = Integer.parseInt(String.valueOf((long) documentSnapshot.get("total_ratings")));
                                            progressBar.setMax(maxProgress);
                                            progressBar.setProgress(Integer.parseInt(String.valueOf((long) documentSnapshot.get((5 - x) + "_star"))));
                                        }
                                        totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_ratings")));
                                        averageRating.setText(documentSnapshot.get("average_rating").toString());
                                        productDetailsViewPager.setAdapter(new ProductDetailsAdaptar(getSupportFragmentManager(), productDetailsTabLayout.getTabCount(), productDescription, productOtherDetails, productSpecificationModelList));

                                        if (currentUser != null) {
                                            if (DBQueries.myRating.size() == 0) {
                                                DBQueries.loadRatingList(ProductDetailActivity.this);
                                            }
                                            if (DBQueries.cartList.size() == 0) {
                                                DBQueries.loadCartList(ProductDetailActivity.this, loadingDialog, false,badgeCount,new TextView(ProductDetailActivity.this));
                                            }
                                            if (DBQueries.wishList.size() == 0) {
                                                DBQueries.loadWishlist(ProductDetailActivity.this, loadingDialog, false);
                                            }
                                            if (DBQueries.rewardModelsList.size() == 0){
                                                DBQueries.loadRewards(ProductDetailActivity.this,loadingDialog,false);
                                            }
                                            if (DBQueries.cartList.size() != 0 && DBQueries.wishList.size() != 0 && DBQueries.rewardModelsList.size() != 0 ){
                                                loadingDialog.show();
                                            }
                                        } else {
                                            loadingDialog.dismiss();
                                        }

                                        if (DBQueries.myRatedIds.contains(productID)) {
                                            int index = DBQueries.myRatedIds.indexOf(productID);
                                            initialRating = Integer.parseInt(String.valueOf(DBQueries.myRating.get(index))) - 1;
                                            setRating(initialRating);
                                        }
                                        if (DBQueries.cartList.contains(productID)) {
                                            ALREADY_ADDED_TO_CART = true;
                                        } else {
                                            ALREADY_ADDED_TO_CART = false;
                                        }
                                        if (DBQueries.wishList.contains(productID)) {
                                            ALREADY_ADDED_TO_WISHLIST = true;
                                            addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.successred));
                                        } else {
                                            addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#817F7F")));
                                            ALREADY_ADDED_TO_WISHLIST = false;
                                        }



                                        if (task.getResult().getDocuments().size() < (long)documentSnapshot.get("stock_quantity")){
                                            inStock = true;
                                            buyNowBtn.setVisibility(View.VISIBLE);
                                            addToCartBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (currentUser == null) {
                                                        signInDialog.show();
                                                    } else {
                                                        if (!running_cart_query) {
                                                            running_cart_query = true;
                                                            if (ALREADY_ADDED_TO_CART) {
                                                                running_cart_query = false;
                                                                Toast.makeText(ProductDetailActivity.this, "Already added to cart!", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Map<String, Object> addProduct = new HashMap<>();
                                                                addProduct.put("product_ID_" + String.valueOf(DBQueries.cartList.size()), productID);
                                                                addProduct.put("list_size", (long) (DBQueries.cartList.size() + 1));
                                                                firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA").document("MY_CART")
                                                                        .update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {

                                                                            if (DBQueries.cartItemModelList.size() != 0) {
                                                                                DBQueries.cartItemModelList.add(0,new CartItemModel(documentSnapshot.getBoolean("COD"),CartItemModel.CART_ITEM, productID, documentSnapshot.get("product_image_1").toString()
                                                                                        , documentSnapshot.get("product_title").toString()
                                                                                        , (long) documentSnapshot.get("free_coupens")
                                                                                        , documentSnapshot.get("product_price").toString()
                                                                                        , documentSnapshot.get("cutted_price").toString()
                                                                                        , (long) 1
                                                                                        , (long) documentSnapshot.get("offers_applied")
                                                                                        , (long) 0
                                                                                        ,inStock
                                                                                        ,(long)documentSnapshot.get("max-quantity")
                                                                                        ,(long)documentSnapshot.get("stock_quantity")));

                                                                            }
                                                                            ALREADY_ADDED_TO_CART = true;
                                                                            DBQueries.cartList.add(productID);
                                                                            Toast.makeText(ProductDetailActivity.this, "Product is added to Cart successfully", Toast.LENGTH_SHORT).show();
                                                                            invalidateOptionsMenu();
                                                                            running_cart_query = false;
                                                                        } else {
                                                                            running_cart_query = false;
                                                                            String error = task.getException().getMessage();
                                                                            Toast.makeText(ProductDetailActivity.this, "error", Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }
                                                                });

                                                            }
                                                        }
                                                    }
                                                }
                                            });
                                        }else {
                                            inStock = false;
                                            buyNowBtn.setVisibility(View.GONE);
                                            TextView outOfStock = (TextView) addToCartBtn.getChildAt(0);
                                            outOfStock.setText("Out of Stock");
                                            outOfStock.setTextColor(getResources().getColor(R.color.successred));
                                            outOfStock.setCompoundDrawables(null,null,null,null);
                                        }

                                    }else{
                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                } else {
                    loadingDialog.dismiss();
                    String error = task.getException().getMessage();
                    Toast.makeText(ProductDetailActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewpagerIndicator.setupWithViewPager(productImagesViewPager, true);

        addToWishlistBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser == null) {
                    signInDialog.show();
                } else {

                    if (!running_wishlist_query) {
                        running_wishlist_query = true;
                        if (ALREADY_ADDED_TO_WISHLIST) {
                            int index = DBQueries.wishList.indexOf(productID);
                            DBQueries.removeFromWishlist(index, ProductDetailActivity.this);
                            addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#817F7F")));
                        } else {
                            addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.successred));
                            Map<String, Object> addProduct = new HashMap<>();
                            addProduct.put("product_ID_" + String.valueOf(DBQueries.wishList.size()), productID);
                            addProduct.put("list_size", (long) (DBQueries.wishList.size() + 1));
                            firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA").document("MY_WISHLIST")
                                    .update(addProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (DBQueries.wishlistModelList.size() != 0) {
                                            DBQueries.wishlistModelList.add(new WishlistModel(productID, documentSnapshot.get("product_image_1").toString()
                                                    , documentSnapshot.get("product_title").toString()
                                                    , (long) documentSnapshot.get("free_coupen")
                                                    , documentSnapshot.get("average_rating").toString()
                                                    , (long) documentSnapshot.get("total_ratings")
                                                    , documentSnapshot.get("product_price").toString()
                                                    , documentSnapshot.get("cutted_price").toString()
                                                    , (boolean) documentSnapshot.get("COD")
                                                    ,inStock));
                                        }
                                        ALREADY_ADDED_TO_WISHLIST = true;
                                        addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.successred));
                                        DBQueries.wishList.add(productID);
                                        Toast.makeText(ProductDetailActivity.this, "Product is added to wishlist successfully", Toast.LENGTH_SHORT).show();

                                    } else {
                                        addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#817F7F")));
                                        String error = task.getException().getMessage();
                                        Toast.makeText(ProductDetailActivity.this, "error", Toast.LENGTH_SHORT).show();
                                    }
                                    running_wishlist_query = false;
                                }
                            });

                        }
                    }
                }
            }
        });


        productDetailsViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(productDetailsTabLayout));
        productDetailsTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                productDetailsViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //// rating layout
        rateNowContainor = findViewById(R.id.rate_now_container);
        for (int x = 0; x < rateNowContainor.getChildCount(); x++) {
            final int starPosition = x;
            rateNowContainor.getChildAt(x).setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    if (currentUser == null) {
                        signInDialog.show();
                    } else {
                        if (starPosition != initialRating) {
                            if (!running_rating_query) {
                                running_rating_query = true;

                                setRating(starPosition);
                                Map<String, Object> updateRating = new HashMap<>();
                                if (DBQueries.myRatedIds.contains(productID)) {

                                    TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                    TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);

                                    updateRating.put(initialRating + 1 + "_star", Long.parseLong(oldRating.getText().toString()) - 1);
                                    updateRating.put(starPosition + 1 + "_star", Long.parseLong(finalRating.getText().toString()) + 1);
                                    updateRating.put("average_rating", calculateAverageRating((long) starPosition - initialRating, true));

                                } else {

                                    updateRating.put(starPosition + 1 + "_star", (long) documentSnapshot.get(starPosition + 1 + "_star") + 1);
                                    updateRating.put("average_rating", (calculateAverageRating((long) starPosition + 1, false)));
                                    updateRating.put("total_ratings", (long) documentSnapshot.get("total_ratings") + 1);

                                }
                                firebaseFirestore.collection("PRODUCTS").document(productID)
                                        .update(updateRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> myRating = new HashMap<>();
                                            if (DBQueries.myRatedIds.contains(productID)) {
                                                myRating.put("rating_" + DBQueries.myRatedIds.indexOf(productID), (long) starPosition + 1);
                                            } else {
                                                myRating.put("list_size", (long) DBQueries.myRatedIds.size() + 1);
                                                myRating.put("product_ID_" + DBQueries.myRatedIds.size(), productID);
                                                myRating.put("rating_" + DBQueries.myRatedIds.size(), (long) starPosition + 1);
                                            }
                                            firebaseFirestore.collection("USERS").document(currentUser.getUid()).collection("USER_DATA").document("MY_RATINGS")
                                                    .update(myRating).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {

                                                        if (DBQueries.myRatedIds.contains(productID)) {

                                                            DBQueries.myRating.set(DBQueries.myRatedIds.indexOf(productID), (long) starPosition + 1);

                                                            TextView oldRating = (TextView) ratingsNoContainer.getChildAt(5 - initialRating - 1);
                                                            TextView finalRating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);
                                                            oldRating.setText(String.valueOf(Integer.parseInt(oldRating.getText().toString()) - 1));
                                                            finalRating.setText(String.valueOf(Integer.parseInt(finalRating.getText().toString()) + 1));
                                                        } else {

                                                            DBQueries.myRatedIds.add(productID);
                                                            DBQueries.myRating.add((long) starPosition + 1);

                                                            TextView rating = (TextView) ratingsNoContainer.getChildAt(5 - starPosition - 1);
                                                            rating.setText(String.valueOf(Integer.parseInt(rating.getText().toString()) + 1));
                                                            totalRatingsMiniView.setText("(" + ((long) documentSnapshot.get("total_ratings") + 1) + ") ratings");
                                                            totalRatings.setText((long) documentSnapshot.get("total_ratings") + 1 + " ratings");
                                                            totalRatingsFigure.setText(String.valueOf((long) documentSnapshot.get("total_ratings") + 1));
                                                            Toast.makeText(ProductDetailActivity.this, "Thank you for rating this product!", Toast.LENGTH_SHORT).show();
                                                        }
                                                        for (int x = 0; x < 5; x++) {

                                                            TextView ratingfigures = (TextView) ratingsNoContainer.getChildAt(x);

                                                            ProgressBar progressBar = (ProgressBar) ratingsProgressbarContainer.getChildAt(x);
                                                            int maxProgress = Integer.parseInt(totalRatingsFigure.getText().toString());
                                                            progressBar.setMax(maxProgress);
                                                            progressBar.setProgress(Integer.parseInt(ratingfigures.getText().toString()));
                                                        }
                                                        initialRating = starPosition;
                                                        averageRating.setText(calculateAverageRating(0, true));
                                                        averageRatingMiniView.setText(calculateAverageRating(0, true));

                                                        if (DBQueries.wishList.contains(productID) && DBQueries.wishlistModelList.size() != 0) {
                                                            int index = DBQueries.wishList.indexOf(productID);
                                                            DBQueries.wishlistModelList.get(index).setRating(averageRating.getText().toString());
                                                            DBQueries.wishlistModelList.get(index).setTotalRatings(Long.parseLong(totalRatingsFigure.getText().toString()));
                                                        }

                                                    } else {
                                                        setRating(initialRating);
                                                        String error = task.getException().getMessage();
                                                        Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                                                    }
                                                    running_rating_query = false;
                                                }
                                            });
                                        } else {
                                            running_rating_query = false;
                                            setRating(initialRating);
                                            String error = task.getException().getMessage();
                                            Toast.makeText(ProductDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
        //// rating layout

        buyNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser == null) {
                    signInDialog.show();
                } else {
                    DeliveryActivity.fromCart = false;
                    loadingDialog.show();
                    productDetailsActivity = ProductDetailActivity.this;
                    DeliveryActivity.cartItemModelList = new ArrayList<>();
                    DeliveryActivity.cartItemModelList.add(new CartItemModel(documentSnapshot.getBoolean("COD"),CartItemModel.CART_ITEM, productID, documentSnapshot.get("product_image_1").toString()
                            , documentSnapshot.get("product_title").toString()
                            , (long) documentSnapshot.get("free_coupens")
                            , documentSnapshot.get("product_price").toString()
                            , documentSnapshot.get("cutted_price").toString()
                            , (long) 1
                            , (long) documentSnapshot.get("offers_applied")
                            , (long) 0
                            ,inStock
                            ,(long)documentSnapshot.get("max-quantity")
                            ,(long)documentSnapshot.get("stock_quantity")));
                    DeliveryActivity.cartItemModelList.add(new CartItemModel(CartItemModel.TOTAL_AMOUNT));

                    if (DBQueries.addressesModelList.size() == 0) {
                        DBQueries.loadAddresses(ProductDetailActivity.this, loadingDialog,true);
                    }else {
                        loadingDialog.dismiss();
                        Intent deliveryIntent = new Intent(ProductDetailActivity.this, DeliveryActivity.class);
                        startActivity(deliveryIntent);
                    }
                }
            }
        });





        coupenRedeemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkCoupenPriceDialog.show();
            }
        });


        ////// signIn dialog
        signInDialog = new Dialog(ProductDetailActivity.this);
        signInDialog.setContentView(R.layout.sign_in_dialogue);
        signInDialog.setCancelable(true);
        signInDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


        Button dialogSignInBtn = signInDialog.findViewById(R.id.sign_in_btn);
        Button dialogSignUpBtn = signInDialog.findViewById(R.id.sign_up_btn);
        final Intent registerIntent = new Intent(ProductDetailActivity.this, RegisterActivity.class);

        dialogSignInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = false;
                startActivity(registerIntent);
            }
        });

        dialogSignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignInFragment.disableCloseBtn = true;
                SignUpFragment.disableCloseBtn = true;
                signInDialog.dismiss();
                setSignUpFragment = true;
                startActivity(registerIntent);
            }
        });
        //////signIn dialog

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onStart() {
        super.onStart();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            coupenRedeemptionLayout.setVisibility(View.GONE);
        } else {
            coupenRedeemptionLayout.setVisibility(View.VISIBLE);
        }

        if (currentUser != null) {
            if (DBQueries.myRating.size() == 0) {
                DBQueries.loadRatingList(ProductDetailActivity.this);
            }
            if (DBQueries.wishList.size() == 0) {
                DBQueries.loadWishlist(ProductDetailActivity.this, loadingDialog, false);
            }
            if (DBQueries.rewardModelsList.size() == 0){
                DBQueries.loadRewards(ProductDetailActivity.this,loadingDialog,false);
            }
            if (DBQueries.cartList.size() != 0 && DBQueries.wishList.size() != 0 && DBQueries.rewardModelsList.size() != 0 ){
                loadingDialog.show();
            }
        } else {
            loadingDialog.dismiss();
        }

        if (DBQueries.myRatedIds.contains(productID)) {
            int index = DBQueries.myRatedIds.indexOf(productID);
            initialRating = Integer.parseInt(String.valueOf(DBQueries.myRating.get(index))) - 1;
            setRating(initialRating);
        }
        if (DBQueries.cartList.contains(productID)) {
            ALREADY_ADDED_TO_CART = true;
        } else {
            ALREADY_ADDED_TO_CART = false;
        }

        if (DBQueries.wishList.contains(productID)) {
            ALREADY_ADDED_TO_WISHLIST = true;
            addToWishlistBtn.setSupportImageTintList(getResources().getColorStateList(R.color.successred));
        } else {
            addToWishlistBtn.setSupportImageTintList(ColorStateList.valueOf(Color.parseColor("#817F7F")));
            ALREADY_ADDED_TO_WISHLIST = false;
        }
        invalidateOptionsMenu();
    }

    private void showDialogRecyclerView() {
        if (coupensRecyclerView.getVisibility() == View.GONE) {
            coupensRecyclerView.setVisibility(View.VISIBLE);
            selectedCoupen.setVisibility(View.GONE);
        } else {
            coupensRecyclerView.setVisibility(View.GONE);
            selectedCoupen.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setRating(int starPosition) {

        for (int x = 0; x < rateNowContainor.getChildCount(); x++) {
            ImageView starButton = (ImageView) rateNowContainor.getChildAt(x);
            starButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#FA8E8B8B")));
            if (x <= starPosition) {
                starButton.setImageTintList(ColorStateList.valueOf(Color.parseColor("#EFD426")));
            }
        }
    }


    private String calculateAverageRating(long currentUserRating, boolean update) {
        Double totalStars = Double.valueOf(0);
        for (int x = 1; x < 6; x++) {
            TextView ratingNo = (TextView) ratingsNoContainer.getChildAt(5 - x);
            totalStars = totalStars + (Long.parseLong(ratingNo.getText().toString()) * x);
        }
        totalStars = totalStars + currentUserRating;
        if (update) {
            return String.valueOf(totalStars / Long.parseLong(totalRatingsFigure.getText().toString())).substring(0, 3);
        } else {
            return String.valueOf(totalStars / (Long.parseLong(totalRatingsFigure.getText().toString()) + 1)).substring(0, 3);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_and_cart_icon, menu);

        cartItem = menu.findItem(R.id.main_cart_icon);
            cartItem.setActionView(R.layout.badge_layout);
            ImageView badgeIcon = cartItem.getActionView().findViewById(R.id.badge_icon);
            badgeIcon.setImageResource(R.drawable.finaladdtocart_icon);
            badgeCount = cartItem.getActionView().findViewById(R.id.badge_count);

        if (currentUser != null) {
            if (DBQueries.cartList.size() == 0) {
                DBQueries.loadCartList(ProductDetailActivity.this, loadingDialog, false, badgeCount,new TextView(ProductDetailActivity.this));
            }
            else {
                badgeCount.setVisibility(View.VISIBLE);
                if (DBQueries.cartList.size()<20) {
                    badgeCount.setText(String.valueOf(DBQueries.cartList.size()));
                }else{
                    badgeCount.setText("20");
                }
            }
        }
            cartItem.getActionView().setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onClick(View view) {
                    if (currentUser == null) {
                        signInDialog.show();
                    } else {
                        Intent cartIntent = new Intent(ProductDetailActivity.this, MainActivity.class);
                        showCart = true;
                        startActivity(cartIntent);
                    }
                }
            });

        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            productDetailsActivity = null;
            finish();
            return true;
        } else if (id == R.id.main_search_icon) {
            if (fromSearch){
                finish();
            }else {
                Intent searchIntent = new Intent(this, SearchActivity.class);
                startActivity(searchIntent);
            }
            return true;
        } else if (id == R.id.main_cart_icon) {
            if (currentUser == null) {
                signInDialog.show();
            } else {
                Intent cartIntent = new Intent(ProductDetailActivity.this, MainActivity.class);
                showCart = true;
                startActivity(cartIntent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fromSearch = false;
    }

    @Override
    public void onBackPressed() {
        productDetailsActivity = null;
        super.onBackPressed();
    }
}


