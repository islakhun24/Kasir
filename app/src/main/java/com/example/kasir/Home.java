package com.example.kasir;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Home extends AppCompatActivity {

    private static final String URL_READ = "https://geprekrame.doktersoftware.my.id/getPesanan.php";
    TextView namakasir;
    List<Pesanan> pesananList;
    private ShimmerRecyclerView recyclerView;
    private LinearLayoutManager llm;
    private DividerItemDecoration did;
    private RecyclerView.Adapter adapter;
    ImageView btnLogout;
    SwipeRefreshLayout mSwipeRefreshLayout;
    public static final String EXTRA_TYPE = "type";

    SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        namakasir = findViewById(R.id.tvNamaKasir);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerView = (ShimmerRecyclerView) findViewById(R.id.recyclerView);

        pesananList = new ArrayList<>();
        adapter = new AdapterPesanan(this.getApplicationContext(),pesananList);

        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        did = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(llm);
        recyclerView.addItemDecoration(did);

        recyclerView.setAdapter(adapter);
        recyclerView.showShimmerAdapter();
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.swifeRefresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Berhenti berputar/refreshing
                mSwipeRefreshLayout.setRefreshing(false);
                recyclerView.showShimmerAdapter();
                // fungsi-fungsi lain yang dijalankan saat refresh berhenti
                recyclerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadpesanan();
                    }
                }, 2000);
            }
        });

        Intent intent = getIntent();
        String extraNmae = intent.getStringExtra("email");

        namakasir.setText(extraNmae);
        final String name = namakasir.getText().toString();




        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this.getApplicationContext(),
                new RecyclerItemClickListener.OnItemClickListener(){

            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(Home.this, PesananDetailActivity.class);
                intent.putExtra("atasnama",pesananList.get(position).atasnama);
                intent.putExtra("name",name);
                startActivity(intent);

            }
        }));

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        /*sharedPrefManager.saveSPBoolean(SharedPrefManager.SP_SUDAH_LOGIN, false);*/
                        startActivity(new Intent(getBaseContext(),Login.class));
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setTitle("Apakah anda Yakin Inging Logout akun ?");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();


            }
        });
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadpesanan();
            }
        }, 3000);
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Tekan Button logout untuk keluar dari akun !", Toast.LENGTH_SHORT).show();
    }

    private void loadpesanan(){

        pesananList.clear();
        StringRequest jsonObjectRequest = new StringRequest(Request.Method.GET,URL_READ,
                new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Aaaa",response);
                try {
                    JSONObject jsonObject = new JSONObject(response);

                    int success = jsonObject.getInt("success");
                    if (success == 1) {
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                JSONObject object = jsonArray.getJSONObject(i);
                                Pesanan pesanan = new Pesanan();
                                pesanan.setAtasnama(object.getString("atasnama"));
                                pesanan.setNoMeja(object.getString("no_meja"));
                                pesananList.add(pesanan);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mSwipeRefreshLayout.setRefreshing(false);
                }
                adapter.notifyDataSetChanged();
                recyclerView.hideShimmerAdapter();

            }

        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());
                Toast.makeText(Home.this,""+error.toString(),Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(jsonObjectRequest);
    }

}
