package com.example.testing1.data

import com.powersync.db.schema.Column
import com.powersync.db.schema.Schema
import com.powersync.db.schema.Table

val schema = Schema(
    Table(
        name = "coffee",
        columns = listOf(
            Column.text("name"),
            Column.text("description"),
            Column.text("category"),
            Column.text("price"),
            Column.text("image_url"),
            Column.text("created_at"),
            Column.text("updated_at"),
            Column.text("name_ja"),
            Column.text("description_ja"),
            Column.text("name_de"),
            Column.text("description_de"),
            Column.text("name_ru"),
            Column.text("description_ru"),
            Column.text("name_pt"),
            Column.text("description_pt"),
            Column.text("name_fr"),
            Column.text("description_fr"),
            Column.text("name_ar"),
            Column.text("description_ar"),
            Column.text("name_es"),
            Column.text("description_es"),
            Column.text("name_zh"),
            Column.text("description_zh"),
            Column.text("name_it"),
            Column.text("description_it")
        )
    ),
    Table(
        name = "discounts",
        columns = listOf(
            Column.text("code"),
            Column.text("description"),
            Column.text("type"),
            Column.text("value"),
            Column.text("min_order_amount"),
            Column.text("max_discount_amount"),
            Column.integer("active"),
            Column.text("created_at")
        )
    ),
    Table(
        name = "addresses",
        columns = listOf(
            Column.text("user_id"),
            Column.text("tag"),
            Column.text("full_address"),
            Column.integer("is_default"),
            Column.integer("last_used_timestamp")
        )
    ),
    Table(
        name = "favorites",
        columns = listOf(
            Column.text("user_id"),
            Column.text("coffee_id")
        )
    ),
    Table(
        name = "cart",
        columns = listOf(
            Column.text("user_id"),
            Column.text("coffee_id"),
            Column.integer("quantity"),
            Column.text("size")
        )
    ),
    Table(
        name = "search_history",
        columns = listOf(
            Column.text("user_id"),
            Column.text("query"),
            Column.integer("result_count"),
            Column.integer("timestamp")
        )
    ),
    Table(
        name = "orders",
        columns = listOf(
            Column.text("user_id"),
            Column.text("total_price"),
            Column.text("status"),
            Column.text("snapshot_address"),
            Column.text("created_at")
        )
    ),
    Table(
        name = "order_items",
        columns = listOf(
            Column.text("order_id"),
            Column.text("coffee_name"),
            Column.integer("quantity"),
            Column.text("size"),
            Column.text("snapshot_price")
        )
    )
)