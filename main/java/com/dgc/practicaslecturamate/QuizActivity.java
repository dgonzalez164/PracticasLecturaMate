package com.dgc.practicaslecturamate;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.CountDownTimer;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class QuizActivity extends AppCompatActivity {
    public static final String EXTRA_SCORE = "extraScore";
    private static final long COUNTDOWN_IN_MILLIS = 30000;

    private static final String KEY_SCORE = "keyScore";
    private static final String KEY_QUESTION_COUNT = "keyQuestionCount";
    private static final String KEY_MILLIS_LEFT = "keyMillisLeft";
    private static final String KEY_ANSWERED = "keyAnswered";
    private static final String KEY_QUESTION_LIST = "keyQuestionList";

    private TextView textViewQuestion;
    private TextView textViewScore;
    private TextView textViewQuestionCount;
    private TextView textViewCategory;
    private TextView textViewDifficulty;
    private TextView textViewCountDown;
    private ImageView questionImageView;
    private TextInputLayout textFieldRespuesta;
    private RadioGroup rbGroup;
    private Button buttonConfirmMath;
    //private RadioButton rb1;
    //private RadioButton rb2;
    //private RadioButton rb3;
    private Button buttonConfirmNext;
    private Button buttonOption1;
    private Button buttonOption2;
    private Button buttonOption3;


    private ColorStateList textColorDefaultRb;
    private ColorStateList textColorDefaultCd;

    private CountDownTimer countDownTimer;
    private long timeLeftInMillis;

    private ArrayList<Question> questionList;
    private ArrayList<Question> filterQuestionList;
    private int questionCounter;
    private int questionCountTotal;
    private Question currentQuestion;
    private int score;
    private boolean answered;

    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        textViewQuestion = findViewById(R.id.text_view_question);
        textViewScore = findViewById(R.id.text_view_score);
        textViewQuestionCount = findViewById(R.id.text_view_question_count);
        textViewCategory = findViewById(R.id.text_view_category);
        textViewDifficulty = findViewById(R.id.text_view_difficulty);
        textViewCountDown = findViewById(R.id.text_view_countdown);
        questionImageView = findViewById(R.id.quiz_question_image);
        rbGroup = findViewById(R.id.radio_group);
        //rb1 = findViewById(R.id.radio_button1);
        //rb2 = findViewById(R.id.radio_button2);
        //rb3 = findViewById(R.id.radio_button3);

        textFieldRespuesta = findViewById(R.id.textFieldRespuesta);
        buttonOption1 = findViewById(R.id.button_opcion_1);
        buttonOption2 = findViewById(R.id.button_opcion_2);
        buttonOption3 = findViewById(R.id.button_opcion_3);
        buttonConfirmMath = findViewById(R.id.button_check_answer_math);
        buttonConfirmNext = findViewById(R.id.button_confirm_next);

        textColorDefaultRb = buttonOption1.getTextColors();
        textColorDefaultCd = textViewCountDown.getTextColors();

        Intent intent = getIntent();
        int categoryID = intent.getIntExtra(MainActivity.EXTRA_CATEGORY_ID, 0);
        String categoryName = intent.getStringExtra(MainActivity.EXTRA_CATEGORY_NAME);
        String difficulty = intent.getStringExtra(MainActivity.EXTRA_DIFFICULTY);
        textViewCategory.setText("Category: " + categoryName);
        textViewDifficulty.setText("Difficulty: " + difficulty);

        if(savedInstanceState == null) {
            QuizDbHelper dbHelper = QuizDbHelper.getInstance(this);
            questionList = dbHelper.getQuestions(categoryID, difficulty);
            // filtrar solamente 5 preguntas aleatorias
            filterQuestionList = new ArrayList<Question>();

            if (questionList.size() < 5) {
                questionCountTotal = questionList.size();
                Collections.shuffle(questionList);
            } else {
                while(filterQuestionList.size() <= 5)
                {
                    Random random = new Random();
                    Question tempQuestion = questionList.get(random.nextInt(questionList.size()));
                    if (!filterQuestionList.contains(tempQuestion)) {
                        filterQuestionList.add(tempQuestion);
                    }
                }
                questionCountTotal = filterQuestionList.size();
                Collections.shuffle(filterQuestionList);
            }
            showNextQuestion();
        } else {
            questionList = savedInstanceState.getParcelableArrayList(KEY_QUESTION_LIST);
            questionCountTotal = questionList.size();
            questionCounter = savedInstanceState.getInt(KEY_QUESTION_COUNT);
            currentQuestion = questionList.get(questionCounter - 1);
            score = savedInstanceState.getInt(KEY_SCORE);
            timeLeftInMillis = savedInstanceState.getLong(KEY_MILLIS_LEFT);
            answered = savedInstanceState.getBoolean(KEY_ANSWERED);
            if (!answered) {
                startCountDown();
            } else {
                updateCountDownText();
                showSolution();
            }
        }

        buttonOption1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!answered) {
                    checkAnswer(buttonOption1);
                } else {
                    showNextQuestion();
                }
            }
        });

        buttonOption2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!answered) {
                    checkAnswer(buttonOption2);
                } else {
                    showNextQuestion();
                }
            }
        });

        buttonOption3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!answered) {
                    checkAnswer(buttonOption3);
                } else {
                    showNextQuestion();
                }
            }
        });

        buttonConfirmMath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (textFieldRespuesta.getEditText().getText().length() > 0) {
                    if(TextUtils.isDigitsOnly(textFieldRespuesta.getEditText().getText())==true) {
                        checkMathAnswer();
                    }
                    else{
                        textFieldRespuesta.setError("Ingresa una respuesta válida");
                    }
                } else {
                    textFieldRespuesta.setError("Favor de escribir respuesta");
                }
            }
        });


        buttonConfirmNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (answered) {
                    showNextQuestion();
                }
            }
        });

    }

    private void showNextQuestion() {
        buttonOption1.setTextColor(textColorDefaultRb);
        buttonOption2.setTextColor(textColorDefaultRb);
        buttonOption3.setTextColor(textColorDefaultRb);
        rbGroup.clearCheck();
        textFieldRespuesta.getEditText().getText().clear();
        textFieldRespuesta.setError(null);
        buttonConfirmMath.setEnabled(true);
        if (questionCounter < questionCountTotal) {
            currentQuestion = questionList.get(questionCounter);
            textViewQuestion.setText(currentQuestion.getQuestion());
            if (currentQuestion.getCategoryID() == Category.MATEMATICAS)
            {
                questionImageView.setVisibility(View.GONE);
                buttonOption1.setVisibility(View.GONE);
                buttonOption2.setVisibility(View.GONE);
                buttonOption3.setVisibility(View.GONE);
                textFieldRespuesta.setVisibility(VISIBLE);
            } else {
                buttonConfirmMath.setVisibility(View.GONE);
                textFieldRespuesta.setVisibility(View.GONE);
                questionImageView.setVisibility(VISIBLE);
                byte[] imageQuestion = currentQuestion.getImage();
                Bitmap imageQuestionBitmap = BitmapFactory.decodeByteArray(imageQuestion, 0, imageQuestion.length);
                if(imageQuestionBitmap != null) {
                    questionImageView.setImageBitmap(imageQuestionBitmap);
                }
                buttonOption1.setVisibility(VISIBLE);
                buttonOption2.setVisibility(VISIBLE);
                buttonOption3.setVisibility(VISIBLE);
                buttonOption1.setText(currentQuestion.getOption1());
                buttonOption2.setText(currentQuestion.getOption2());
                buttonOption3.setText(currentQuestion.getOption3());
            }

            questionCounter++;
            textViewQuestionCount.setText("Question: " + questionCounter + "/" + questionCountTotal);
            answered = false;
            buttonConfirmNext.setVisibility(INVISIBLE);

            timeLeftInMillis = COUNTDOWN_IN_MILLIS;
            startCountDown();
        } else {
            finishQuiz();
        }
    }

    private void startCountDown() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateCountDownText();
            }
            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateCountDownText();
                showSolution();
            }
        }.start();
    }

    private void updateCountDownText() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        textViewCountDown.setText(timeFormatted);
        if (timeLeftInMillis < 10000) {
            textViewCountDown.setTextColor(Color.RED);
        } else {
            textViewCountDown.setTextColor(textColorDefaultCd);
        }
    }

    private void checkMathAnswer() {
        answered = true;
        countDownTimer.cancel();
        if (textFieldRespuesta.getEditText().getText().toString().equalsIgnoreCase(currentQuestion.getAnswer())) {
            score++;
            textViewScore.setText("Score: " + score);
        }
        showSolution();
    }

    private void checkAnswer(Button checkButtonAnswer) {
        answered = true;
        countDownTimer.cancel();

        if (checkButtonAnswer.getText().toString().equalsIgnoreCase(currentQuestion.getAnswer())) {
            score++;
            textViewScore.setText("Score: " + score);
        }
        showSolution();
    }

    private void showSolution() {
        if (currentQuestion.getCategoryID() == Category.MATEMATICAS) {
            buttonConfirmMath.setEnabled(false);
            if (textFieldRespuesta.getEditText().getText().toString().equalsIgnoreCase(currentQuestion.getAnswer())) {
                textViewQuestion.setText("Tu respuesta fue correcta: "+ textFieldRespuesta.getEditText().getText());
            } else {
                textFieldRespuesta.setError("Respuesta Incorrecta");
                textViewQuestion.setText("La respuesta correcta es: "+ currentQuestion.getAnswer());


            }
        } else {
            buttonOption1.setTextColor(Color.RED);
            buttonOption2.setTextColor(Color.RED);
            buttonOption3.setTextColor(Color.RED);

            if (buttonOption1.getText().toString().equalsIgnoreCase(currentQuestion.getAnswer())) {
                buttonOption1.setTextColor(Color.GREEN);
                textViewQuestion.setText("La respuesta " +  buttonOption1.getText() + " es correcta");
            }

            if (buttonOption2.getText().toString().equalsIgnoreCase(currentQuestion.getAnswer())) {
                buttonOption2.setTextColor(Color.GREEN);
                textViewQuestion.setText("La respuesta " +  buttonOption2.getText() + " es correcta");
            }

            if (buttonOption3.getText().toString().equalsIgnoreCase(currentQuestion.getAnswer())) {
                buttonOption3.setTextColor(Color.GREEN);
                textViewQuestion.setText("Answer " +  buttonOption3.getText() + " es correcta");
            }
        }

        buttonConfirmNext.setVisibility(VISIBLE);
        if (questionCounter < questionCountTotal) {
            buttonConfirmNext.setText("Siguiente");
        } else {
            buttonConfirmNext.setText("Terminar");
        }
    }

    private void finishQuiz() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_SCORE, score);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            finishQuiz();
        } else {
            Toast.makeText(this, "Presiona otra vez para terminar", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SCORE, score);
        outState.putInt(KEY_QUESTION_COUNT, questionCounter);
        outState.putLong(KEY_MILLIS_LEFT, timeLeftInMillis);
        outState.putBoolean(KEY_ANSWERED, answered);
        outState.putParcelableArrayList(KEY_QUESTION_LIST, questionList);
    }
}
