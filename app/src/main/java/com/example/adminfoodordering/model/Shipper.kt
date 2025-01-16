package com.example.adminfoodordering.model

data class Shipper(
    val shipperID: String,
    val name: String,
    val phone: String,
    val latitute: String,
    val longitute: String,
    val timestamp: String
){
    //contructor
    constructor() : this("", "", "", "", "", "")
}
