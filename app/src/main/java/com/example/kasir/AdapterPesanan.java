package com.example.kasir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterPesanan extends RecyclerView.Adapter<AdapterPesanan.FoodViewHolder>{

    private Context mCtx;
    private List<Pesanan> pesananList;

    public AdapterPesanan(Context mCtx,List<Pesanan>pesananList){
        this.mCtx = mCtx;
        this.pesananList = pesananList;
    }

    @NonNull
    @Override
    public AdapterPesanan.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mCtx).inflate(R.layout.data_list,parent,false);
        return new FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterPesanan.FoodViewHolder holder, int position) {
        Pesanan pesanan = pesananList.get(position);

        holder.atasnama.setText(pesanan.getAtasnama());
        holder.noMeja.setText(pesanan.getNoMeja());

    }

    @Override
    public int getItemCount() {
        return pesananList.size();
    }

    public class FoodViewHolder extends RecyclerView.ViewHolder{

        TextView atasnama,noMeja;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);

            atasnama = itemView.findViewById(R.id.tvAtasNama);
            noMeja = itemView.findViewById(R.id.tvNoMeja);

        }
    }
}
