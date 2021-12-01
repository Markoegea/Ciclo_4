package com.titantec.punzon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.titantec.punzon.Modelos.Productos;
import com.titantec.punzon.databinding.MainPageBinding;

import java.util.ArrayList;
import java.util.List;

public class MainPage extends Fragment {
    RecyclerView rv;
    MainPageBinding mainPageBinding;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ProductoAdapter productoAdapter;
    List<Productos> productosList = new ArrayList<>();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mainPageBinding = MainPageBinding.inflate(inflater, container, false);
        View root = mainPageBinding.getRoot();

        rv = mainPageBinding.recyclerView;
        rv.setLayoutManager(new LinearLayoutManager(mainPageBinding.recyclerView.getContext()));

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Query query = firestore.collection("Productos");
        FirestoreRecyclerOptions<Productos> firestoreRO =
                new FirestoreRecyclerOptions.Builder<Productos>().setQuery(query, Productos.class).build();
        productoAdapter = new ProductoAdapter(firestoreRO);
        productoAdapter.notifyDataSetChanged();
        rv.setAdapter(productoAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        productoAdapter.startListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        productoAdapter.stopListening();
        mainPageBinding = null;
    }

    public class ProductoAdapter extends FirestoreRecyclerAdapter<Productos, ProductoAdapter.ViewHolder>{
    private List<Productos> listaCarro;
        public ProductoAdapter (@NonNull FirestoreRecyclerOptions<Productos> options){
            super(options);
            listaCarro = new ArrayList<>();
        }

        @Override
        protected void onBindViewHolder(@NonNull ProductoAdapter.ViewHolder holder, int position, @NonNull Productos model) {
            DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
            final String id= documentSnapshot.getId();
            holder.txvProducto.setText(model.getNombre());
            holder.txvCosto.setText(model.getPrecio());
            Productos p = new Productos(model.getNombre(),model.getId(), model.getPrecio(),
                    model.getDescripcion(),model.getCantidad(),model.getMarca());
            productosList.add(p);
        }

        @NonNull
        @Override
        public ProductoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
            return new ProductoAdapter.ViewHolder(view);
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView txvProducto,txvCosto;
            Button btnAgregar;



            public ViewHolder(@NonNull View itemView){
                super(itemView);
                txvProducto = itemView.findViewById(R.id.txvProducto);
                txvCosto= itemView.findViewById(R.id.txvApe);
                btnAgregar = itemView.findViewById(R.id.btnAgregar);

                btnAgregar.setOnClickListener(agregarProductoClickListener);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavController abrir = Navigation.findNavController(v);

                        Bundle bundle = new Bundle();
                        bundle.putString("Nombre",productosList.get(getLayoutPosition()).getNombre());
                        bundle.putString("Id",productosList.get(getLayoutPosition()).getId());
                        bundle.putString("Precio",productosList.get(getLayoutPosition()).getPrecio());
                        bundle.putString("Descripcion",productosList.get(getLayoutPosition()).getDescripcion());
                        bundle.putString("Cantidad",productosList.get(getLayoutPosition()).getCantidad());
                        bundle.putString("Marca",productosList.get(getLayoutPosition()).getMarca());
                        getParentFragmentManager().setFragmentResult("param1",bundle);

                        abrir.navigate(R.id.Ver_Inventario);
                    }
                });
            }

            private View.OnClickListener agregarProductoClickListener = view -> {
                listaCarro.add(productosList.get(getBindingAdapterPosition()));
                MainActivity actividadCatalogo = (MainActivity) MainPage.this.getActivity();
                actividadCatalogo.actualizarNotificacion(listaCarro.size());
            };
        }
    }
}
