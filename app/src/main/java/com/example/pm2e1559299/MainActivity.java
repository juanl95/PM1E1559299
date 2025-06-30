package com.example.pm2e1559299;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spinnerPais;
    EditText etNombre, etTelefono, etNota;
    ImageView imgContacto;
    Button btnGuardar, btnVerLista, btnSeleccionarImagen;
    DBHelper dbHelper;
    byte[] imagenByte;
    static final int REQUEST_IMAGE_CAPTURE = 2;
    static final int REQUEST_GALLERY_PICK = 3;
    static final int REQUEST_CAMERA_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerPais = findViewById(R.id.spinnerPais);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        imgContacto = findViewById(R.id.imgContacto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVerLista = findViewById(R.id.btnVerLista);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);

        dbHelper = new DBHelper(this);

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
        spinnerPais.setSelection(0);

        spinnerPais.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String seleccionado = (String) parent.getItemAtPosition(position);
                int index = seleccionado.indexOf("+");
                if (index != -1) {
                    String prefijo = seleccionado.substring(index);
                    etTelefono.setText(prefijo);
                    etTelefono.setSelection(etTelefono.getText().length()); // Coloca el cursor al final
                } else {
                    etTelefono.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        btnSeleccionarImagen.setOnClickListener(v -> mostrarOpcionesImagen());
        btnGuardar.setOnClickListener(v -> guardarContacto());
        btnVerLista.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ListaContactosActivity.class);
            startActivity(i);
        });
    }

    String[] getPaises() {
        String[] codes = Locale.getISOCountries();
        String[] paises = new String[codes.length];
        for (int i = 0; i < codes.length; i++) {
            Locale obj = new Locale("", codes[i]);
            paises[i] = obj.getDisplayCountry();
        }
        return paises;
    }

    void mostrarOpcionesImagen() {
        String[] opciones = {"Tomar Foto", "Elegir de Galería"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Seleccionar Imagen")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) verificarPermisosCamara();
                    else elegirDeGaleria();
                }).show();
    }

    void verificarPermisosCamara() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            abrirCamara();
        }
    }

    void abrirCamara() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    void elegirDeGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_GALLERY_PICK);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                abrirCamara();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
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

    void guardarContacto() {
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

        long id = db.insert("contactos", null, values);
        if (id > 0) {
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
            limpiarFormulario();
        } else {
            Toast.makeText(this, "Error al guardar", Toast.LENGTH_SHORT).show();
        }
    }

    void limpiarFormulario() {
        spinnerPais.setSelection(0);
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        imgContacto.setImageResource(android.R.drawable.ic_menu_camera);
    }
}
