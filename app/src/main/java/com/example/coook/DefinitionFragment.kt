package com.example.coook

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import kotlinx.android.synthetic.main.fragment_defination.*
import java.io.ByteArrayOutputStream


class DefinitionFragment : Fragment() {
   var chosenImage: Uri? =null
    var chosenBitmap:Bitmap?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_defination, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button.setOnClickListener { 

            save(it)
        }

        imageView.setOnClickListener{

            imageChose(it)

        }
        arguments?.let {

            var comingInformation=DefinitionFragmentArgs.fromBundle(it).information
            if(comingInformation.equals("comingfrommenu")){
                //yeni yemek ekleme

                nameCookText.setText("")
                cookMaterialText.setText("")
                button.visibility=View.VISIBLE
                val image=BitmapFactory.decodeResource(context?.resources,R.drawable.ck)
                imageView.setImageBitmap(image)


            }
            else{

                //daha önce oluşturulan yemek
                button.visibility=View.INVISIBLE

                val chosenId=DefinitionFragmentArgs.fromBundle(it).id

                context?.let {

                    try {

                        val db=it.openOrCreateDatabase("Cook",Context.MODE_PRIVATE,null)
                        val cursor=db.rawQuery("SELECT* FROM cook WHERE id=?", arrayOf(chosenId.toString()))

                        val cookNameIndex=cursor.getColumnIndex("cookName")
                        val cookMaterialIndex=cursor.getColumnIndex("cookMaterial")
                        val imageIndex=cursor.getColumnIndex("image")
                        while (cursor.moveToNext()){

                            nameCookText.setText(cursor.getString(cookNameIndex))
                            cookMaterialText.setText(cursor.getString(cookMaterialIndex))

                            //gorsel bayt dizisi şeklinde kaydedildi

                            val byteImageArray=cursor.getBlob(imageIndex)
                            val bitmap=BitmapFactory.decodeByteArray(byteImageArray,0,byteImageArray.size)

                            imageView.setImageBitmap(bitmap)
//asd


                        }

                        cursor.close()

                    }catch (e:Exception){

                        e.printStackTrace()
                    }




                }



            }
        }

    }

    fun save(view:View){

        val cookNameText=nameCookText.text.toString()
        val cookMaterialText=cookMaterialText.text.toString()
        if(chosenBitmap!=null){

            val smallBitmap=smallBitmapCreate(chosenBitmap!!,300)
            //bitmapi veriye kaydedeceğiz
            val outputStream=ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArrays=outputStream.toByteArray()


            try {

                context?.let {

                    val db=it.openOrCreateDatabase("Cook",Context.MODE_PRIVATE,null)
                    db.execSQL("CREATE TABLE IF NOT EXISTS cook (id INTEGER PRIMARY KEY,cookName VARCHAR,cookMaterial VARCHAR,image BLOB )")

                    val sqlString="INSERT INTO cook (cookName,cookMaterial,image) VALUES(?,?,?)"
                    val statement=db.compileStatement(sqlString)
                    statement.bindString(1,cookNameText)
                    statement.bindString(2,cookMaterialText)
                    statement.bindBlob(3,byteArrays)
                    statement.execute()


                }






            }catch (e:Exception){


                e.printStackTrace()

            }


            val action=DefinitionFragmentDirections.actionDefinitionFragmentToListFragment()
            Navigation.findNavController(view).navigate(action)

        }





    }

    fun imageChose(view: View){

        activity?.let {

            if(ContextCompat.checkSelfPermission(it.applicationContext,Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){

                //izin iste
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }
            else{
                //istemeden galery git
                val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)

            }

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode==1){

            if(grantResults.size>0 &&grantResults[0]==PackageManager.PERMISSION_GRANTED){

                //izin alındı

                val galleryIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galleryIntent,2)


            }


        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode==2 && resultCode==Activity.RESULT_OK &&data!=null){


            chosenImage=data.data

            try {
                context?.let {



                    if(chosenImage!=null){

                        if(Build.VERSION.SDK_INT>=28){

                            val source=  ImageDecoder.createSource(it.contentResolver,chosenImage!!)
                            chosenBitmap=ImageDecoder.decodeBitmap(source)
                            imageView.setImageBitmap(chosenBitmap)

                        }
                        else{



                            chosenBitmap=MediaStore.Images.Media.getBitmap(it.contentResolver,chosenImage)
                            imageView.setImageBitmap(chosenBitmap)

                        }



                    }


                }






            }catch (e:Exception){


                e.printStackTrace()

            }

        }


        super.onActivityResult(requestCode, resultCode, data)
    }

    fun smallBitmapCreate(chosenBitmap:Bitmap,maxSize:Int):Bitmap{

        var width=chosenBitmap.width
        var height=chosenBitmap.height

        val bitmapRate:Double= width.toDouble()/height.toDouble()
        if(bitmapRate>1){

            //görsel yatay
            width=maxSize
            val cutterHeight= width/bitmapRate
            height=cutterHeight.toInt()


        }
        else{

            height=maxSize
            val cutterWidth=height*bitmapRate
            width=cutterWidth.toInt()


        }

        return Bitmap.createScaledBitmap(chosenBitmap, width,height,true)


    }


}