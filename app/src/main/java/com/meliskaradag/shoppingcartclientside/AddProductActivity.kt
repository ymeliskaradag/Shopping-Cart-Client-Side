package com.meliskaradag.shoppingcartclientside

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddProductActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        databaseHelper = DatabaseHelper(this)

        val productNameEditText = findViewById<EditText>(R.id.editTextProductName)
        val productPriceEditText = findViewById<EditText>(R.id.editTextProductPrice)
        val addProductButton = findViewById<Button>(R.id.buttonAddProduct)

        addProductButton.setOnClickListener {
            val productName = productNameEditText.text.toString()
            val productPrice = productPriceEditText.text.toString().toDoubleOrNull()

            if (productName.isNotBlank() && productPrice != null && productPrice in 0.01..99.99) {
                val db = databaseHelper.writableDatabase

                val cursor = db.rawQuery("SELECT MAX(${DatabaseHelper.COLUMN_ID}) FROM ${DatabaseHelper.TABLE_PRODUCTS}", null)
                cursor.moveToFirst()
                val newId = (cursor.getInt(0) + 1)
                cursor.close()

                val insertSQL = """
                    INSERT INTO ${DatabaseHelper.TABLE_PRODUCTS} (${DatabaseHelper.COLUMN_ID}, ${DatabaseHelper.COLUMN_NAME}, ${DatabaseHelper.COLUMN_PRICE})
                    VALUES (?, ?, ?)
                """
                db.execSQL(insertSQL, arrayOf(newId, productName, productPrice))

                Toast.makeText(this, "Product added successfully!", Toast.LENGTH_SHORT).show()
                productNameEditText.text.clear()
                productPriceEditText.text.clear()
            } else {
                Toast.makeText(this, "Please enter valid product name and price!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
