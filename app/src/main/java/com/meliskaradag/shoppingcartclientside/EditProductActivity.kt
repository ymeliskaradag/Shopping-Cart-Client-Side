package com.meliskaradag.shoppingcartclientside

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.database.Cursor
import android.widget.SimpleCursorAdapter

class EditProductActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var productListView: ListView
    private lateinit var productNameEditText: EditText
    private lateinit var productPriceEditText: EditText
    private var selectedProductId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        databaseHelper = DatabaseHelper(this)

        val searchProductEditText = findViewById<EditText>(R.id.editTextSearchProduct)
        val searchButton = findViewById<Button>(R.id.buttonSearchProduct)
        productListView = findViewById(R.id.listViewProducts)
        productNameEditText = findViewById(R.id.editTextProductNameEdit)
        productPriceEditText = findViewById(R.id.editTextProductPriceEdit)
        val updateButton = findViewById<Button>(R.id.buttonUpdateProduct)
        val deleteButton = findViewById<Button>(R.id.buttonDeleteProduct)

        searchButton.setOnClickListener {
            val searchText = searchProductEditText.text.toString()
            searchProducts(searchText)
        }

        updateButton.setOnClickListener {
            updateProduct()
        }

        deleteButton.setOnClickListener {
            deleteProduct()
        }

        productListView.setOnItemClickListener { _, _, position, id ->
            val cursor = productListView.adapter.getItem(position) as Cursor
            selectedProductId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
            productNameEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME)))
            productPriceEditText.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE)))
        }
    }

    private fun searchProducts(searchText: String) {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE ${DatabaseHelper.COLUMN_NAME} LIKE ?", arrayOf("%$searchText%"))
        val adapter = SimpleCursorAdapter(
            this,
            android.R.layout.simple_list_item_2,
            cursor,
            arrayOf(DatabaseHelper.COLUMN_NAME, DatabaseHelper.COLUMN_PRICE),
            intArrayOf(android.R.id.text1, android.R.id.text2),
            0
        )
        productListView.adapter = adapter
    }

    private fun updateProduct() {
        val db = databaseHelper.writableDatabase
        val productName = productNameEditText.text.toString()
        val productPrice = productPriceEditText.text.toString().toDoubleOrNull()

        if (selectedProductId != null && productName.isNotBlank() && productPrice != null && productPrice in 0.01..99.99) {
            val updateSQL = """
                UPDATE ${DatabaseHelper.TABLE_PRODUCTS} 
                SET ${DatabaseHelper.COLUMN_NAME} = ?, ${DatabaseHelper.COLUMN_PRICE} = ? 
                WHERE ${DatabaseHelper.COLUMN_ID} = ?
            """
            db.execSQL(updateSQL, arrayOf(productName, productPrice, selectedProductId))
            Toast.makeText(this, "Product updated successfully!", Toast.LENGTH_SHORT).show()
            searchProducts("")  // Refresh the list
        } else {
            Toast.makeText(this, "Please select a valid product and enter valid details!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteProduct() {
        val db = databaseHelper.writableDatabase

        if (selectedProductId != null) {
            val deleteSQL = "DELETE FROM ${DatabaseHelper.TABLE_PRODUCTS} WHERE ${DatabaseHelper.COLUMN_ID} = ?"
            db.execSQL(deleteSQL, arrayOf(selectedProductId))
            Toast.makeText(this, "Product deleted successfully!", Toast.LENGTH_SHORT).show()
            searchProducts("")  // Refresh the list
            productNameEditText.text.clear()
            productPriceEditText.text.clear()
        } else {
            Toast.makeText(this, "Please select a product to delete!", Toast.LENGTH_SHORT).show()
        }
    }
}
