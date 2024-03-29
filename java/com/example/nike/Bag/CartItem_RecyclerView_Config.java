package com.example.nike.Bag;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nike.FirebaseDataHelper;
import com.example.nike.Product.I_OnItemClickListener;
import com.example.nike.Product.Product;
import com.example.nike.Product.ProductDetails_Activity;
import com.example.nike.Profile.User;
import com.example.nike.R;
import com.example.nike.Tab.ENUM_ActivityType;
import com.example.nike.Tab.STR_IntentKey;
import com.example.nike.Tab.Test_MainActivity;

import java.util.ArrayList;

public class CartItem_RecyclerView_Config {
    private Context _context;
    private CartItemAdapter _cartItemAdapter;

    public void setConfig(RecyclerView recyclerView, Context context, ArrayList<CartItem> _cartItemList, ArrayList<String> _keys)
    {
        this._context = context;
        this._cartItemAdapter = new CartItemAdapter(_cartItemList, _keys);

        this._cartItemAdapter.set_OnItemClickListener((view, position) ->
        {
            Toast.makeText(recyclerView.getContext(), "Item clicked at position " + position, Toast.LENGTH_SHORT).show();

            Intent shopIntent = new Intent(context, ProductDetails_Activity.class);
            shopIntent.putExtra(STR_IntentKey.ProductID, _cartItemList.get(position).get_productID());
            shopIntent.putExtra(STR_IntentKey.ActivityType, ENUM_ActivityType.Bag);
            context.startActivity(shopIntent);
        });

        recyclerView.setAdapter(this._cartItemAdapter);
    }

    public class CartItemAdapter extends RecyclerView.Adapter<CartItemView>
    {
        private I_OnItemClickListener _listener;

        public void set_OnItemClickListener(I_OnItemClickListener _listener)
        {
            this._listener = _listener;
        }

        private ArrayList<CartItem> _cartItemList;
        private ArrayList<String> _keys;

        public CartItemAdapter(ArrayList<CartItem> _cartItemList, ArrayList<String> keys) {
            this._cartItemList = _cartItemList;
            this._keys = keys;
        }

        @NonNull
        @Override
        public CartItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CartItemView(parent, this._listener);
        }

        @Override
        public void onBindViewHolder(@NonNull CartItemView holder, int position) {
            holder.Bind(_cartItemList.get(position), _keys.get(position));
        }

