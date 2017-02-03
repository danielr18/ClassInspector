package darb.componentes;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


public class MainWindow {

	Class<?> classToInspect;
	Object objectFromClassToInspect;
	Method selectedMethod;

	private JFrame frame;
	private JLabel lblFrameTitle;
	private JPanel panelTopMargin;
	private JPanel panelLeftMargin;
	private JPanel rightMarginPanel;

	private JLabel lblClassName;
	private JTextField tfClassName;
	private JLabel lblInstanceName;
	private JTextField tfInstanceName;
	private JButton btnInspect;

	private JLabel lblVariables;
	private JScrollPane scrollPaneVariablesTable;
	private JTable tableVariables;
	private String [] variablesTableColumnNames =  {"Name", "Value", "Type", "Public", "Private", "Final", "Static"};

	private JLabel lblMethods;
	private JScrollPane scrollPaneMethodsTable;
	private JTable tableMethods;
	private String [] methodsTableColumnNames =  {"Name", "Parameters Quantity", "Parameter Types", "Return Type", "Public", "Private", "Final", "Static"};

	private JLabel lblConstructors;
	private JScrollPane scrollPaneConstructorsTable;
	private JTable tableConstructors;
	private String [] constructorsTableColumnNames =  {"Parameters Quantity", "Parameter Types", "Public", "Private"};

	private JScrollPane scrollPaneMethodParamsTable;
	private JTable tableMethodParams;
	private String [] methodParamsTableColumnNames =  {"Type", "Value"};
	private JLabel labelSelectedMethod;

	//Used to test instance inspector
	Person person1;
	private JButton buttonInvokeMethod;


	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();

		//Person to inspect
		person1 = new Person("Jorge", "Reinoso", 24);

