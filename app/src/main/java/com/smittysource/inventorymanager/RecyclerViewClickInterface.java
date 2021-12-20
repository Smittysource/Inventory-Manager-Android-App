package com.smittysource.inventorymanager;

/*This file defines the interface to connect the RecyclerView with the InventoryListActivity
  to allow the InventoryListActivity to know when icons are clicked in the RecyclerView
*/
public interface RecyclerViewClickInterface {
    void onDeleteClick(int position);
    void onEditClick(int position);
    void onIncrementClick(int position);
    void onDecrementClick(int position);
}