        @Override
        public int getItemCount() {
            return this._cartItemList.size();
        }
    }

    public class CartItemView extends RecyclerView.ViewHolder
    {
        private ImageView _ivw_productImage_Bag;
        private TextView _tvw_productName_Bag;
        private TextView _tvw_productType_Bag;
        private TextView _tvw_productColor_Bag;
        private TextView _tvw_productSize_Bag;
        private Button _btn_minusOne_Bag;
        private TextView _tvw_productNumber_Bag;
        private Button _btn_plusOne_Bag;
        private TextView _tvw_productPrice_Bag;
        private CheckBox cbx_isSelected_CartItem;
        private ImageView ivw_deleteProduct_Bag;
        private String _key;

        public CartItemView(ViewGroup parent, final I_OnItemClickListener listener) {
            super(LayoutInflater.from(_context)
                    .inflate(R.layout.cart_item, parent, false));

            this.cbx_isSelected_CartItem = itemView.findViewById(R.id.cbx_isSelected_CartItem);
            this._ivw_productImage_Bag = itemView.findViewById(R.id.ivw_productImage_Bag);
            this._tvw_productName_Bag = itemView.findViewById(R.id.tvw_productName_Bag);
            this._tvw_productType_Bag = itemView.findViewById(R.id.tvw_productType_Bag);
            this._tvw_productColor_Bag = itemView.findViewById(R.id.tvw_productColor_Bag);
            this._tvw_productSize_Bag = itemView.findViewById(R.id.tvw_productSize_Bag);
            this._btn_minusOne_Bag = itemView.findViewById(R.id.btn_minusOne_Bag);
            this._tvw_productNumber_Bag = itemView.findViewById(R.id.tvw_productNumber_Bag);
            this._btn_plusOne_Bag = itemView.findViewById(R.id.btn_plusOne_Bag);
            this._tvw_productPrice_Bag = itemView.findViewById(R.id.tvw_productPrice_Bag);
            this.ivw_deleteProduct_Bag = itemView.findViewById(R.id.ivw_deleteProduct_Bag);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });
        }

        public void Bind(CartItem cartItem, String key)
        {
            Glide.with(this._ivw_productImage_Bag.getContext()).load(cartItem.get_productImageLink()).into(this._ivw_productImage_Bag);
            this._tvw_productName_Bag.setText(cartItem.get_productName());
            this._tvw_productType_Bag.setText(cartItem.get_productType());
            this._tvw_productColor_Bag.setText(cartItem.get_productColor());
            this._tvw_productSize_Bag.setText(String.valueOf(cartItem.get_productSize()));
            this._tvw_productNumber_Bag.setText(String.valueOf(cartItem.get_productNumber()));
            this.cbx_isSelected_CartItem.setChecked(cartItem.get_isSelected());

            this.cbx_isSelected_CartItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cartItem.set_isSelected(!cartItem.get_isSelected());
                    UpdateProductNumber(cartItem, key);
                }
            });

            this._btn_minusOne_Bag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer productNumber = cartItem.get_productNumber();
                     if (productNumber <= 1) return;
                    cartItem.set_productNumber(productNumber - 1);
                    UpdateProductNumber(cartItem, key);
                }
            });

            this._btn_plusOne_Bag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Integer productNumber = cartItem.get_productNumber();
                    cartItem.set_productNumber(productNumber + 1);
                    UpdateProductNumber(cartItem, key);
                }
            });

            this.ivw_deleteProduct_Bag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new FirebaseDataHelper().DeleteProductToCart(key, new FirebaseDataHelper.DataStatus() {
                        @Override
                        public void DataIsLoaded_Product(ArrayList<Product> products, ArrayList<String> keys) {}
                        @Override
                        public void DataIsInserted_Product() {}
                        @Override
                        public void DataIsUpdated_Product() {}
                        @Override
                        public void DataIsDeleted_Product() {}
                        @Override
                        public void DataIsLoaded_CartItem(ArrayList<CartItem> cartItems, ArrayList<CartItem> _cartItemSelected, ArrayList<String> keys) {}
                        @Override
                        public void DataIsInserted_CartItem() {}
                        @Override
                        public void DataIsUpdated_CartItem(ArrayList<CartItem> cartItemSelected) {}
                        @Override
                        public void DataIsDeleted_CartItem(ArrayList<CartItem> cartItemSelected) {
                            Toast.makeText(_context,"Delete_CartItem", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void HasTheSelectedProduct_CartItem(boolean isEmpty) {

                        }

                        @Override
                        public void DataIsLoaded_User(User user) {

                        }
                    });
                }
            });

            /* Product Price */
            int i_productPrice = cartItem.get_productPrice();
            String str_productPrice =  this.ConvertNumberToString_productPrice(i_productPrice);
            this._tvw_productPrice_Bag.setText(str_productPrice);

            this._key = key;
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

        private void UpdateProductNumber(CartItem cartItem, String key)
        {
                new FirebaseDataHelper().UpdateProductToCart(key, cartItem, new FirebaseDataHelper.DataStatus() {
                @Override
                public void DataIsLoaded_Product(ArrayList<Product> products, ArrayList<String> keys) { }
                @Override
                public void DataIsInserted_Product() { }
                @Override
                public void DataIsUpdated_Product() { }
                @Override
                public void DataIsDeleted_Product() { }
                @Override
                public void DataIsLoaded_CartItem(ArrayList<CartItem> cartItems, ArrayList<CartItem> _cartItemSelected, ArrayList<String> keys) {}
                @Override
                public void DataIsInserted_CartItem() {}
                @Override
                public void DataIsUpdated_CartItem(ArrayList<CartItem> cartItemSelected) {
                    Toast.makeText(_context, "Updated_CartItem", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void DataIsDeleted_CartItem(ArrayList<CartItem> cartItem) {}

                    @Override
                    public void HasTheSelectedProduct_CartItem(boolean isEmpty) {

                    }

                    @Override
                    public void DataIsLoaded_User(User user) {

                    }
                });

            _tvw_productNumber_Bag.setText(String.valueOf(cartItem.get_productNumber()));
        }


    }

}
