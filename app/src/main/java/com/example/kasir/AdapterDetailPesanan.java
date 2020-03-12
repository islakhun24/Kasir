package com.example.kasir;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AdapterDetailPesanan extends RecyclerView.Adapter<AdapterDetailPesanan.FoodViewHolder>{

    private Context mCtx;
    private List<Pesanan> pesananList2;

    public AdapterDetailPesanan(Context mCtx,List<Pesanan>pesananList2){
        this.mCtx = mCtx;
        this.pesananList2 = pesananList2;
    }

    @NonNull
    @Override
    public AdapterDetailPesanan.FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mCtx).inflate(R.layout.cart_list,parent,false);
        return new AdapterDetailPesanan.FoodViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterDetailPesanan.FoodViewHolder holder, int position) {
        Pesanan pesanan = pesananList2.get(position);

        holder.tvName.setText(pesanan.getNama());
        holder.tvHarga.setText(pesanan.getHarga());
        holder.tvQty.setText(pesanan.getQty()+"x");
        holder.tvSubTotal.setText(pesanan.getSubtotal());
        holder.tvKeterangan.setText(pesanan.getKeterangan());
    }

    @Override
    public int getItemCount() {
        return pesananList2.size();
    }

    public class FoodViewHolder extends RecyclerView.ViewHolder{
        TextView tvQty, tvName, tvHarga, tvSubTotal, tvKeterangan;
        CardView cvList;

        public FoodViewHolder(@NonNull View v) {
            super(v);

            tvName = v.findViewById(R.id.tvNamamenu);
            tvQty = v.findViewById(R.id.tvQty);
            tvHarga = v.findViewById(R.id.textViewHarga);
            tvSubTotal = v.findViewById(R.id.textViewSubtotal);
            cvList = v.findViewById(R.id.cvList2);
            tvKeterangan = itemView.findViewById(R.id.tvKeterangan);
        }
    }
}
