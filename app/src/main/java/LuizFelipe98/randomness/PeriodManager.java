package LuizFelipe98.randomness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class PeriodManager {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    public PeriodManager(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public boolean hasOpenPeriod() {
        boolean hasOpenPeriod = false;
        Cursor cursor = null;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT id FROM periodos WHERE state = 1", null);
            hasOpenPeriod = cursor.moveToFirst();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "Erro ao verificar período aberto: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
        return hasOpenPeriod;
    }

    public void createNewPeriod(String title, String descricao) {
        try {
            database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("state", 1);
            values.put("descricao", descricao);
            values.put("title", title);

            database.insert("periodos", null, values);
            Toast.makeText(context, "Período iniciado com sucesso", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "Erro ao inserir período: " + e.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    public void encerrarPeriodo() {
        try {
            database = dbHelper.getWritableDatabase();
            String sql = "UPDATE periodos SET state = 2 WHERE state = 1";
            SQLiteStatement stmt = database.compileStatement(sql);
            int rowsAffected = stmt.executeUpdateDelete();

            if (rowsAffected > 0) {
                Toast.makeText(context, "Mês encerrado com sucesso!", Toast.LENGTH_SHORT).show();

                // Após encerrar o período, configure a visualização para iniciar um novo período
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;
                    mainActivity.loadIniciarPeriodoLayout();
                }
            } else {
                Toast.makeText(context, "Nenhum período aberto encontrado!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "Erro ao encerrar período: " + e.getMessage());
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    public static class ListarPeriodosResult {
        public SimpleCursorAdapter adapter;
        public double totalPeriodos;
        public Cursor cursor;

        public ListarPeriodosResult(SimpleCursorAdapter adapter, double totalPeriodos, Cursor cursor) {
            this.adapter = adapter;
            this.totalPeriodos = totalPeriodos;
            this.cursor = cursor;
        }
    }

    public ListarPeriodosResult listarPeriodosEncerrados() {
        Cursor cursor = null;
        double totalPeriodos = 0.0;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT id AS _id, title, descricao FROM periodos WHERE state = 2", null);

            String[] from = {"title", "descricao"};
            int[] to = {android.R.id.text1, android.R.id.text2};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_2, cursor, from, to, 0);

            return new ListarPeriodosResult(adapter, totalPeriodos, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
