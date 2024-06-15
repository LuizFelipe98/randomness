package LuizFelipe98.randomness;

import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private PeriodManager periodManager;
    private ExpenseManager expenseManager;
    private EditText editImputGasto;
    private EditText editImputDescricaoGasto;
    private TextView textTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        periodManager = new PeriodManager(this);
        expenseManager = new ExpenseManager(this);

        if (!periodManager.hasOpenPeriod()) {
            loadIniciarPeriodoLayout();
        } else {
            loadMainActivityLayout();
        }
        editImputGasto = findViewById(R.id.editImputGasto);
        editImputDescricaoGasto = findViewById(R.id.editImputDescricaoGasto);
        textTotal = findViewById(R.id.textTotal);

        Button menuGastoButton = findViewById(R.id.menuGastoMes);
        menuGastoButton.setOnClickListener(this::showPopupMenuMain);
    }

    private void loadMainActivityLayout() {
        setContentView(R.layout.activity_main);
        initializeMainActivityComponents();
    }

    public void loadIniciarPeriodoLayout() {
        setContentView(R.layout.iniciar_periodo);

        Button buttonCadastrar = findViewById(R.id.buttonIniciarPeriodo);
        buttonCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = ((EditText) findViewById(R.id.editImputperiodo)).getText().toString().trim();
                String descricao = ((EditText) findViewById(R.id.editImputDescricaoPeriodo)).getText().toString().trim();
                if (!title.isEmpty()) {
                    periodManager.createNewPeriod(title, descricao);
                    loadMainActivityLayout();
                } else {
                    Toast.makeText(MainActivity.this, "Nome do período não pode estar vazio", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void initializeMainActivityComponents() {
        editImputGasto = findViewById(R.id.editImputGasto);
        editImputDescricaoGasto = findViewById(R.id.editImputDescricaoGasto);
        textTotal = findViewById(R.id.textTotal);

        // Atualizar o total de gastos ao inicializar os componentes
        atualizarTotalGastos();

        findViewById(R.id.buttonInserirGasto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String descricao = editImputDescricaoGasto.getText().toString().trim();
                String valorGastoStr = editImputGasto.getText().toString().trim();

                if (descricao.isEmpty() || valorGastoStr.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Descrição e valor não podem estar vazios", Toast.LENGTH_SHORT).show();
                    return;
                }

                double valorGasto;
                try {
                    valorGasto = Double.parseDouble(valorGastoStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(MainActivity.this, "Valor inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
                inserirGastosMes(descricao, valorGasto);
                limparCampos();
                // Atualizar o total de gastos após inserir um novo gasto
                atualizarTotalGastos();
            }
        });

        Button menuGastoButton = findViewById(R.id.menuGastoMes);
        menuGastoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenuMain(v);
            }
        });
    }

    private void atualizarTotalGastos() {
        double totalGastos = expenseManager.calcularTotalGastos();
        textTotal.setText(String.format("TOTAL: R$ %.2f", totalGastos));
    }

    public void limparCampos() {
        editImputDescricaoGasto.setText("");
        editImputGasto.setText("");
    }

    private void showPopupMenuMain(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_listar_gastos_mes) {
                listarGastosMes();
                return true;
            }
            if (item.getItemId() == R.id.action_encerrar_periodo) {
                encerrarPeriodo();
                return true;
            }
            if (item.getItemId() == R.id.action_listar_periodos) {
                listarPeriodosEncerrados();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void showPopupMenuGastosMes(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_gasto_mes, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_voltar_inicio) {
                loadMainActivityLayout();
                return true;
            }
            return false;
        });
        popup.show();
    }
    private void showPopupMenuPeriodos(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_gasto_mes, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_voltar_inicio) {
                loadMainActivityLayout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    public void listarGastosMes() {
        setContentView(R.layout.gastos_mes);
        ListView listViewGastosMes = findViewById(R.id.listViewGastosMes);
        TextView textTotalMes = findViewById(R.id.textTotalMes);

        ExpenseManager.ListarGastosResult result = expenseManager.listarGastosMes();
        if (result != null) {
            listViewGastosMes.setAdapter(result.adapter);
            textTotalMes.setText(String.format("Total: R$ %.2f", result.totalGastos));

            listViewGastosMes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    expenseManager.confirmarExclusao(id);

                    // Atualiza a lista após exclusão
                    ExpenseManager.ListarGastosResult updatedResult = expenseManager.listarGastosMes();
                    listViewGastosMes.setAdapter(updatedResult.adapter);
                    textTotalMes.setText(String.format("Total: R$ %.2f", updatedResult.totalGastos));
                }
            });

            listViewGastosMes.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    expenseManager.exibirDialogoEdicao(id);
                    return true;
                }
            });
            Button menuGastoButton = findViewById(R.id.menuGastoMes);
            menuGastoButton.setOnClickListener(this::showPopupMenuGastosMes);
        }
    }

    public void encerrarPeriodo() {
        periodManager.encerrarPeriodo();
        Toast.makeText(this, "Período encerrado", Toast.LENGTH_SHORT).show();
    }

    public void inserirGastosMes(String descricao, double valorGasto) {
        EditText edtDescricao = findViewById(R.id.editImputDescricaoGasto);
        EditText edtValorGasto = findViewById(R.id.editImputGasto);

        descricao = edtDescricao.getText().toString();
        valorGasto = Double.parseDouble(edtValorGasto.getText().toString());

        expenseManager.inserirGastosMes(descricao, valorGasto);

        Toast.makeText(this, "Gasto inserido", Toast.LENGTH_SHORT).show();
    }
    public void listarPeriodosEncerrados() {
        setContentView(R.layout.periodos_encerrados);
        ListView listViewClosedPeriods = findViewById(R.id.listViewPeriodosFechados);
        TextView textTotalClosedPeriods = findViewById(R.id.textTotalPeriodo);

        PeriodManager.ListarPeriodosResult result = periodManager.listarPeriodosEncerrados();
        if (result != null) {
            listViewClosedPeriods.setAdapter(result.adapter);
            textTotalClosedPeriods.setText(String.format("Total: R$ %.2f", result.totalPeriodos));

            listViewClosedPeriods.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Chama o método listarGastosPorPeriodo passando o ID do período selecionado
                    listarGastosPorPeriodo((int) id);
                }
            });
        }
        Button menuGastoButton = findViewById(R.id.menuGastoMes);
        menuGastoButton.setOnClickListener(this::showPopupMenuPeriodos);
    }

    public void listarGastosPorPeriodo(int periodoId) {
        setContentView(R.layout.gastos_mes);
        ListView listViewGastosMes = findViewById(R.id.listViewGastosMes);
        TextView textTotalMes = findViewById(R.id.textTotalMes);

        ExpenseManager.ListarGastosResult result = expenseManager.listarGastosPorPeriodo(periodoId);
        if (result != null) {
            listViewGastosMes.setAdapter(result.adapter);
            textTotalMes.setText(String.format("Total: R$ %.2f", result.totalGastos));

            listViewGastosMes.setOnItemClickListener((parent, view, position, id) -> {
                expenseManager.confirmarExclusao(id);

                // Atualiza a lista após exclusão
                ExpenseManager.ListarGastosResult updatedResult = expenseManager.listarGastosPorPeriodo(periodoId);
                listViewGastosMes.setAdapter(updatedResult.adapter);
                textTotalMes.setText(String.format("Total: R$ %.2f", updatedResult.totalGastos));
            });

            listViewGastosMes.setOnItemLongClickListener((parent, view, position, id) -> {
                expenseManager.exibirDialogoEdicao(id);
                return true;
            });
        }
        Button menuGastoButton = findViewById(R.id.menuGastoMes);
        menuGastoButton.setOnClickListener(this::showPopupMenuPeriodos);
    }

}
