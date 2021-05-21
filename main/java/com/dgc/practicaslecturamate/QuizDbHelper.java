package com.dgc.practicaslecturamate;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import com.dgc.practicaslecturamate.QuizContract.*;

public class QuizDbHelper extends SQLiteOpenHelper{
    public static final String DATABASE_NAME = "QuizLecturaMate.db";
    public static final int DATABASE_VERSION = 3;

    private static QuizDbHelper instance;

    private SQLiteDatabase db;

    private QuizDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized QuizDbHelper getInstance(Context context)
    {
        if(instance==null)
        {
            instance = new QuizDbHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;

        final String SQL_CREATE_CATEGORIES_TABLE = "CREATE TABLE " +
                CategoriesTable.TABLE_NAME + "( " +
                CategoriesTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CategoriesTable.COLUMN_NAME + " TEXT " +
                ")";

        final String SQL_CREATE_QUESTIONS_TABLE = "CREATE TABLE " +
                QuestionsTable.TABLE_NAME + " ( " +
                QuestionsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                QuestionsTable.COLUMN_QUESTION + " TEXT, " +
                QuestionsTable.COLUMN_IMAGE + " BLOB, " +
                QuestionsTable.COLUMN_OPTION1 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION2 + " TEXT, " +
                QuestionsTable.COLUMN_OPTION3 + " TEXT, " +
                QuestionsTable.COLUMN_ANSWER_NR + " TEXT, " +
                QuestionsTable.COLUMN_DIFFICULTY + " TEXT," +
                QuestionsTable.COLUMN_CATEGORY_ID + " INTEGER, " +
                "FOREIGN KEY(" + QuestionsTable.COLUMN_CATEGORY_ID + ") REFERENCES " +
                CategoriesTable.TABLE_NAME + "(" + CategoriesTable._ID + ")" + "ON DELETE CASCADE" +
                ")";

        db.execSQL(SQL_CREATE_CATEGORIES_TABLE);
        db.execSQL(SQL_CREATE_QUESTIONS_TABLE);
        fillCategoriesTable();
        //fillQuestionsTable();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CategoriesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + QuestionsTable.TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    private void fillCategoriesTable() {
        Category c1 = new Category("Letras");
        insertCategory(c1);
        Category c2 = new Category("Sílabas");
        insertCategory(c2);
        Category c3 = new Category("Palabras");
        insertCategory(c3);
        Category c4 = new Category("Oraciones");
        insertCategory(c4);
        Category c5 = new Category("Comprensión Lectora");
        insertCategory(c5);
        Category c6 = new Category("Matematicas");
        insertCategory(c6);
    }

    public void addCategory(Category category){
        db= getWritableDatabase();
        insertCategory(category);
    }

    public void addCategories(List<Category> categories){
        db= getWritableDatabase();

        for(Category category : categories){
            insertCategory(category);
        }
    }

    private void insertCategory(Category category) {
        ContentValues cv = new ContentValues();
        cv.put(CategoriesTable.COLUMN_NAME, category.getName());
        db.insert(CategoriesTable.TABLE_NAME, null, cv);
    }

    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + CategoriesTable.TABLE_NAME, null);
        if (c.moveToFirst()) {
            do {
                Category category = new Category();
                category.setId(c.getInt(c.getColumnIndex(CategoriesTable._ID)));
                category.setName(c.getString(c.getColumnIndex(CategoriesTable.COLUMN_NAME)));
                categoryList.add(category);
            } while (c.moveToNext());
        }
        c.close();
        return categoryList;
    }

    private void fillQuestionsTable() {

        Drawable d = ContextCompat.getDrawable(null, R.drawable.avion);

        Question q1 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "A", "P", "E", "A",
                Question.DIFFICULTY_EASY, Category.LETRAS);
        insertQuestion(q1);
        Question q2 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "J", "P", "G", "G",
                Question.DIFFICULTY_EASY, Category.LETRAS);
        insertQuestion(q2);
        Question q3 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "L", "G", "U", "G",
                Question.DIFFICULTY_HARD, Category.LETRAS);
        insertQuestion(q3);
        Question q4 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "L", "G", "R", "L",
                Question.DIFFICULTY_EASY, Category.LETRAS);
        insertQuestion(q4);
        Question q5 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "A", "R", "I", "R",
                Question.DIFFICULTY_EASY, Category.LETRAS);
        insertQuestion(q5);
        Question q6 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "A", "O", "U", "A",
                Question.DIFFICULTY_MEDIUM, Category.LETRAS);
        insertQuestion(q6);
        Question q7 = new Question("Elige con que letra empieza esta palabra",new byte[1],
                "L", "G", "F", "G",
                Question.DIFFICULTY_MEDIUM, Category.LETRAS);
        insertQuestion(q7);
        Question q8 = new Question("2 x 2 =", new byte[1], "", "", "", "4", Question.DIFFICULTY_EASY,Category.MATEMATICAS);
        insertQuestion(q8);
        Question q9 = new Question("6 x 2 =", new byte[1], "", "", "", "12", Question.DIFFICULTY_EASY,Category.MATEMATICAS);
        insertQuestion(q9);
        Question q10 = new Question("3 x 2 =", new byte[1], "", "", "", "6", Question.DIFFICULTY_EASY,Category.MATEMATICAS);
        insertQuestion(q10);
        Question q11 = new Question("5 x 3 =", new byte[1], "", "", "", "15", Question.DIFFICULTY_EASY,Category.MATEMATICAS);
        insertQuestion(q11);
    }

    public void addQuestion (Question question){
        db= getWritableDatabase();
        insertQuestion(question);
    }

    public void addQuestions(List<Question> questions){
        db= getWritableDatabase();
        for (Question question : questions){
            insertQuestion(question);
        }
    }

    private void insertQuestion(Question question) {
        ContentValues cv = new ContentValues();
        cv.put(QuestionsTable.COLUMN_QUESTION, question.getQuestion());
        cv.put(QuestionsTable.COLUMN_IMAGE, question.getImage());
        cv.put(QuestionsTable.COLUMN_OPTION1, question.getOption1());
        cv.put(QuestionsTable.COLUMN_OPTION2, question.getOption2());
        cv.put(QuestionsTable.COLUMN_OPTION3, question.getOption3());
        cv.put(QuestionsTable.COLUMN_ANSWER_NR, question.getAnswer());
        cv.put(QuestionsTable.COLUMN_DIFFICULTY, question.getDifficulty());
        cv.put(QuestionsTable.COLUMN_CATEGORY_ID, question.getCategoryID());
        db.insert(QuestionsTable.TABLE_NAME, null, cv);
    }

    private void removeQuestion(Question question) {
        db.delete(QuestionsTable.TABLE_NAME, QuestionsTable._ID + "=" + question.getId(), null);
    }

    public ArrayList<Question> getAllQuestions() {
        ArrayList<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + QuestionsTable.TABLE_NAME, null);
        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setImage(c.getBlob(c.getColumnIndex(QuestionsTable.COLUMN_IMAGE)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswer(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                question.setCategoryID(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }

    public ArrayList<Question> getQuestions(int categoryID, String difficulty) {
        ArrayList<Question> questionList = new ArrayList<>();
        db = getReadableDatabase();
        String selection = QuestionsTable.COLUMN_CATEGORY_ID + " = ? " +
                " AND " + QuestionsTable.COLUMN_DIFFICULTY + " = ? ";
        String[] selectionArgs = new String[]{String.valueOf(categoryID), difficulty};
        Cursor c = db.query(
                QuestionsTable.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        if (c.moveToFirst()) {
            do {
                Question question = new Question();
                question.setId(c.getInt(c.getColumnIndex(QuestionsTable._ID)));
                question.setQuestion(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_QUESTION)));
                question.setImage(c.getBlob(c.getColumnIndex(QuestionsTable.COLUMN_IMAGE)));
                question.setOption1(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION1)));
                question.setOption2(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION2)));
                question.setOption3(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_OPTION3)));
                question.setAnswer(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_ANSWER_NR)));
                question.setDifficulty(c.getString(c.getColumnIndex(QuestionsTable.COLUMN_DIFFICULTY)));
                question.setCategoryID(c.getInt(c.getColumnIndex(QuestionsTable.COLUMN_CATEGORY_ID)));
                questionList.add(question);
            } while (c.moveToNext());
        }
        c.close();
        return questionList;
    }
}

