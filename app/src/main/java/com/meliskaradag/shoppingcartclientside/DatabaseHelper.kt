package com.meliskaradag.shoppingcartclientside

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createProductsTable = """
            CREATE TABLE $TABLE_PRODUCTS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_PRICE REAL NOT NULL
            )
        """
        val createCartsTable = """
            CREATE TABLE $TABLE_CARTS (
                $COLUMN_CART_DATE_TIME TEXT NOT NULL,
                $COLUMN_PRODUCT_ID INTEGER NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                FOREIGN KEY($COLUMN_PRODUCT_ID) REFERENCES $TABLE_PRODUCTS($COLUMN_ID)
            )
        """
        db.execSQL(createProductsTable)
        db.execSQL(createCartsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PRODUCTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARTS")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "cart.db"
        const val DATABASE_VERSION = 1

        const val TABLE_PRODUCTS = "products"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_PRICE = "price"

        const val TABLE_CARTS = "carts"
        const val COLUMN_CART_DATE_TIME = "cartDateTime"
        const val COLUMN_PRODUCT_ID = "productId"
        const val COLUMN_CART_PRICE = "price"
    }
}
