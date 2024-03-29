package com.example.nike;

import androidx.annotation.NonNull;

import com.example.nike.Bag.CartItem;
import com.example.nike.Product.Product;
import com.example.nike.Product.ENUM_ProductType;
import com.example.nike.Product.ENUM_SortType;
import com.example.nike.Product.STR_ProductType;
import com.example.nike.Profile.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FirebaseDataHelper
{
    /* PROPERTY */
    FirebaseUser _user;
    String _bagKey;
    String _profileKey;
    private FirebaseDatabase _firebaseDatabase;
    private DatabaseReference _databaseReference;
    private ArrayList<Product> _products =  new ArrayList<Product>();
    private ArrayList<CartItem> _cartItems =  new ArrayList<CartItem>();
    private ArrayList<CartItem> _cartItemSelected =  new ArrayList<CartItem>();

    public interface DataStatus
    {
        /* Product */
        void DataIsLoaded_Product(ArrayList<Product> products, ArrayList<String> keys);
        void DataIsInserted_Product();
        void DataIsUpdated_Product();
        void DataIsDeleted_Product();

        /* Cart Item */
        void DataIsLoaded_CartItem(ArrayList<CartItem> cartItems,ArrayList<CartItem> _cartItemSelected, ArrayList<String> keys);
        void DataIsInserted_CartItem();
        void DataIsUpdated_CartItem(ArrayList<CartItem> cartItemSelected);
        void DataIsDeleted_CartItem(ArrayList<CartItem> cartItem);
        void HasTheSelectedProduct_CartItem(boolean isEmpty);

        /* Cart Item */
        void DataIsLoaded_User(User user);
    }

    public FirebaseDataHelper()
    {
        this._firebaseDatabase = FirebaseDatabase.getInstance();
        this._databaseReference = _firebaseDatabase.getReference();
        this._user = FirebaseAuth.getInstance().getCurrentUser();
        this._bagKey = this._user.getUid();
        this._profileKey = this._user.getUid();
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

    public void CreateBagForUser()
    {
        this._databaseReference.child("Bags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    if (snap.getKey().equals(_bagKey))
                    {
                        return;
                    }
                }
                _databaseReference.child("Bags").child(_bagKey).setValue("null");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void CreateProfileForUser()
    {
        this._databaseReference.child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snap : snapshot.getChildren())
                {
                    if (snap.getKey().equals(_bagKey))
                    {
                        return;
                    }
                }
                _databaseReference.child("Users").child(_bagKey).child("_name").setValue("Unknown");
                _databaseReference.child("Users").child(_bagKey).child("_address").setValue("Unknown");
                _databaseReference.child("Users").child(_bagKey).child("_phoneNumber").setValue("Unknown");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    public void ReadTheCartItemList(final DataStatus dataStatus) {
        this._databaseReference.child("Bags").child(this._bagKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                _cartItems.clear();
                _cartItemSelected.clear();
                ArrayList<String> keys = new ArrayList<>();

                if (!snapshot.exists()) return;
                if (!snapshot.hasChildren())
                {
                    dataStatus.DataIsLoaded_CartItem(_cartItems, _cartItemSelected, keys);
                }

                for (DataSnapshot snap : snapshot.getChildren()) {
                    keys.add(snap.getKey());

                    boolean isSelected = snap.child("_isSelected").getValue(boolean.class);
                    String productID = snap.child("_productID").getValue(String.class);
                    String productName = snap.child("_productName").getValue(String.class);
                    Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                    Integer productSize = snap.child("_productSize").getValue(Integer.class);
                    Integer productNumber = snap.child("_productNumber").getValue(Integer.class);
                    String productImageLink = snap.child("_productImageLink").getValue(String.class);
                    String productType = snap.child("_productType").getValue(String.class);
                    String productColor = snap.child("_productColor").getValue(String.class);

                    CartItem cartItem = new CartItem(productID, productImageLink, productName, productPrice,
                            productColor, productSize, productType, productNumber, isSelected);

                    _cartItems.add(cartItem);

                    if (isSelected)
                    {
                        _cartItemSelected.add(cartItem);
                    }
                }

                dataStatus.DataIsLoaded_CartItem(_cartItems, _cartItemSelected, keys);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }

    public void AddProductToCart(CartItem cartItem, final DataStatus dataStatus)
    {
        this._databaseReference.child("Bags").child(this._bagKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Long counter = snapshot.getChildrenCount();
                _databaseReference.child("Bags").child(_bagKey).child(String.valueOf(counter)).setValue(cartItem).addOnSuccessListener(new OnSuccessListener<Void>() {
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

    public void UpdateProductToCart(String key, CartItem cartItem, final DataStatus dataStatus)
    {
        // OLD
        /*
        _databaseReference.child("CartItems").child(key).setValue(cartItem).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
            public void onSuccess(Void unused) {
                dataStatus.DataIsUpdated_CartItem();
            }
        });
        */

        HashMap CartItem = new HashMap();
        CartItem.put("_isSelected", cartItem.get_isSelected());
        CartItem.put("_productID", cartItem.get_productID());
        CartItem.put("_productName", cartItem.get_productName());
        CartItem.put("_productPrice", cartItem.get_productPrice());
        CartItem.put("_productSize", cartItem.get_productSize());
        CartItem.put("_productNumber", cartItem.get_productNumber());
        CartItem.put("_productImageLink", cartItem.get_productImageLink());
        CartItem.put("_productType", cartItem.get_productType());
        CartItem.put("_productColor", cartItem.get_productColor());

        _databaseReference.child("Bags").child(this._bagKey).child(key).updateChildren(CartItem).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                _databaseReference.child("Bags").child(_bagKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<CartItem> productSelected = new ArrayList<>();

                        if (!snapshot.exists()) return;
                        if (!snapshot.hasChildren()) return;

                        for (DataSnapshot snap : snapshot.getChildren()) {

                            boolean isSelected = snap.child("_isSelected").getValue(boolean.class);
                            String productID = snap.child("_productID").getValue(String.class);
                            String productName = snap.child("_productName").getValue(String.class);
                            Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                            Integer productSize = snap.child("_productSize").getValue(Integer.class);
                            Integer productNumber = snap.child("_productNumber").getValue(Integer.class);
                            String productImageLink = snap.child("_productImageLink").getValue(String.class);
                            String productType = snap.child("_productType").getValue(String.class);
                            String productColor = snap.child("_productColor").getValue(String.class);

                            CartItem cartItem = new CartItem(productID, productImageLink, productName, productPrice,
                                    productColor, productSize, productType, productNumber, isSelected);

                            if (isSelected)
                            {
                                productSelected.add(cartItem);
                            }
                        }

                        dataStatus.DataIsUpdated_CartItem(productSelected);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }

                });
            }
        });

    }

    public void DeleteProductToCart(String key, final DataStatus dataStatus)
    {
        _databaseReference.child("Bags").child(_bagKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                if (!snapshot.hasChildren() || snapshot.getChildrenCount() <= 1)
                {
                    dataStatus.DataIsLoaded_CartItem(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                    _databaseReference.child("Bags").child(_bagKey).setValue("null");
                }
                else
                {
                    DeleteCartItem(key, dataStatus);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });

    }

    private void DeleteCartItem(String key, final DataStatus dataStatus)
    {
        _databaseReference.child("Bags").child(this._bagKey).child(key).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                _databaseReference.child("Bags").child(_bagKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<CartItem> productSelected = new ArrayList<>();

                        if (!snapshot.exists()) return;
                        if (!snapshot.hasChildren())
                        {
                            dataStatus.DataIsLoaded_CartItem(productSelected, productSelected, new ArrayList<String>());
                            return;
                        }

                        int i_key = 0;
                        for (DataSnapshot snap : snapshot.getChildren()) {
                            String oldkey = snap.getKey();
                            Object data =  snap.getValue();
                            _databaseReference.child("Bags").child(_bagKey).child(oldkey).removeValue();
                            _databaseReference.child("Bags").child(_bagKey).child(String.valueOf(i_key)).setValue(data);
                            i_key++;
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

                _databaseReference.child("Bags").child(_bagKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<CartItem> productSelected = new ArrayList<>();

                        if (!snapshot.exists()) return;
                        if (!snapshot.hasChildren()) return;

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            boolean isSelected = snap.child("_isSelected").getValue(boolean.class);
                            String productID = snap.child("_productID").getValue(String.class);
                            String productName = snap.child("_productName").getValue(String.class);
                            Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                            Integer productSize = snap.child("_productSize").getValue(Integer.class);
                            Integer productNumber = snap.child("_productNumber").getValue(Integer.class);
                            String productImageLink = snap.child("_productImageLink").getValue(String.class);
                            String productType = snap.child("_productType").getValue(String.class);
                            String productColor = snap.child("_productColor").getValue(String.class);

                            CartItem cartItem = new CartItem(productID, productImageLink, productName, productPrice,
                                    productColor, productSize, productType, productNumber, isSelected);

                            if (isSelected)
                            {
                                productSelected.add(cartItem);
                            }
                        }

                        dataStatus.DataIsDeleted_CartItem(productSelected);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    public void HasTheSelectedProductInTheBag(final DataStatus dataStatus)
    {
        _databaseReference.child("Bags").child(_bagKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<CartItem> productSelected = new ArrayList<>();

                if (!snapshot.exists()) return;
                if (!snapshot.hasChildren()) return;

                for (DataSnapshot snap : snapshot.getChildren()) {
                    boolean isSelected = snap.child("_isSelected").getValue(boolean.class);
                    String productID = snap.child("_productID").getValue(String.class);
                    String productName = snap.child("_productName").getValue(String.class);
                    Integer productPrice = snap.child("_productPrice").getValue(Integer.class);
                    Integer productSize = snap.child("_productSize").getValue(Integer.class);
                    Integer productNumber = snap.child("_productNumber").getValue(Integer.class);
                    String productImageLink = snap.child("_productImageLink").getValue(String.class);
                    String productType = snap.child("_productType").getValue(String.class);
                    String productColor = snap.child("_productColor").getValue(String.class);

                    CartItem cartItem = new CartItem(productID, productImageLink, productName, productPrice,
                            productColor, productSize, productType, productNumber, isSelected);

                    if (isSelected)
                    {
                        productSelected.add(cartItem);
                    }
                }

                dataStatus.HasTheSelectedProduct_CartItem(productSelected.isEmpty());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    /* User */

    public void ReadUser(final DataStatus dataStatus)
    {
        _databaseReference.child("Users").child(this._profileKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = new User();

                if (!snapshot.exists()) return;
                if (!snapshot.hasChildren()) return;

                String name = snapshot.child("_name").getValue(String.class);
                String phoneNumber = snapshot.child("_phoneNumber").getValue(String.class);
                String address = snapshot.child("_address").getValue(String.class);

                user.set_name(name);
                user.set_phoneNumber(phoneNumber);
                user.set_address(address);

                dataStatus.DataIsLoaded_User(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void UpdateUserInformation(User user, final DataStatus dataStatus)
    {
        // OLD
        /*
        _databaseReference.child("CartItems").child(key).setValue(cartItem).addOnSuccessListener(new OnSuccessListener<Void>() {
        @Override
            public void onSuccess(Void unused) {
                dataStatus.DataIsUpdated_CartItem();
            }
        });
        */

        HashMap User = new HashMap();
        User.put("_name", user.get_name());
        User.put("_phoneNumber", user.get_phoneNumber());
        User.put("_address", user.get_address());

        _databaseReference.child("Users").child(this._profileKey).updateChildren(User).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                _databaseReference.child("Users").child(_profileKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = new User();

                        if (!snapshot.exists()) return;
                        if (!snapshot.hasChildren()) return;

                        String name = snapshot.child("_name").getValue(String.class);
                        String phoneNumber = snapshot.child("_phoneNumber").getValue(String.class);
                        String address = snapshot.child("_address").getValue(String.class);

                        user.set_name(name);
                        user.set_phoneNumber(phoneNumber);
                        user.set_address(address);

                        dataStatus.DataIsLoaded_User(user);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

    }


}
