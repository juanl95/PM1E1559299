package com.example.pm2e1559299;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class EditarContactoActivity extends AppCompatActivity {

    EditText etNombre, etTelefono, etNota;
    Spinner spinnerPais;
    ImageView imgContacto;
    Button btnActualizar, btnImagen;
    DBHelper dbHelper;
    int contactoId;
    byte[] imagenByte;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_GALLERY_PICK = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_contacto);

        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        spinnerPais = findViewById(R.id.spinnerPais);
        imgContacto = findViewById(R.id.imgContacto);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnImagen = findViewById(R.id.btnImagen);

        dbHelper = new DBHelper(this);

        // Cargar países en spinner No prefijo de cada uno
        List<String> paises = new ArrayList<>();
        paises.add("Seleccione su país");
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        String[] codes = Locale.getISOCountries();
        for (String countryCode : codes) {
            Locale locale = new Locale("", countryCode);
            String nombrePais = locale.getDisplayCountry();
            int codigoPrefijo = phoneUtil.getCountryCodeForRegion(countryCode);
            if (codigoPrefijo != 0) {
                paises.add(nombrePais + " (+" + codigoPrefijo + ")");
            } else {
                paises.add(nombrePais);
            }
        }
        ArrayAdapter<String> paisAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, paises);
        spinnerPais.setAdapter(paisAdapter);

        // Obteneiendo datos del contacto a editar
        Intent i = getIntent();
        contactoId = i.getIntExtra("id", -1);
        etNombre.setText(i.getStringExtra("nombre"));
        etTelefono.setText(i.getStringExtra("telefono"));
        etNota.setText(i.getStringExtra("nota"));

        String paisRecibido = i.getStringExtra("pais");
        if (paisRecibido != null) {
            int index = paisAdapter.getPosition(paisRecibido);
            if (index >= 0) {
                spinnerPais.setSelection(index);
            }
        }

        byte[] img = i.getByteArrayExtra("imagen");
        if (img != null) {
            imgContacto.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(img, 0, img.length));
        } else {
            imgContacto.setImageResource(R.drawable.ic_person_default);
        }

        btnImagen.setOnClickListener(v -> seleccionarImagen());
        btnActualizar.setOnClickListener(v -> actualizarContacto());
    }

    void seleccionarImagen() {
        String[] opciones = {"Tomar Foto", "Elegir de Galería"};
        new android.app.AlertDialog.Builder(this)
                .setTitle("Seleccionar Imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) abrirCamara();
                    else abrirGaleria();
                }).show();
    }

    void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_IMAGE_CAPTURE) {
                Bitmap foto = (Bitmap) data.getExtras().get("data");
                imgContacto.setImageBitmap(foto);
            } else if (requestCode == REQUEST_GALLERY_PICK) {
                Uri selectedImage = data.getData();
                imgContacto.setImageURI(selectedImage);
            }
        }
    }

    void actualizarContacto() {
        String pais = spinnerPais.getSelectedItem().toString();
        String nombre = etNombre.getText().toString();
        String telefono = etTelefono.getText().toString();
        String nota = etNota.getText().toString();

        if (pais.equals("Seleccione su país") || nombre.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Drawable drawable = imgContacto.getDrawable();
        if (drawable == null) {
            Toast.makeText(this, "Debe seleccionar una imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        imagenByte = stream.toByteArray();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pais", pais);
        values.put("nombre", nombre);
        values.put("telefono", telefono);
        values.put("nota", nota);
        values.put("imagen", imagenByte);

        int res = db.update("contactos", values, "id = ?", new String[]{String.valueOf(contactoId)});
        if (res > 0) {
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show();
        }
    }

    String[] getPaises() {
        String[] codes = java.util.Locale.getISOCountries();
        String[] paises = new String[codes.length];
        for (int i = 0; i < codes.length; i++) {
            java.util.Locale obj = new java.util.Locale("", codes[i]);
            paises[i] = obj.getDisplayCountry();
        }
        return paises;
    }
}
