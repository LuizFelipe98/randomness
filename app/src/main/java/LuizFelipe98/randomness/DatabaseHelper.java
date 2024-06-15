package LuizFelipe98.randomness;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "randomness";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Cria a tabela gastos
        db.execSQL("CREATE TABLE IF NOT EXISTS gastos (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " descricao VARCHAR, " +
                " valorGasto DOUBLE, " +
                " ref_periodo INTEGER, " +
                "FOREIGN KEY(ref_periodo) REFERENCES periodos(id))");

        // Cria a tabela periodos
        db.execSQL("CREATE TABLE IF NOT EXISTS periodos (" +
                " id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " state INTEGER, " +
                " descricao VARCHAR, " +
                " title VARCHAR )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Espaço para implementar lógica de upgrade do banco de dados, se necessário
    }
}
