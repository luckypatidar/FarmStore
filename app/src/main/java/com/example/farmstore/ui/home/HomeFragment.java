package com.example.farmstore.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.farmstore.CategoryAdaptor;
import com.example.farmstore.CategoryModel;
import com.example.farmstore.DBQueries;
import com.example.farmstore.GridProductLayoutAdapter;
import com.example.farmstore.HomePageAdapter;
import com.example.farmstore.HomePageModel;
import com.example.farmstore.HorizontalProductScrollAdaptar;
import com.example.farmstore.HorizontalProductScrollModel;
import com.example.farmstore.MainActivity;
import com.example.farmstore.R;
import com.example.farmstore.SliderAdaptar;
import com.example.farmstore.SliderModel;
import com.example.farmstore.WishlistModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.farmstore.DBQueries.categoryModelList;
import static com.example.farmstore.DBQueries.firebaseFirestore;

import static com.example.farmstore.DBQueries.lists;
import static com.example.farmstore.DBQueries.loadCategories;
import static com.example.farmstore.DBQueries.loadFragmentData;
import static com.example.farmstore.DBQueries.loadedCategoriesName;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        //required empty public constructor
    }


    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;

    public static SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView categoryRecyclerView;
    public static List<CategoryModel> categoryModelFakeList = new ArrayList<>();
    private CategoryAdaptor categoryAdaptor;
    private RecyclerView homePageRecyclerView;
    private List<HomePageModel> homePageModelFakeList = new ArrayList<>();
    private HomePageAdapter adapter;
    private ImageView noInternetConnection;
    private Button retryBtn;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        swipeRefreshLayout=view.findViewById(R.id.refresh_layout);
        noInternetConnection = view.findViewById(R.id.no_internet_connection);
        retryBtn=view.findViewById(R.id.retry_btn);
        homePageRecyclerView = view.findViewById(R.id.home_page_recycler_view);
        categoryRecyclerView = view.findViewById(R.id.category_recyclerview);
        swipeRefreshLayout.setColorSchemeColors(getContext().getResources().getColor(R.color.colorPrimary), getContext().getResources().getColor(R.color.colorPrimary), getContext().getResources().getColor(R.color.colorPrimary));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        categoryRecyclerView.setLayoutManager(layoutManager);

        LinearLayoutManager testingLayoutManager = new LinearLayoutManager(getContext());
        testingLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        homePageRecyclerView.setLayoutManager(testingLayoutManager);




//////////category fake list
        categoryModelFakeList.add(new CategoryModel("null",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));
        categoryModelFakeList.add(new CategoryModel("",""));


//////////category fake list


//////////home page fake list

        List<SliderModel> sliderModelFakeList=new ArrayList<>();

        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));
        sliderModelFakeList.add(new SliderModel("null","#dfdfdf"));

        List<HorizontalProductScrollModel> horizontalProductScrollModelFakeList=new ArrayList<>();
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));
        horizontalProductScrollModelFakeList.add(new HorizontalProductScrollModel("","","","",""));

        homePageModelFakeList.add(new HomePageModel(0,sliderModelFakeList));
        homePageModelFakeList.add(new HomePageModel(1,"","#dfdfdf"));
        homePageModelFakeList.add(new HomePageModel(2,"","#dfdfdf",horizontalProductScrollModelFakeList,new ArrayList<WishlistModel>()));
        homePageModelFakeList.add(new HomePageModel(3,"","#dfdfdf",horizontalProductScrollModelFakeList));

//////////home page fake list

//        categoryAdaptor = new CategoryAdaptor(categoryModelFakeList);
//        adapter = new HomePageAdapter(homePageModelFakeList);

        connectivityManager=(ConnectivityManager)getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();


        if(networkInfo != null && networkInfo.isConnected()==true){
            MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            noInternetConnection.setVisibility(View.GONE);
            retryBtn.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
            homePageRecyclerView.setVisibility(View.VISIBLE);

            if(categoryModelList.size() == 0){
                loadCategories(categoryRecyclerView,getContext());
            }else {
                categoryAdaptor = new CategoryAdaptor(categoryModelList);
                categoryAdaptor.notifyDataSetChanged();
            }
            categoryRecyclerView.setAdapter(categoryAdaptor);

            if(lists.size() == 0){
                loadedCategoriesName.add("HOME");
                lists.add(new ArrayList<HomePageModel>());
                loadFragmentData(homePageRecyclerView,getContext(),0,"Home");
            }else {
                adapter =new HomePageAdapter(lists.get(0));
                adapter.notifyDataSetChanged();
            }
            homePageRecyclerView.setAdapter(adapter);
        }
        else {
            MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            categoryRecyclerView.setVisibility(View.GONE);
            homePageRecyclerView.setVisibility(View.GONE);
            Glide.with(this).load(R.mipmap.no_internet_connection).into(noInternetConnection);
            noInternetConnection.setVisibility(View.VISIBLE);
            retryBtn.setVisibility(View.VISIBLE);

        }

        ///refresh layout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                reloadPage();
            }
        });
        ///refresh layout

        retryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reloadPage();
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        reloadPage();
    }

    private void reloadPage(){
        networkInfo = connectivityManager.getActiveNetworkInfo();
        categoryModelFakeList.clear();
        categoryModelList.clear();
        lists.clear();
        DBQueries.loadedCategoriesName.clear();
        DBQueries.clearData();
        if(networkInfo != null && networkInfo.isConnected()==true){
            MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            noInternetConnection.setVisibility(View.GONE);
            retryBtn.setVisibility(View.GONE);
            categoryRecyclerView.setVisibility(View.VISIBLE);
            homePageRecyclerView.setVisibility(View.VISIBLE);
            categoryAdaptor = new CategoryAdaptor(categoryModelFakeList);
            adapter = new HomePageAdapter(homePageModelFakeList);
            categoryRecyclerView.setAdapter(categoryAdaptor);
            homePageRecyclerView.setAdapter(adapter);

            loadCategories(categoryRecyclerView,getContext());
            loadedCategoriesName.add("HOME");
            lists.add(new ArrayList<HomePageModel>());
            loadFragmentData(homePageRecyclerView,getContext(),0,"Home");
        }else {
            MainActivity.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            Toast.makeText(getContext(),"No internet Connction!",Toast.LENGTH_SHORT).show();
            categoryRecyclerView.setVisibility(View.GONE);
            homePageRecyclerView.setVisibility(View.GONE);
            Glide.with(getContext()).load(R.mipmap.no_internet_connection).into(noInternetConnection);
            noInternetConnection.setVisibility(View.VISIBLE);
            retryBtn.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}