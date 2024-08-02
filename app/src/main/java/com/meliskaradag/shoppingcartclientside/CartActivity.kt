import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meliskaradag.shoppingcartclientside.DatabaseHelper
import okhttp3.*
import java.io.IOException

data class Product(
    val id: Int,
    val price: Double,
    var isSold: Boolean = false
) {
    override fun toString(): String {
        return "Product ID: $id, Price: $$price"
    }
}

data class RequestData(val cardLimit: Double, val selectedProductList: List<Product>)
data class Response(val result: String) // Yanıt verinizi temsil eden sınıf

class CartActivity : AppCompatActivity() {

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var productListView: ListView
    private lateinit var productAdapter: ArrayAdapter<Product>
    private var productList: MutableList<Product> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        databaseHelper = DatabaseHelper(this)
        productListView = findViewById(R.id.listViewCartProducts)
        val loadProductsButton = findViewById<Button>(R.id.buttonLoadProducts)
        val submitCartButton = findViewById<Button>(R.id.buttonSubmitCart)
        val cardLimitEditText = findViewById<EditText>(R.id.editTextCardLimit)

        loadProductsButton.setOnClickListener {
            loadProducts()
        }

        submitCartButton.setOnClickListener {
            val cardLimit = cardLimitEditText.text.toString().toDoubleOrNull()
            if (cardLimit != null) {
                submitCart(cardLimit)
            } else {
                Toast.makeText(this, "Please enter a valid card limit", Toast.LENGTH_SHORT).show()
            }
        }

        productAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, productList)
        productListView.adapter = productAdapter
        productListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
    }

    private fun loadProducts() {
        val db = databaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_PRODUCTS}", null)
        productList.clear()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID))
                val price = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRICE))
                productList.add(Product(id, price))
            } while (cursor.moveToNext())
        }
        cursor.close()
        productAdapter.notifyDataSetChanged()
    }

    private fun submitCart(cardLimit: Double) {
        val selectedProducts = productListView.checkedItemPositions
        val selectedProductList = mutableListOf<Product>()

        for (i in 0 until selectedProducts.size()) {
            val key = selectedProducts.keyAt(i)
            if (selectedProducts.valueAt(i)) {
                val product = productList[key]
                // Ürün seçilmiş ve isSold durumu güncellenmemişse listeye ekleyin
                if (!product.isSold) {
                    selectedProductList.add(product)
                }
            }
        }

        if (selectedProductList.isEmpty()) {
            Toast.makeText(this, "No products selected", Toast.LENGTH_SHORT).show()
            return
        }

        val requestData = RequestData(cardLimit, selectedProductList)
        val gson = Gson()
        val jsonRequest = gson.toJson(requestData)

        val client = OkHttpClient()
        val mediaType = MediaType.parse("application/json")
        val body = RequestBody.create(mediaType, jsonRequest)
        val request = okhttp3.Request.Builder()
            .url("http://your_server_ip:port")  // Sunucu adresinizi ve portunuzu buraya ekleyin
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@CartActivity, "Failed to connect to server", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body()?.string()
                val responseType = object : TypeToken<Response>() {}.type
                val responseObj = gson.fromJson<Response>(responseBody, responseType)
                runOnUiThread {
                    // Yanıtı işleyin, örneğin ürünlerin sepete eklenmesi gibi
                    Toast.makeText(this@CartActivity, "Cart submitted successfully", Toast.LENGTH_SHORT).show()

                    // Satılan ürünlerin durumunu güncelleyin
                    updateSoldProducts(selectedProductList)
                }
            }
        })
    }

    private fun updateSoldProducts(selectedProducts: List<Product>) {
        // Satılan ürünlerin durumunu güncelleyin
        for (product in selectedProducts) {
            product.isSold = true
        }
        // Güncellenmiş ürünlerin veritabanına kaydedilmesi gerekebilir
        // Bu işlem için uygun veritabanı güncellemelerini yapmanız gerekebilir
    }
}

