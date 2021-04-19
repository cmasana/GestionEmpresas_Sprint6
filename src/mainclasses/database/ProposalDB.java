package mainclasses.database;

import auxiliar.CustomException;
import auxiliar.DatabaseConnection;

import auxiliar.Error;
import auxiliar.InputOutput;
import auxiliar.Log;
import custom_ui.tables.CustomTableConfig;
import custom_ui.tables.CustomTableModel;
import mainclasses.entity.Company;
import mainclasses.entity.School;
import mainclasses.proposal.Proposal;

import javax.swing.*;
import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Grupo Individual Sprint 3 2021 - Carlos Masana
 * Clase ProposalDB: Contiene todos los métodos que permiten realizar operaciones en la bbdd y que hacen referencia
 * a la gestión de propuestas
 */
public class ProposalDB {
    // Aquí se cargan los datos del ResultSet
    private final ArrayList<Proposal> PROPOSALS = new ArrayList<Proposal>();

    // Constructor vacío
    public ProposalDB() {
        this.getProposalsTable();
    }


    // Métodos para realizar operaciones básicas en nuestro ArrayList

    /**
     * Añadir propuesta al array list
     *
     * @param proposal Objeto de la clase Proposal
     */
    public void addProposal(Proposal proposal) {
        PROPOSALS.add(proposal);
    }

    /**
     * Elimina una propuesta del ArrayList
     *
     * @param idProposal ID de la propuesta en la bbdd
     */
    public void removeProposal(int idProposal) {
        Proposal proposal = null;
        for (int i = 0; i < sizeProposalDB(); i++) {
            if (PROPOSALS.get(i).getIdProposal() == idProposal) {
                proposal = PROPOSALS.get(i);
            }
        }
        PROPOSALS.remove(proposal);
    }

    /**
     * Obtiene una propuesta desde el ArrayList
     *
     * @param posicion posición de la propuesta dentro del ArrayList
     * @return devuelve un objeto de la clase Proposal
     */
    public Proposal getProposalFromDB(int posicion) {
        return PROPOSALS.get(posicion);
    }

    /**
     * Obtiene el tamaño del ArrayList de propuestas
     *
     * @return devuelve un entero con el tamaño del ArrayList de tipo Proposal
     */
    public int sizeProposalDB() {
        return PROPOSALS.size();
    }

    /**
     * Permite transformar un arraylist en un array 2d de Strings
     * (es necesario para cargar los datos del arraylist en el JTable)
     *
     * @return devuelve un array de Strings
     */
    public String[][] listProposalsObject() {
        String[][] array = new String[sizeProposalDB()][5];

        for (int i = 0; i < sizeProposalDB(); i++) {
            array[i][0] = String.valueOf(getProposalFromDB(i).getIdProposal());
            array[i][1] = getProposalFromDB(i).getTitle();
            array[i][2] = getProposalFromDB(i).getDescription();
            array[i][3] = String.valueOf(getProposalFromDB(i).getStartDate());
            array[i][4] = String.valueOf(getProposalFromDB(i).getEntity());
        }

        return array;
    }

    /**
     * Permite cargar un ResultSet con los datos de la bbdd en el arraylist de esta clase
     */
    private void getProposalsTable() {
        String sql = "SELECT p.idproposal, p.title, p.description, p.startdate, e.id, e.entityname, e.city, e.phone, e.cif, e.territorialid " +
                "FROM proposals AS p " +
                "INNER JOIN entities AS e " +
                "ON p.identity = e.id " +
                "WHERE status = 'active'" +
                "ORDER BY p.startdate ASC";

        String title, description, entityName, city, territorialId, cif;
        int idProposal, phone, idSchool, idCompany;
        Date date;


        // Try-with-resources Statement: Se realiza el close() automaticamente
        try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);

            Proposal proposal;
            School school;
            Company company;

