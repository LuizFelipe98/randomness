package LuizFelipe98.randomness;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;  // Adicione esta linha
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class ExpenseManager {

    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context context;

    public ExpenseManager(Context context) {
        this.context = context;
        dbHelper = new DatabaseHelper(context);
    }

    public void inserirGastosMes(String descricao, double valorGasto) {
        Cursor cursor = null;
        try {
            database = dbHelper.getWritableDatabase();

            // Selecionar o id do período onde state = 1
            String selectSql = "SELECT id FROM periodos WHERE state = 1";
            cursor = database.rawQuery(selectSql, null);

            int refPeriodo = -1;
            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndex("id");
                if (idIndex != -1) {
                    refPeriodo = cursor.getInt(idIndex);
                } else {
                    Log.e("ERROR", "Coluna 'id' não encontrada na tabela 'periodos'");
                }
            }

            if (refPeriodo != -1) {
                // Inserir o novo registro na tabela gastos com o ref_periodo obtido
                ContentValues values = new ContentValues();
                values.put("descricao", descricao);
                values.put("valorGasto", valorGasto);
                values.put("ref_periodo", refPeriodo);

                database.insert("gastos", null, values);
                Toast.makeText(context, "Gasto cadastrado com sucesso", Toast.LENGTH_SHORT).show();

                if (context instanceof MainActivity) {
                    ((MainActivity) context).limparCampos();
                }

            } else {
                Toast.makeText(context, "Período ativo não encontrado", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ERROR", "Erro ao inserir gastos: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
                Log.d("DEBUG", "Banco de dados fechado após inserção");
            }
        }
    }

    public double calcularTotalGastos() {
        double total = 0.0;
        Cursor cursor = null;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT SUM(valorGasto) FROM gastos where ref_periodo in (select id from periodos where state = 1)", null);
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
        return total;
    }

    public static class ListarGastosResult {
        public SimpleCursorAdapter adapter;
        public double totalGastos;
        public Cursor cursor;

        public ListarGastosResult(SimpleCursorAdapter adapter, double totalGastos, Cursor cursor) {
            this.adapter = adapter;
            this.totalGastos = totalGastos;
            this.cursor = cursor;
        }
    }

    public ListarGastosResult listarGastosMes() {
        Cursor cursor = null;
        double totalGastos = 0.0;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT id AS _id, descricao, valorGasto FROM gastos WHERE ref_periodo IN (SELECT id FROM periodos WHERE state = 1)", null);

            if (cursor.moveToFirst()) {
                do {
                    totalGastos += cursor.getDouble(cursor.getColumnIndex("valorGasto"));
                } while (cursor.moveToNext());
            }

            String[] from = {"descricao", "valorGasto"};
            int[] to = {android.R.id.text1, android.R.id.text2};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_2, cursor, from, to, 0);

            return new ListarGastosResult(adapter, totalGastos, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void confirmarExclusao(long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirmar Exclusão");
        builder.setMessage("Você realmente deseja excluir este item?");
        builder.setPositiveButton("Sim", (dialog, which) -> {
            excluirGasto(id);
            // Atualiza a lista após exclusão
            if (context instanceof MainActivity) {
                ((MainActivity) context).listarGastosMes();
            }
        });
        builder.setNegativeButton("Não", null);
        builder.show();
    }

    public void excluirGasto(long id) {
        try {
            database = dbHelper.getWritableDatabase();
            String sql = "DELETE FROM gastos WHERE id = ?";
            SQLiteStatement stmt = database.compileStatement(sql);

            stmt.bindLong(1, id);
            stmt.executeUpdateDelete();

            Toast.makeText(context, "Gasto excluído", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    public void atualizarGasto(long id, String descricao, double valorGasto) {
        try {
            database = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("descricao", descricao);
            values.put("valorGasto", valorGasto);

            database.update("gastos", values, "id = ?", new String[]{String.valueOf(id)});
            Toast.makeText(context, "Gasto atualizado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    public void exibirDialogoEdicao(long id) {
        // Recupera os detalhes do gasto
        Cursor cursor = null;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT descricao, valorGasto FROM gastos WHERE id = ?", new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) {
                String descricaoAtual = cursor.getString(cursor.getColumnIndex("descricao"));
                double valorGastoAtual = cursor.getDouble(cursor.getColumnIndex("valorGasto"));

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Editar Gasto");

                LayoutInflater inflater = LayoutInflater.from(context);
                View dialogView = inflater.inflate(R.layout.dialog_edit_gasto, null);  // Certifique-se de que o nome do layout esteja correto
                builder.setView(dialogView);

                EditText descricaoEditText = dialogView.findViewById(R.id.editDescricao);
                EditText valorGastoEditText = dialogView.findViewById(R.id.editValorGasto);

                descricaoEditText.setText(descricaoAtual);
                valorGastoEditText.setText(String.valueOf(valorGastoAtual));

                builder.setPositiveButton("Salvar", (dialog, which) -> {
                    String novaDescricao = descricaoEditText.getText().toString();
                    String valorGastoStr = valorGastoEditText.getText().toString().trim();

                    if (novaDescricao.isEmpty() || valorGastoStr.isEmpty()) {
                        Toast.makeText(context, "Descrição e valor não podem estar vazios", Toast.LENGTH_SHORT).show();
                    } else {
                        double novoValorGasto;
                        try {
                            novoValorGasto = Double.parseDouble(valorGastoStr);
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Valor inválido", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        atualizarGasto(id, novaDescricao, novoValorGasto);
                        if (context instanceof MainActivity) {
                            ((MainActivity) context).listarGastosMes();
                        }
                    }
                });
                builder.setNegativeButton("Cancelar", null);
                builder.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }
    }

    public ListarGastosResult listarGastosPorPeriodo(int periodoId) {
        Cursor cursor = null;
        double totalGastos = 0.0;
        try {
            database = dbHelper.getReadableDatabase();
            cursor = database.rawQuery("SELECT id AS _id, descricao, valorGasto FROM gastos WHERE ref_periodo = ?", new String[]{String.valueOf(periodoId)});

            if (cursor.moveToFirst()) {
                do {
                    totalGastos += cursor.getDouble(cursor.getColumnIndex("valorGasto"));
                } while (cursor.moveToNext());
            }

            String[] from = {"descricao", "valorGasto"};
            int[] to = {android.R.id.text1, android.R.id.text2};

            SimpleCursorAdapter adapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_2, cursor, from, to, 0);

            return new ListarGastosResult(adapter, totalGastos, cursor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
