package com.example.jingjie.maptest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.content.Context;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    Button button;
    Button button2;
    Button button3;
    Button button4;
    Spinner spinner1;
    Spinner spinner2;
    long start=0;
    long end=0;
    ArrayAdapter<CharSequence> adapter;
    ArrayAdapter<CharSequence> adapter2;
    String startPos;
    String endPos;
    LocationData locationData=new LocationData();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addListenerOnButton();
        //addListenerOnButton2();
        addListenerOnButton3();
        addListenerOnButton4();

        // listen for database change at child "random"
//        mDatabase.child("random").addValueEventListener(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot)
//            {
//                if (dataSnapshot.exists())
//                {
//                    Toast.makeText(MainActivity.this,dataSnapshot.getValue().toString(),Toast.LENGTH_LONG ).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError)
//            {
//
//            }
//        });

        // listen to all types of hanges to child "Requests"
//        mDatabase.child("Requests").addChildEventListener(new ChildEventListener()
//        {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s)
//            {
//
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s)
//            {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot)
//            {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s)
//            {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError)
//            {
//
//            }
//        });
        final Button firebase = (Button)findViewById(R.id.firebase);
        firebase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {


                mDatabase.child("Users").addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Log.i("Child", snapshot.getValue().toString());
                        }

                        if (dataSnapshot.getChildrenCount() == 0){
                            Log.i("no children", " no children");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });


                // get a single value
//                mDatabase.child("random").addListenerForSingleValueEvent(new ValueEventListener()
//                {
//                    @Override
//                    public void onDataChange(DataSnapshot dataSnapshot)
//                    {
//                        if (dataSnapshot.exists())
//                        {
//                            Toast.makeText(MainActivity.this, dataSnapshot.getValue().toString(), Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(MainActivity.this,"" + dataSnapshot.getChildrenCount(),Toast.LENGTH_LONG).show();
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(DatabaseError databaseError)
//                    {
//
//                    }
//                });


                // set a single value
//                mDatabase.child("random").setValue("Vrezh");

            }
        });



        spinner1 = (Spinner)findViewById(R.id.pickUp_spinner);
        try
        {
            adapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,locationData.getLocName());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?>parent, View view, int i, long l) {
                start=parent.getItemIdAtPosition(i);
                startPos= (String) adapter.getItem(i);
                //Toast.makeText(getBaseContext(), parent.getItemIdAtPosition(i)+" selected "+startPos,
                        //Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        spinner2 = (Spinner)findViewById(R.id.dropOff_spinner);
        try
        {
            adapter2 = new ArrayAdapter(this,android.R.layout.simple_spinner_item,locationData.getLocName());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?>parent, View view, int i, long l) {
                end=parent.getItemIdAtPosition(i);
                endPos= (String) adapter2.getItem(i);
                //Toast.makeText(getBaseContext(), parent.getItemIdAtPosition(i) +" selected "+endPos,
                        //Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });




    }

    public void addListenerOnButton(){
        final Context context = this;
        button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,mapActivity.class);
                i.putExtra("start",startPos);
                i.putExtra("end",endPos);
                startActivity(i);
            }
        });
    }
    /*
    public void addListenerOnButton2(){
        final Context context = this;
        button2 = (Button)findViewById(R.id.sportsEventBtn);

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,EventsActivity.class);
                startActivity(i);
            }
        });
    }
    */
    public void addListenerOnButton3(){
        final Context context = this;
        button3 = (Button)findViewById(R.id.HistorySearchBtn);

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,HistoryActivity.class);
                startActivity(i);
            }
        });
    }
    public void addListenerOnButton4(){
        final Context context = this;
        button4 = (Button)findViewById(R.id.searchByFunction);

        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(MainActivity.this,ClassifySearchActivity.class);
                startActivity(i);
            }
        });
    }
}
