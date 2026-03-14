package com.example.mad_project.navigation

object Routes {
    const val LOGIN = "login"
    const val INVENTORY = "inventory"
    const val ADD_ITEM = "add_item"
    const val ITEM_DETAIL = "item_detail/{itemId}"

    fun itemDetail(itemId: String) = "item_detail/$itemId"
}
