package com.example.pm2e1559299;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class LlamarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String telefono = getIntent().getStringExtra("telefono");

        new AlertDialog.Builder(this)
                .setTitle("Llamar")
                .setMessage("¿Deseas llamar a " + telefono + "?")
                .setPositiveButton("Sí", (dialog, which) -> realizarLlamada(telefono))
                .setNegativeButton("No", (dialog, which) -> finish())
                .show();
    }

    void realizarLlamada(String telefono) {
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse("tel:" + telefono));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }
        startActivity(i);
    }
}