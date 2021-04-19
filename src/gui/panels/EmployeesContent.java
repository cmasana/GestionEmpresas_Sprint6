package gui.panels;

import auxiliar.InputOutput;
import custom_ui.components.buttons.ImageButton;
import custom_ui.components.forms.*;
import custom_ui.tables.CustomTableConfig;
import custom_ui.tables.CustomTableModel;
import mainclasses.database.EmployeeDB;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Permite crear el panel que muestra la parte de Gestión de empleados
 */
public class EmployeesContent extends ContentWindow {

    // Paneles
    private JPanel module;
    private JPanel management;

    // Formulario
    private RowForm rowId;
    private RowForm rowName;
    private RowForm rowDni;
    private RowForm rowNss;
    private RowForm rowEmployeeId;

    // Permite seleccionar archivos
    private JFileChooser fileChooser;

    // Tabla
    private JTable userTable;

    // Base de datos usuarios
    private final EmployeeDB EMPLOYEEDB = new EmployeeDB();

    // Constructor
    public EmployeesContent() throws IOException {
        super("GESTIÓN DE EMPLEADOS");
    }

    /**
     * Método que permite personalizar el contenido del apartado de empleados
     */
    @Override
    protected void putContentModule() throws IOException {
        module = new JPanel();
        module.setLayout(new GridLayout(2,1));
        module.setBackground(DYE.getSECONDARY());

        this.putManagementPanel();

        this.putUserListTable();

        this.add(module, BorderLayout.CENTER);
    }

    /**
     * Añade un panel con una serie de botones y un formulario
     */
    private void putManagementPanel() throws IOException {
        management = new JPanel();
        management.setLayout(new BorderLayout());
        this.putManagementButtons();
        this.putForm();

        module.add(management);
    }

    /**
     * Añade un formulario con labels e inputs
     */
    private void putForm() throws IOException {
        JPanel form = new JPanel();
        form.setBackground(DYE.getSECONDARY());
        form.setLayout(new GridLayout(4, 1));
        form.setBorder(new EmptyBorder(20,50,20,100)); // Top, left, bottom, right

        rowId = new RowForm("ID", false);

        rowName = new RowForm("Nombre", true);
        form.add(rowName);

        rowDni = new RowForm("DNI (8 dígitos y 1 letra)", true);
        form.add(rowDni);

        rowNss = new RowForm("NSS (12 dígitos)", true);
        form.add(rowNss);

        rowEmployeeId = new RowForm("Cod. Empleado", true);
        form.add(rowEmployeeId);

        management.add(form, BorderLayout.CENTER);
    }

