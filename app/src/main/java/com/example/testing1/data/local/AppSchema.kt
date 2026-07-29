package com.example.testing1.data.local

import com.powersync.db.schema.Column
import com.powersync.db.schema.ColumnType
import com.powersync.db.schema.Schema
import com.powersync.db.schema.Table

val AppSchema = Schema(
    Table(
        name = "coffee",
        columns = listOf(
            Column("name", ColumnType.TEXT),
            Column("description", ColumnType.TEXT),
            Column("category", ColumnType.TEXT),
            Column("price", ColumnType.REAL),
            Column("image_url", ColumnType.TEXT),
            Column("created_at", ColumnType.TEXT),
            Column("updated_at", ColumnType.TEXT),
            Column("name_ja", ColumnType.TEXT),
            Column("description_ja", ColumnType.TEXT),
            Column("name_de", ColumnType.TEXT),
            Column("description_de", ColumnType.TEXT),
            Column("name_ru", ColumnType.TEXT),
            Column("description_ru", ColumnType.TEXT),
            Column("name_pt", ColumnType.TEXT),
            Column("description_pt", ColumnType.TEXT),
            Column("name_fr", ColumnType.TEXT),
            Column("description_fr", ColumnType.TEXT),
            Column("name_ar", ColumnType.TEXT),
            Column("description_ar", ColumnType.TEXT),
            Column("name_es", ColumnType.TEXT),
            Column("description_es", ColumnType.TEXT),
            Column("name_zh", ColumnType.TEXT),
            Column("description_zh", ColumnType.TEXT),
            Column("name_it", ColumnType.TEXT),
            Column("description_it", ColumnType.TEXT)
        )
    ),
    Table(
        name = "addresses",
        columns = listOf(
            Column("user_id", ColumnType.TEXT),
            Column("tag", ColumnType.TEXT),
            Column("full_address", ColumnType.TEXT),
            Column("is_default", ColumnType.INTEGER),
            Column("last_used_timestamp", ColumnType.INTEGER)
        )
    ),
    Table(
        name = "favorites",
        columns = listOf(
            Column("user_id", ColumnType.TEXT),
            Column("coffee_id", ColumnType.TEXT)
        )
    ),
    Table(
        name = "cart",
        columns = listOf(
            Column("user_id", ColumnType.TEXT),
            Column("coffee_id", ColumnType.TEXT),
            Column("quantity", ColumnType.INTEGER),
            Column("size", ColumnType.TEXT)
        )
    ),
    Table(
        name = "discounts",
        columns = listOf(
            Column("code", ColumnType.TEXT),
            Column("description", ColumnType.TEXT),
            Column("type", ColumnType.TEXT),
            Column("value", ColumnType.REAL),
            Column("min_order_amount", ColumnType.REAL),
            Column("max_discount_amount", ColumnType.REAL),
            Column("active", ColumnType.INTEGER),
            Column("created_at", ColumnType.TEXT)
        )
    ),
    Table(
        name = "orders",
        columns = listOf(
            Column("user_id", ColumnType.TEXT),
            Column("total_price", ColumnType.REAL),
            Column("status", ColumnType.TEXT),
            Column("snapshot_address", ColumnType.TEXT),
            Column("created_at", ColumnType.TEXT)
        )
    ),
    Table(
        name = "order_items",
        columns = listOf(
            Column("order_id", ColumnType.TEXT),
            Column("coffee_name", ColumnType.TEXT),
            Column("quantity", ColumnType.INTEGER),
            Column("size", ColumnType.TEXT),
            Column("snapshot_price", ColumnType.REAL)
        )
    ),
    Table(
        name = "search_history",
        columns = listOf(
            Column("user_id", ColumnType.TEXT),
            Column("query", ColumnType.TEXT),
            Column("result_count", ColumnType.INTEGER),
            Column("timestamp", ColumnType.INTEGER)
        )
    )
)