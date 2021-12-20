package com.smittysource.inventorymanager;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

/* This class provides both item and header recycler view adapters. Check the
   constructor for how to differentiate
 */
public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    // Create class fields
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ArrayList<ItemModel> mItems;
    private boolean isHeader = false;
    private RecyclerViewClickInterface mRecyclerViewClickInterface;

    // Pass in the data in the ArrayList. Null ArrayList parameter sets header mode
    MyRecyclerViewAdapter(Context context, ArrayList<ItemModel>items,
                          RecyclerViewClickInterface recyclerViewClickInterface) {
        // Connect to recyclerViewClickInterface
        mRecyclerViewClickInterface = recyclerViewClickInterface;

        // Check if header or not
        if(items == null) {
            isHeader = true;
        } else {
            mItems = items;
        }

        // Inflate layout for the recycler view
        this.mInflater = LayoutInflater.from(context);
    }

    // Set view holder for recycler view
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        // Create view
        View view;

        // Inflate header or item view
        if(isHeader) {
            view = mInflater.inflate(R.layout.recyclerview_header, parent, false);
        } else {
            view = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        }

        // Return the view in a view holder
        return new ViewHolder(view);
    }

    // Bind the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Header doesn't have data to load.
        if(!isHeader) {
            holder.myTextDescription.setText(mItems.get(position).getName());
            holder.myTextQuantity.setText(String.valueOf(mItems.get(position).getCount()));
        }
    }

    // Get total number of cells
    @Override
    public int getItemCount() {
        if(!isHeader) {
            // Item list size
            return mItems.size();
        } else {
            // Headers have 1 item
            return 1;
        }
    }


    // Store and recycle views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Define class fields
        TextView myTextQuantity;
        TextView myTextDescription;
        ImageButton myIncrementButton;
        ImageButton myDecrementButton;
        ImageButton myDeleteButton;
        ImageButton myEditButton;
        int position;

        ViewHolder(View itemView) {

            // Call superclass method
            super(itemView);

            // Header has no need to get onclick events
            if(isHeader) {
                return;
            }

            // Assign local variables with UI controls
            myTextDescription = itemView.findViewById(R.id.recyclerViewItemDesc);
            myTextQuantity = itemView.findViewById(R.id.recyclerViewQuantity);
            myEditButton = itemView.findViewById(R.id.recyclerViewEdit);
            myDeleteButton = itemView.findViewById(R.id.recyclerViewDelete);
            myDecrementButton = itemView.findViewById(R.id.recyclerViewDecrement);
            myIncrementButton = itemView.findViewById(R.id.recyclerViewIncrement);

            // Default on click
            itemView.setOnClickListener((v) -> {
                position = getBindingAdapterPosition();
            });

            // Respond to increment icon clicks
            itemView.findViewById(R.id.recyclerViewIncrement)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use interface to pass position and icon that was clicked back to activity
                    mRecyclerViewClickInterface.onIncrementClick(getBindingAdapterPosition());
                }
            });

            // Respond to delete icon clicks
            itemView.findViewById(R.id.recyclerViewDelete)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use interface to pass position and icon that was clicked back to activity
                    mRecyclerViewClickInterface.onDeleteClick(getBindingAdapterPosition());
                }
            });

            // Respond to edit icon clicks
            itemView.findViewById(R.id.recyclerViewEdit)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use interface to pass position and icon that was clicked back to activity
                    mRecyclerViewClickInterface.onEditClick(getBindingAdapterPosition());
                }
            });

            // Respond to decrement icon clicks
            itemView.findViewById(R.id.recyclerViewDecrement)
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Use interface to pass position and icon that was clicked back to activity
                    mRecyclerViewClickInterface.onDecrementClick(getBindingAdapterPosition());
                }
            });
        }

        // Override default onclick
        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener
                    .onItemClick(view, getBindingAdapterPosition());
        }
    }

    // Unused method that retrieves item name at position in mItems
    String getItem(int position) {

        if(!isHeader) {
            return mItems.get(position).toString();
        } else {
            return "null";
        }
    }

    // Allow click events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // Parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}