            while (rs.next()) {
                idProposal = rs.getInt("idproposal");
                title = rs.getString("title");
                description = rs.getString("description");
                date = rs.getDate("startdate");

                // Si el cif es nulo, la entidad es una escuela
                if (rs.getString("cif") == null) {
                    idSchool = rs.getInt("id");
                    entityName = rs.getString("entityname");
                    city = rs.getString("city");
                    phone = rs.getInt("phone");
                    territorialId = rs.getString("territorialid");

                    school = new School(idSchool, entityName, city, phone, territorialId);
                    proposal = new Proposal(idProposal, title, description, date, school);

                    this.addProposal(proposal);
                } else {
                    idCompany = rs.getInt("id");
                    entityName = rs.getString("entityname");
                    city = rs.getString("city");
                    phone = rs.getInt("phone");
                    cif = rs.getString("cif");

                    company = new Company(idCompany, entityName, city, phone, cif);
                    proposal = new Proposal(idProposal, title, description, date, company);

                    this.addProposal(proposal);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Permite crear propuestas
     *
     * @param proposalTable tabla donde se visualizan las propuestas
     * @param title         nombre de la propuesta
     * @param description   descripción de la propuesta
     * @param startDate     fecha de la propuesta
     * @param entity        entidad de la propuesta
     */
    public void createProposal(JTable proposalTable, String title, String description, String startDate, int entity) {
        String sql = "{ call ADD_PROPOSAL(?,?,?,?) }";

        try (CallableStatement stmt = DatabaseConnection.getConnection().prepareCall(sql)) {
            // Si hay algún campo vacío
            if (title.isEmpty() || description.isEmpty() || startDate.isEmpty() || entity == 0) {
                throw new CustomException(1111);

            } else {
                // Si la fecha introducida es anterior a la actual
                if (InputOutput.wrongDate(startDate)) {
                    throw new CustomException(1117);

                } else {
                    stmt.setString(1, title);
                    stmt.setString(2, description);
                    stmt.setString(3, startDate);
                    stmt.setObject(4, entity);

                    stmt.executeUpdate();

                    // Añadimos la entrada al log
                    Log.capturarRegistro("PROPOSAL CREATE " + title + " " + description + " " + startDate);

                    // Actualizamos los datos en la tabla
                    showData(proposalTable);
                }
            }
        } catch (CustomException ce) {
            InputOutput.printAlert(ce.getMessage());

            // Capturamos error para el registro
            Error.capturarError("PROPOSAL " + ce.getMessage());

        } catch (ParseException pe) {
            String alerta = "Error: Fecha con formato desconocido";
            InputOutput.printAlert(alerta);

            // Capturamos error para el registro
            Error.capturarError("PROPOSAL " + alerta);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }


    /**
     * Permite modificar propuestas
     *
     * @param proposalTable tabla donde se visualizan las propuestas
     * @param title         nombre de la propuesta
     * @param description   descripción de la propuesta
     * @param startDate     fecha de la propuesta
     * @param indexEntity   indice del objeto del JCombobox
     */
    public void editProposal(JTable proposalTable, String title, String description, String startDate, int indexEntity, String idProposal) {
        String sql = "{ call UPDATE_PROPOSAL(?,?,?,?,?) }";
        String sqlGetDate = "{ ? = call PROP_GET_CREATIONDATE(?) }";

        int resultado;

        try (CallableStatement stmt = DatabaseConnection.getConnection().prepareCall(sqlGetDate)) {
            stmt.registerOutParameter(1, Types.DATE);
            stmt.setInt(2, InputOutput.stringToInt(idProposal));

            stmt.execute();

            Date creationDate = stmt.getDate(1);

            // Almacenamos el nº total de filas que hay en la tabla
            int totalRows = proposalTable.getRowCount();

            // Almacena el nº de fila seleccionado
            int selectedRow = proposalTable.getSelectedRow();

            // Si no hay ninguna fila creada
            if (totalRows == 0) {
                throw new CustomException(1116);

                // Si no hay ninguna fila seleccionada
            } else if (selectedRow < 0) {
                throw new CustomException(1114);

                // Si los campos están vacíos
            } else if (title.isEmpty() || description.isEmpty() || startDate.isEmpty() || indexEntity == -1) {
                throw new CustomException(1111);

                // Si la fecha introducida es anterior a la fecha de creación original y a hoy
            } else if (InputOutput.wrongDate(startDate) && InputOutput.wrongEditedDate(startDate, creationDate)) {
                throw new CustomException(1119);

            } else {
                // Mensaje de confirmación al modificar
                resultado = InputOutput.editConfirmation();

                if (InputOutput.ifOk(resultado)) {
                    try (CallableStatement pStmt = DatabaseConnection.getConnection().prepareCall(sql)) {

                        pStmt.setString(1, title);
                        pStmt.setString(2, description);
                        pStmt.setString(3, startDate);
                        pStmt.setInt(4, indexEntity);

                        pStmt.setInt(5, InputOutput.stringToInt(idProposal));

                        pStmt.executeUpdate();

                        // Añadimos la entrada al log
                        Log.capturarRegistro("PROPOSAL EDIT " + title + " "
                                + description + " "
                                + InputOutput.stringToDate(startDate) + "  "
                                + "ENTITY ID " + indexEntity);

                        // Actualizamos datos de la tabla
                        showData(proposalTable);
                    }
                }
            }
        } catch (SQLException | CustomException ce) {
            InputOutput.printAlert(ce.getMessage());

            // Capturamos error para el registro
            Error.capturarError("PROPOSAL " + ce.getMessage());
        } catch (ParseException e) {
            InputOutput.printAlert("Error: La fecha introducida no es válida");
        }

    }

    /**
     * Permite modificar propuestas
     *
     * @param proposalTable tabla donde se visualizan las propuestas
     * @param title         nombre de la propuesta
     * @param description   descripción de la propuesta
     * @param startDate     fecha de la propuesta
     * @param indexEntity   indice del objeto del JCombobox
     * @param idProposal    id de la propuesta
     */
    public void softDeleteProposal(JTable proposalTable, String title, String description, String startDate, int indexEntity, String idProposal) {
        String sql = "{ call DELETE_PROPOSAL(?) }";
        int resultado;

        try {
            // Almacenamos el nº total de filas que hay en la tabla
            int totalRows = proposalTable.getRowCount();

            // Almacena el nº de fila seleccionado
            int selectedRow = proposalTable.getSelectedRow();

            // Si no hay ninguna fila creada
            if (totalRows == 0) {
                throw new CustomException(1116);

                // Si no hay ninguna fila seleccionada
            } else if (selectedRow < 0) {
                throw new CustomException(1114);

                // Si los campos están vacíos
            } else if (title.isEmpty() || description.isEmpty() || startDate.isEmpty() || indexEntity == -1) {
                throw new CustomException(1111);

            } else {
                // Mensaje de confirmación para eliminar
                resultado = InputOutput.deleteConfirmation();

                if (InputOutput.ifOk(resultado)) {
                    try (CallableStatement stmt = DatabaseConnection.getConnection().prepareCall(sql)) {

                        stmt.setInt(1, InputOutput.stringToInt(idProposal));

                        stmt.executeUpdate();
                        //stmt.close(); // No hace falta con el try-with-resources

                        // Añadimos la entrada al log
                        Log.capturarRegistro("PROPOSAL DELETE " + title + " "
                                + description + " "
                                + InputOutput.stringToDate(startDate) + "  "
                                + "ENTITY ID " + indexEntity);

                        // Actualizamos datos de la tabla
                        showData(proposalTable);

                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (CustomException ce) {
            InputOutput.printAlert(ce.getMessage());

            // Capturamos error para el registro
            Error.capturarError("PROPOSAL " + ce.getMessage());
        } catch (ParseException e) {
            InputOutput.printAlert("Error: La fecha introducida no es válida");
        }

    }

    /**
     * Cambia el estado de una propuesta a 'in progress' cuando la transformamos en proyecto
     *
     * @param proposalTable tabla donde se visualizan las propuestas
     * @param idProposal    id de la propuesta
     */
    public void toProject(JTable proposalTable, String idProposal) {
        // Almacena el nº de fila seleccionado
        int selectedRow = proposalTable.getSelectedRow();

        String sql = "{ call PROPOSAL_TO_PROJECT(?) }";

        if (selectedRow >= 0) {
            try (CallableStatement stmt = DatabaseConnection.getConnection().prepareCall(sql)) {
                stmt.setInt(1, InputOutput.stringToInt(idProposal));

                stmt.executeUpdate();

                // Eliminamos elemento del ArrayList
                this.removeProposal(InputOutput.stringToInt(idProposal));

                // Actualizamos datos de la tabla
                this.showData(proposalTable);

            } catch (SQLException ce) {
                InputOutput.printAlert(ce.getMessage());

                // Capturamos error para el registro
                Error.capturarError("PROJECT CREATE " + ce.getMessage());
            }
        }
    }

    /**
     * Muestra los datos actualizados en la tabla de propuestas
     *
     * @param proposalTable tabla dónde se visualizan las propuestas creadas
     */
    public void showData(JTable proposalTable) {
        // Encabezados de la tabla
        String[] colIdentifiers = {"ID", "Título", "Descripción", "Fecha de inicio", "Entidad"};

        // Instanceamos de nuevo para refrescar datos (Sino no funciona)
        ProposalDB proposals = new ProposalDB();

        // Añade los datos al modelo
        proposalTable.setModel(new CustomTableModel(
                proposals.listProposalsObject(),
                colIdentifiers
        ));

        // Diseño básico de la tabla
        CustomTableConfig.initConfig(proposalTable);
    }
}
