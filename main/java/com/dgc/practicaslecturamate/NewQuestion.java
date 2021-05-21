package com.dgc.practicaslecturamate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class NewQuestion extends AppCompatActivity {

    private Spinner spinnerCategory;
    private Spinner spinnerDifficulty;
    private Button newImageButton;
    private ImageView newImageView;

    private static final int PICK_IMAGE_REQUEST = 9544;

    // Permissions for accessing the storage
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    Uri selectedImage;
    String part_image;
    byte[] imageByte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_question);

        spinnerCategory = findViewById(R.id.spinner_category);
        spinnerDifficulty = findViewById(R.id.spinner_difficulty);
        newImageButton = findViewById(R.id.button_new_image);
        newImageView = findViewById(R.id.new_image_view);


        loadCategories();
        loadDifficultyLevels();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Agregar Pregunta");
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validarContenidoPregunta()) {
                    addQuestionToDB();
                    Snackbar.make(view, "La Pregunta ha sido Agregada", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else {
                    Snackbar.make(view, "Favor de llenar todos los campos obligatorios", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        newImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private void selectImage() {
        verifyStoragePermissions(NewQuestion.this);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Open Gallery"), PICK_IMAGE_REQUEST);
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }

    // Method to get the absolute path of the selected image from its URI
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedImage = data.getData();                                                         // Get the image file URI
                String[] imageProjection = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, imageProjection, null, null, null);
                if(cursor != null) {
                    cursor.moveToFirst();
                    int indexImage = cursor.getColumnIndex(imageProjection[0]);
                    part_image = cursor.getString(indexImage);
                    //imgPath.setText(part_image);                                                        // Get the image file absolute path
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    newImageView.setImageBitmap(bitmap);
                    imageByte = getBitmapAsByteArray(bitmap);// Set the ImageView with the bitmap of the image
                }
            }
        }
    }

    private void loadCategories() {
        QuizDbHelper dbHelper = QuizDbHelper.getInstance(this);
        List<Category> categories = dbHelper.getAllCategories();

        ArrayAdapter<Category> adapterCategories= new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapterCategories);
    }

    private void loadDifficultyLevels(){
        String[] difficultyLevels = Question.getAllDifficultyLevels();
        ArrayAdapter<String> adapterDifficulty = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, difficultyLevels);
        adapterDifficulty.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapterDifficulty);
    }

    private void addQuestionToDB() {
        QuizDbHelper dbHelper = QuizDbHelper.getInstance(this);
        Question newQuestion = new Question();

        TextInputLayout textFieldPregunta = findViewById(R.id.textFieldPregunta);
        newQuestion.setQuestion(textFieldPregunta.getEditText().getText().toString());
        TextInputLayout textFieldOpcion1 = findViewById(R.id.textFieldOpcion1);
        newQuestion.setOption1(textFieldOpcion1.getEditText().getText().toString());
        TextInputLayout textFieldOpcion2 = findViewById(R.id.textFieldOpcion2);
        newQuestion.setOption2(textFieldOpcion2.getEditText().getText().toString());
        TextInputLayout textFieldOpcion3 = findViewById(R.id.textFieldOpcion3);
        newQuestion.setOption3(textFieldOpcion3.getEditText().getText().toString());
        TextInputLayout textFieldRespuesta = findViewById(R.id.textFieldRespuesta);
        newQuestion.setAnswer(textFieldRespuesta.getEditText().getText().toString());

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();
        int categoryID= selectedCategory.getId();
        newQuestion.setCategoryID(categoryID);
        String difficulty = spinnerDifficulty.getSelectedItem().toString();
        newQuestion.setDifficulty(difficulty);

        newQuestion.setImage(imageByte);

        dbHelper.addQuestion(newQuestion);
    }

    private boolean validarContenidoPregunta() {



        TextInputLayout textFieldPregunta = findViewById(R.id.textFieldPregunta);

        TextInputLayout textFieldOpcion1 = findViewById(R.id.textFieldOpcion1);

        TextInputLayout textFieldOpcion2 = findViewById(R.id.textFieldOpcion2);

        TextInputLayout textFieldOpcion3 = findViewById(R.id.textFieldOpcion3);

        TextInputLayout textFieldRespuesta = findViewById(R.id.textFieldRespuesta);

        Category selectedCategory = (Category) spinnerCategory.getSelectedItem();

        if(selectedCategory.getId() == Category.MATEMATICAS) {
            if(textFieldPregunta.getEditText().getText().toString().isEmpty()) {
                return false;
            }

            if(textFieldRespuesta.getEditText().getText().toString().isEmpty()) {
                return false;
            }
        } else {

            if (selectedImage == null) {
                return false;
            }

            if(textFieldPregunta.getEditText().getText().toString().isEmpty()) {
                return false;
            }

            if(textFieldRespuesta.getEditText().getText().toString().isEmpty()) {
                return false;
            }

            if(textFieldOpcion1.getEditText().getText().toString().isEmpty()) {
                return false;
            }
            if(textFieldOpcion2.getEditText().getText().toString().isEmpty()) {
                return false;
            }
            if(textFieldOpcion3.getEditText().getText().toString().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}