    /**
     * Añade los botones de la parte superior que permiten realizar toda la gestión de empleados
     */
    private void putManagementButtons() throws IOException {

        JPanel mButtonsEmployee = new JPanel();
        mButtonsEmployee.setLayout(new BoxLayout(mButtonsEmployee, BoxLayout.Y_AXIS));
        mButtonsEmployee.setBorder(new EmptyBorder(110,100,10,0)); // Top, left, bottom, right
        mButtonsEmployee.setBackground(DYE.getSECONDARY());

        // Botón Crear
        ImageButton btnCreate = new ImageButton("img/create.png", "CREAR");
        btnCreate.setPreferredSize(new Dimension(150, 40));
        btnCreate.setMaximumSize(new Dimension(150, 40));
        btnCreate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                EMPLOYEEDB.createUser(
                        userTable,
                        rowName.getTxtInput().getText(),
                        rowDni.getTxtInput().getText(),
                        rowNss.getTxtInput().getText(),
                        rowEmployeeId.getTxtInput().getText()
                );

                InputOutput.cleanInputs(rowId, rowName, rowDni, rowNss, rowEmployeeId);
            }
        });
        mButtonsEmployee.add(btnCreate);

        // Crea un espacio en blanco de separación
        mButtonsEmployee.add(Box.createRigidArea(new Dimension(0, 5)));

        // Botón editar
        ImageButton btnEdit = new ImageButton("img/edit.png", "MODIFICAR");
        btnEdit.setPreferredSize(new Dimension(150, 40));
        btnEdit.setMaximumSize(new Dimension(150, 40));
        btnEdit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                EMPLOYEEDB.editUser(
                        userTable,
                        rowId.getTxtInput().getText(),
                        rowName.getTxtInput().getText(),
                        rowDni.getTxtInput().getText(),
                        rowNss.getTxtInput().getText(),
                        rowEmployeeId.getTxtInput().getText()
                );

                InputOutput.cleanInputs(rowId, rowName, rowDni, rowNss, rowEmployeeId);
            }
        });
        mButtonsEmployee.add(btnEdit);

        // Crea un espacio en blanco de separación
        mButtonsEmployee.add(Box.createRigidArea(new Dimension(0, 5)));

        // Botón eliminar
        ImageButton btnDelete = new ImageButton("img/delete.png", "ELIMINAR");
        btnDelete.setPreferredSize(new Dimension(150, 40));
        btnDelete.setMaximumSize(new Dimension(150, 40));
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                EMPLOYEEDB.softDeleteUser(userTable,
                        rowId.getTxtInput().getText(),
                        rowName.getTxtInput().getText(),
                        rowDni.getTxtInput().getText(),
                        rowNss.getTxtInput().getText(),
                        rowEmployeeId.getTxtInput().getText()
                );

                InputOutput.cleanInputs(rowId, rowName, rowDni, rowNss, rowEmployeeId);
            }
        });
        mButtonsEmployee.add(btnDelete);

        // Crea un espacio en blanco de separación
        mButtonsEmployee.add(Box.createRigidArea(new Dimension(0, 40)));

        // Botón importar CSV empleados
        ImageButton btnImportCsv = new ImageButton("img/upload.png", "IMPORTAR");
        btnImportCsv.setPreferredSize(new Dimension(150, 40));
        btnImportCsv.setMaximumSize(new Dimension(150, 40));
        btnImportCsv.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("."));
                fileChooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isFile() && f.getName().toLowerCase().endsWith(".csv");
                    }

                    @Override
                    public String getDescription() {
                        return "*.csv";
                    }
                });

                int returnVal = fileChooser.showOpenDialog(management);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    EMPLOYEEDB.importUsers(userTable, file);
                }
            }
        });

        mButtonsEmployee.add(btnImportCsv);

        mButtonsEmployee.setVisible(true);

        management.add(mButtonsEmployee, BorderLayout.WEST);
    }

    /**
     * Añade una tabla y su configuración a la interfaz
     */
    private void putUserListTable() throws IOException {
        String[] colIdentifiers = {"ID","Nombre", "DNI", "NSS", "Cod. Empleado"};
        JPanel panelTable = new JPanel(new BorderLayout());
        panelTable.setBackground(DYE.getSECONDARY());

        // Tabla
        JScrollPane scrollPane = new JScrollPane();
        panelTable.add(scrollPane, BorderLayout.CENTER);

        userTable = new JTable();

        // Datos del ArrayList que almacena el ResultSet de la bbdd
        EmployeeDB empdb = new EmployeeDB();

        // Modelo por defecto de la tabla
        userTable.setModel(new CustomTableModel(
                empdb.listEmployeesObject(),
                colIdentifiers
        ));

        // Diseño básico de la tabla
        CustomTableConfig.initConfig(userTable);

        scrollPane.setViewportView(userTable);


        // Carga los datos de una fila cuando hacemos clic en ella
        userTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = userTable.getSelectedRow();

                // Condición que limpia datos cada vez que seleccionamos una fila (sino peta)
                if (selectedRow == -1) {
                    InputOutput.cleanInputs(rowId, rowName, rowDni, rowNss, rowEmployeeId);
                }
                else {
                    rowId.setTxtInput(userTable.getValueAt(userTable.getSelectedRow(), 0).toString());
                    rowName.setTxtInput(userTable.getValueAt(userTable.getSelectedRow(), 1).toString());
                    rowDni.setTxtInput(userTable.getValueAt(userTable.getSelectedRow(), 2).toString());
                    rowNss.setTxtInput(userTable.getValueAt(userTable.getSelectedRow(), 3).toString());
                    rowEmployeeId.setTxtInput(userTable.getValueAt(userTable.getSelectedRow(), 4).toString());
                }
            }
        });

        module.add(panelTable, BorderLayout.CENTER);
    }
}