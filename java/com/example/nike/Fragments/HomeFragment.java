package com.example.nike.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.nike.Home.HomePageImage;
import com.example.nike.Home.HomePageImageAdapter;
import com.example.nike.Home.I_OnClickToShop;
import com.example.nike.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    public HomeFragment(final I_OnClickToShop _listener) {
        this._listener = _listener;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /* PROPERTY */
    ProgressBar progressBar_loadding_Home;
    LinearLayout llt_home;
    ListView _liv_home;
    HomePageImageAdapter _homePageImageAdapter;
    ArrayList<HomePageImage> _homePageImages =  new ArrayList<HomePageImage>();
    private I_OnClickToShop _listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        // Loads Shop Data

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        this.progressBar_loadding_Home = view.findViewById(R.id.progressBar_loadding_Home);
        this.llt_home = view.findViewById(R.id.llt_home);
        this._liv_home = view.findViewById(R.id.liv_home);

        // Show Home Page View
        this.llt_home.setVisibility(View.GONE);
        this.progressBar_loadding_Home.setVisibility(View.VISIBLE);
        this.LoadsHomePageData();

        // Inflate the layout for this fragment
        return view;
    }

    private void LoadsHomePageData()
    {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference();

        databaseReference.child("HomePageImageLinks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() == false) return;

                _homePageImages.clear();
                for (DataSnapshot snap : snapshot.getChildren())
                {
                    String homePageImageLink = snap.getValue(String.class);
                    HomePageImage homePageImage = new HomePageImage(homePageImageLink);

                    _homePageImages.add(homePageImage);
                }

                _homePageImageAdapter.notifyDataSetChanged();
                ShowHomePageView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        this._homePageImageAdapter = new HomePageImageAdapter(_homePageImages, getContext(), this._listener);
    }

    private void ShowHomePageView()
    {
        this.progressBar_loadding_Home.setVisibility(View.GONE);
        this._liv_home.setAdapter(this._homePageImageAdapter);
        this.llt_home.setVisibility(View.VISIBLE);
    }

    public void set_listener(I_OnClickToShop listener)
    {
        this._listener = listener;
    }

}