package com.example.coook

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_list.*


class ListFragment : Fragment() {


    var cookNameList=ArrayList<String>()
    var cookIdList=ArrayList<Int>()
    private lateinit var listAdapter:ListRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)







    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        listAdapter= ListRecyclerAdapter(cookNameList,cookIdList)
        recyclerView.layoutManager=LinearLayoutManager(context)
        recyclerView.adapter=listAdapter





        sqlGetData()
    }

    fun sqlGetData(){

        try {

            activity?.let {

                val db=it.openOrCreateDatabase("Cook",Context.MODE_PRIVATE,null)
                //db.execSQL("SELECT * FROM cook")
                val cursor=db.rawQuery("SELECT * FROM cook",null)
                val cookNameIndex=cursor.getColumnIndex("cookName")
                val cookIdIndex=cursor.getColumnIndex("id")

                cookIdList.clear()
                cookNameList.clear()
                while (cursor.moveToNext()){

                    /*println(cursor.getString(cookNameIndex))
                    println(cursor.getInt(cookIdIndex))*/

                    cookIdList.add(cursor.getInt(cookIdIndex))
                    cookNameList.add(cursor.getString(cookNameIndex))



                }
                listAdapter.notifyDataSetChanged()
                cursor.close()

            }



        }catch (e:Exception){

            e.printStackTrace()
        }

    }



}