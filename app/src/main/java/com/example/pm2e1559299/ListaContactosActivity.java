package com.example.pm2e1559299;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class ListaContactosActivity extends AppCompatActivity {

    ListView listView;
    SearchView searchView;
    ArrayAdapter<String> adapter;
    ArrayList<String> nombres;
    ArrayList<Contacto> contactos;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_contactos);

        listView = findViewById(R.id.listView);
        searchView = findViewById(R.id.searchView);
        dbHelper = new DBHelper(this);

        cargarContactos("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                cargarContactos(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                cargarContactos(newText);
                return true;
            }
        });

        listView.setOnItemClickListener((parent, view, position, id) -> mostrarOpciones(position));
    }

    void cargarContactos(String filtro) {
        Cursor cursor = dbHelper.obtenerContactos();
        nombres = new ArrayList<>();
        contactos = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String pais = cursor.getString(1);
            String nombre = cursor.getString(2);
            String telefono = cursor.getString(3);
            String nota = cursor.getString(4);
            byte[] imagen = cursor.getBlob(5);

            if (filtro.isEmpty() || nombre.toLowerCase().contains(filtro.toLowerCase()) || telefono.contains(filtro)) {
                contactos.add(new Contacto(id, pais, nombre, telefono, nota, imagen));
                nombres.add(nombre + " - " + telefono);
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, nombres);
        listView.setAdapter(adapter);
    }

    void mostrarOpciones(int pos) {
        Contacto c = contactos.get(pos);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opciones")
                .setItems(new String[]{"Llamar", "Ver Imagen", "Compartir", "Eliminar", "Actualizar"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            llamar(c.getTelefono());
                            break;
                        case 1:
                            mostrarImagen(c.getImagen());
                            break;
                        case 2:
                            compartir(c);
                            break;
                        case 3:
                            eliminar(c);
                            break;
                        case 4:
                            actualizar(c);
                            break;
                    }
                }).show();
    }

    void llamar(String telefono) {
        Intent i = new Intent(this, LlamarActivity.class);
        i.putExtra("telefono", telefono);
        startActivity(i);
    }

    void mostrarImagen(byte[] bytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        ImageView img = new ImageView(this);
        img.setImageBitmap(bitmap);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setPadding(16, 16, 16, 16);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(img);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Imagen del contacto")
                .setView(scroll)
                .setPositiveButton("Cerrar", null)
                .show();
    }


    void compartir(Contacto c) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, c.getNombre() + " - " + c.getTelefono());
        startActivity(Intent.createChooser(intent, "Compartir contacto"));
    }

    void eliminar(Contacto c) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("contactos", "id = ?", new String[]{String.valueOf(c.getId())});
        cargarContactos("");
        Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
    }

    void actualizar(Contacto c) {
        Intent i = new Intent(this, EditarContactoActivity.class);
        i.putExtra("id", c.getId());
        i.putExtra("pais", c.getPais());
        i.putExtra("nombre", c.getNombre());
        i.putExtra("telefono", c.getTelefono());
        i.putExtra("nota", c.getNota());
        i.putExtra("imagen", c.getImagen());
        startActivity(i);
    }

}