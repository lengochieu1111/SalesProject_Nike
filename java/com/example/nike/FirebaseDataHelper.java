package com.example.nike;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.nike.Bag.CartItem;
import com.example.nike.Product.Product;
import com.example.nike.Product.ENUM_ProductType;
import com.example.nike.Product.ENUM_SortType;
import com.example.nike.Product.STR_ProductType;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FirebaseDataHelper {
    /* PROPERTY */
    private FirebaseDatabase _firebaseDatabase;
    private DatabaseReference _databaseReference;
    private ArrayList<Product> _products =  new ArrayList<Product>();
    private ArrayList<CartItem> _cartItems =  new ArrayList<CartItem>();

    public interface DataStatus
    {
        void DataIsLoaded_Product(ArrayList<Product> products, ArrayList<String> keys);
        void DataIsLoaded_CartItem(ArrayList<CartItem> cartItems, ArrayList<String> keys);
        void DataIsInserted();
        void DataIsInserted_CartItem();
        void DataIsUpdated();
        void DataIsDeleted();
    }

    public FirebaseDataHelper()
    {
        this._firebaseDatabase = FirebaseDatabase.getInstance();
        this._databaseReference = _firebaseDatabase.getReference();
    }

    public void ReadTheProductList(final DataStatus dataStatus, String productNameSearch, List<ENUM_ProductType> productTypeSearch, ENUM_SortType sortType) {
        this._databaseReference.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                ArrayList<String> keys = new ArrayList<>();
                _products.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    keys.add(snap.getKey());
                    String productID = snap.child("_productID").getValue(String.class);
                    Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                    String productName = snap.child("_productName").getValue(String.class);
                    String productImageLink = snap.child("_productImageLink").getValue(String.class);
                    String productDescription = snap.child("_productDescription").getValue(String.class);
                    String productType = snap.child("_productType").getValue(String.class);

                    /*  Product Size */
                    List<Integer> productSize = new ArrayList<>();
                    Iterable<DataSnapshot> dss_productSize = snap.child("_productSize").getChildren();
                    for (DataSnapshot child : dss_productSize) {
                        productSize.add(child.getValue(Integer.class));
                    }

                    /*  Product Image Color Link */
                    List<List<String>> productImageColorLink = new ArrayList<>();
                    Iterable<DataSnapshot> dss_productImageColorLink = snap.child("_productImageColorLink").getChildren();
                    for (DataSnapshot colorNumber : dss_productImageColorLink) {
                        Iterable<DataSnapshot> dss_imageColorLink = colorNumber.getChildren();
                        ArrayList<String> imageColorLink = new ArrayList<>();
                        for (DataSnapshot icl : dss_imageColorLink) {
                            imageColorLink.add(icl.getValue(String.class));
                        }
                        productImageColorLink.add(imageColorLink);
                    }

                    Product product = new Product(productID, productName, productPrice, productImageLink,
                            productDescription, productSize, productImageColorLink, productType);

                    boolean satisfyCondition_Name = CheckProductNameSearch(product, productNameSearch);
                    boolean satisfyCondition_Type = CheckProductTypeSearch(product, productTypeSearch);

                    if (satisfyCondition_Name && satisfyCondition_Type)
                    {
                        _products.add(product);
                    }

                    SortProducts(_products, sortType);

                }

                dataStatus.DataIsLoaded_Product(_products, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }

    private boolean CheckProductNameSearch(Product product, String productNameSearch)
    {
        if (productNameSearch == null)
        {
            return true;
        }
        else
        {
            if (product.get_productName().toLowerCase().contains(productNameSearch.toLowerCase()))
            {
                return true;
            }
        }

        return  false;
    }

    private boolean CheckProductTypeSearch(Product product, List<ENUM_ProductType> productTypeSearch)
    {
        if (productTypeSearch.contains(ENUM_ProductType.TOTAL))
        {
            return true;
        }

        if (productTypeSearch.contains(ENUM_ProductType.MEN) && (product.get_productType().equals(STR_ProductType.MEN)))
        {
            return true;
        }

        if (productTypeSearch.contains(ENUM_ProductType.WOMEN) && (product.get_productType().equals(STR_ProductType.WOMEN)))
        {
            return true;
        }

        if (productTypeSearch.contains(ENUM_ProductType.KID) && (product.get_productType().equals(STR_ProductType.KID)))
        {
            return true;
        }

        return  false;
    }

    private void SortProducts(ArrayList<Product> products, ENUM_SortType sortType) {
        boolean sortUpAscending = false;
        if (sortType == ENUM_SortType.FEATURED)
        {
            return;
        }
        else if (sortType == ENUM_SortType.LOW_HIGH) {
            sortUpAscending = true;
        }

        for (int i = 0; i < products.size() - 1; i++) {
            for (int j = i + 1; j < products.size(); j++) {
                if (sortUpAscending && (products.get(i).get_productPrice() > products.get(j).get_productPrice()))
                {
                    SwapProductData(products.get(i), products.get(j));
                }
                else if (!sortUpAscending && (products.get(i).get_productPrice() < products.get(j).get_productPrice()))
                {
                    SwapProductData(products.get(i), products.get(j));
                }
            }
        }
    }

    private void  SwapProductData(Product product_1, Product product_2)
    {
        Product product_copy = new Product(product_1);
        product_1.ChangeDataProduct(product_2);
        product_2.ChangeDataProduct(product_copy);
    }

    /* Cart Item */
    public void ReadTheCartItemList(final DataStatus dataStatus) {
        this._databaseReference.child("CartItems").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                _cartItems.clear();
                ArrayList<String> keys = new ArrayList<>();

                if (!snapshot.exists()) return;
                if (!snapshot.hasChildren())
                {
                    dataStatus.DataIsLoaded_CartItem(_cartItems, keys);
                }

                for (DataSnapshot snap : snapshot.getChildren()) {
                    keys.add(snap.getKey());

                    String productID = snap.child("_productID").getValue(String.class);
                    String productName = snap.child("_productName").getValue(String.class);
                    Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                    Integer productSize = snap.child("_productSize").getValue(Integer.class);
                    Integer productNumber = snap.child("_productNumber").getValue(Integer.class);
                    String productImageLink = snap.child("_productImageLink").getValue(String.class);
                    String productType = snap.child("_productType").getValue(String.class);
                    String productColor = snap.child("_productColor").getValue(String.class);

                    CartItem cartItem = new CartItem(productID, productImageLink, productName, productPrice,
                            productColor, productSize, productType, productNumber);

                    _cartItems.add(cartItem);
                }

                dataStatus.DataIsLoaded_CartItem(_cartItems, keys);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }

    public void AddProductToCart(CartItem cartItem, final DataStatus dataStatus)
    {
        this._databaseReference.child("CartItems").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long counter = snapshot.getChildrenCount();
                _databaseReference.child("CartItems").child(String.valueOf(counter)).setValue(cartItem).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dataStatus.DataIsInserted_CartItem();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


}
