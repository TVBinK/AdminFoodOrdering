package com.example.adminfoodordering

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.adminfoodordering.model.AllMenu
import com.example.adminfoodordering.databinding.ActivityAddItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class AddItemActivity : AppCompatActivity() {
    private val binding : ActivityAddItemBinding by lazy {
        ActivityAddItemBinding.inflate(layoutInflater)
    }
    private lateinit var foodName:String
    private lateinit var foodPrice:String
    private lateinit var foodDescription:String
    private var foodImage: Uri? = null
    //firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item)
        setContentView(binding.root)
        //khởi tạo firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.SelectImage.setOnClickListener {
            pickimage.launch("image/*")
        }
        binding.btnBack.setOnClickListener {
            finish()
        }
        binding.btnAddItem.setOnClickListener {
            foodName = binding.editTextName.text.toString().trim()
            foodPrice = binding.editTextPrice.text.toString().trim()
            foodDescription = binding.editTextDescription.text.toString().trim()
            if(foodName.isNotEmpty() && foodPrice.isNotEmpty() && foodDescription.isNotEmpty() && foodImage != null){
                val menuRef = database.getReference("menu")
                val newItemKey = menuRef.push().key
                if(foodImage!=null){
                    val storageRef = FirebaseStorage.getInstance().reference.child("FoodImage/${newItemKey}.jpg")
                    val uploadTask = storageRef.putFile(foodImage!!)
                    uploadTask.addOnSuccessListener{
                        storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                            val newMenu = AllMenu(
                                foodName,
                                foodPrice,
                                foodDescription,
                                downloadUrl.toString()
                            )
                            newItemKey?.let {
                                key ->
                                menuRef.child(key).setValue(newMenu).addOnSuccessListener {
                                    binding.editTextName.text?.clear()
                                    binding.editTextPrice.text?.clear()
                                    binding.editTextDescription.text?.clear()
                                    binding.Image.setImageResource(R.drawable.add_image)
                                    Toast.makeText(this, "Upload Item Success", Toast.LENGTH_SHORT).show()
                                }
                                    .addOnFailureListener {
                                        Toast.makeText(this,"Upload Item Failed",Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                            .addOnFailureListener {
                                Toast.makeText(this,"Upload Item Failed",Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                else{
                    Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show()
                }
            }
        }

}
    private val pickimage = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
        if(uri != null){
            binding.Image.setImageURI(uri)
            foodImage = uri
        }
    }
}
