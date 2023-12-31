package com.example.nike.Product;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.nike.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ProductDetails extends AppCompatActivity {


    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;


    ImageButton ibn_undo;
    TextView tvw_productTitle;
    LinearLayout llo_productDetails;
    ViewPager2 vpg_viewPager;
    LinearLayout llo_productSamples;
    GridLayout glt_size;


    TextView tvw_name;
    TextView tvw_price;
    TextView tvw_description;
    private final int MAX = 6;
    Button[] btn_size = new Button[MAX];
    Button btn_addToBag;
    private int _selectedSizeIndex = -1;

    String _productID;
    RecyclerView rvw_productDetails;
    ScrollView svw_productDetails;
    LinearLayout llt_productDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        ibn_undo = findViewById(R.id.ibn_undo);
        tvw_productTitle = findViewById(R.id.tvw_productTitle);
        llo_productDetails = findViewById(R.id.llo_productDetails);
        vpg_viewPager = findViewById(R.id.vpg_viewPager);
        llo_productSamples = findViewById(R.id.llo_productSamples);
        glt_size = findViewById(R.id.glt_size);

        tvw_name = findViewById(R.id.tvw_name);
        tvw_price = findViewById(R.id.tvw_price);
        tvw_description = findViewById(R.id.tvw_description);
        btn_size[0] = findViewById(R.id.btn_size0);
        btn_size[1] = findViewById(R.id.btn_size1);
        btn_size[2] = findViewById(R.id.btn_size2);
        btn_size[3] = findViewById(R.id.btn_size3);
        btn_size[4] = findViewById(R.id.btn_size4);
        btn_size[5] = findViewById(R.id.btn_size5);

        btn_addToBag = findViewById(R.id.btn_addToBag);

        // rvw_productDetails = findViewById(R.id.rvw_productDetails);
        svw_productDetails = findViewById(R.id.svw_productDetails);
        llt_productDetails = findViewById(R.id.llt_productDetails);

        this._productID = "658d9b6651371b47a5950469";

        //
        Intent resultIntent = getIntent();
        String productID = resultIntent.getExtras().getString("productID");
        LoadProduct(productID);
        HandleSizeButtonClick();
        HandleClickTheAddToBagButton();
        HandleClickTheUndoButton();

/*
        // Tạo LayoutParams để đảm bảo rằng ImageView có kích thước vừa khít màn hình theo chiều rộng
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.width = getResources().getDisplayMetrics().widthPixels; // Thiết lập chiều rộng bằng chiều rộng thiết bị
        imageView_1.setLayoutParams(params);
        imageView_1.setBackgroundColor(R.drawable.ic_launcher_background);
        imageView_1.setAdjustViewBounds(true); // Đảm bảo ảnh không thay đổi tỉ lệ và vừa khít kích thước
        imageView_1.setScaleType(ImageView.ScaleType.FIT_XY);
*/

    }

    private void LoadProduct(String productIDToSearch) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String productID = snap.child("_productID").getValue(String.class);
                    if (productIDToSearch.equals(productID))
                    {
                        // Product Image Color Link
                        List<List<String>> productImageColorLink = new ArrayList<>();
                        Iterable<DataSnapshot> dss_productImageColorLink = snap.child("_productImageColorLink").getChildren();
                        int i_pic = 0;
                        for (DataSnapshot colorNumber : dss_productImageColorLink) {
                            Iterable<DataSnapshot> dss_imageColorLink = colorNumber.getChildren();
                            ArrayList<String> imageColorLink = new ArrayList<>();
                            for (DataSnapshot icl : dss_imageColorLink) {
                                imageColorLink.add(icl.getValue(String.class));
                            }
                            productImageColorLink.add(imageColorLink);

                            // View Pager
                            if (i_pic == 0)
                            {
                                ProductImageAdapter productImageAdapter = new ProductImageAdapter(imageColorLink, vpg_viewPager.getContext());
                                vpg_viewPager.setAdapter(productImageAdapter);
                            }

                            // Product Sample
                            ImageView imageView = new ImageView(llo_productSamples.getContext());
                            imageView.setTag(i_pic);
                            imageView.setBackgroundResource(R.drawable.img_empty);
                            imageView.setAdjustViewBounds(true);
                            imageView.setScaleType(ImageView.ScaleType.FIT_START);
                            imageView.setMaxHeight(450);
                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String tag = imageView.getTag().toString();
                                    LoadProductSampleImage(productIDToSearch, tag);
                                }
                            });
                            Glide.with(imageView.getContext()).load(imageColorLink.get(0)).into(imageView);
                            llo_productSamples.addView(imageView);

                            Space space = new Space(llo_productSamples.getContext());
                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(30, llo_productSamples.getHeight());
                            space.setLayoutParams(layoutParams);
                            llo_productSamples.addView(space);

                            i_pic++;
                        }

                        // Name, Price, Description
                        String productName = snap.child("_productName").getValue(String.class);
                        Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                        String productDescription = snap.child("_productDescription").getValue(String.class);
                        // String productType = snap.child("_productType").getValue(String.class);

                        tvw_name.setText(productName);
                        tvw_productTitle.setText(productName);
                        String str_productPrice =  ConvertNumberToString_productPrice(productPrice);
                        tvw_price.setText(str_productPrice);
                        tvw_description.setText(productDescription);

                        // Product Size
                        List<Integer> productSizes = new ArrayList<>();
                        Iterable<DataSnapshot> dss_productSize = snap.child("_productSize").getChildren();
                        for (DataSnapshot child : dss_productSize) {
                            productSizes.add(child.getValue(Integer.class));
                        }

                        for (int i_ps = 0; i_ps < MAX; i_ps++)
                        {
                            Button btn = btn_size[i_ps];
                            btn.setText(String.valueOf(productSizes.get(i_ps)));
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void LoadProductSampleImage(String productIDToSearch, String productTagToSearch) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String productID = snap.child("_productID").getValue(String.class);
                    if (productIDToSearch.equals(productID))
                    {
                        // Product Image Color Link
                        List<List<String>> productImageColorLink = new ArrayList<>();
                        Iterable<DataSnapshot> dss_productImageColorLink = snap.child("_productImageColorLink").getChildren();
                        int i = 0;
                        for (DataSnapshot colorNumber : dss_productImageColorLink) {
                            Iterable<DataSnapshot> dss_imageColorLink = colorNumber.getChildren();
                            ArrayList<String> imageColorLink = new ArrayList<>();
                            for (DataSnapshot icl : dss_imageColorLink) {
                                imageColorLink.add(icl.getValue(String.class));
                            }

                            /* View Pager */
                            if (productTagToSearch.equals(String.valueOf(i)))
                            {
                                ProductImageAdapter productImageAdapter = new ProductImageAdapter(imageColorLink, vpg_viewPager.getContext());
                                vpg_viewPager.setAdapter(productImageAdapter);
                            }

                            i++;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    private String ConvertNumberToString_productPrice(int i_productPrice)
    {
        String str_productPrice = String.valueOf(i_productPrice);
        int numberLenght = str_productPrice.length();
        int surplus = numberLenght % 3;
        int dotNumber = numberLenght / 3;
        if (numberLenght % 3 == 0)
            dotNumber -= 1;

        String str_result = "₫";
        for (int i = 0; i < dotNumber; i++)
        {
            if (i == 0)
            {
                if (surplus != 0)
                    str_result += str_productPrice.substring(0, surplus) + ",";
                else
                    str_result += str_productPrice.substring(surplus, surplus + 3) + ",";
            }
            else if (i != 0)
                str_result += str_productPrice.substring(surplus, surplus + 3) + ",";
        }
        str_result += str_productPrice.substring(surplus, surplus + 3);

        return str_result;
    }

    private void HandleSizeButtonClick()
    {
            btn_size[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickSizeButton(v, 0);
                }
            });

            btn_size[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickSizeButton(v, 1);
                }
            });

            btn_size[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickSizeButton(v, 2);
                }
            });

            btn_size[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickSizeButton(v, 3);
                }
            });

            btn_size[4].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickSizeButton(v, 4);
                }
            });

            btn_size[5].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClickSizeButton(v, 5);
                }
            });
    }

    @SuppressLint("ResourceAsColor")
    private void ClickSizeButton(View v, int position)
    {
        for (Button btn : btn_size)
            btn.setBackgroundResource(R.color.white);

        v.setBackgroundColor(R.color.black);
        this._selectedSizeIndex = position;
    }

    private void HandleClickTheAddToBagButton()
    {
        btn_addToBag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_selectedSizeIndex == -1)
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn size cho sản phẩm", Toast.LENGTH_SHORT).show();
                else
                    LoadSelectedSize(_productID, _selectedSizeIndex);
            }
        });
    }

    private void LoadSelectedSize(String productIDToSearch, Integer position) {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    String productID = snap.child("_productID").getValue(String.class);
                    if (productIDToSearch.equals(productID))
                    {
                        // Product Size
                        List<Integer> productSizes = new ArrayList<>();
                        Iterable<DataSnapshot> dss_productSize = snap.child("_productSize").getChildren();
                        for (DataSnapshot child : dss_productSize) {
                            productSizes.add(child.getValue(Integer.class));
                        }

                        tvw_productTitle.setText(String.valueOf(productSizes.get(position)));
                        // Add To Bag

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });

    }

    private void HandleClickTheUndoButton()
    {
        ibn_undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Undo", Toast.LENGTH_SHORT).show();
            }
        });
    }
}