		tfInstanceName.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent ke) {
	        	tfClassName.setText("");
	        }
	    });

		tfClassName.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent ke) {
	        	tfInstanceName.setText("");
	        }
	    });

		buttonInvokeMethod.addActionListener(e -> {
			if(selectedMethod != null) {
				try {
					Object[] args = new Object[selectedMethod.getParameterCount()];
		        	Class<?>[] parameterTypes = selectedMethod.getParameterTypes();
					for (int i = 0; i < args.length; i++) {
						String cellValue = (String) tableMethodParams.getModel().getValueAt(i, 1);
						switch(parameterTypes[i].getName()) {
							case "javax.lang.String":
								args[i] = cellValue;
								break;
							case "int":
								args[i] = Integer.parseInt(cellValue);
								break;
							default:
								try {
									Constructor<?> paramTypeConstructor = parameterTypes[i].getConstructor(String.class);
									args[i] = paramTypeConstructor.newInstance(cellValue);
								}
								catch (NoSuchMethodException e1) {
									Constructor<?> paramTypeConstructor = parameterTypes[i].getConstructor();
									args[i] = paramTypeConstructor.newInstance();
								}
							}
						}

					if("void".equals(selectedMethod.getReturnType().getName())) {
						selectedMethod.invoke(objectFromClassToInspect, args);
					}
					else {
						Object value = selectedMethod.invoke(objectFromClassToInspect, args);
						try {
							showMessageDialog(null, "Method returned:  " + value.toString());
						}
						catch (NullPointerException err) {
							showMessageDialog(null, "Method returned a null value.");
						}
					}

					//Refresh vars table
					Field[] classDeclaredFields = classToInspect.getDeclaredFields();
				    String[][] varsData = new String[classDeclaredFields.length][variablesTableColumnNames.length];
				    for(int i = 0; i < classDeclaredFields.length; i++) {
			    		int fieldModifier = classDeclaredFields[i].getModifiers();
			    		classDeclaredFields[i].setAccessible(true);

			    		varsData[i][0] = classDeclaredFields[i].getName();
			    		varsData[i][1] = (objectFromClassToInspect != null) ? String.valueOf(classDeclaredFields[i].get(objectFromClassToInspect)) : "";
			    		varsData[i][2] = classDeclaredFields[i].getType().getName();
			    		varsData[i][3] = Modifier.isPublic(fieldModifier) ? "\u2714" : "\u2718";
			    		varsData[i][4] = Modifier.isPrivate(fieldModifier) ? "\u2714" : "\u2718";
			    		varsData[i][5] = Modifier.isFinal(fieldModifier) ? "\u2714" : "\u2718";
			    		varsData[i][6] = Modifier.isStatic(fieldModifier) ? "\u2714" : "\u2718";
				    }

		        	DefaultTableModel varsTableModel = new DefaultTableModel(varsData, variablesTableColumnNames);
					tableVariables.setModel(varsTableModel);

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (NoSuchMethodException e1) {
					// TODO Auto-generated catch block
					showMessageDialog(null, "One of the param types doesn't have an empty constructor.");
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InstantiationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else {
				showMessageDialog(null, "Select a method first.");
			}
		});

		tableMethods.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
	        public void valueChanged(ListSelectionEvent e) {
	        	try {
	        		String selectedMethodName = tableMethods.getValueAt(tableMethods.getSelectedRow(), 0).toString();
		        	Method[] classDeclaredMethods = classToInspect.getDeclaredMethods();
		        	selectedMethod = classDeclaredMethods[tableMethods.getSelectedRow()];
		        	Class<?>[] parameterTypes = selectedMethod.getParameterTypes();

		        	String[][] paramsTableData = new String[parameterTypes.length][parameterTypes.length];

		    		for(int i = 0; i < parameterTypes.length; i++) {
		    			paramsTableData[i][0] = parameterTypes[i].getName();
		    		}

		        	DefaultTableModel paramsTableModel = new DefaultTableModel(paramsTableData, methodParamsTableColumnNames);
		    		tableMethodParams.setModel(paramsTableModel);

		        	labelSelectedMethod.setText("Selected: " + selectedMethodName);
	        	}
	        	catch(ArrayIndexOutOfBoundsException err) {
	        		//When table model changes
	    			selectedMethod = null;
	    			DefaultTableModel emptyTM = new DefaultTableModel();
	    			tableMethodParams.setModel(emptyTM);
	    			labelSelectedMethod.setText("Select a method from the list on the side.");

	        	}

	        }
	    });


		btnInspect.addActionListener(e -> {
			classToInspect = null;
			objectFromClassToInspect = null;
			selectedMethod = null;
			Constructor<?> classToInspectConstructor = null;

			try {
				String instanceName = tfInstanceName.getText();

				if(instanceName.length() > 0) {
					objectFromClassToInspect = this.getClass().getDeclaredField(instanceName).get(this);
					classToInspect = objectFromClassToInspect.getClass();
				}
				else {
					classToInspect = Class.forName(tfClassName.getText());
					try {
						classToInspectConstructor = classToInspect.getConstructor();
						objectFromClassToInspect = classToInspectConstructor.newInstance();
					} catch (NoSuchMethodException e1) {
						// TODO Auto-generated catch block
					}
				}

				Field[] classDeclaredFields = classToInspect.getDeclaredFields();
				Method[] classDeclaredMethods = classToInspect.getDeclaredMethods();
				Constructor<?>[] classDeclaredConstructors = classToInspect.getDeclaredConstructors();

				//Variables
			    String[][] varsData = new String[classDeclaredFields.length][variablesTableColumnNames.length];
			    for(int i = 0; i < classDeclaredFields.length; i++) {
		    		int fieldModifier = classDeclaredFields[i].getModifiers();
		    		classDeclaredFields[i].setAccessible(true);

		    		varsData[i][0] = classDeclaredFields[i].getName();
		    		varsData[i][1] = (objectFromClassToInspect != null) ? String.valueOf(classDeclaredFields[i].get(objectFromClassToInspect)) : "";
		    		varsData[i][2] = classDeclaredFields[i].getType().getName();
		    		varsData[i][3] = Modifier.isPublic(fieldModifier) ? "\u2714" : "\u2718";
		    		varsData[i][4] = Modifier.isPrivate(fieldModifier) ? "\u2714" : "\u2718";
		    		varsData[i][5] = Modifier.isFinal(fieldModifier) ? "\u2714" : "\u2718";
		    		varsData[i][6] = Modifier.isStatic(fieldModifier) ? "\u2714" : "\u2718";
			    }
			    
			    DefaultTableModel varsTableModel = new DefaultTableModel(varsData, variablesTableColumnNames);
				tableVariables.setModel(varsTableModel);

				//Methods
				String[][] methodsData = new String[classDeclaredMethods.length][methodsTableColumnNames.length];
			    for(int i = 0; i < classDeclaredMethods.length; i++) {
		    		int fieldModifier = classDeclaredMethods[i].getModifiers();
		    		classDeclaredMethods[i].setAccessible(true);
		    		String methodParams = "";
		    		Class<?>[] parameterTypes = classDeclaredMethods[i].getParameterTypes();

		    		for (Class<?> parameterType : parameterTypes) {
		    			methodParams += parameterType.getName() + ", ";
		    		}

		    		methodsData[i][0] = classDeclaredMethods[i].getName();
		    		methodsData[i][1] = String.valueOf(classDeclaredMethods[i].getParameterCount());
		    		methodsData[i][2] = (methodParams.length() > 0) ? methodParams.substring(0, methodParams.length() - 2) : "";
		    		methodsData[i][3] = classDeclaredMethods[i].getReturnType().getName();
		    		methodsData[i][4] = Modifier.isPublic(fieldModifier) ? "\u2714" : "\u2718";
		    		methodsData[i][5] = Modifier.isPrivate(fieldModifier) ? "\u2714" : "\u2718";
		    		methodsData[i][6] = Modifier.isFinal(fieldModifier) ? "\u2714" : "\u2718";
		    		methodsData[i][7] = Modifier.isStatic(fieldModifier) ? "\u2714" : "\u2718";
			    }

			    DefaultTableModel tableMethodsModel = new DefaultTableModel(methodsData, methodsTableColumnNames);
				tableMethods.setModel(tableMethodsModel);

				//Constructors
				String[][] constructorsData = new String[classDeclaredConstructors.length][constructorsTableColumnNames.length];
			    for(int i = 0; i < classDeclaredConstructors.length; i++) {
		    		int fieldModifier = classDeclaredConstructors[i].getModifiers();
		    		classDeclaredConstructors[i].setAccessible(true);
		    		String constructorParams = "";
		    		Class<?>[] parameterTypes = classDeclaredConstructors[i].getParameterTypes();

		    		for (Class<?> parameterType : parameterTypes) {
		    			constructorParams += parameterType.getName() + ", ";
		    		}

		    		constructorsData[i][0] = String.valueOf(classDeclaredConstructors[i].getParameterCount());
		    		constructorsData[i][1] = (constructorParams.length() > 0) ? constructorParams.substring(0, constructorParams.length() - 2) : "";
		    		constructorsData[i][2] = Modifier.isPublic(fieldModifier) ? "\u2714" : "\u2718";
		    		constructorsData[i][3] = Modifier.isPrivate(fieldModifier) ? "\u2714" : "\u2718";
			    }
			    
			    DefaultTableModel tableConstructorsModel = new DefaultTableModel(constructorsData, constructorsTableColumnNames);
			    tableConstructors.setModel(tableConstructorsModel);
			    
			} catch (ClassNotFoundException e1) {
				// TODO Auto-generated catch block
				showMessageDialog(null, "Class " + tfClassName.getText() + " not found.");
			} catch (NoSuchFieldException e1) {
				// TODO Auto-generated catch block
				showMessageDialog(null, "Instance " + tfInstanceName.getText() + " not found.");
			} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException | SecurityException err) {
				// TODO Auto-generated catch block
				err.printStackTrace();
			}
		});

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1250, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 65, 100, 124, 0, 188, 180, 210, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 158, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);

		panelTopMargin = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 10;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		frame.getContentPane().add(panelTopMargin, gbc_panel);

		lblFrameTitle = new JLabel("Class / Instance Inspector");
		lblFrameTitle.setFont(new Font("Helvetica", Font.PLAIN, 20));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridwidth = 10;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		frame.getContentPane().add(lblFrameTitle, gbc_lblNewLabel_1);

		lblClassName = new JLabel("Fully Qualified Class Name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 2;
		frame.getContentPane().add(lblClassName, gbc_lblNewLabel);

		lblInstanceName = new JLabel("Instance Name");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.gridx = 3;
		gbc_lblNewLabel_4.gridy = 2;
		frame.getContentPane().add(lblInstanceName, gbc_lblNewLabel_4);

		panelLeftMargin = new JPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridheight = 8;
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 3;
		frame.getContentPane().add(panelLeftMargin, gbc_panel_2);

		tfClassName = new JTextField();
		tfClassName.setText("darb.componentes.Person");
		GridBagConstraints gbc_txtDarbcomponentesperson = new GridBagConstraints();
		gbc_txtDarbcomponentesperson.gridwidth = 2;
		gbc_txtDarbcomponentesperson.insets = new Insets(0, 0, 5, 5);
		gbc_txtDarbcomponentesperson.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDarbcomponentesperson.gridx = 1;
		gbc_txtDarbcomponentesperson.gridy = 3;
		frame.getContentPane().add(tfClassName, gbc_txtDarbcomponentesperson);
		tfClassName.setColumns(10);

		tfInstanceName = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 3;
		gbc_textField_1.gridy = 3;
		frame.getContentPane().add(tfInstanceName, gbc_textField_1);
		tfInstanceName.setColumns(10);

		btnInspect = new JButton("Inspect");
		btnInspect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 3;
		frame.getContentPane().add(btnInspect, gbc_btnNewButton);

		lblVariables = new JLabel("Variables");
		GridBagConstraints gbc_lblVariables = new GridBagConstraints();
		gbc_lblVariables.anchor = GridBagConstraints.WEST;
		gbc_lblVariables.insets = new Insets(0, 0, 5, 5);
		gbc_lblVariables.gridx = 1;
		gbc_lblVariables.gridy = 5;
		frame.getContentPane().add(lblVariables, gbc_lblVariables);

		rightMarginPanel = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.gridheight = 8;
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 9;
		gbc_panel_3.gridy = 3;
		frame.getContentPane().add(rightMarginPanel, gbc_panel_3);

		scrollPaneVariablesTable = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 8;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 6;
		frame.getContentPane().add(scrollPaneVariablesTable, gbc_scrollPane);

	    Object[][] enptyData = {};
		tableVariables = new JTable(enptyData, this.variablesTableColumnNames);
		scrollPaneVariablesTable.setViewportView(tableVariables);

		lblMethods = new JLabel("Methods");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 1;
		gbc_lblNewLabel_2.gridy = 7;
		frame.getContentPane().add(lblMethods, gbc_lblNewLabel_2);

		labelSelectedMethod = new JLabel("Select a method from the list on the side.");
		GridBagConstraints gbc_lblNewLabel2 = new GridBagConstraints();
		gbc_lblNewLabel2.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel2.gridx = 7;
		gbc_lblNewLabel2.gridy = 7;
		frame.getContentPane().add(labelSelectedMethod, gbc_lblNewLabel2);

		buttonInvokeMethod = new JButton("Invoke");
		GridBagConstraints gbc_btnNewButton2 = new GridBagConstraints();
		gbc_btnNewButton2.anchor = GridBagConstraints.EAST;
		gbc_btnNewButton2.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton2.gridx = 8;
		gbc_btnNewButton2.gridy = 7;
		frame.getContentPane().add(buttonInvokeMethod, gbc_btnNewButton2);

		scrollPaneMethodsTable = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.gridwidth = 6;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 8;
		frame.getContentPane().add(scrollPaneMethodsTable, gbc_scrollPane_1);

		tableMethods = new JTable(enptyData, methodsTableColumnNames);
		scrollPaneMethodsTable.setViewportView(tableMethods);

		scrollPaneMethodParamsTable = new JScrollPane();
		GridBagConstraints gbc_scrollPane2 = new GridBagConstraints();
		gbc_scrollPane2.gridwidth = 2;
		gbc_scrollPane2.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane2.gridx = 7;
		gbc_scrollPane2.gridy = 8;
		frame.getContentPane().add(scrollPaneMethodParamsTable, gbc_scrollPane2);

		tableMethodParams = new JTable(enptyData, methodParamsTableColumnNames);
		scrollPaneMethodParamsTable.setViewportView(tableMethodParams);

		lblConstructors = new JLabel("Constructors");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 1;
		gbc_lblNewLabel_3.gridy = 9;
		frame.getContentPane().add(lblConstructors, gbc_lblNewLabel_3);

		scrollPaneConstructorsTable = new JScrollPane();
		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
		gbc_scrollPane_2.gridwidth = 8;
		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_2.gridx = 1;
		gbc_scrollPane_2.gridy = 10;
		frame.getContentPane().add(scrollPaneConstructorsTable, gbc_scrollPane_2);

		tableConstructors = new JTable(enptyData, constructorsTableColumnNames);
		scrollPaneConstructorsTable.setViewportView(tableConstructors);


		frame.setVisible(true);
	}


}
